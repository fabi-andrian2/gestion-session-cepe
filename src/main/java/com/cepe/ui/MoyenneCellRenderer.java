package com.cepe.ui;

import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer aligné à droite pour les colonnes numériques (moyenne, rang, etc.).
 * Formate les nombres avec 2 décimales.
 */
public class MoyenneCellRenderer extends DefaultTableCellRenderer {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.00");

    public MoyenneCellRenderer() {
        setHorizontalAlignment(RIGHT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof Number) {
            setText(FORMAT.format(((Number) value).doubleValue()));
        }
        setHorizontalAlignment(RIGHT);
        return this;
    }
}