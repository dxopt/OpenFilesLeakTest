# OpenFilesLeakTest

This project aims to explore Android bugs about opened files leak.

## Open Files Leak

What's *open files leak*? Android is based on Linux and every process in Linux (or in Android) has a limit on the number of opened files. In most Android devices, the limit is 1024.

You can use the shell command *ulimit* to view the limit. For example,
```
shell@hammerhead:/ $ ulimit -a
time(cpu-seconds)    unlimited
file(blocks)         unlimited
coredump(blocks)     0
data(KiB)            unlimited
stack(KiB)           8192
lockedmem(KiB)       65536
nofiles(descriptors) 1024    -------> look here
processes            12281
flocks               unlimited
sigpending           12281
msgqueue(bytes)      819200
maxnice              40
maxrtprio            0
resident-set(KiB)    unlimited
address-space(KiB)   unlimited
shell@hammerhead:/ $ ulimit --help
/system/bin/sh: ulimit: --: unknown option
/system/bin/sh: ulimit: usage: ulimit [-acdefHiLlmnpqrSstv] [value]
1|shell@hammerhead:/ $ ulimit -n
1024

```
You can use the shell commands *run-as* (or *su*) and *lsof* to view the open files of one process. For example,
```
shell@hammerhead:/ $ ps | grep me.ycdev.android.demo.openfilesleak
u0_a128   9473  19269 1545928 53648 ffffffff 00000000 S me.ycdev.android.demo.openfilesleak
shell@hammerhead:/ $ run-as me.ycdev.android.demo.openfilesleak
shell@hammerhead:/data/data/me.ycdev.android.demo.openfilesleak $ lsof 9473
COMMAND     PID       USER   FD      TYPE             DEVICE  SIZE/OFF       NODE NAME
me.ycdev.  9473    u0_a128  exe       ???                ???       ???        ??? /system/bin/app_process32_original
me.ycdev.  9473    u0_a128    0       ???                ???       ???        ??? /dev/null
me.ycdev.  9473    u0_a128    1       ???                ???       ???        ??? /dev/null
me.ycdev.  9473    u0_a128    2       ???                ???       ???        ??? /dev/null
me.ycdev.  9473    u0_a128    3       ???                ???       ???        ??? socket:[3262494]
me.ycdev.  9473    u0_a128    4       ???                ???       ???        ??? /sys/kernel/debug/tracing/trace_marker
me.ycdev.  9473    u0_a128    5       ???                ???       ???        ??? /system/framework/framework.jar
me.ycdev.  9473    u0_a128    6       ???                ???       ???        ??? /system/framework/core-libart.jar
me.ycdev.  9473    u0_a128    7       ???                ???       ???        ??? socket:[3262595]
me.ycdev.  9473    u0_a128    8       ???                ???       ???        ??? /system/framework/framework-res.apk
me.ycdev.  9473    u0_a128    9       ???                ???       ???        ??? /dev/__properties__
me.ycdev.  9473    u0_a128   10       ???                ???       ???        ??? /dev/binder
me.ycdev.  9473    u0_a128   11       ???                ???       ???        ??? pipe:[5082666]
me.ycdev.  9473    u0_a128   12       ???                ???       ???        ??? /dev/alarm
me.ycdev.  9473    u0_a128   13       ???                ???       ???        ??? socket:[5082660]
me.ycdev.  9473    u0_a128   14       ???                ???       ???        ??? /dev/cpuctl/apps/tasks
me.ycdev.  9473    u0_a128   15       ???                ???       ???        ??? /dev/cpuctl/apps/bg_non_interactive/tasks
me.ycdev.  9473    u0_a128   16       ???                ???       ???        ??? pipe:[5082661]
me.ycdev.  9473    u0_a128   17       ???                ???       ???        ??? pipe:[5082661]
me.ycdev.  9473    u0_a128   18       ???                ???       ???        ??? pipe:[5082666]
me.ycdev.  9473    u0_a128   19       ???                ???       ???        ??? anon_inode:[eventpoll]
me.ycdev.  9473    u0_a128   20       ???                ???       ???        ??? /data/app/me.ycdev.android.demo.openfilesleak-1/base.apk
me.ycdev.  9473    u0_a128   21       ???                ???       ???        ??? pipe:[5089340]
me.ycdev.  9473    u0_a128   22       ???                ???       ???        ??? socket:[5088566]
me.ycdev.  9473    u0_a128   23       ???                ???       ???        ??? pipe:[5089340]
me.ycdev.  9473    u0_a128   24       ???                ???       ???        ??? anon_inode:[eventpoll]
me.ycdev.  9473    u0_a128   25       ???                ???       ???        ??? /dev/kgsl-3d0
me.ycdev.  9473    u0_a128   26       ???                ???       ???        ??? socket:[5082687]
me.ycdev.  9473    u0_a128   27       ???                ???       ???        ??? socket:[5082691]
me.ycdev.  9473    u0_a128   28       ???                ???       ???        ??? anon_inode:dmabuf
me.ycdev.  9473    u0_a128   29       ???                ???       ???        ??? anon_inode:dmabuf
me.ycdev.  9473    u0_a128   30       ???                ???       ???        ??? /dev/ion
me.ycdev.  9473    u0_a128   31       ???                ???       ???        ??? /dev/ion
me.ycdev.  9473    u0_a128   34       ???                ???       ???        ??? anon_inode:dmabuf
me.ycdev.  9473    u0_a128   35       ???                ???       ???        ??? anon_inode:dmabuf
me.ycdev.  9473    u0_a128   36       ???                ???       ???        ??? socket:[5082749]
me.ycdev.  9473    u0_a128   37       ???                ???       ???        ??? anon_inode:dmabuf
me.ycdev.  9473    u0_a128   38       ???                ???       ???        ??? anon_inode:dmabuf
me.ycdev.  9473    u0_a128  mem       ???              00:04         0    3262499 /dev/ashmem/dalvik-main
```

