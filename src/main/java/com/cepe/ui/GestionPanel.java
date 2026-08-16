package com.cepe.ui;

import java.awt.GridLayout;
import javax.swing.JPanel;

/**
 * Onglet "Gestion des Matières & Écoles".
 * Contient deux panneaux d'administration CRUD côte à côte.
 */
public class GestionPanel extends JPanel {

    public GestionPanel() {
        setLayout(new GridLayout(1, 2, 12, 0));
        add(new EcoleAdminPanel());
        add(new MatiereAdminPanel());
    }
}