package com.cepe.dao;

import java.sql.SQLException;
import java.util.List;

import com.cepe.model.Eleve;

public interface EleveDAO extends GenericDAO<Eleve, Integer> {

    List<Eleve> findByEcole(int numEcole) throws SQLException;

    List<Eleve> findByNomContaining(String keyword) throws SQLException;

    List<Eleve> findAllWithEcole() throws SQLException;
}