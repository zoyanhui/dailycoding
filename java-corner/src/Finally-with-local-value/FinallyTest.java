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