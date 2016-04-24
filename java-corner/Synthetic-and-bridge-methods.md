Synthetic and bridge methods


If you have ever played with reflection and executed getDeclaredMethods() you may have been surprised. You may get methods that are not present in the source code. Or, perhaps, you had a look at the modifiers of some of the methods and saw that some of these special methods are volatile. Btw: this is nasty question for Java interviews “What does it mean, when a method is volatile?” The proper answer is that a method can not be volatile. At the same time there can be some method among those returned by getDeclaredMethods() or evengetMethods() for which Modifier.isVolatile(method.getModifiers()) is true.

This has happened to one of the users of the project immutator. He realized that immutator (which itself digs quite deep into the dark details of Java) generated Java source that was not compilable using the keyword volatile as modifier for a method. As a consequence it did not work either.

What has happened there? What are the bridge and syntethic methods?

Visibility

When you create a nested or embedded class the private variables and methods of the nested class are reachable from the top level class. This used by the immutable embedded builder pattern. This is a well defined behavior of Java, defined in the language specification.

JLS7, 6.6.1 Determining Accessibility

… if the member or constructor is declared private, then access is
permitted if and only if it occurs within the body of the top level class (§7.6)
that encloses the declaration of the member or constructor…

package synthetic;

public class SyntheticMethodTest1 {
    private A aObj = new A();

    public class A {
        private int i;
    }

    private class B {
        private int i = aObj.i;
    }

    public static void main(String[] args) {
        SyntheticMethodTest1 me = new SyntheticMethodTest1();
        me.aObj.i = 1;
        B bObj = me.new B();
        System.out.println(bObj.i);
    }
}
How is it handled by the JVM? The JVM does not know inner or nested classes. For the JVM all classes are top level outer classes. All classes are compiled to be a top level class, and this is the way how those nice ...$. .class files are created.

 $ ls -Fart
../                         SyntheticMethodTest2$A.class  MyClass.java  SyntheticMethodTest4.java  SyntheticMethodTest2.java
SyntheticMethodTest2.class  SyntheticMethodTest3.java     ./            MyClassSon.java            SyntheticMethodTest1.java
If you create an nested or inner class it will be compiled to be a full blown top level class.

How will the private fields be available from the outer class? If those get into a top level class and are private, as they really are, then how will they be reachable from the outer class?

The way javac solves this issue that for any field, method or constructor being private but used from the top level class it generates a synthetic method. These synthetic methods are used to reach the original private filed/method/constructor. The generation of these methods are done in a clever way: only those are generated that are really needed and used from outside.

package synthetic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class SyntheticMethodTest2 {

    public static class A {
        private A(){}
        private int x;
        private void x(){};
    }

    public static void main(String[] args) {
        A a = new A();
        a.x = 2;
        a.x();
        System.out.println(a.x);
        for (Method m : A.class.getDeclaredMethods()) {
            System.out.println(String.format("%08X", m.getModifiers()) + " " + m.getName());
        }
        System.out.println("--------------------------");
        for (Method m : A.class.getMethods()) {
            System.out.println(String.format("%08X", m.getModifiers()) + " " + m.getReturnType().getSimpleName() + " " + m.getName());
        }
        System.out.println("--------------------------");
        for( Constructor<?> c : A.class.getDeclaredConstructors() ){
            System.out.println(String.format("%08X", c.getModifiers()) + " " + c.getName());
        }
    }
}
Since the name of the generated methods depend on the implementation and is not guaranteed the most I can say for the output of the above program is that on the specific platform where I executed it produced the following output:

2
00001008 access$1
00001008 access$2
00001008 access$3
00000002 x
--------------------------
00000111 void wait
00000011 void wait
00000011 void wait
00000001 boolean equals
00000001 String toString
00000101 int hashCode
00000111 Class getClass
00000111 void notify
00000111 void notifyAll
--------------------------
00000002 synthetic.SyntheticMethodTest2$A
00001000 synthetic.SyntheticMethodTest2$A
In the program above we assign value to the field x and we also call the method of the same name. These are needed to trigger the compiler to generate the synthetic methods. You can see that it generated three methods, presumably the setter and the getter for the fieldx and a synthetic method to the method x(). These synthetic methods, however, are not listed in the next list returned by getMethods() since these are synthetic methods and as such are not available for generic invocation. They are, in this sense, as private methods.

