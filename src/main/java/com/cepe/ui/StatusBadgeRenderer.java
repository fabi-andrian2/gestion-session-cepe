package com.cepe.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer personnalisé affichant le statut sous forme de badge coloré.
 * <ul>
 *   <li>ADMIS    : fond vert pastel, texte vert foncé</li>
 *   <li>AJOURNÉ  : fond rouge/rose clair, texte rouge foncé</li>
 * </ul>
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    private static final Color BG_ADMIS = new Color(220, 252, 231);
    private static final Color FG_ADMIS = new Color(22, 101, 52);
    private static final Color BG_AJOURNE = new Color(254, 226, 226);
    private static final Color FG_AJOURNE = new Color(153, 27, 27);

    public StatusBadgeRenderer() {
        setHorizontalAlignment(CENTER);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        String statut = value != null ? value.toString() : "";

        if ("ADMIS".equals(statut)) {
            label.setBackground(BG_ADMIS);
            label.setForeground(FG_ADMIS);
        } else if ("AJOURNÉ".equals(statut)) {
            label.setBackground(BG_AJOURNE);
            label.setForeground(FG_AJOURNE);
        } else {
            label.setBackground(table.getBackground());
            label.setForeground(table.getForeground());
        }

        if (!isSelected) {
            label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        }
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setHorizontalAlignment(CENTER);

        return label;
    }
}