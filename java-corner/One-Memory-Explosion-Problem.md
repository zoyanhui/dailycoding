# One problem with memory explosion

A online java process is running after a while time, then it's memory increases explosively. It reaches the `Xmx` meomery, and run full gc again and again, wath this by `jstat -gcutil`.

When some processes deployed on different online machine, they run in different way. Some's explosion are fast, some can run for a long time.

So, it is different from normal `Memory Leak`, whose memroy increases gradually.

I dump the heap by `jamp -dump`, and use `jhat` to show the details.

First, watch the `histo`:

```
Heap Histogram
All Classes (excluding platform)

Class Instance Count  Total Size
class [Ljava.lang.Object; 22536592  9474878240
class java.util.ArrayList 22529025  360464400
class [I  362746  48162148
class java.util.HashMap$Node  718024  20104672
class com.huaban.analysis.misearch.jieba.DictSegment  498102  15939264
class [C  461525  14859906
class [[I 2 14483168
class [Ljava.util.HashMap$Node; 28050 11139472
class [B  4493  10523372
class [Lcom.huaban.analysis.misearch.jieba.DictSegment; 186659  7466360
class java.lang.String  460773  5529276
class [Lscala.concurrent.forkjoin.ForkJoinTask; 63  4129776
class java.lang.Double  384547  3076376
class com.xiaomi.xtrace.common.RpcContext 8193  1556670
class java.util.HashMap 28527 1369296
class java.lang.Class 14059 1293428
class java.util.concurrent.ConcurrentHashMap  15353 1289652
......

```

The instance number of `class [Ljava.lang.Object` and `class java.util.ArrayList` are too large. There may be a problem.

Then, the gc log:
```
2018-05-02T17:53:59.275+0800: 611.453: [GC concurrent-mark-start]
{Heap before GC invocations=95 (full 18):
 garbage-first heap   total 8388608K, used 8350713K [0x00000005c0000000, 0x00000005c0404000,
 0x00000007c0000000)
  region size 4096K, 0 young (0K), 0 survivors (0K)
 Metaspace       used 77752K, capacity 78208K, committed 78908K, reserved 1118208K
  class space    used 9906K, capacity 10016K, committed 10060K, reserved 1048576K
2018-05-02T17:53:59.275+0800: 611.454: [GC pause (G1 Evacuation Pause) (young)2018-05-02T17:
53:59.277+0800: 611.455: [SoftReference, 0 refs, 0.0000209 secs]2018-05-02T17:53:59.277+0800
: 611.456: [WeakReference, 0 refs, 0.0000084 secs]2018-05-02T17:53:59.277+0800: 611.456: [Fi
nalReference, 0 refs, 0.0000088 secs]2018-05-02T17:53:59.277+0800: 611.456: [PhantomReferenc
e, 0 refs, 0 refs, 0.0000099 secs]2018-05-02T17:53:59.277+0800: 611.456: [JNI Weak Reference, 0.0000105 secs], 0.0021200 secs]
   [Parallel Time: 1.1 ms, GC Workers: 18]
      [GC Worker Start (ms): Min: 611454.0, Avg: 611454.1, Max: 611454.2, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.5, Avg: 0.6, Max: 0.9, Diff: 0.4, Sum: 10.8]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
         [Processed Buffers: Min: 0, Avg: 0.1, Max: 1, Diff: 1, Sum: 2]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.4]
      [Termination (ms): Min: 0.0, Avg: 0.2, Max: 0.3, Diff: 0.3, Sum: 4.5]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 18]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 0.8, Avg: 0.9, Max: 0.9, Diff: 0.1, Sum: 15.9]
      [GC Worker End (ms): Min: 611455.0, Avg: 611455.0, Max: 611455.0, Diff: 0.0]
   [Code Root Fixup: 0.2 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.2 ms]
   [Other: 0.6 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.3 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.2 ms]
      [Humongous Register: 0.0 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.0 ms]
   [Eden: 0.0B(2048.0M)->0.0B(2048.0M) Survivors: 0.0B->0.0B Heap: 8155.0M(8192.0M)->8155.0M(8192.0M)]
Heap after GC invocations=96 (full 18):
 garbage-first heap   total 8388608K, used 8350713K [0x00000005c0000000, 0x00000005c0404000, 0x00000007c0000000)
  region size 4096K, 0 young (0K), 0 survivors (0K)
 Metaspace       used 77752K, capacity 78208K, committed 78908K, reserved 1118208K
  class space    used 9906K, capacity 10016K, committed 10060K, reserved 1048576K
}
 [Times: user=0.01 sys=0.00, real=0.00 secs] 
2018-05-02T17:53:59.277+0800: 611.456: Total time for which application threads were stopped: 0.0027057 seconds, Stopping threads took: 0.0001205 seconds
```
It shows the full gc can not release enough memory. So, there are too much heap memory can not be released, because the references do not lose efficacy. With the addition of several process in different machine have different situation. Every machine has almost the same amount of traffic. So, the problem may be related to the inputs of program.

Through the log, I found the inputs that can replay the problem in testing environment. The reason is that some long inputs cause a method in a dependent library creating `ArrayList` instance and  a Node class which extends Object instance, exponential increase. And then the maxminum memory are used, the session is not completed, those instances can not be `gc`.


## Inclusion:
This problem is because of lacking protection towards abnormal inputs, then cause memory exponential increases.