When your users suffer a lot of strange DB operation issues, the root cause may be "open files leak"! SQLite DB is operating on a DB file. When any DB operation happens, the DB file will be opened first. If number of the open files reaches the ulimit, then no any more file can be opened.

Also, *open files leak* may lead other strange issues.

## Known Android bugs

There are some *open files leak* bugs found in Android.

### ZipFile leak

In constructor of java.util.ZipFile, if the zip file is invalid, the opened file (a RandomAccessFile object) will be leaked by ZipFile. Next, there are a good news and a bad news.

The good news is that when GC happens, the RandomAccessFile object will close the leaked file. So, this issue is not a big problem in most cases. But if no GC happens, it's possible to reach the ulimit!

The bad news is that when GC happens, the leaked file will not be closed on some devices! In this case, the ulimit will be reached eventually.
> Please refer the test reports section. In my test report, the opened files will be leaked in those devices with Android 4.0 ~ 4.1. Bug I didn't find out the leak reason in Android source code.

Ref: https://code.google.com/p/android/issues/detail?id=66383

> This leak was already fixed in Android 5.0.

### PackageManager#getPackageArchiveInfo() leak

When use PackageManager#getPackageArchiveInfo() to parse an invalid apk file in Android 5.0+, the opened file will be leaked.

Ref: https://code.google.com/p/android/issues/detail?id=171099

> This leak is still there, waiting for fix.

## How this project works

This project use Android test cases to confirm the *open files leak* bugs in Android versions.

Use the following command to run the test cases (connect the device first):

```
[PWD: ~/work/tyc/ycdev/demos/OpenFilesLeakTest]  (master)
$ ./gradlew connectedAndroidTest
```

Currently, all the test cases pass most devices. But me.ycdev.android.demo.openfilesleak.ZipFileTest#testInvalidZipFile() may fail on some devices.

## Test report

### Bad news

| Device Brand | Device Model | Android Version | Test Result | Remark |
| :----------- | :----------- | :-------------- | :---------- | :----- |

### Good news

| Device Brand | Device Model | Android Version | Test Result | Remark |
| :----------- | :----------- | :-------------- | :---------- | :----- |
| Google       | Nexus 5      | 5.0.1           | Pass        |
| Google       | Nexus 6      | 5.1             | Pass        |
| Google       | Nexus One    | 2.3.3           | Pass        | No SD card |
| Google       | Galaxy Nexus | 4.3             | Pass        |
| Google       | Galaxy Nexus | 4.1.2           | Pass        |
| Google       | Nexus 4      | 5.0             | Pass        |
| Huawei       | H60-L02      | 4.4.2           | Pass        |
| Huawei       | HUAWEI MT7-TL00 | 4.4.2        | Pass        |
| Huawei       | H60-L11      | 4.4.2           | Pass        |
| Huawei       | H30-U10      | 4.2.2           | Pass        |
| Samsung      | SM-N9100     | 4.4.4           | Pass        |
| Samsung      | GT-I9300     | 4.3             | Pass        |
| Samsung      | GT-I8552     | 4.1.2           | Pass        |
| Samsung      | SM-N9002     | 4.3             | Pass        |
| Samsung      | SM-G9006W    | 5.0             | Pass        |
| Xiaomi       | MI 4W        | 4.4.4           | Pass        |
| Xiaomi       | MI 2SC       | 4.1.1           | Pass        |
| Xiaomi       | MI 3         | 4.4.2           | Pass        |
| MOTO         | MT887        | 4.1.2           | Pass        |
| MOTO         | XT910        | 4.0.4           | Pass        |
| OPPO         | N5207        | 4.4.4           | Pass        |
| Lenovo       | Lenovo K900  | 4.2.2           | Pass        |
| HTC          | HTC 919d     | 4.4.2           | Pass        |
| Nubia        | NX505J       | 4.4.2           | Pass        |
