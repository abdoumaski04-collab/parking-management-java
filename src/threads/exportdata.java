package threads;
import java.util.*;
import java.io.*;

public class exportdata {
    public static synchronized void exporterdata(String chemin, String data) {
        try {
            FileWriter fr = new FileWriter(chemin,true);
            BufferedWriter bw = new BufferedWriter(fr);
            bw.write(data);
            bw.close();
        } catch (Exception e) {
            System.out.println("erreur: " + e.getMessage());
        }
    }
}
