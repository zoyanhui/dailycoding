# java进程的res(resident set site)内存超大问题调查

## 问题现场
系统：
```
cat /etc/redhat-release 
CentOS Linux release 7.3.1611 (Core) 
```
另一台机器也有同样问题：
```
cat /etc/redhat-release 
CentOS release 6.3 (Final)
```
一个javac进程(jdk1.8.0_144)：
```
    PID USER    PR    NI   VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND
110666 work      20   0 22.508g 6.179g  29660 S  21.6  4.9   4:01.59 java   
```
查看JVM配置：
```
-XX:CICompilerCount=12 -XX:ConcGCThreads=5 -XX:+CrashOnOutOfMemoryError -XX:G1HeapRegionSize=4194304 -XX:GCLogFileSize=134217728 -XX:+HeapDumpOnOutOfMemoryError -XX:InitialHeapSize=8589934592 -XX:+ManagementServer -XX:MarkStackSize=4194304 -XX:MaxDirectMemorySize=1048576000 -XX:MaxGCPauseMillis=200 -XX:MaxHeapSize=8589934592 -XX:MaxNewSize=5150605312 -XX:MinHeapDeltaBytes=4194304 -XX:NumberOfGCLogFiles=100 -XX:+PrintGC -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintPromotionFailure -XX:+PrintReferenceGC -XX:ThreadStackSize=512 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:+UseG1GC -XX:+UseGCLogFileRotation 
```
查看堆内存效果:
```
/usr/java/jdk1.8.0_144/bin/jmap -heap 110666
Attaching to process ID 110666, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 25.144-b01

using thread-local object allocation.
Garbage-First (G1) GC with 18 thread(s)

Heap Configuration:
   MinHeapFreeRatio         = 40
   MaxHeapFreeRatio         = 70
   MaxHeapSize              = 8589934592 (8192.0MB)
   NewSize                  = 1363144 (1.2999954223632812MB)
   MaxNewSize               = 5150605312 (4912.0MB)
   OldSize                  = 5452592 (5.1999969482421875MB)
   NewRatio                 = 2
   SurvivorRatio            = 8
   MetaspaceSize            = 21807104 (20.796875MB)
   CompressedClassSpaceSize = 1073741824 (1024.0MB)
   MaxMetaspaceSize         = 17592186044415 MB
   G1HeapRegionSize         = 4194304 (4.0MB)

Heap Usage:
G1 Heap:
   regions  = 2048
   capacity = 8589934592 (8192.0MB)
   used     = 2343230992 (2234.679214477539MB)
   free     = 6246703600 (5957.320785522461MB)
   27.27879900485277% used
G1 Young Generation:
Eden Space:
   regions  = 439
   capacity = 5360320512 (5112.0MB)
   used     = 1841299456 (1756.0MB)
   free     = 3519021056 (3356.0MB)
   34.35054773082942% used
Survivor Space:
   regions  = 12
   capacity = 50331648 (48.0MB)
   used     = 50331648 (48.0MB)
   free     = 0 (0.0MB)
   100.0% used
G1 Old Generation:
   regions  = 112
   capacity = 3179282432 (3032.0MB)
   used     = 451599888 (430.67921447753906MB)
   free     = 2727682544 (2601.320785522461MB)
   14.204459580393769% used

21220 interned Strings occupying 1923608 bytes.
```
从上面的数据可以看出。配置的堆内存是8G，对外内存1000M(约1G),当前堆使用大小2234M(约2G)，所以Res的大小(6.179g)远超过堆的使用大小 + 堆外内存大小。这些内存会是哪里来的呢？

