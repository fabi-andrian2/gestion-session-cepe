package com.cepe.ui;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Onglet regroupant la gestion des Élèves et des Notes.
 */
public class EleveNotePanel extends JPanel {

    public EleveNotePanel() {
        setLayout(new java.awt.BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Gestion des Élèves", new EleveAdminPanel());
        tabs.addTab("Saisie des Notes", new NoteAdminPanel());
        add(tabs, java.awt.BorderLayout.CENTER);
    }
}