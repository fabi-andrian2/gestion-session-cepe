package com.cepe.dao;

import com.cepe.database.DatabaseConnection;
import com.cepe.model.Matiere;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatiereDAOImpl implements MatiereDAO {

    @Override
    public Optional<Matiere> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM MATIERE WHERE numMat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Matiere> findAll() throws SQLException {
        List<Matiere> list = new ArrayList<>();
        String sql = "SELECT * FROM MATIERE ORDER BY numMat";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Matiere save(Matiere matiere) throws SQLException {
        String sql = "INSERT INTO MATIERE (designMat, coef) VALUES (?, ?) RETURNING numMat";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matiere.getDesignMat());
            ps.setInt(2, matiere.getCoef());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    matiere.setNumMat(rs.getInt(1));
                }
            }
        }
        return matiere;
    }

    @Override
    public void update(Matiere matiere) throws SQLException {
        String sql = "UPDATE MATIERE SET designMat = ?, coef = ? WHERE numMat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matiere.getDesignMat());
            ps.setInt(2, matiere.getCoef());
            ps.setInt(3, matiere.getNumMat());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM MATIERE WHERE numMat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Matiere mapRow(ResultSet rs) throws SQLException {
        return new Matiere(
            rs.getInt("numMat"),
            rs.getString("designMat"),
            rs.getInt("coef")
        );
    }
}