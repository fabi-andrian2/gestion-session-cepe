package com.cepe.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entité représentant la note d'un élève dans une matière pour une année scolaire.
 * Clé primaire composite : (anneeScolaire, numEleve, numMat).
 */
public class Note {

    private String anneeScolaire;
    private int numEleve;
    private int numMat;
    private BigDecimal note;

    // Objets liés pour les jointures
    private Eleve eleve;
    private Matiere matiere;

    public Note() {}

    public Note(String anneeScolaire, int numEleve, int numMat, BigDecimal note) {
        this.anneeScolaire = anneeScolaire;
        this.numEleve = numEleve;
        this.numMat = numMat;
        this.note = note;
    }

    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }

    public int getNumEleve() { return numEleve; }
    public void setNumEleve(int numEleve) { this.numEleve = numEleve; }

    public int getNumMat() { return numMat; }
    public void setNumMat(int numMat) { this.numMat = numMat; }

    public BigDecimal getNote() { return note; }
    public void setNote(BigDecimal note) { this.note = note; }

    public Eleve getEleve() { return eleve; }
    public void setEleve(Eleve eleve) { this.eleve = eleve; }

    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }

    /** Retourne la note pondérée : note × coefficient */
    public BigDecimal getNotePonderee() {
        if (note == null || matiere == null) return BigDecimal.ZERO;
        return note.multiply(BigDecimal.valueOf(matiere.getCoef()));
    }

    @Override
    public String toString() {
        return String.format("Note[%s | élève=%d | matière=%d | note=%s]", 
            anneeScolaire, numEleve, numMat, note);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note other = (Note) o;
        return numEleve == other.numEleve 
            && numMat == other.numMat 
            && Objects.equals(anneeScolaire, other.anneeScolaire);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anneeScolaire, numEleve, numMat);
    }
}