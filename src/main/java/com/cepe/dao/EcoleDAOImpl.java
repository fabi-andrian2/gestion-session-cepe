package com.cepe.dao;

import com.cepe.database.DatabaseConnection;
import com.cepe.model.Ecole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EcoleDAOImpl implements EcoleDAO {

    @Override
    public Optional<Ecole> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM ECOLE WHERE numEcole = ?";
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
    public List<Ecole> findAll() throws SQLException {
        List<Ecole> list = new ArrayList<>();
        String sql = "SELECT * FROM ECOLE ORDER BY design";
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
    public Ecole save(Ecole ecole) throws SQLException {
        String sql = "INSERT INTO ECOLE (design, adresse) VALUES (?, ?) RETURNING numEcole";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ecole.getDesign());
            ps.setString(2, ecole.getAdresse());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ecole.setNumEcole(rs.getInt(1));
                }
            }
        }
        return ecole;
    }

    @Override
    public void update(Ecole ecole) throws SQLException {
        String sql = "UPDATE ECOLE SET design = ?, adresse = ? WHERE numEcole = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ecole.getDesign());
            ps.setString(2, ecole.getAdresse());
            ps.setInt(3, ecole.getNumEcole());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM ECOLE WHERE numEcole = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Ecole> findByDesignContaining(String keyword) throws SQLException {
        List<Ecole> list = new ArrayList<>();
        String sql = "SELECT * FROM ECOLE WHERE design ILIKE ? ORDER BY design";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Ecole mapRow(ResultSet rs) throws SQLException {
        return new Ecole(
            rs.getInt("numEcole"),
            rs.getString("design"),
            rs.getString("adresse")
        );
    }
}