## 异常描述

InputChannel.nativeReadFromParcel()可能抛出如下异常信息：

```
java.lang.RuntimeException: Could not read input channel file descriptors from parcel.
	at android.view.InputChannel.nativeReadFromParcel(Native Method)
	at android.view.InputChannel.readFromParcel(InputChannel.java:148)
	at android.view.IWindowSession$Stub$Proxy.addToDisplay(IWindowSession.java:717)
	at android.view.ViewRootImpl.setView(ViewRootImpl.java:693)
	at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:278)
	at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:69)
	...
```

出现此问题的一个示例机器信息：

```
4.4.2_19
samsung
SM-N9009
Sys mem: 620448, 2877620
MemoryClass: 128
Debug.NativeHeapAllocated: 15262720
Debug.NativeHeapFree: 195632
Debug.NativeHeapSize: 15556608
Debug.MemInfo.dalvikPss: 18747
Debug.MemInfo.dalvikPrivateDirty: 18592
Debug.MemInfo.dalvikSharedDirty: 15336
Debug.MemInfo.nativePss: 0
Debug.MemInfo.nativePrivateDirty: 0
Debug.MemInfo.nativeSharedDirty: 0
Debug.MemInfo.otherPss: 34823
Debug.MemInfo.otherPrivateDirty: 26504
Debug.MemInfo.otherSharedDirty: 9528
Debug.MemInfo.totalPss: 53570
Debug.MemInfo.totalPrivateDirty: 45096
Debug.MemInfo.totalSharedDirty: 24864
```

## 异常分析

### 文件句柄原因

初看到这个异常信息，无从判断发生了什么。那么，还是从Android源码入手分析。

从Android源码知道（android-4.4.4_r2），在InputChannel.nativeReadFromParcel()的native实现函数android_view_InputChannel_nativeReadFromParcel()中，会调用Linux系统调用dup来复制文件句柄。当复制文件句柄失败时，就会抛出此异常。而复制文件句柄失败的主要原因就是文件句柄用完导致。

## 相关问题

### Case#1

在其它涉及InputChannel的地方也会出现类似问题：

```
java.lang.RuntimeException: Could not read input channel file descriptors from parcel.
	at android.view.InputChannel.nativeReadFromParcel(Native Method)
	at android.view.InputChannel.readFromParcel(InputChannel.java:148)
	at android.view.InputChannel$1.createFromParcel(InputChannel.java:39)
	at android.view.InputChannel$1.createFromParcel(InputChannel.java:36)
	at com.android.internal.view.InputBindResult.<init>(InputBindResult.java:62)
	at com.android.internal.view.InputBindResult$1.createFromParcel(InputBindResult.java:102)
	at com.android.internal.view.InputBindResult$1.createFromParcel(InputBindResult.java:99)
	at com.android.internal.view.IInputMethodManager$Stub$Proxy.startInput(IInputMethodManager.java:709)
	at android.view.inputmethod.InputMethodManager.startInputInner(InputMethodManager.java:1296)
	at android.view.inputmethod.InputMethodManager$H.handleMessage(InputMethodManager.java:475)
	at android.os.Handler.dispatchMessage(Handler.java:102)
	at android.os.Looper.loop(Looper.java:157)
	at android.app.ActivityThread.main(ActivityThread.java:5356)
	at java.lang.reflect.Method.invokeNative(Native Method)
	at java.lang.reflect.Method.invoke(Method.java:515)
	at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1265)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1081)
	at dalvik.system.NativeStart.main(Native Method)
```

### Case#2

在调用WindowManager#addView()时，还可能遇到如下异常：

```
java.lang.RuntimeException: Adding window failed
	at android.view.ViewRootImpl.setView(ViewRootImpl.java:568)
	at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:269)
	at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:69)
	...
Caused by: android.os.TransactionTooLargeException
	at android.os.BinderProxy.transact(Native Method)
	at android.view.IWindowSession$Stub$Proxy.addToDisplay(IWindowSession.java:701)
	at android.view.ViewRootImpl.setView(ViewRootImpl.java:557)
	...
```

从收集到的信息判断，这个问题也有可能是由于文件句柄原因引起的，但还未能从Android源码中找到证据。

