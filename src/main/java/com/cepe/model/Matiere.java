package com.cepe.model;

import java.util.Objects;

/**
 * Entité représentant une matière du CEPE.
 */
public class Matiere {

    private int numMat;
    private String designMat;
    private int coef;

    public Matiere() {}

    public Matiere(int numMat, String designMat, int coef) {
        this.numMat = numMat;
        this.designMat = designMat;
        this.coef = coef;
    }

    public int getNumMat() { return numMat; }
    public void setNumMat(int numMat) { this.numMat = numMat; }

    public String getDesignMat() { return designMat; }
    public void setDesignMat(String designMat) { this.designMat = designMat; }

    public int getCoef() { return coef; }
    public void setCoef(int coef) { this.coef = coef; }

    @Override
    public String toString() {
        return designMat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matiere matiere = (Matiere) o;
        return numMat == matiere.numMat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numMat);
    }
}