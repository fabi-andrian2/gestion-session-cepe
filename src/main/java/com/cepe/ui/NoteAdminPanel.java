package com.cepe.ui;

import com.cepe.model.Eleve;
import com.cepe.model.Matiere;
import com.cepe.model.Note;
import com.cepe.service.EleveService;
import com.cepe.service.EleveServiceImpl;
import com.cepe.service.MatiereService;
import com.cepe.service.MatiereServiceImpl;
import com.cepe.service.NoteService;
import com.cepe.service.NoteServiceImpl;
import com.cepe.service.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

/**
 * Panneau d'administration pour la saisie/modification des notes.
 * L'année scolaire est désormais dynamique via SessionManager.
 */
public class NoteAdminPanel extends JPanel {

    private final NoteService noteService;
    private final EleveService eleveService;
    private final MatiereService matiereService;

    private JComboBox<Eleve> comboEleves;
    private JPanel panelFormulaire;
    private JButton btnEnregistrer;
    private JLabel lblAnnee;

    private List<Matiere> matieres;
    private List<Eleve> eleves;
    private final Map<Integer, JSpinner> spinnersParMatiere = new HashMap<>();

    public NoteAdminPanel() {
        this.noteService = new NoteServiceImpl();
        this.eleveService = new EleveServiceImpl();
        this.matiereService = new MatiereServiceImpl();
        initUI();

        // Écoute du changement d'année scolaire
        SessionManager.getInstance().addPropertyChangeListener(evt -> {
            if ("anneeScolaire".equals(evt.getPropertyName())) {
                lblAnnee.setText("Année scolaire : " + SessionManager.getInstance().getAnneeScolaireActive());
                comboEleves.setSelectedIndex(-1);
                spinnersParMatiere.values().forEach(s -> s.setValue(0.0));
                btnEnregistrer.setEnabled(false);
            }
        });

        rafraichirDonnees();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ====== SÉLECTION ÉLÈVE ======
        JPanel panelHaut = new JPanel(new GridBagLayout());
        panelHaut.setBorder(BorderFactory.createTitledBorder("Sélection de l'élève"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelHaut.add(new JLabel("Élève :"), gbc);

        comboEleves = new JComboBox<>();
        comboEleves.setPreferredSize(new java.awt.Dimension(350, 28));
        comboEleves.addActionListener(e -> chargerNotesEleve());
        gbc.gridx = 1;
        panelHaut.add(comboEleves, gbc);

        lblAnnee = new JLabel("Année scolaire : " + SessionManager.getInstance().getAnneeScolaireActive());
        lblAnnee.setFont(lblAnnee.getFont().deriveFont(Font.ITALIC));
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        panelHaut.add(lblAnnee, gbc);

        add(panelHaut, BorderLayout.NORTH);

        // ====== FORMULAIRE DES NOTES ======
        panelFormulaire = new JPanel(new GridBagLayout());
        panelFormulaire.setBorder(BorderFactory.createTitledBorder("Notes par matière"));

        JScrollPane scroll = new JScrollPane(panelFormulaire);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scroll, BorderLayout.CENTER);

        // ====== BOUTON ENREGISTRER ======
        btnEnregistrer = new JButton("Enregistrer les notes");
        btnEnregistrer.setEnabled(false);
        btnEnregistrer.addActionListener(e -> enregistrerNotes());

        JPanel panelBas = new JPanel();
        panelBas.add(btnEnregistrer);
        add(panelBas, BorderLayout.SOUTH);

        // Rafraîchissement auto lors de la visibilité
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                rafraichirDonnees();
            }
        });
    }

    public void rafraichirDonnees() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                eleves = eleveService.findAllWithEcole();
                matieres = matiereService.findAll();
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    comboEleves.removeAllItems();
                    for (Eleve e : eleves) {
                        comboEleves.addItem(e);
                    }
                    construireFormulaire();
                    btnEnregistrer.setEnabled(false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NoteAdminPanel.this,
                            "Erreur rafraîchissement : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void construireFormulaire() {
        panelFormulaire.removeAll();
        spinnersParMatiere.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        for (Matiere m : matieres) {
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel lbl = new JLabel(m.getDesignMat() + " (coef. " + m.getCoef() + ") :");
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            panelFormulaire.add(lbl, gbc);

            gbc.gridx = 1;
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 20.0, 0.25));
            spinner.setPreferredSize(new java.awt.Dimension(100, 28));
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.00");
            spinner.setEditor(editor);
            panelFormulaire.add(spinner, gbc);

            spinnersParMatiere.put(m.getNumMat(), spinner);
            row++;
        }

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        panelFormulaire.add(new JLabel(""), gbc);

        panelFormulaire.revalidate();
        panelFormulaire.repaint();
    }

    private void chargerNotesEleve() {
        Eleve eleve = (Eleve) comboEleves.getSelectedItem();
        if (eleve == null) {
            btnEnregistrer.setEnabled(false);
            return;
        }

        btnEnregistrer.setEnabled(true);
        spinnersParMatiere.values().forEach(s -> s.setValue(0.0));

        SwingWorker<List<Note>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Note> doInBackground() throws SQLException {
                return noteService.findByEleveAndAnnee(eleve.getNumEleve(), SessionManager.getInstance().getAnneeScolaireActive());
            }
            @Override
            protected void done() {
                try {
                    List<Note> notes = get();
                    for (Note n : notes) {
                        JSpinner spinner = spinnersParMatiere.get(n.getNumMat());
                        if (spinner != null) {
                            spinner.setValue(n.getNote().doubleValue());
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NoteAdminPanel.this,
                            "Erreur chargement notes : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void enregistrerNotes() {
        Eleve eleve = (Eleve) comboEleves.getSelectedItem();
        if (eleve == null) return;

        btnEnregistrer.setEnabled(false);
        btnEnregistrer.setText("Enregistrement...");

        String annee = SessionManager.getInstance().getAnneeScolaireActive();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                for (Matiere m : matieres) {
                    JSpinner spinner = spinnersParMatiere.get(m.getNumMat());
                    if (spinner == null) continue;

                    double val = ((Number) spinner.getValue()).doubleValue();
                    BigDecimal noteVal = BigDecimal.valueOf(val).setScale(2, java.math.RoundingMode.HALF_UP);

                    Optional<Note> existante = noteService.findById(annee, eleve.getNumEleve(), m.getNumMat());
                    Note note = new Note(annee, eleve.getNumEleve(), m.getNumMat(), noteVal);

                    if (existante.isPresent()) {
                        noteService.update(note);
                    } else {
                        noteService.save(note);
                    }
                }
                return null;
            }
            @Override
            protected void done() {
                btnEnregistrer.setEnabled(true);
                btnEnregistrer.setText("Enregistrer les notes");
                try {
                    get();
                    JOptionPane.showMessageDialog(NoteAdminPanel.this,
                            "Notes enregistrées avec succès.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NoteAdminPanel.this,
                            "Erreur enregistrement : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}