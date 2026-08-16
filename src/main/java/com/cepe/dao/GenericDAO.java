package com.cepe.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interface générique CRUD pour tous les DAOs.
 *
 * @param <T>  type de l'entité
 * @param <ID> type de l'identifiant
 */
public interface GenericDAO<T, ID> {

    Optional<T> findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    T save(T entity) throws SQLException;

    void update(T entity) throws SQLException;

    void delete(ID id) throws SQLException;
}