package com.cepe.service;

import com.cepe.model.Eleve;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EleveService {

    Optional<Eleve> findById(int id) throws SQLException;

    List<Eleve> findAll() throws SQLException;

    List<Eleve> findAllWithEcole() throws SQLException;

    Eleve save(Eleve eleve) throws SQLException;

    void update(Eleve eleve) throws SQLException;

    void delete(int id) throws SQLException;

    List<Eleve> findByEcole(int numEcole) throws SQLException;

    List<Eleve> findByNomContaining(String keyword) throws SQLException;
}