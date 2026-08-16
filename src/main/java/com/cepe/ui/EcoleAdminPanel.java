package com.cepe.ui;

import com.cepe.model.Ecole;
import com.cepe.service.EcoleService;
import com.cepe.service.EcoleServiceImpl;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Panneau d'administration CRUD pour la table ECOLE.
 */
public class EcoleAdminPanel extends JPanel {

    private final EcoleService ecoleService;
    private JTable table;
    private DefaultTableModel model;

    public EcoleAdminPanel() {
        this.ecoleService = new EcoleServiceImpl();
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Toolbar
        JPanel toolbar = new JPanel();
        toolbar.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

        JButton btnAjouter = new JButton("Nouvelle École");
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

        // Table
        model = new DefaultTableModel(new String[]{"N°", "Désignation", "Adresse"}, 0) {
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
        SwingWorker<List<Ecole>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Ecole> doInBackground() throws SQLException {
                return ecoleService.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Ecole> liste = get();
                    model.setRowCount(0);
                    for (Ecole e : liste) {
                        model.addRow(new Object[]{e.getNumEcole(), e.getDesign(), e.getAdresse()});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EcoleAdminPanel.this,
                            "Erreur chargement écoles : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void modifierSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une école.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String design = (String) model.getValueAt(row, 1);
        String adresse = (String) model.getValueAt(row, 2);
        ouvrirDialog(new Ecole(id, design, adresse));
    }

    private void supprimerSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une école.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String design = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirmer la suppression de l'école \"" + design + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                ecoleService.delete(id);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(EcoleAdminPanel.this, "École supprimée.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerDonnees();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EcoleAdminPanel.this,
                            "Erreur suppression : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void ouvrirDialog(Ecole ecole) {
        boolean isEdit = (ecole != null);
        JTextField txtDesign = new JTextField(isEdit ? ecole.getDesign() : "");
        JTextField txtAdresse = new JTextField(isEdit ? ecole.getAdresse() : "");

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setPreferredSize(new Dimension(350, 100));
        panel.add(new JLabel("Désignation :"));
        panel.add(txtDesign);
        panel.add(new JLabel("Adresse :"));
        panel.add(txtAdresse);

        int result = JOptionPane.showConfirmDialog(this, panel,
                isEdit ? "Modifier l'École" : "Nouvelle École",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String design = txtDesign.getText().trim();
        String adresse = txtAdresse.getText().trim();

        if (design.isEmpty() || adresse.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Ecole nouvelle = isEdit ? new Ecole(ecole.getNumEcole(), design, adresse) : new Ecole(0, design, adresse);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                if (isEdit) {
                    ecoleService.update(nouvelle);
                } else {
                    ecoleService.save(nouvelle);
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(EcoleAdminPanel.this,
                            isEdit ? "École modifiée." : "École créée.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerDonnees();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EcoleAdminPanel.this,
                            "Erreur enregistrement : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}