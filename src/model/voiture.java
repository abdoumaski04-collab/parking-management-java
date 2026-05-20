package model;
import java.util.*;
import java.time.LocalDateTime;
import java.time.Duration;

public class voiture {

    // attributs:
    private String immatricule;
    private String marque;
    private String proprietere;
    public LocalDateTime dateentree;
    public LocalDateTime datesortir;
    public Duration duree=null;
    public double tarif=0.0;

    // les methodes:
    public String getimmatricule(){
        return immatricule;
    }
    public String getMarque(){
        return marque;
    }
    public String getProprietere(){
        return proprietere;
    }
    // constructeur:
    public voiture(String immatricule,String marque,String proprietere){
        this.immatricule=immatricule;
        this.marque=marque;
        this.proprietere=proprietere;
    }
    public void entrer(){
        if(duree!=null)
            duree=null;
        dateentree=LocalDateTime.now();
        System.out.println("la voiture avec immatricule "+immatricule+" a entrée");
    }
    public void sortir(){
        datesortir=LocalDateTime.now();
        this.duree=getdureeparcking();
        System.out.println("la voiture avec immatricule "+immatricule+" a sorti");
    }
    public void stationner(int nmr){
        System.out.println("j'ai pris ma place nmr "+nmr);
    }
    public Duration getdureeparcking()
    {
        Duration duree=Duration.between(dateentree,datesortir);
        return duree;
    }
}
