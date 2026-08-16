package com.cepe.service;

import com.cepe.model.Ecole;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EcoleService {

    Optional<Ecole> findById(int id) throws SQLException;

    List<Ecole> findAll() throws SQLException;

    Ecole save(Ecole ecole) throws SQLException;

    void update(Ecole ecole) throws SQLException;

    void delete(int id) throws SQLException;

    List<Ecole> findByDesignContaining(String keyword) throws SQLException;
}