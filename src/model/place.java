package model;

public class place {

    // attibuts:

    public int nmr;
    public boolean vide=true;

    public place(int nmr){
        this.nmr=nmr;
    }
    public void charger()
    {
        vide=false;
        System.out.println("la place nmr "+nmr+" alloué");
    }
    public void liberer()
    {
        vide=true;
        System.out.println("la place nmr "+ nmr+" est libre");
    }


}
