package com.cepe.service;

import com.cepe.dao.MatiereDAO;
import com.cepe.dao.MatiereDAOImpl;
import com.cepe.model.Matiere;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MatiereServiceImpl implements MatiereService {

    private final MatiereDAO matiereDAO;

    public MatiereServiceImpl() {
        this.matiereDAO = new MatiereDAOImpl();
    }

    @Override
    public Optional<Matiere> findById(int id) throws SQLException {
        return matiereDAO.findById(id);
    }

    @Override
    public List<Matiere> findAll() throws SQLException {
        return matiereDAO.findAll();
    }

    @Override
    public Matiere save(Matiere matiere) throws SQLException {
        return matiereDAO.save(matiere);
    }

    @Override
    public void update(Matiere matiere) throws SQLException {
        matiereDAO.update(matiere);
    }

    @Override
    public void delete(int id) throws SQLException {
        matiereDAO.delete(id);
    }
}