package com.cepe.service;

import com.cepe.model.Matiere;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MatiereService {

    Optional<Matiere> findById(int id) throws SQLException;

    List<Matiere> findAll() throws SQLException;

    Matiere save(Matiere matiere) throws SQLException;

    void update(Matiere matiere) throws SQLException;

    void delete(int id) throws SQLException;
}