package com.cepe.service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton gérant l'année scolaire active.
 * Par défaut : 2025-2026. Les années disponibles sont peuplées depuis la BDD.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String anneeScolaireActive;
    private final PropertyChangeSupport pcs;
    private final List<String> anneesDisponibles;

    private SessionManager() {
        this.anneeScolaireActive = "2025-2026";
        this.pcs = new PropertyChangeSupport(this);
        this.anneesDisponibles = new ArrayList<>();
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public String getAnneeScolaireActive() {
        return anneeScolaireActive;
    }

    public void setAnneeScolaireActive(String annee) {
        String old = this.anneeScolaireActive;
        this.anneeScolaireActive = annee;
        if (!anneesDisponibles.contains(annee)) {
            anneesDisponibles.add(annee);
        }
        pcs.firePropertyChange("anneeScolaire", old, annee);
    }

    public List<String> getAnneesDisponibles() {
        return new ArrayList<>(anneesDisponibles);
    }

    public void setAnneesDisponibles(List<String> annees) {
        this.anneesDisponibles.clear();
        this.anneesDisponibles.addAll(annees);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}