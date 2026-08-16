package com.cepe.service;

import com.cepe.model.Note;
import com.cepe.service.dto.ResultatEleve;
import com.cepe.service.dto.StatistiquesEcole;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface NoteService {

    Optional<Note> findById(String anneeScolaire, int numEleve, int numMat) throws SQLException;

    List<Note> findAll() throws SQLException;

    List<Note> findByEleve(int numEleve) throws SQLException;

    List<Note> findByEleveAndAnnee(int numEleve, String anneeScolaire) throws SQLException;

    List<Note> findByAnneeScolaire(String anneeScolaire) throws SQLException;

    List<Note> findAllWithDetails() throws SQLException;

    /** Retourne les années scolaires distinctes présentes en base. */
    List<String> getAnneesScolaires() throws SQLException;

    Note save(Note note) throws SQLException;

    void update(Note note) throws SQLException;

    void delete(String anneeScolaire, int numEleve, int numMat) throws SQLException;

    ResultatEleve calculerResultatEleve(int numEleve, String anneeScolaire) throws SQLException;

    List<ResultatEleve> calculerResultatsGlobaux(String anneeScolaire) throws SQLException;

    List<StatistiquesEcole> calculerStatistiquesParEcole(String anneeScolaire) throws SQLException;
}