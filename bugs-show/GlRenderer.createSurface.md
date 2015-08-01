## 异常描述

GlRenderer.createSurface()异常信息类似下面的callstack：

```
java.lang.IllegalStateException: eglMakeCurrent failed EGL_BAD_ALLOC
	at android.view.HardwareRenderer$GlRenderer.createSurface(HardwareRenderer.java:1354)
	at android.view.HardwareRenderer$GlRenderer.createEglSurface(HardwareRenderer.java:1241)
	at android.view.HardwareRenderer$GlRenderer.initialize(HardwareRenderer.java:1058)
	at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:1810)
	at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:1234)
	at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:6467)
	at android.view.Choreographer$CallbackRecord.run(Choreographer.java:803)
	at android.view.Choreographer.doCallbacks(Choreographer.java:603)
	at android.view.Choreographer.doFrame(Choreographer.java:573)
	at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:789)
	at android.os.Handler.handleCallback(Handler.java:733)
	at android.os.Handler.dispatchMessage(Handler.java:95)
	at android.os.Looper.loop(Looper.java:157)
	at android.app.ActivityThread.main(ActivityThread.java:5356)
	at java.lang.reflect.Method.invokeNative(Native Method)
	at java.lang.reflect.Method.invoke(Method.java:515)
	at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1265)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1081)
	at dalvik.system.NativeStart.main(Native Method)
```

出现此问题的一个示例机器信息：

```
4.4.2_19
samsung
GT-I9500
Sys mem: 621980, 1905684
MemoryClass: 128
Debug.NativeHeapAllocated: 9952888
Debug.NativeHeapFree: 1032584
Debug.NativeHeapSize: 32489472
Debug.MemInfo.dalvikPss: 36636
Debug.MemInfo.dalvikPrivateDirty: 36364
Debug.MemInfo.dalvikSharedDirty: 15524
Debug.MemInfo.nativePss: 4
Debug.MemInfo.nativePrivateDirty: 4
Debug.MemInfo.nativeSharedDirty: 0
Debug.MemInfo.otherPss: 50999
Debug.MemInfo.otherPrivateDirty: 20540
Debug.MemInfo.otherSharedDirty: 10456
Debug.MemInfo.totalPss: 87639
Debug.MemInfo.totalPrivateDirty: 56908
Debug.MemInfo.totalSharedDirty: 25980
```

打开的文件列表类似如下信息：

```
964:/proc/5255/fd/pipe:[9230505]
965:/proc/5255/fd/pipe:[9230508]
966:/proc/5255/fd/pipe:[9241717]
967:/proc/5255/fd/pipe:[9230509]
968:/proc/5255/fd/pipe:[9241718]
969:/proc/5255/fd/pipe:[9242690]
970:/proc/5255/fd/pipe:[9241719]
971:/proc/5255/fd/pipe:[9242691]
972:/proc/5255/fd/pipe:[9237753]
973:/proc/5255/fd/pipe:[9242692]
974:/proc/5255/fd/pipe:[9237754]
975:/proc/5255/fd/pipe:[9244876]
976:/proc/5255/fd/pipe:[9237755]
977:/proc/5255/fd/pipe:[9244877]
978:/proc/5255/fd/pipe:[9244880]
979:/proc/5255/fd/pipe:[9244878]
980:/proc/5255/fd/pipe:[9244881]
981:/proc/5255/fd/pipe:[9242694]
982:/proc/5255/fd/pipe:[9244882]
983:/proc/5255/fd/pipe:[9242695]
984:/proc/5255/fd/pipe:[9239715]
985:/proc/5255/fd/pipe:[9242696]
986:/proc/5255/fd/pipe:[9239716]
987:/proc/5255/fd/pipe:[9248898]
988:/proc/5255/fd/pipe:[9239717]
989:/proc/5255/fd/pipe:[9248899]
990:/proc/5255/fd/pipe:[9248902]
991:/proc/5255/fd/pipe:[9248900]
992:/proc/5255/fd/pipe:[9248903]
993:/proc/5255/fd/pipe:[9248906]
994:/proc/5255/fd/pipe:[9248904]
995:/proc/5255/fd/pipe:[9248907]
996:/proc/5255/fd/pipe:[9250073]
997:/proc/5255/fd/pipe:[9248908]
998:/proc/5255/fd/pipe:[9250074]
999:/proc/5255/fd/pipe:[9250958]
1000:/proc/5255/fd/pipe:[9250075]
1001:/proc/5255/fd/pipe:[9250959]
1002:/proc/5255/fd/pipe:[9254012]
1003:/proc/5255/fd/pipe:[9250960]
1004:/proc/5255/fd/pipe:[9254013]
1005:/proc/5255/fd/pipe:[9255021]
1006:/proc/5255/fd/pipe:[9254014]
1007:/proc/5255/fd/pipe:[9255022]
1008:/proc/5255/fd/pipe:[9255025]
1009:/proc/5255/fd/pipe:[9255023]
1010:/proc/5255/fd/pipe:[9255026]
1011:/proc/5255/fd/pipe:[9255029]
1012:/proc/5255/fd/pipe:[9255027]
1013:/proc/5255/fd/pipe:[9255030]
1014:/proc/5255/fd/pipe:[9257072]
```

## 异常分析

虽然异常提示为“EGL_BAD_ALLOC”，但从内存情况来看，应该不是由于内存不够导致的。从打开的文件列表分析得知，文件句柄已用尽。因此，很有可能此异常是由文件句柄引起。但从Android源码中，还未找到跟文件句柄的关联信息。

另外，打开的文件主要是“/proc/5255/fd/pipe:[xxxxx]”，需要进一步研究这个跟什么相关。特别是，是否与硬件加速相关（OpenGL）。