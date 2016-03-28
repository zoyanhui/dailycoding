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
        A a = me.new A();
        a.i = 2;
    }
}

