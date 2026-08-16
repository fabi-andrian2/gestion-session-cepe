package com.cepe.ui;

import com.cepe.service.NoteService;
import com.cepe.service.NoteServiceImpl;
import com.cepe.service.SessionManager;
import com.cepe.service.dto.StatistiquesEcole;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Onglet "Statistiques par École".
 */
public class StatistiquesPanel extends JPanel {

    private final NoteService noteService;
    private final DecimalFormat nbFormat = new DecimalFormat("#,##0.00");

    private DefaultTableModel tableModel;
    private JTable tableStats;

    private KpiCard kpiTotal;
    private KpiCard kpiAdmis;
    private KpiCard kpiAjournes;
    private KpiCard kpiTauxGlobal;

    public StatistiquesPanel() {
        this.noteService = new NoteServiceImpl();
        initUI();

        SessionManager.getInstance().addPropertyChangeListener(evt -> {
            if ("anneeScolaire".equals(evt.getPropertyName())) {
                chargerStatistiques();
            }
        });

        chargerStatistiques();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel panelKpi = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        panelKpi.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        kpiTotal = new KpiCard("Candidats", "0", new Color(30, 41, 59));
        kpiAdmis = new KpiCard("Admis", "0", new Color(22, 101, 52));
        kpiAjournes = new KpiCard("Ajournés", "0", new Color(153, 27, 27));
        kpiTauxGlobal = new KpiCard("Taux Global", "0,00 %", new Color(37, 99, 235));

        panelKpi.add(kpiTotal);
        panelKpi.add(kpiAdmis);
        panelKpi.add(kpiAjournes);
        panelKpi.add(kpiTauxGlobal);

        add(panelKpi, BorderLayout.NORTH);

        String[] colonnes = {"École", "Total", "Admis", "Ajournés", "Taux %", "Moyenne"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 1, 2, 3 -> Integer.class;
                    case 4, 5 -> Double.class;
                    default -> String.class;
                };
            }
        };

        tableStats = new JTable(tableModel);
        tableStats.setRowHeight(32);
        tableStats.setShowGrid(false);
        tableStats.setIntercellSpacing(new Dimension(0, 0));
        tableStats.getTableHeader().setFont(tableStats.getTableHeader().getFont().deriveFont(Font.BOLD));
        tableStats.getTableHeader().setReorderingAllowed(false);

        MoyenneCellRenderer rendererNombres = new MoyenneCellRenderer();
        for (int i = 1; i <= 5; i++) {
            tableStats.getColumnModel().getColumn(i).setCellRenderer(rendererNombres);
        }

        JScrollPane scrollPane = new JScrollPane(tableStats);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scrollPane, BorderLayout.CENTER);

        JButton btnRafraichir = new JButton("Rafraîchir les statistiques");
        btnRafraichir.setFont(btnRafraichir.getFont().deriveFont(Font.BOLD));
        btnRafraichir.addActionListener(e -> chargerStatistiques());

        JPanel panelSud = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSud.add(btnRafraichir);
        add(panelSud, BorderLayout.SOUTH);
    }

    private void chargerStatistiques() {
        SwingWorker<List<StatistiquesEcole>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StatistiquesEcole> doInBackground() throws SQLException {
                return noteService.calculerStatistiquesParEcole(SessionManager.getInstance().getAnneeScolaireActive());
            }
            @Override
            protected void done() {
                try {
                    List<StatistiquesEcole> stats = get();
                    afficherStatistiques(stats);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            StatistiquesPanel.this,
                            "Erreur lors du chargement : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void afficherStatistiques(List<StatistiquesEcole> stats) {
        int totalGlobal = stats.stream().mapToInt(StatistiquesEcole::getNombreTotal).sum();
        int admisGlobal = stats.stream().mapToInt(StatistiquesEcole::getNombreAdmis).sum();
        int ajournesGlobal = totalGlobal - admisGlobal;

        BigDecimal tauxGlobal = BigDecimal.ZERO;
        if (totalGlobal > 0) {
            tauxGlobal = BigDecimal.valueOf(admisGlobal)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalGlobal), 2, RoundingMode.HALF_UP);
        }

        kpiTotal.setValeur(String.valueOf(totalGlobal));
        kpiAdmis.setValeur(String.valueOf(admisGlobal));
        kpiAjournes.setValeur(String.valueOf(ajournesGlobal));
        kpiTauxGlobal.setValeur(nbFormat.format(tauxGlobal) + " %");

        tableModel.setRowCount(0);
        for (StatistiquesEcole s : stats) {
            tableModel.addRow(new Object[]{
                    s.getEcole().getDesign(),
                    s.getNombreTotal(),
                    s.getNombreAdmis(),
                    s.getNombreAjournes(),
                    s.getTauxReussite().doubleValue(),
                    s.getMoyenneEcole().doubleValue()
            });
        }
    }
}