package com.cepe.service;

import com.cepe.dao.EcoleDAO;
import com.cepe.dao.EcoleDAOImpl;
import com.cepe.model.Ecole;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EcoleServiceImpl implements EcoleService {

    private final EcoleDAO ecoleDAO;

    public EcoleServiceImpl() {
        this.ecoleDAO = new EcoleDAOImpl();
    }

    @Override
    public Optional<Ecole> findById(int id) throws SQLException {
        return ecoleDAO.findById(id);
    }

    @Override
    public List<Ecole> findAll() throws SQLException {
        return ecoleDAO.findAll();
    }

    @Override
    public Ecole save(Ecole ecole) throws SQLException {
        return ecoleDAO.save(ecole);
    }

    @Override
    public void update(Ecole ecole) throws SQLException {
        ecoleDAO.update(ecole);
    }

    @Override
    public void delete(int id) throws SQLException {
        ecoleDAO.delete(id);
    }

    @Override
    public List<Ecole> findByDesignContaining(String keyword) throws SQLException {
        return ecoleDAO.findByDesignContaining(keyword);
    }
}