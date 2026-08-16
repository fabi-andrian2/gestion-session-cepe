package com.cepe.ui;

import com.cepe.model.Ecole;
import com.cepe.model.Eleve;
import com.cepe.service.EcoleService;
import com.cepe.service.EcoleServiceImpl;
import com.cepe.service.EleveService;
import com.cepe.service.EleveServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
 * Panneau d'administration CRUD pour la table ELEVE.
 * Format de date : JJ-MM-AAAA (dd-MM-yyyy).
 */
public class EleveAdminPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final EleveService eleveService;
    private final EcoleService ecoleService;

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> comboFiltreEcole;
    private JTextField txtFiltreNom;

    private List<Eleve> elevesComplets;
    private List<Ecole> ecolesCache;

    public EleveAdminPanel() {
        this.eleveService = new EleveServiceImpl();
        this.ecoleService = new EcoleServiceImpl();
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ====== BARRE DE FILTRES ======
        JPanel panelFiltres = new JPanel(new GridBagLayout());
        panelFiltres.setBorder(BorderFactory.createTitledBorder("Filtres"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFiltres.add(new JLabel("École :"), gbc);

        comboFiltreEcole = new JComboBox<>();
        comboFiltreEcole.setPreferredSize(new java.awt.Dimension(220, 28));
        comboFiltreEcole.addItem("Toutes les écoles");
        comboFiltreEcole.addActionListener(e -> appliquerFiltres());
        gbc.gridx = 1;
        panelFiltres.add(comboFiltreEcole, gbc);

        gbc.gridx = 2;
        panelFiltres.add(new JLabel("Nom / Prénom :"), gbc);

        txtFiltreNom = new JTextField(18);
        txtFiltreNom.setPreferredSize(new java.awt.Dimension(200, 28));
        txtFiltreNom.putClientProperty("JTextField.placeholderText", "Rechercher...");
        txtFiltreNom.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                appliquerFiltres();
            }
        });
        gbc.gridx = 3;
        panelFiltres.add(txtFiltreNom, gbc);

        add(panelFiltres, BorderLayout.NORTH);

        // ====== TABLEAU ======
        model = new DefaultTableModel(new String[]{"N°", "Nom", "Prénom", "Date Naissance", "École"}, 0) {
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

        // ====== TOOLBAR ACTIONS ======
        JPanel toolbar = new JPanel();
        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton btnAjouter = new JButton("Nouvel Élève");
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
        add(toolbar, BorderLayout.SOUTH);
    }

    private void chargerDonnees() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                elevesComplets = eleveService.findAllWithEcole();
                ecolesCache = ecoleService.findAll();
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    peuplerComboEcoles();
                    appliquerFiltres();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EleveAdminPanel.this,
                            "Erreur chargement : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void peuplerComboEcoles() {
        comboFiltreEcole.removeAllItems();
        comboFiltreEcole.addItem("Toutes les écoles");
        if (ecolesCache != null) {
            ecolesCache.stream()
                    .sorted(java.util.Comparator.comparing(Ecole::getDesign))
                    .forEach(e -> comboFiltreEcole.addItem(e.getDesign()));
        }
    }

    private void appliquerFiltres() {
        if (elevesComplets == null) return;

        String ecoleSel = (String) comboFiltreEcole.getSelectedItem();
        String recherche = txtFiltreNom.getText().trim().toLowerCase();

        List<Eleve> filtres = elevesComplets.stream()
                .filter(el -> {
                    boolean matchEcole = "Toutes les écoles".equals(ecoleSel)
                            || (el.getEcole() != null && el.getEcole().getDesign().equals(ecoleSel));
                    boolean matchNom = recherche.isEmpty()
                            || el.getNomComplet().toLowerCase().contains(recherche);
                    return matchEcole && matchNom;
                })
                .collect(Collectors.toList());

        model.setRowCount(0);
        for (Eleve el : filtres) {
            model.addRow(new Object[]{
                    el.getNumEleve(),
                    el.getNom(),
                    el.getPrenom(),
                    el.getDateNaissance().format(DATE_FORMATTER),
                    el.getEcole() != null ? el.getEcole().getDesign() : "N/A"
            });
        }
    }

    private void modifierSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un élève.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        Eleve eleve = elevesComplets.stream()
                .filter(e -> e.getNumEleve() == id)
                .findFirst()
                .orElse(null);
        if (eleve != null) ouvrirDialog(eleve);
    }

    private void supprimerSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un élève.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String nom = (String) model.getValueAt(row, 1);
        String prenom = (String) model.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirmer la suppression de \"" + nom + " " + prenom + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                eleveService.delete(id);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(EleveAdminPanel.this, "Élève supprimé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerDonnees();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EleveAdminPanel.this,
                            "Erreur suppression : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Ouvre le dialogue d'ajout/modification après avoir rechargé
     * la liste des écoles fraîchement depuis la BDD.
     */
    private void ouvrirDialog(Eleve eleve) {
        boolean isEdit = (eleve != null);

        SwingWorker<List<Ecole>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Ecole> doInBackground() throws SQLException {
                return ecoleService.findAll();
            }
            @Override
            protected void done() {
                try {
                    List<Ecole> ecolesFraiches = get();
                    construireEtAfficherDialog(eleve, isEdit, ecolesFraiches);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EleveAdminPanel.this,
                            "Erreur chargement écoles : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Construit et affiche le dialogue modal avec les écoles fraîchement chargées.
     */
    private void construireEtAfficherDialog(Eleve eleve, boolean isEdit, List<Ecole> ecolesFraiches) {
        JTextField txtNom = new JTextField(isEdit ? eleve.getNom() : "");
        JTextField txtPrenom = new JTextField(isEdit ? eleve.getPrenom() : "");
        JTextField txtDate = new JTextField(isEdit ? eleve.getDateNaissance().format(DATE_FORMATTER) : "");
        txtDate.putClientProperty("JTextField.placeholderText", "JJ-MM-AAAA");

        JComboBox<Ecole> comboEcoles = new JComboBox<>();
        ecolesFraiches.stream()
                .sorted(java.util.Comparator.comparing(Ecole::getDesign))
                .forEach(comboEcoles::addItem);

        if (isEdit && eleve.getEcole() != null) {
            comboEcoles.setSelectedItem(eleve.getEcole());
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setPreferredSize(new java.awt.Dimension(400, 140));
        panel.add(new JLabel("Nom :"));
        panel.add(txtNom);
        panel.add(new JLabel("Prénom(s) :"));
        panel.add(txtPrenom);
        panel.add(new JLabel("Date naissance (JJ-MM-AAAA) :"));
        panel.add(txtDate);
        panel.add(new JLabel("École :"));
        panel.add(comboEcoles);

        int result = JOptionPane.showConfirmDialog(this, panel,
                isEdit ? "Modifier l'Élève" : "Nouvel Élève",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String dateStr = txtDate.getText().trim();
        Ecole ecole = (Ecole) comboEcoles.getSelectedItem();

        if (nom.isEmpty() || prenom.isEmpty() || dateStr.isEmpty() || ecole == null) {
            JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate dateNaissance;
        try {
            dateNaissance = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Date invalide. Format attendu : JJ-MM-AAAA", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Eleve nouvel = isEdit
                ? new Eleve(eleve.getNumEleve(), ecole.getNumEcole(), nom, prenom, dateNaissance)
                : new Eleve(0, ecole.getNumEcole(), nom, prenom, dateNaissance);

        SwingWorker<Void, Void> saveWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                if (isEdit) {
                    eleveService.update(nouvel);
                } else {
                    eleveService.save(nouvel);
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(EleveAdminPanel.this,
                            isEdit ? "Élève modifié." : "Élève créé.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerDonnees();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EleveAdminPanel.this,
                            "Erreur enregistrement : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        saveWorker.execute();
    }
}