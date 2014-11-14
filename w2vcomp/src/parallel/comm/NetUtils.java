package parallel.comm;

import java.io.IOException;

public class NetUtils {
    
    public static String getHostname() throws IOException {
        return convertStreamToString(Runtime.getRuntime().exec("hostname").getInputStream()).trim();
    }
    
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is);
        s.useDelimiter("\\A");
        String ret = s.hasNext() ? s.next() : "";
        s.close();
        return ret;
    }
}
