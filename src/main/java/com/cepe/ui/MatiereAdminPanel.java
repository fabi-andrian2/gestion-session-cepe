package com.cepe.ui;

import com.cepe.model.Matiere;
import com.cepe.service.MatiereService;
import com.cepe.service.MatiereServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Panneau d'administration CRUD pour la table MATIERE.
 */
public class MatiereAdminPanel extends JPanel {

    private final MatiereService matiereService;
    private JTable table;
    private DefaultTableModel model;

    public MatiereAdminPanel() {
        this.matiereService = new MatiereServiceImpl();
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel();
        toolbar.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

        JButton btnAjouter = new JButton("Nouvelle Matière");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnRafraichir = new JButton("Rafraîchir");

        btnAjouter.addActionListener(e -> ouvrirDialog(null));
        btnModifier.addActionListener(e -> modifierSelection());
        btnSupprimer.addActionListener(e -> supprimerSelection());
        btnRafraichir.addActionListener(e -> chargerDonnees());

        toolbar.add(btnAjouter);
        toolbar.add(btnModifier);
        toolbar.add(btnSupprimer);
        toolbar.add(btnRafraichir);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"N°", "Désignation", "Coefficient"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scroll, BorderLayout.CENTER);
    }

    private void chargerDonnees() {
        SwingWorker<List<Matiere>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Matiere> doInBackground() throws SQLException {
                return matiereService.findAll();
            }
            @Override
            protected void done() {
                try {
                    List<Matiere> liste = get();
                    model.setRowCount(0);
                    for (Matiere m : liste) {
                        model.addRow(new Object[]{m.getNumMat(), m.getDesignMat(), m.getCoef()});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MatiereAdminPanel.this,
                            "Erreur chargement matières : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void modifierSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une matière.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String design = (String) model.getValueAt(row, 1);
        int coef = (int) model.getValueAt(row, 2);
        ouvrirDialog(new Matiere(id, design, coef));
    }

    private void supprimerSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une matière.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String design = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirmer la suppression de la matière \"" + design + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                matiereService.delete(id);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MatiereAdminPanel.this, "Matière supprimée.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerDonnees();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MatiereAdminPanel.this,
                            "Erreur suppression : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void ouvrirDialog(Matiere matiere) {
        boolean isEdit = (matiere != null);
        JTextField txtDesign = new JTextField(isEdit ? matiere.getDesignMat() : "");
        JSpinner spinnerCoef = new JSpinner(new SpinnerNumberModel(isEdit ? matiere.getCoef() : 2, 1, 20, 1));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setPreferredSize(new Dimension(350, 80));
        panel.add(new JLabel("Désignation :"));
        panel.add(txtDesign);
        panel.add(new JLabel("Coefficient :"));
        panel.add(spinnerCoef);

        int result = JOptionPane.showConfirmDialog(this, panel,
                isEdit ? "Modifier la Matière" : "Nouvelle Matière",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String design = txtDesign.getText().trim();
        int coef = (Integer) spinnerCoef.getValue();

        if (design.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La désignation est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Matiere nouvelle = isEdit ? new Matiere(matiere.getNumMat(), design, coef) : new Matiere(0, design, coef);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                if (isEdit) {
                    matiereService.update(nouvelle);
                } else {
                    matiereService.save(nouvelle);
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MatiereAdminPanel.this,
                            isEdit ? "Matière modifiée." : "Matière créée.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerDonnees();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MatiereAdminPanel.this,
                            "Erreur enregistrement : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}