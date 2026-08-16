package com.cepe.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Carte KPI réutilisable affichant un label et une grande valeur.
 */
public class KpiCard extends JPanel {

    private final JLabel lblValeur;
    private final JLabel lblTitre;

    public KpiCard(String titre, String valeurInitiale, Color couleurValeur) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));
        setBackground(new Color(248, 250, 252));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 4, 0);

        lblTitre = new JLabel(titre);
        lblTitre.setFont(new Font(lblTitre.getFont().getName(), Font.PLAIN, 13));
        lblTitre.setForeground(new Color(100, 116, 139));
        add(lblTitre, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);

        lblValeur = new JLabel(valeurInitiale);
        lblValeur.setFont(new Font(lblValeur.getFont().getName(), Font.BOLD, 28));
        lblValeur.setForeground(couleurValeur);
        add(lblValeur, gbc);
    }

    public void setValeur(String valeur) {
        lblValeur.setText(valeur);
    }

    public void setCouleurValeur(Color couleur) {
        lblValeur.setForeground(couleur);
    }
}