package com.cepe.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entité représentant un élève candidat au CEPE.
 */
public class Eleve {

    private int numEleve;
    private int numEcole;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;

    // Objet lié pour les jointures
    private Ecole ecole;

    public Eleve() {}

    public Eleve(int numEleve, int numEcole, String nom, String prenom, LocalDate dateNaissance) {
        this.numEleve = numEleve;
        this.numEcole = numEcole;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
    }

    public int getNumEleve() { return numEleve; }
    public void setNumEleve(int numEleve) { this.numEleve = numEleve; }

    public int getNumEcole() { return numEcole; }
    public void setNumEcole(int numEcole) { this.numEcole = numEcole; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public Ecole getEcole() { return ecole; }
    public void setEcole(Ecole ecole) { this.ecole = ecole; }

    /** Retourne le nom complet : NOM Prénom */
    public String getNomComplet() {
        return nom + " " + prenom;
    }

    @Override
    public String toString() {
        return getNomComplet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Eleve eleve = (Eleve) o;
        return numEleve == eleve.numEleve;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numEleve);
    }
}