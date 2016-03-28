import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MyTransientSerializable implements Serializable {
    transient String content1 = "I will not be serialized";
    String content2 = "I will be serialized";

    public static void main(String[] args) {
        ByteArrayOutputStream bs = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            bs = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(bs);
            objectOutputStream.writeObject(new MyTransientSerializable());
            objectOutputStream.flush();
            System.out.println(bs.toString("ISO-8859-1"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (Exception e) {
                }
            }
            if (bs != null) {
                try {
                    bs.close();
                } catch (Exception e) {
                }
            }
        }
    }
}