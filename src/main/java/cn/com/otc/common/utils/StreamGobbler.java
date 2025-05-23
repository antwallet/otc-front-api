package cn.com.otc.common.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {  
	  
    InputStream is;  
    String type;  
  
    public StreamGobbler(InputStream is, String type) {  
        this.is = is;  
        this.type = type;  
    }  
  
    public void run() {  
        try {  
            InputStreamReader isr = new InputStreamReader(is);  
            BufferedReader br = new BufferedReader(isr);  
            String line = null;  
            while ((line = br.readLine()) != null) {  
                if (type.equals("Error")) {  
                    System.out.println("Error   :" + line);  
                } else {  
                    System.out.println("Debug:" + line);  
                }  
            }  
        } catch (Exception ioe) {  
            ioe.printStackTrace();  
        }  
    }  
}  
