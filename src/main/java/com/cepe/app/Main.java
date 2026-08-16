package com.cepe.app;

import com.cepe.ui.MainFrame;
import javax.swing.SwingUtilities;

/**
 * Point d'entrée de l'application.
 * Lance l'interface graphique Swing avec FlatLaf sur le Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}