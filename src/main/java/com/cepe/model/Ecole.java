package com.cepe.model;

import java.util.Objects;

/**
 * Entité représentant une école primaire.
 */
public class Ecole {

    private int numEcole;
    private String design;
    private String adresse;

    public Ecole() {}

    public Ecole(int numEcole, String design, String adresse) {
        this.numEcole = numEcole;
        this.design = design;
        this.adresse = adresse;
    }

    public int getNumEcole() { return numEcole; }
    public void setNumEcole(int numEcole) { this.numEcole = numEcole; }

    public String getDesign() { return design; }
    public void setDesign(String design) { this.design = design; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    @Override
    public String toString() {
        return design;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ecole ecole = (Ecole) o;
        return numEcole == ecole.numEcole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numEcole);
    }
}