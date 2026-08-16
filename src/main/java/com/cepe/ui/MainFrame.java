package com.cepe.ui;

import com.cepe.service.NoteService;
import com.cepe.service.NoteServiceImpl;
import com.cepe.service.SessionManager;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Fenêtre principale. Charge les années scolaires depuis la BDD au démarrage.
 */
public class MainFrame extends JFrame {

    private JComboBox<String> comboAnnees;

    public MainFrame() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 6);
        } catch (Exception ex) {
            System.err.println("Impossible de charger FlatLaf : " + ex.getMessage());
        }

        initUI();
        chargerAnneesDepuisBDD();
    }

    private void initUI() {
        setTitle("Gestion Session CEPE — Madagascar");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(950, 650));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel lblTitre = new JLabel("Gestion d'une Session CEPE");
        lblTitre.setFont(new Font(lblTitre.getFont().getName(), Font.BOLD, 22));
        lblTitre.setForeground(Color.WHITE);
        header.add(lblTitre, BorderLayout.WEST);

        JPanel panelSession = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelSession.setOpaque(false);

        JLabel lblSession = new JLabel("Session :");
        lblSession.setFont(new Font(lblSession.getFont().getName(), Font.PLAIN, 14));
        lblSession.setForeground(new Color(148, 163, 184));
        panelSession.add(lblSession);

        comboAnnees = new JComboBox<>();
        comboAnnees.addActionListener(e -> {
            String selected = (String) comboAnnees.getSelectedItem();
            if (selected != null && !selected.equals(SessionManager.getInstance().getAnneeScolaireActive())) {
                SessionManager.getInstance().setAnneeScolaireActive(selected);
            }
        });
        panelSession.add(comboAnnees);

        JButton btnNouvelleSession = new JButton("+");
        btnNouvelleSession.setToolTipText("Nouvelle session");
        btnNouvelleSession.setFont(btnNouvelleSession.getFont().deriveFont(Font.BOLD, 16));
        btnNouvelleSession.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this,
                    "Entrez la nouvelle année scolaire (format AAAA-AAAA) :",
                    "Nouvelle Session", JOptionPane.PLAIN_MESSAGE);
            if (input != null) {
                String trimmed = input.trim();
                if (trimmed.matches("\\d{4}-\\d{4}")) {
                    if (comboAnnees.getSelectedItem() == null || !trimmed.equals(comboAnnees.getSelectedItem())) {
                        comboAnnees.addItem(trimmed);
                    }
                    comboAnnees.setSelectedItem(trimmed);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Format invalide. Utilisez le format AAAA-AAAA (ex: 2026-2027).",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelSession.add(btnNouvelleSession);

        header.add(panelSession, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 13));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabbedPane.addTab("Résultats & Délibération", new ResultatsPanel());
        tabbedPane.addTab("Statistiques par École", new StatistiquesPanel());
        tabbedPane.addTab("Gestion Matières & Écoles", new GestionPanel());
        tabbedPane.addTab("Gestion Élèves & Notes", new EleveNotePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Charge les années scolaires distinctes depuis la BDD.
     * Si aucune n'existe, 2025-2026 est utilisée par défaut.
     */
    private void chargerAnneesDepuisBDD() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                NoteService noteService = new NoteServiceImpl();
                List<String> annees = noteService.getAnneesScolaires();
                if (annees.isEmpty()) {
                    annees.add("2025-2026");
                }
                return annees;
            }

            @Override
            protected void done() {
                try {
                    List<String> annees = get();
                    SessionManager.getInstance().setAnneesDisponibles(annees);

                    comboAnnees.removeAllItems();
                    annees.forEach(comboAnnees::addItem);

                    String defaut = annees.contains("2025-2026") ? "2025-2026" : annees.get(0);
                    comboAnnees.setSelectedItem(defaut);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}