The hexa numbers can be interpreter looking at the constants defined in the class java.lang.reflect.Modifier:

00001008 SYNTHETIC|STATIC
00000002 PRIVATE
00000111 NATIVE|FINAL|PUBLIC
00000011 FINAL|PUBLIC
00000001 PUBLIC
00001000 SYNTHETIC
There are two constructors in the list. There is a private one and a synthetic one. The private exists, since we defined it. The synthetic on the other hand exists because we invoked the private one from outside. Bridge methods, however, do not had any so far.

Generics and inheritance

So good, so far, but we still did not see any “volatile” methods.

Looking at the source code of java.lang.reflec.Modifier you can see that the constant 0x00000040 is defined twice. Once as VOLATILE and once as BRIDGE (this latter is package private and is not for general use).

To have such a method a very simple program will do:

package synthetic;

import java.lang.reflect.Method;
import java.util.LinkedList;

public class SyntheticMethodTest3 {

    public static class MyLink extends LinkedList<String> {
        @Override
        public String get(int i) {
            return "";
        }
    }

    public static void main(String[] args) {

        for (Method m : MyLink.class.getDeclaredMethods()) {
            System.out.println(String.format("%08X", m.getModifiers()) + " " + m.getReturnType().getSimpleName() + " " + m.getName());
        }
    }
}
We have a linked list that has a method get(int) returning String. Let’s not discuss the clean code issues. This is a sample code to demonstrate the topic. The same issues come up in clean code as well, though more complex and harder to get to the point when it causes a problem.

The output says

00000001 String get
00001041 Object get
we have two get() methods. One that appears in the source code and another one, which is synthetic and bridge. The decompiler javap says that the generated code is:

public java.lang.String get(int);
  Code:
   Stack=1, Locals=2, Args_size=2
   0:   ldc     #2; //String
   2:   areturn
  LineNumberTable:
   line 12: 0


public java.lang.Object get(int);
  Code:
   Stack=2, Locals=2, Args_size=2
   0:   aload_0
   1:   iload_1
   2:   invokevirtual   #3; //Method get:(I)Ljava/lang/String;
   5:   areturn
The interesting this is that the signature of the two methods is the same and only the return types are different. This is allowed in the JVM even though this is not possible in the Java language. The bridge method does not do anything else, but calls the original one.

Why do we need this synthetic method? Who will use it. For example the code that wants to invoke the method get(int) using a variable that is no of the type MyLink:

        List<?> a = new MyLink();
        Object z = a.get(0);
It can not call the method returning String because there is no such in List. To make it more demonstrative lets override the method add() instead of get():

package synthetic;

import java.util.LinkedList;
import java.util.List;

public class SyntheticMethodTest4 {

    public static class MyLink extends LinkedList<String> {
        @Override
        public boolean add(String s) {
            return true;
        }
    }

    public static void main(String[] args) {
        List a = new MyLink();
        a.add("");
        a.add(13);
    }
}
We can see that the bridge method

public boolean add(java.lang.Object);
  Code:
   Stack=2, Locals=2, Args_size=2
   0:   aload_0
   1:   aload_1
   2:   checkcast       #2; //class java/lang/String
   5:   invokevirtual   #3; //Method add:(Ljava/lang/String;)Z
   8:   ireturn
not only calls the original one. It also checks that the type conversion is OK. This is done during run-time not done by the JVM itself. As you expect it does throw up in the line 18:

Exception in thread "main" java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
	at synthetic.SyntheticMethodTest4$MyLink.add(SyntheticMethodTest4.java:1)
	at synthetic.SyntheticMethodTest4.main(SyntheticMethodTest4.java:18)
When you get the question about volatile methods at an interview next time, you may know even more than the interviewer.