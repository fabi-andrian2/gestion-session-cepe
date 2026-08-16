package com.cepe.ui;

import com.cepe.model.Ecole;
import com.cepe.service.NoteService;
import com.cepe.service.NoteServiceImpl;
import com.cepe.service.PdfService;
import com.cepe.service.SessionManager;
import com.cepe.service.dto.ResultatEleve;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Onglet "Résultats & Délibération".
 */
public class ResultatsPanel extends JPanel {

    private final NoteService noteService;

    private JTable tableResultats;
    private DefaultTableModel tableModel;
    private JComboBox<String> comboEcoles;
    private JTextField txtRecherche;
    private JSpinner spinnerSeuil;
    private JComboBox<String> comboVue;
    private JButton btnRafraichir;
    private JButton btnRelevePDF;
    private JLabel lblCompteur;

    private List<ResultatEleve> resultatsComplets;
    private List<ResultatEleve> resultatsFiltres;

    public ResultatsPanel() {
        this.noteService = new NoteServiceImpl();
        this.resultatsFiltres = new ArrayList<>();
        initUI();

        // Écoute du changement d'année scolaire
        SessionManager.getInstance().addPropertyChangeListener(evt -> {
            if ("anneeScolaire".equals(evt.getPropertyName())) {
                chargerDonnees();
            }
        });

        chargerDonnees();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ====== BARRE SUPÉRIEURE (Filtres) ======
        JPanel panelFiltres = new JPanel(new GridBagLayout());
        panelFiltres.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(), "Filtres"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Ligne 0
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFiltres.add(new JLabel("École :"), gbc);

        comboEcoles = new JComboBox<>();
        comboEcoles.setPreferredSize(new Dimension(220, 28));
        comboEcoles.addItem("Toutes les écoles");
        comboEcoles.addActionListener(e -> appliquerFiltres());
        gbc.gridx = 1;
        panelFiltres.add(comboEcoles, gbc);

        gbc.gridx = 2;
        panelFiltres.add(new JLabel("Recherche :"), gbc);

        txtRecherche = new JTextField(18);
        txtRecherche.setPreferredSize(new Dimension(200, 28));
        txtRecherche.putClientProperty("JTextField.placeholderText", "Nom ou prénom...");
        txtRecherche.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                appliquerFiltres();
            }
        });
        gbc.gridx = 3;
        panelFiltres.add(txtRecherche, gbc);

        btnRafraichir = new JButton("Lancer la Délibération / Rafraîchir");
        btnRafraichir.setFont(btnRafraichir.getFont().deriveFont(Font.BOLD));
        btnRafraichir.addActionListener(e -> chargerDonnees());
        gbc.gridx = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        panelFiltres.add(btnRafraichir, gbc);

        // Ligne 1
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        panelFiltres.add(new JLabel("Seuil délib. :"), gbc);

        SpinnerNumberModel seuilModel = new SpinnerNumberModel(9.75, 0.0, 20.0, 0.25);
        spinnerSeuil = new JSpinner(seuilModel);
        spinnerSeuil.setPreferredSize(new Dimension(80, 28));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinnerSeuil, "0.00");
        spinnerSeuil.setEditor(editor);
        spinnerSeuil.addChangeListener(e -> appliquerFiltres());
        gbc.gridx = 1;
        panelFiltres.add(spinnerSeuil, gbc);

        gbc.gridx = 2;
        panelFiltres.add(new JLabel("Vue :"), gbc);

        comboVue = new JComboBox<>(new String[]{
                "Tous les élèves",
                "Admis au CEPE",
                "Échoués / Ajournés",
                "Admis en 6ème"
        });
        comboVue.setPreferredSize(new Dimension(180, 28));
        comboVue.addActionListener(e -> appliquerFiltres());
        gbc.gridx = 3;
        panelFiltres.add(comboVue, gbc);

        add(panelFiltres, BorderLayout.NORTH);

        // ====== TABLEAU CENTRAL ======
        String[] colonnes = {"Rang Global", "Nom et Prénoms", "École", "Moyenne", "Rang / École", "Statut"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 4 -> Integer.class;
                    case 3 -> Double.class;
                    default -> String.class;
                };
            }
        };

        tableResultats = new JTable(tableModel);
        tableResultats.setRowHeight(32);
        tableResultats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableResultats.setShowGrid(false);
        tableResultats.setIntercellSpacing(new Dimension(0, 0));
        tableResultats.getTableHeader().setFont(tableResultats.getTableHeader().getFont().deriveFont(Font.BOLD));
        tableResultats.getTableHeader().setReorderingAllowed(false);

        javax.swing.table.TableColumn colRangEcole = tableResultats.getColumnModel().getColumn(4);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setToolTipText("Rang au sein de l'établissement");
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        colRangEcole.setHeaderRenderer(headerRenderer);

        tableResultats.getColumnModel().getColumn(3).setCellRenderer(new MoyenneCellRenderer());
        tableResultats.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        tableResultats.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableResultats.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableResultats.getColumnModel().getColumn(2).setPreferredWidth(180);
        tableResultats.getColumnModel().getColumn(3).setPreferredWidth(90);
        tableResultats.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableResultats.getColumnModel().getColumn(5).setPreferredWidth(100);

        tableResultats.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    btnRelevePDF.setEnabled(tableResultats.getSelectedRow() >= 0);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableResultats);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scrollPane, BorderLayout.CENTER);

        // ====== PIED DE PAGE ======
        JPanel panelSud = new JPanel(new BorderLayout());
        panelSud.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        lblCompteur = new JLabel(" ");
        lblCompteur.setHorizontalAlignment(SwingConstants.LEFT);
        panelSud.add(lblCompteur, BorderLayout.WEST);

        btnRelevePDF = new JButton("Télécharger Relevé PDF");
        btnRelevePDF.setEnabled(false);
        btnRelevePDF.addActionListener(e -> genererRelevePDF());
        panelSud.add(btnRelevePDF, BorderLayout.EAST);

        add(panelSud, BorderLayout.SOUTH);
    }

    private void chargerDonnees() {
        btnRafraichir.setEnabled(false);
        btnRafraichir.setText("Chargement en cours...");

        SwingWorker<List<ResultatEleve>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ResultatEleve> doInBackground() throws SQLException {
                return noteService.calculerResultatsGlobaux(SessionManager.getInstance().getAnneeScolaireActive());
            }

            @Override
            protected void done() {
                try {
                    resultatsComplets = get();
                    peuplerComboEcoles();
                    appliquerFiltres();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ResultatsPanel.this,
                            "Erreur lors du chargement : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnRafraichir.setEnabled(true);
                    btnRafraichir.setText("Lancer la Délibération / Rafraîchir");
                }
            }
        };
        worker.execute();
    }

    private void peuplerComboEcoles() {
        comboEcoles.removeAllItems();
        comboEcoles.addItem("Toutes les écoles");

        if (resultatsComplets != null) {
            resultatsComplets.stream()
                    .map(r -> r.getEleve().getEcole())
                    .filter(e -> e != null)
                    .distinct()
                    .sorted(java.util.Comparator.comparing(Ecole::getDesign))
                    .forEach(e -> comboEcoles.addItem(e.getDesign()));
        }
    }

    private void appliquerFiltres() {
        if (resultatsComplets == null) return;

        double seuilDouble = ((Number) spinnerSeuil.getValue()).doubleValue();
        BigDecimal seuil = BigDecimal.valueOf(seuilDouble);

        String ecoleSelectionnee = (String) comboEcoles.getSelectedItem();
        String recherche = txtRecherche.getText().trim().toLowerCase();
        String vue = (String) comboVue.getSelectedItem();

        resultatsFiltres = resultatsComplets.stream()
                .filter(r -> {
                    boolean matchEcole = "Toutes les écoles".equals(ecoleSelectionnee)
                            || (r.getEleve().getEcole() != null
                            && r.getEleve().getEcole().getDesign().equals(ecoleSelectionnee));
                    boolean matchNom = recherche.isEmpty()
                            || r.getEleve().getNomComplet().toLowerCase().contains(recherche);
                    BigDecimal moyenne = r.getMoyenneGenerale();
                    boolean matchVue = switch (vue) {
                        case "Admis au CEPE" -> moyenne.compareTo(seuil) >= 0;
                        case "Échoués / Ajournés" -> moyenne.compareTo(seuil) < 0;
                        case "Admis en 6ème" -> moyenne.compareTo(BigDecimal.valueOf(12.00)) >= 0;
                        default -> true;
                    };
                    return matchEcole && matchNom && matchVue;
                })
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (ResultatEleve r : resultatsFiltres) {
            String statutDynamique = r.getMoyenneGenerale().compareTo(seuil) >= 0 ? "ADMIS" : "AJOURNÉ";
            tableModel.addRow(new Object[]{
                    r.getRangGlobal(),
                    r.getEleve().getNomComplet(),
                    r.getEleve().getEcole() != null ? r.getEleve().getEcole().getDesign() : "N/A",
                    r.getMoyenneGenerale().doubleValue(),
                    r.getRangEcole(),
                    statutDynamique
            });
        }

        lblCompteur.setText(resultatsFiltres.size() + " élève(s) affiché(s)");
        btnRelevePDF.setEnabled(false);
    }

    private void genererRelevePDF() {
        int selectedRow = tableResultats.getSelectedRow();
        if (selectedRow < 0 || resultatsFiltres == null || selectedRow >= resultatsFiltres.size()) {
            return;
        }

        ResultatEleve selected = resultatsFiltres.get(selectedRow);
        String annee = SessionManager.getInstance().getAnneeScolaireActive();

        JFileChooser chooser = new JFileChooser();
        String nomFichier = "Releve_" + selected.getEleve().getNom() + "_" + selected.getEleve().getPrenom() + ".pdf";
        chooser.setSelectedFile(new File(nomFichier));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        btnRelevePDF.setEnabled(false);
        btnRelevePDF.setText("Génération...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                PdfService pdfService = new PdfService();
                pdfService.genererRelevePDF(
                        selected.getEleve().getNumEleve(),
                        annee,
                        file.getAbsolutePath());
                return null;
            }
            @Override
            protected void done() {
                btnRelevePDF.setEnabled(true);
                btnRelevePDF.setText("Télécharger Relevé PDF");
                try {
                    get();
                    int option = JOptionPane.showConfirmDialog(
                            ResultatsPanel.this,
                            "Relevé PDF généré avec succès.\n\nSouhaitez-vous l'ouvrir maintenant ?",
                            "Succès",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (option == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ResultatsPanel.this,
                            "Erreur lors de la génération du PDF :\n" + ex.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}