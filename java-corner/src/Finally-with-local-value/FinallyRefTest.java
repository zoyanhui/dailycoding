import java.lang.StringBuilder;

public class FinallyRefTest{
    public static void main(String[] args){
        System.out.println(new FinallyRefTest().test().toString());
    }

    public StringBuilder test(){
        StringBuilder sb = new StringBuilder();
        try{
            sb.append("hello");
            throw new Exception();            
        }catch (Exception e) {
            return sb;
        }finally{
            sb.append("world");
            System.out.println("finally");
        }
    }
}
