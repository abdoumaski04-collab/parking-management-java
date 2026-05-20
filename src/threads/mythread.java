package threads;
import java.util.*;
import model.*;


public class mythread implements Runnable{
    public parking p;
    public voiture v;

    public mythread(parking p,voiture v){
        this.p=p;
        this.v=v;
    }

    public void run(){

        int nmrplace=p.bonjourvoiture(v);
        if(nmrplace!=-1) {
            try {
                int sec = 2000 + (int)( Math.random() * 5000);// entre 2s et 5s
                Thread.sleep(sec);
            } catch (Exception e) {
                System.out.println("erreur: " + e.getMessage());
            }
            p.baybayvoiture(nmrplace);
        }
    }

}