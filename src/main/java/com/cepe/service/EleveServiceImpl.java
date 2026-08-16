package com.cepe.service;

import com.cepe.dao.EleveDAO;
import com.cepe.dao.EleveDAOImpl;
import com.cepe.model.Eleve;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EleveServiceImpl implements EleveService {

    private final EleveDAO eleveDAO;

    public EleveServiceImpl() {
        this.eleveDAO = new EleveDAOImpl();
    }

    @Override
    public Optional<Eleve> findById(int id) throws SQLException {
        return eleveDAO.findById(id);
    }

    @Override
    public List<Eleve> findAll() throws SQLException {
        return eleveDAO.findAll();
    }

    @Override
    public List<Eleve> findAllWithEcole() throws SQLException {
        return eleveDAO.findAllWithEcole();
    }

    @Override
    public Eleve save(Eleve eleve) throws SQLException {
        return eleveDAO.save(eleve);
    }

    @Override
    public void update(Eleve eleve) throws SQLException {
        eleveDAO.update(eleve);
    }

    @Override
    public void delete(int id) throws SQLException {
        eleveDAO.delete(id);
    }

    @Override
    public List<Eleve> findByEcole(int numEcole) throws SQLException {
        return eleveDAO.findByEcole(numEcole);
    }

    @Override
    public List<Eleve> findByNomContaining(String keyword) throws SQLException {
        return eleveDAO.findByNomContaining(keyword);
    }
}