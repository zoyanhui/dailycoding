# java 双括号初始化集合类
见 src/DoubleBranceInitializeCollection/DoubleBrance.java

javac 编译后，会生成两个类：
-rw-rw-r-- 1 yhzhou yhzhou 614 1月   2 11:51 'DoubleBrace$1.class'
-rw-rw-r-- 1 yhzhou yhzhou 500 1月   2 11:51  DoubleBrace.class
-rw-rw-r-- 1 yhzhou yhzhou 211 1月   2 11:51  DoubleBrace.java


其中‘DoubleBrace$1.class’ 为ArrayList的子类，是匿名类。详细见下面字节码

查看字节码
* javap -c DoubleBrace.class
```
Warning: Binary file DoubleBrace contains double_brace_initialize_collection.DoubleBrace
Compiled from "DoubleBrace.java"
public class double_brace_initialize_collection.DoubleBrace {
  public double_brace_initialize_collection.DoubleBrace();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: aload_0
       5: new           #2                  // class double_brace_initialize_collection/DoubleBrace$1
       8: dup
       9: aload_0
      10: invokespecial #3                  // Method double_brace_initialize_collection/DoubleBrace$1."<init>":(Ldouble_brace_initialize_collection/DoubleBrace;)V
      13: putfield      #4                  // Field array:Ljava/util/List;
      16: return
}
```
* javap -c DoubleBrace\$1.class
```
Compiled from "DoubleBrace.java"
class double_brace_initialize_collection.DoubleBrace$1 extends java.util.ArrayList<java.lang.String> {
final double_brace_initialize_collection.DoubleBrace this$0;

double_brace_initialize_collection.DoubleBrace$1(double_brace_initialize_collection.DoubleBrace);
  Code:
     0: aload_0
     1: aload_1
     2: putfield      #1                  // Field this$0:Ldouble_brace_initialize_collection/DoubleBrace;
     5: aload_0
     6: invokespecial #2                  // Method java/util/ArrayList."<init>":()V
     9: aload_0
    10: ldc           #3                  // String hello
    12: invokevirtual #4                  // Method add:(Ljava/lang/Object;)Z
    15: pop
    16: return
}
```

### 结论
1. 双大括号初始化方法生成的.class文件要比常规方法多
2. 双大括号初始化方法运行时间要比常规方法长