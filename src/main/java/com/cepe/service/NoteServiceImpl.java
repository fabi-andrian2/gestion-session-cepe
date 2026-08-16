package com.cepe.service;

import com.cepe.dao.EleveDAO;
import com.cepe.dao.EleveDAOImpl;
import com.cepe.dao.MatiereDAO;
import com.cepe.dao.MatiereDAOImpl;
import com.cepe.dao.NoteDAO;
import com.cepe.dao.NoteDAOImpl;
import com.cepe.model.Ecole;
import com.cepe.model.Eleve;
import com.cepe.model.Matiere;
import com.cepe.model.Note;
import com.cepe.service.dto.ResultatEleve;
import com.cepe.service.dto.StatistiquesEcole;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des notes.
 */
public class NoteServiceImpl implements NoteService {

    public static final BigDecimal SEUIL_ADMISSION = BigDecimal.valueOf(10);

    private final NoteDAO noteDAO;
    private final EleveDAO eleveDAO;
    private final MatiereDAO matiereDAO;

    public NoteServiceImpl() {
        this.noteDAO = new NoteDAOImpl();
        this.eleveDAO = new EleveDAOImpl();
        this.matiereDAO = new MatiereDAOImpl();
    }

    /* ---------- Délégation CRUD ---------- */

    @Override
    public Optional<Note> findById(String anneeScolaire, int numEleve, int numMat) throws SQLException {
        return noteDAO.findById(anneeScolaire, numEleve, numMat);
    }

    @Override
    public List<Note> findAll() throws SQLException {
        return noteDAO.findAll();
    }

    @Override
    public List<Note> findByEleve(int numEleve) throws SQLException {
        return noteDAO.findByEleve(numEleve);
    }

    @Override
    public List<Note> findByEleveAndAnnee(int numEleve, String anneeScolaire) throws SQLException {
        return noteDAO.findByEleveAndAnnee(numEleve, anneeScolaire);
    }

    @Override
    public List<Note> findByAnneeScolaire(String anneeScolaire) throws SQLException {
        return noteDAO.findByAnneeScolaire(anneeScolaire);
    }

    @Override
    public List<Note> findAllWithDetails() throws SQLException {
        return noteDAO.findAllWithDetails();
    }

    @Override
    public List<String> getAnneesScolaires() throws SQLException {
        return noteDAO.findAnneesScolaires();
    }

    @Override
    public Note save(Note note) throws SQLException {
        return noteDAO.save(note);
    }

    @Override
    public void update(Note note) throws SQLException {
        noteDAO.update(note);
    }

    @Override
    public void delete(String anneeScolaire, int numEleve, int numMat) throws SQLException {
        noteDAO.delete(anneeScolaire, numEleve, numMat);
    }

    /* ---------- Logique métier / Délibération ---------- */

    @Override
    public ResultatEleve calculerResultatEleve(int numEleve, String anneeScolaire) throws SQLException {
        Eleve eleve = eleveDAO.findById(numEleve).orElse(null);
        List<Note> notes = noteDAO.findByEleveAndAnnee(numEleve, anneeScolaire);
        List<Matiere> matieres = matiereDAO.findAll();

        Map<Integer, Matiere> matiereMap = matieres.stream()
                .collect(Collectors.toMap(Matiere::getNumMat, m -> m));

        return buildResultat(eleve, notes, matiereMap);
    }

