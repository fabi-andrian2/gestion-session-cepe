package com.cepe.service.dto;

import com.cepe.model.Ecole;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO représentant les statistiques de réussite d'une école pour une session.
 */
public class StatistiquesEcole {

    private Ecole ecole;
    private int nombreTotal;
    private int nombreAdmis;
    private int nombreAjournes;
    private BigDecimal tauxReussite;
    private BigDecimal moyenneEcole;

    /**
     * Constructeur avec calcul automatique du taux de réussite.
     *
     * @param ecole         l'école concernée
     * @param nombreTotal   nombre total de candidats
     * @param nombreAdmis   nombre d'élèves admis
     * @param moyenneEcole  moyenne générale de l'école
     */
    public StatistiquesEcole(Ecole ecole, int nombreTotal, int nombreAdmis, BigDecimal moyenneEcole) {
        this.ecole = ecole;
        this.nombreTotal = nombreTotal;
        this.nombreAdmis = nombreAdmis;
        this.nombreAjournes = nombreTotal - nombreAdmis;
        if (nombreTotal > 0) {
            this.tauxReussite = BigDecimal.valueOf(nombreAdmis)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(nombreTotal), 2, RoundingMode.HALF_UP);
        } else {
            this.tauxReussite = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        this.moyenneEcole = moyenneEcole;
    }

    public Ecole getEcole() {
        return ecole;
    }

    public void setEcole(Ecole ecole) {
        this.ecole = ecole;
    }

    public int getNombreTotal() {
        return nombreTotal;
    }

    public void setNombreTotal(int nombreTotal) {
        this.nombreTotal = nombreTotal;
    }

    public int getNombreAdmis() {
        return nombreAdmis;
    }

    public void setNombreAdmis(int nombreAdmis) {
        this.nombreAdmis = nombreAdmis;
    }

    public int getNombreAjournes() {
        return nombreAjournes;
    }

    public void setNombreAjournes(int nombreAjournes) {
        this.nombreAjournes = nombreAjournes;
    }

    public BigDecimal getTauxReussite() {
        return tauxReussite;
    }

    public void setTauxReussite(BigDecimal tauxReussite) {
        this.tauxReussite = tauxReussite;
    }

    public BigDecimal getMoyenneEcole() {
        return moyenneEcole;
    }

    public void setMoyenneEcole(BigDecimal moyenneEcole) {
        this.moyenneEcole = moyenneEcole;
    }

    @Override
    public String toString() {
        return String.format("StatistiquesEcole[%s | Total=%d | Admis=%d | Ajournés=%d | Taux=%.2f%% | Moy=%.2f]",
                ecole != null ? ecole.getDesign() : "N/A",
                nombreTotal, nombreAdmis, nombreAjournes, tauxReussite, moyenneEcole);
    }
}