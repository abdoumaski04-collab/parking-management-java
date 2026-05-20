package model;
import threads.*;
import java.util.*;

public class parking {

    // attributs:
    private String name;
    private int nbr_places_max;
    private boolean vide=true;
    private int nbr_places_dispo=0;
    public ArrayList<place> places;
    public HashMap<Integer,voiture> voitures_presentes;
    public Queue<voiture> liste_attente;
    public double prixheure;

    public parking(String name,int nbr_places_max,double prixh)
    {
        liste_attente=new LinkedList<>(); // la création de la liste d'attente de type queue
        voitures_presentes=new HashMap<>(); // la création d'un bdd key value simple non ordonné pour stocker les voiture récentes
        places=new ArrayList<>(); // création d'' un tableau des places
        this.name=name;
        this.prixheure=prixh;
        this.nbr_places_max=nbr_places_max;
        for(int i=0;i<nbr_places_max;i++)
        {
            places.add(new place(i));
        }
        this.nbr_places_dispo=nbr_places_max;
    }
    public int getNbr_places_max(){
        return nbr_places_max;
    }
    public int getNbr_places_dispo(){
        return nbr_places_dispo;
    }
    public boolean verifierplein(){
        if(!vide)
            return true;
        else return false;
    }
    public int geturnmrplace() //vérifier une place vide et retourner son numero
    {

        int urnmrplace=0;
        for(int i=0;i<places.size();i++){
            if(places.get(i).vide) {
                urnmrplace=i;
                break;
            }
        }
        return urnmrplace;
    }
    public synchronized void ajouterlisteattente(voiture v) {
        liste_attente.add(v);
        System.out.println("vous êtes dans la liste d'attente ");
    }
    public synchronized voiture retirerlisteattente(){
        return (liste_attente.remove());
    }
    public synchronized int bonjourvoiture(voiture v)
    {
        if(verifierplein()) {
            System.out.println("le parcking est plein");
            ajouterlisteattente(v);
            return -1;
        }
        else{
            // collect des nmr des places vides
            int nmr_ur_place=geturnmrplace();
            voitures_presentes.put(nmr_ur_place,v);
            v.entrer();
            v.stationner(nmr_ur_place);
            places.get(nmr_ur_place).charger();
            nbr_places_dispo--;
            if(nbr_places_dispo==0) vide=false;
            return nmr_ur_place;
        }
    }
    public boolean trouvervoiture(int nmr){
        if(voitures_presentes.containsKey(nmr)) return true;
        else return false;
    }
    public synchronized void baybayvoiture(int nmr)
    {
        voiture v1;// qui existe dans le  parcking et veux sortie
        voiture v2;// qui est dans la liste d'attente et va prendre ça place dans le parcking
        if(trouvervoiture(nmr)){
            nbr_places_dispo++;
            places.get(nmr).liberer();
            v1=voitures_presentes.get(nmr);
            if(!vide) vide=true;
            v1.sortir();
            voitures_presentes.remove(nmr);
            calcultarif(v1);
            exportdata.exporterdata("src/data/transactions.csv",collectdata(v1));
            if(!liste_attente.isEmpty()) {
                v2 = retirerlisteattente();
                System.out.println(v2.getimmatricule() + " premier dans la liste attente ");
                bonjourvoiture(v2);
            }
        }
        else{
            System.out.println("vous avez entrer le mauvais nmr");
        }
    }
    void calcultarif(voiture v){
        v.tarif=this.prixheure*(v.getdureeparcking().toMinutes()/60.0);
        System.out.println(v.getimmatricule()+" doit payer "+v.tarif+" DH");
    }
    public void afficherparking(){
        for(Map.Entry<Integer,voiture> entry: voitures_presentes.entrySet()){
            int integer=entry.getKey();
            String immatr=entry.getValue().getimmatricule();
            System.out.println(integer+" "+immatr);
        }
    }
    public synchronized String collectdata(voiture v){
        String data=((v.getimmatricule()+","+v.dateentree+","+v.datesortir+","+v.duree+","+v.tarif)+"\n");
        return data;
    }
}
