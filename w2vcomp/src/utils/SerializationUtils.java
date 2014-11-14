package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class SerializationUtils {

    public static byte[] serialize(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            byte[] ret = bos.toByteArray();
            return ret;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            try {
                bos.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public static Object deserialize(byte[] arr) {
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        ObjectInput in = null;
        try {
          in = new ObjectInputStream(bis);
          Object o = in.readObject();
          return o;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        finally {
          try {
            bis.close();
          } catch (IOException ex) {
              throw new RuntimeException(ex);
          }
          try {
            if (in != null) {
              in.close();
            }
          } catch (IOException ex) {
              throw new RuntimeException(ex);
          }
        }
    }

}