    /**
     * Calcule les résultats pour une année scolaire.
     * FILTRAGE STRICT : seuls les élèves ayant au moins une note pour l'année
     * sont pris en compte. Si une session n'a aucune note, le résultat est vide.
     */
    @Override
    public List<ResultatEleve> calculerResultatsGlobaux(String anneeScolaire) throws SQLException {
        List<Eleve> eleves = eleveDAO.findAllWithEcole();
        List<Matiere> matieres = matiereDAO.findAll();
        List<Note> allNotes = noteDAO.findByAnneeScolaire(anneeScolaire);

        Map<Integer, Matiere> matiereMap = matieres.stream()
                .collect(Collectors.toMap(Matiere::getNumMat, m -> m));

        Map<Integer, List<Note>> notesParEleve = allNotes.stream()
                .collect(Collectors.groupingBy(Note::getNumEleve));

        List<ResultatEleve> resultats = new ArrayList<>();
        for (Eleve eleve : eleves) {
            List<Note> notesEleve = notesParEleve.get(eleve.getNumEleve());

            // FILTRAGE STRICT : ignorer les élèves sans notes pour cette session
            if (notesEleve == null || notesEleve.isEmpty()) {
                continue;
            }

            ResultatEleve res = buildResultat(eleve, notesEleve, matiereMap);
            resultats.add(res);
        }

        resultats.sort(Comparator.comparing(ResultatEleve::getMoyenneGenerale).reversed());

        for (int i = 0; i < resultats.size(); i++) {
            resultats.get(i).setRangGlobal(i + 1);
        }

        Map<Integer, List<ResultatEleve>> parEcoleId = resultats.stream()
                .filter(r -> r.getEleve() != null)
                .collect(Collectors.groupingBy(r -> r.getEleve().getNumEcole()));

        for (List<ResultatEleve> listeEcole : parEcoleId.values()) {
            listeEcole.sort(Comparator.comparing(ResultatEleve::getMoyenneGenerale).reversed());
            for (int i = 0; i < listeEcole.size(); i++) {
                listeEcole.get(i).setRangEcole(i + 1);
            }
        }

        resultats.sort(Comparator.comparing(ResultatEleve::getRangGlobal));
        return resultats;
    }

    @Override
    public List<StatistiquesEcole> calculerStatistiquesParEcole(String anneeScolaire) throws SQLException {
        List<ResultatEleve> resultats = calculerResultatsGlobaux(anneeScolaire);

        Map<Ecole, List<ResultatEleve>> parEcole = resultats.stream()
                .filter(r -> r.getEleve() != null && r.getEleve().getEcole() != null)
                .collect(Collectors.groupingBy(r -> r.getEleve().getEcole()));

        List<StatistiquesEcole> stats = new ArrayList<>();

        for (Map.Entry<Ecole, List<ResultatEleve>> entry : parEcole.entrySet()) {
            Ecole ecole = entry.getKey();
            List<ResultatEleve> liste = entry.getValue();

            int total = liste.size();
            int admis = (int) liste.stream()
                    .filter(r -> "ADMIS".equals(r.getStatut()))
                    .count();

            BigDecimal sommeMoyennes = liste.stream()
                    .map(ResultatEleve::getMoyenneGenerale)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal moyenneEcole = BigDecimal.ZERO;
            if (total > 0) {
                moyenneEcole = sommeMoyennes.divide(
                        BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            }

            stats.add(new StatistiquesEcole(ecole, total, admis, moyenneEcole));
        }

        stats.sort(Comparator.comparing(StatistiquesEcole::getTauxReussite).reversed());
        return stats;
    }

    /* ---------- Méthodes privées ---------- */

    private ResultatEleve buildResultat(Eleve eleve, List<Note> notes, Map<Integer, Matiere> matiereMap) {
        BigDecimal totalPoints = BigDecimal.ZERO;
        int totalCoef = 0;

        for (Note note : notes) {
            Matiere matiere = matiereMap.get(note.getNumMat());
            if (matiere != null) {
                totalPoints = totalPoints.add(
                        note.getNote().multiply(BigDecimal.valueOf(matiere.getCoef())));
                totalCoef += matiere.getCoef();
            }
        }

        BigDecimal moyenne = BigDecimal.ZERO;
        if (totalCoef > 0) {
            moyenne = totalPoints.divide(BigDecimal.valueOf(totalCoef), 2, RoundingMode.HALF_UP);
        }

        String statut = moyenne.compareTo(SEUIL_ADMISSION) >= 0 ? "ADMIS" : "AJOURNÉ";

        ResultatEleve resultat = new ResultatEleve();
        resultat.setEleve(eleve);
        resultat.setMoyenneGenerale(moyenne);
        resultat.setStatut(statut);
        resultat.setRangGlobal(0);
        resultat.setRangEcole(0);

        return resultat;
    }
}