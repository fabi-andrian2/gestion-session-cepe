package com.cepe.service.dto;

import com.cepe.model.Eleve;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO représentant le résultat complet d'un élève à la session CEPE.
 */
public class ResultatEleve {

    private Eleve eleve;
    private BigDecimal moyenneGenerale;
    private String statut;
    private int rangGlobal;
    private int rangEcole;

    public ResultatEleve() {}

    public ResultatEleve(Eleve eleve, BigDecimal moyenneGenerale, String statut, int rangGlobal, int rangEcole) {
        this.eleve = eleve;
        this.moyenneGenerale = moyenneGenerale;
        this.statut = statut;
        this.rangGlobal = rangGlobal;
        this.rangEcole = rangEcole;
    }

    public Eleve getEleve() {
        return eleve;
    }

    public void setEleve(Eleve eleve) {
        this.eleve = eleve;
    }

    public BigDecimal getMoyenneGenerale() {
        return moyenneGenerale;
    }

    public void setMoyenneGenerale(BigDecimal moyenneGenerale) {
        this.moyenneGenerale = moyenneGenerale;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getRangGlobal() {
        return rangGlobal;
    }

    public void setRangGlobal(int rangGlobal) {
        this.rangGlobal = rangGlobal;
    }

    public int getRangEcole() {
        return rangEcole;
    }

    public void setRangEcole(int rangEcole) {
        this.rangEcole = rangEcole;
    }

    @Override
    public String toString() {
        return String.format("ResultatEleve[%s | moy=%.2f | %s | RG=%d | RE=%d]",
                eleve != null ? eleve.getNomComplet() : "N/A",
                moyenneGenerale, statut, rangGlobal, rangEcole);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultatEleve that = (ResultatEleve) o;
        return Objects.equals(eleve, that.eleve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eleve);
    }
}