package com.cepe.dao;

import com.cepe.model.Ecole;
import java.sql.SQLException;
import java.util.List;

public interface EcoleDAO extends GenericDAO<Ecole, Integer> {

    List<Ecole> findByDesignContaining(String keyword) throws SQLException;
}