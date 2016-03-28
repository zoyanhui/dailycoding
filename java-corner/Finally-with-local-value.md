# Local value's changing doesn't work in finally

```
public class FinallyTest{
    public static void main(String[] args){
        System.out.println(new FinallyTest().test());
    }

    public int test(){
        int res = 1;
        try{
            throw new Exception();            
        }catch (Exception e) {
            return res;
        }finally{
            res = 2;
            System.out.println("finally");
        }
    }
}
```
The output is :  
```
finally
1
```

Because, the `res`'s value *1* has been stored as a local temp variable. When `res` is changed to *2* in finally, it will not change the local temp variable which will be returned.

See the bytecode:  
```
public int test();
    descriptor: ()I
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=5, args_size=1
         0: iconst_1
         1: istore_1
         2: new           #7                  // class java/lang/Exception
         5: dup
         6: invokespecial #8                  // Method java/lang/Exception."<init>":()V
         9: athrow
        10: astore_2
        11: iload_1
        12: istore_3
        13: iconst_2
        14: istore_1
        15: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
        18: ldc           #9                  // String finally
        20: invokevirtual #10                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        23: iload_3
        24: ireturn
        25: astore        4
        27: iconst_2
        28: istore_1
        29: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
        32: ldc           #9                  // String finally
        34: invokevirtual #10                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        37: aload         4
        39: athrow
      Exception table:
         from    to  target type
             2    10    10   Class java/lang/Exception
             2    13    25   any
            25    27    25   any
      LineNumberTable:
        line 7: 0
        line 9: 2
        line 10: 10
        line 11: 11
        line 13: 13
        line 14: 15
        line 13: 25
        line 14: 29
      StackMapTable: number_of_entries = 2
        frame_type = 255 /* full_frame */
          offset_delta = 10
          locals = [ class FinallyTest, int ]
          stack = [ class java/lang/Exception ]
        frame_type = 78 /* same_locals_1_stack_item */
          stack = [ class java/lang/Throwable ]
```

The line `13: iconst_2` and `14: istore_1`, is related to `res = 2`. But above this two, is:
```
11: iload_1
12: istore_3
```

This means load the local variable one, and restore value to variable 3, which will be returned:
```
23: iload_3
24: ireturn
```