### 分析
这里通过引入一些其他知识，逐步分析
1. Linux top查看的内存，[StackOverflow](https://stackoverflow.com/questions/561245/virtual-memory-usage-from-java-under-linux-too-much-memory-used)
    > On Linux, the `top` command gives you several different numbers for memory. 
    ** VIRT is the virtual memory space: the sum of everything in the virtual memory map (see below). It is largely meaningless, except when it isn't (see below).
    * RES is the resident set size: the number of pages that are currently resident in RAM. In almost all cases, this is the only number that you should use when saying "too big." But it's still not a very good number, especially when talking about Java.
    * SHR is the amount of resident memory that is shared with other processes. For a Java process, this is typically limited to shared libraries and memory-mapped JARfiles. 
    * SWAP isn't turned on by default, and isn't shown here. It indicates the amount of virtual memory that is currently resident on disk, whether or not it's actually in the swap space. The OS is very good about keeping active pages in RAM, and the only cures for swapping are (1) buy more memory, or (2) reduce the number of processes, so it's best to ignore this number.
2. VIRT(virtual memory), 使用Linux命令`pmap`查看。例如这个例子：
    ```
    pmap -x 110666
    110666:   /usr/java/jdk1.8.0_144//bin/java -Xms8192M -Xmx8192M -XX:MaxDirectMemorySize=1000M -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xss512K -XX:+UseCompressedOops -XX:+CrashOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintReferenceGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -XX:+PrintGCApplicationStoppedTime -XX:+PrintPromotionFailure -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=100 -XX:GCLogFileSize=128
    Address           Kbytes     RSS   Dirty Mode  Mapping
    0000000000400000       4       4       0 r-x-- java
    0000000000600000       4       4       4 rw--- java
    0000000000af8000     132      36      36 rw---   [ anon ]
    00000005c0000000 8399104 5726376 5726376 rw---   [ anon ]
    00000007c0a40000 1038080       0       0 -----   [ anon ]
    00007fe115f43000      12       0       0 -----   [ anon ]
    00007fe115f46000     504      88      88 rw---   [ anon ]
    00007fe115fc4000      12       0       0 -----   [ anon ]
    ...
    00007fe29e6c9000      12       0       0 -----   [ anon ]
    00007fe29e6cc000    2552    2148    2148 rw---   [ anon ]
    00007fe29e94a000      60      60       0 r-x-- libbz2.so.1.0.6
    00007fe29e959000    2044       0       0 ----- libbz2.so.1.0.6
    00007fe29eb58000       4       4       4 r---- libbz2.so.1.0.6
    00007fe29eb59000       4       4       4 rw--- libbz2.so.1.0.6
    00007fe29eb5a000     148      60       0 r-x-- liblzma.so.5.2.2
    00007fe29eb7f000    2044       0       0 ----- liblzma.so.5.2.2
    ...
    00007fe41aff1000   16444   14068   14068 rw---   [ anon ]
    00007fe41c000000     312     312     312 rw---   [ anon ]
    00007fe41c04e000   65224       0       0 -----   [ anon ]
    00007fe420000000     764     764     764 rw---   [ anon ]
    00007fe4200bf000   64772       0       0 -----   [ anon ]
    00007fe424000000     544     544     544 rw---   [ anon ]
    00007fe424088000   64992       0       0 -----   [ anon ]
    00007fe428000000     304     304     304 rw---   [ anon ]
    00007fe42804c000   65232       0       0 -----   [ anon ]
    00007fe42c000000     348     348     348 rw---   [ anon ]
    00007fe42c057000   65188       0       0 -----   [ anon ]
    00007fe430000000     348     348     348 rw---   [ anon ]
    00007fe430057000   65188       0       0 -----   [ anon ]
    00007fe434000000     436     436     436 rw---   [ anon ]
    00007fe43406d000   65100       0       0 -----   [ anon ]
    00007fe438000000     356     356     356 rw---   [ anon ]
    00007fe438059000   65180       0       0 -----   [ anon ]
    00007fe43c000000     364     364     364 rw---   [ anon ]
    00007fe43c05b000   65172       0       0 -----   [ anon ]
    00007fe440000000     292     292     292 rw---   [ anon ]
    00007fe440049000   65244       0       0 -----   [ anon ]
    ...
    00007fe394fb5000     188     188       0 r--s- com.google.guava.guava-20.0.jar
    00007fe394fe4000       8       8       0 r--s- zkclient.zkclient-0.2.3.jar
    ...
    00007fe4608fa000    1024      28      28 rw---   [ anon ]
    00007fe4609fa000       4       0       0 -----   [ anon ]
    00007fe4609fb000    1024      28      28 rw---   [ anon ]
    00007fe460afb000       4       0       0 -----   [ anon ]
    00007fe460afc000    1024      28      28 rw---   [ anon ]
    00007fe460bfc000       4       0       0 -----   [ anon ]
    00007fe460bfd000    1024      28      28 rw---   [ anon ]
    00007fe460cfd000       4       0       0 -----   [ anon ]
    ...
    00007fff69064000     156      56      56 rw---   [ stack ]
    00007fff690c0000       8       0       0 r----   [ anon ]
    00007fff690c2000       8       8       0 r-x--   [ anon ]
    ffffffffff600000       4       0       0 r-x--   [ anon ]
    ---------------- ------- ------- ------- 
    total kB         23612180 6496080 6466096
    ```

    > A quick explanation of the format: each row starts with the virtual memory address of the segment. This is followed by the segment size, permissions, and the source of the segment. This last item is either a file or "anon", which indicates a block of memory allocated via `mmap`.
    > Starting from the top, we have
    * The JVM loader (ie, the program that gets run when you type java). This is very small; all it does is load in the shared libraries where the real JVM code is stored.
    * A bunch of anon blocks holding the Java heap and internal data. This is a Sun JVM, so the heap is broken into multiple generations, each of which is its own memory block. Note that the JVM allocates virtual memory space based on the -Xmx value; this allows it to have a contiguous heap. The -Xms value is used internally to say how much of the heap is "in use" when the program starts, and to trigger garbage collection as that limit is approached.
    * A memory-mapped JARfile, in this case the file that holds the "JDK classes." When you memory-map a JAR, you can access the files within it very efficiently (versus reading it from the start each time). The Sun JVM will memory-map all JARs on the classpath; if your application code needs to access a JAR, you can also memory-map it.
    * Per-thread data for two threads. The 1M block is a thread stack; I don't know what goes into the 4K block. For a real app, you will see dozens if not hundreds of these entries repeated through the memory map.
    * One of the shared libraries that holds the actual JVM code. There are several of these.
    * The shared library for the C standard library. This is just one of many things that the JVM loads that are not strictly part of Java.
    > The shared libraries are particularly interesting: each shared library has at least two segments: a read-only segment containing the library code, and a read-write segment that contains global per-process data for the library (I don't know what the segment with no permissions is; I've only seen it on x64 Linux). The read-only portion of the library can be shared between all processes that use the library; for example, libc has 1.5M of virtual memory space that can be shared.
    

   虚拟内存，是进程的内存空间映射，包含了最大的堆，线程栈，虚拟机进程，以及映射的磁盘文件（jar， C library）等。这是个非常大的空间概念，通常情况下，不需要关注这个，特别是在64位机器上。但是其中一部分是实实在在需要在RAM中分配存在的，这就涉及了Res内存的问题。
3. Res部分的内存，包括当前使用的堆内存，线程栈的内存空间（JVM默认线程栈空间是1M），堆外内存（DirectMemorySize）。
    > Resident Set size is that portion of the virtual memory space that is actually in RAM. If your RSS grows to be a significant portion of your total physical memory, it might be time to start worrying. If your RSS grows to take up all your physical memory, and your system starts swapping, it's well past time to start worrying.
    But RSS is also misleading, especially on a lightly loaded machine. The operating system doesn't expend a lot of effort to reclaiming the pages used by a process. There's little benefit to be gained by doing so, and the potential for an expensive page fault if the process touches the page in the future. As a result, the RSS statistic may include lots of pages that aren't in active use.
   在实际使用的机器中，这部分内存同样会包含进程未实际使用的内存。这就是为什么上面那个Res内存会比堆内外内存之和大的原因。

   上面`pmap`看到的内存占用中，有很多`[ anon ]`的部分，这部分不仅有java堆的部分，有DirectMemory部分。还有线程栈的内存部分。
   可以看到这种：
   ```
    00007fe460afc000    1024      28      28 rw---   [ anon ]
    00007fe460bfc000       4       0       0 -----   [ anon ]
    00007fe460bfd000    1024      28      28 rw---   [ anon ]
    00007fe460cfd000       4       0       0 -----   [ anon ]
   ```
   这些就是默认线程栈大小的一个线程占用的空间。
   还有这种：
   ```
    00007fe43c000000     364     364     364 rw---   [ anon ]
    00007fe43c05b000   65172       0       0 -----   [ anon ]
    00007fe440000000     292     292     292 rw---   [ anon ]
    00007fe440049000   65244       0       0 -----   [ anon ]
   ```
    这是一个64MB（364+65172=65536,292+65244）大小的空间。线程运行时，需要创建对象，这需要申请内存。在分配内存时，系统会额外分分配一部分内存来避免锁竞争带来的开销。这也被看做是glibc的一个bug。[stackoverflow](https://serverfault.com/questions/341579/what-consumes-memory-in-java-process/554697#554697)
    > The issue might be related to this [glibc issue: malloc uses excessive memory for multi-threaded applications](https://sourceware.org/bugzilla/show_bug.cgi?id=11261).
    > Basically, when you have multiple threads allocating memory, glibc will scale up the number of available arenas to do allocation from to avoid lock contention. An arena is 64Mb large. The upper limit is to create 8 times the numeber of cores arenas. Arenas will be created on demand when a thread access an arena that is already locked so it grows with time.
    > In Java where you sprinkle with threads, this can quickly lead to a lot of arenas being created. And there being allocations spread all over these arenas. Initially each 64Mb arena is just mapped uncomitted memory, but as you do allocations you start to use actual memory for them.

4. [Malloc per-thread arenas in glibc](https://siddhesh.in/posts/malloc-per-thread-arenas-in-glibc.html)
    对glibc的内存分配，更多了解一下。
    > The malloc subsystem in glibc has had the feature of per-thread arenas for quite some time now and based on my experience, it seems to be the source of a lot of confusion. This is especially for enterprise users who move from RHEL-5 to RHEL-6 to see their apps taking up a lot more ‘memory’ and you’ll see a fair amount of content in this regard in the Red Hat Customer Portal if you’re a RHEL customer. This post is an attempt to get more perspective on this from the internal design perspective. I have not written any of the code in this implementation (barring a tiny improvement), so all of the talk about ‘original intention’ of any design is speculation on my behalf.
    > 
    > **Background**
    > The glibc malloc function allocates blocks of address space to callers by requesting memory from the kernel. I have written about the two syscalls glibc uses to do this, so I won’t repeat that here beyond mentioning that they two ‘places’ from where the address space is obtained are ‘arenas’ and ‘anonymous memory maps’ (referred to as anon maps in the rest of the post). The concept of interest here is the arena, which is nothing but a contiguous block of memory obtained from the kernel. The difference from the anon maps is that one anon map fulfills only one malloc request while an arena is a scratchpad that glibc maintains to return smaller blocks to the requestor. As you may have guessed, the data area (or ‘heap’) created by extending the process break (using the brk syscall) is also an arena - it is referred to as the main arena. The ‘heap’ keyword has a different meaning in relation to the glibc malloc implementation as we’ll find out later.
    > 
    > **The Arenas**
    > In addition to the main arena, glibc malloc allocates additional arenas. The reason for creation of arenas always seems to have been to improve performance of multithreaded processes. A malloc call needs to lock an arena to get a block from it and contention for this lock among threads is quite a performance hit. So the multiple arenas implementation did the following to reduce this contention:
    > 
    > 1. Firstly, the malloc call always tries to always stick to the arena it accessed the last time
    > 2. If the earlier arena is not available immediately (as tested by a pthread_mutex_trylock), try the next arena
    > 3. If we don't have uncontended access on any of the current arenas, then create a new arena and use it.
    > 
    > We obviously start with just the main arena. The main arena is extended using the brk() syscall and the other arenas are extended by mapping new ‘heaps’ (using mmap) and linking them. So an arena can generally be seen as a linked list of heaps.
    > 
    > **An Arena for a Thread**
    > The arena implementation was faster than the earlier model of using just a single arena with an on-demand model for reduction of contention in the general case. The process of detecting contention however was sufficiently slow and hence a better idea was needed. This is where the idea of having a thread stick to one arena comes in.
    > 
    > With the arena per-thread model, if malloc is called for the first time within a thread, a new arena is created without looking at whether the earlier locks would be contended or not. As a result, for a sane number of threads*, one can expect zero contention among threads when locking arenas since they’re all working on their own arenas.
    > 
    > There is a limit to the number of arenas that are created in this manner and that limit is determined based on the number of cores the system has. 32-bit systems get twice the number of cores and 64-bit systems get 8 times the number of cores. This can also be controlled using the MALLOC_ARENA_MAX environment variable.
    > 
    > A sane number of threads is usually not more than twice the number of cores. Anything more and you’ve have a different set of performance problems to deal with.
    > 
    > **The Problem (or not)**
    > The whole issue around the concept of arenas is the amount of ‘memory’ it tends to waste. The common complaint is that the virtual memory footprint of programs increase due to use of arenas and definitely more so due to using a different arena for each thread. The complaint is mostly bogus because of a couple of very important features that have been there for years - loads and loads of address space and demand paging.
    > 
    > The linux virtual memory model can allow a process to access most of its virtual memory address space (provided it is requested and mapped in). A 64-bit address space is massive and is hence not a resource that is likely to run out for any reasonable process. Add to it the fact that the kernel has mechanisms to use physical memory only for pages that are needed by the process at that time and you can rest assured that even if you map in terabytes of memory in your process, only the pages that are used will ever be accounted against you.
    > 
    > Both arena models rely on these facts. A mapped in arena is explicitly requested without any permissions (PROT_NONE) so that the kernel knows that it does not need any physical memory to back it. Block requests are then fulfilled by giving read+write permissions in parts. Freed blocks near the end of heaps in an arena are given back to the system either by using madvise(MADV_DONTNEED) or by explicitly unmapping.
    > 
    > So in the light of all of this, as an administrator or programmer, one needs to shift focus from the virtual memory column in your top output to the RSS column, since that is where the real resource usage is. Address space usage is not the same thing as memory usage. It is of course a problem if RSS is also high and in such cases one should look at the possibility of of a memory leak within to process and if that is not the problem, then fragmentation within the arenas. There are tuning options available to reduce fragmentation. Setting resource limits on address space usage is passe and it is time that better alternatives such as cgroups are given serious consideration.


### 总结（有问题）：
由于`glibc`分配的额外内存属于uncommit的部分，未记在堆使用内存中，但是会反应在进程占用的系统内存中。在qps较大的情况下，或者加上java程序YGC不频繁（分配内存过大），存活的线程句柄过多，则会导致RES内存过大。未gc回收的线程对象越多，占用内存就会越大。通过减少堆内存或新生代的内存分配，可以达到RES不会增加太离谱的目的。   

**思考**
    根据上面的分析，似乎可以得到内存占用高的原因是因为，`glibc`内存分配的arena预留空间导致的。但是，由于`pmap`看到的其实是虚拟内存，所以这个并不是导致内存过高的直接原因。可能只是影响到了运行中的线程的空间。
    Res内存高于当前java堆内存很多，有可能是因为之前java堆增长分配了比较高的内存，所以JVM并没有立即释放还给操作系统，仍然预留了这部分内存。
    另外，由于，运行时的线程数量比较多，也会一定程度的受到anera的影响。这个可以通过设置`MALLOC_ARENA_MAX`的值，来优化。或者更换`malloc`的内存管理，比如Google的`tcmalloc`等。

