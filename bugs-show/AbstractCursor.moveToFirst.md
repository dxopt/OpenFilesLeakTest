## 异常描述

CursorWindowAllocationException异常信息类似下面的callstack：

```
android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed.
	at android.database.CursorWindow.<init>(CursorWindow.java:109)
	at android.database.CursorWindow.<init>(CursorWindow.java:100)
	at android.database.AbstractWindowedCursor.clearOrCreateWindow(AbstractWindowedCursor.java:198)
	at android.database.sqlite.SQLiteCursor.clearOrCreateWindow(SQLiteCursor.java:301)
	at android.database.sqlite.SQLiteCursor.fillWindow(SQLiteCursor.java:139)
	at android.database.sqlite.SQLiteCursor.getCount(SQLiteCursor.java:133)
	at android.database.AbstractCursor.moveToPosition(AbstractCursor.java:197)
	at android.database.AbstractCursor.moveToFirst(AbstractCursor.java:237)
	...
```

出现此问题的一个示例机器信息：

```
4.4.4_19
samsung
SM-N9150
Sys mem: 352344/2850148
MemoryClass: 256
Debug.NativeHeapAllocated: 23623032
Debug.NativeHeapFree: 2696
Debug.NativeHeapSize: 23625728
Debug.MemInfo.dalvikPss: 14781
Debug.MemInfo.dalvikPrivateDirty: 14756
Debug.MemInfo.dalvikSharedDirty: 2660
Debug.MemInfo.nativePss: 0
Debug.MemInfo.nativePrivateDirty: 0
Debug.MemInfo.nativeSharedDirty: 0
Debug.MemInfo.otherPss: 31466
Debug.MemInfo.otherPrivateDirty: 27992
Debug.MemInfo.otherSharedDirty: 5496
Debug.MemInfo.totalPss: 46247
Debug.MemInfo.totalPrivateDirty: 42748
Debug.MemInfo.totalSharedDirty: 8156
```

## 异常分析

### 内存原因

当你遇到CursorWindowAllocationException异常时，直观判断会认为是内存用完导致的。除了这个容易想到的原因，还有另外一个不易被发现的可能原因，后面再解释。

先来看看这个异常是不是由内存用完导致的。

从机器信息中我们知道，在异常发生时，此机器有可用内存约350MB（352344KB），总内存约3G（2850148KB）；异常发生的进程，使用了约46MB内存（46247KB），native heap也才用了23MB（23625728B）；而此机器的memory class为256MB（参见android.app.ActivityManager#getMemoryClass()）。

从这些信息来看，该进程的可用内存应该还剩很多，不应该是内存用完导致的。

### 文件句柄原因

那么，我们来看看另一个可能的原因，这要从Android源码入手分析。

从Android源码知道（android-4.4.4_r2），当抛出此异常时，是因为CursorWindow在调用native方法CursorWindow#nativeCreate()时，分配内存失败了；在CursorWindow#nativeCreate()中，会调用函数CursorWindow::create()；而CursorWindow::create()会最终调用函数ashmem_create_region()来分配**共享内存**。从这里我们知道，创建一个CursorWindow对象，最终是去分配一块共享内存，并且需要用到文件句柄（文件描述符）。

因此，另外一个可能的原因出现了，那就是：**如果当前进程的文件句柄数用完了，那么也会出现此异常**。针对前面的示例异常，由于分析得出不是内存的原因，那么很有可能就是文件句柄的原因了。而从该进程的已打开文件列表来看，确实是文件句柄的原因。

## 相关问题

由文件句柄引起的数据库异常表现形式还有很多，如下：

Case#1:

```
android.database.sqlite.SQLiteCantOpenDatabaseException: unable to open database file (code 14)
	at android.database.sqlite.SQLiteConnection.nativeExecuteForCursorWindow(Native Method)
	at android.database.sqlite.SQLiteConnection.executeForCursorWindow(SQLiteConnection.java:845)
	at android.database.sqlite.SQLiteSession.executeForCursorWindow(SQLiteSession.java:836)
	at android.database.sqlite.SQLiteQuery.fillWindow(SQLiteQuery.java:62)
	at android.database.sqlite.SQLiteCursor.fillWindow(SQLiteCursor.java:144)
	at android.database.sqlite.SQLiteCursor.getCount(SQLiteCursor.java:133)
	at android.database.AbstractCursor.moveToPosition(AbstractCursor.java:197)
	at android.database.AbstractCursor.moveToFirst(AbstractCursor.java:237)
	...
```
