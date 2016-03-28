public class Ceboxing{
   public static void main(String[] args) throws Exception {
           Boolean b = true ? returnsNull() : false; // NPE on this line.
               System.out.println(b);
   }

   public static Boolean returnsNull() {
           return null;
   }
}
