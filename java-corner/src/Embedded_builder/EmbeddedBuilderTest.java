package embedded_builder;

public class EmbeddedBuilderTest{
    public static void main(String[] args){
        Knight.Builder builder = new Knight.Builder();
        builder.setState("nihao");
        Knight knight = builder.build();
        System.out.println(knight.saysNi());
    }
}
