

import java.io.*;

/**
 * Created by zhouyanhui on 3/26/16.
 */
public class MyExternalizable implements Externalizable {
    private transient String content = "I will be serialized";
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(content);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        content = (String) obj;
    }

    public static void main(String[] args) {
        MyExternalizable myExternalizable = new MyExternalizable();
        ByteArrayOutputStream bs = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInput in = null;
        try {
            // serialize
            bs = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(bs);
            objectOutputStream.writeObject(myExternalizable);
            objectOutputStream.flush();
            // Conversion to "UTF-8" does because it is not bijective (some characters are 2 bytes
            // but not all 2 bytes sequences are allowed as character sequences),
            // while "ISO-8859-1" is bijective (1 character of a String is a byte and vice-versa).
            String seriliazed = bs.toString("ISO-8859-1"); // can not use utf-8,
            System.out.println(seriliazed);

            // unserialize
            in = new ObjectInputStream(new ByteArrayInputStream(seriliazed.getBytes("ISO-8859-1")));
            MyExternalizable newMyExternalizable = (MyExternalizable) in.readObject();
            System.out.println(newMyExternalizable.content);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally{
            if(objectOutputStream != null){
                try{
                    objectOutputStream.close();
                }catch(Exception e){
                }
            }
            if(bs != null){
                try{
                    bs.close();
                }catch(Exception e){
                }
            }
            if(in != null){
                try{
                    in.close();
                }catch (Exception e){
                }
            }
        }

    }
}
