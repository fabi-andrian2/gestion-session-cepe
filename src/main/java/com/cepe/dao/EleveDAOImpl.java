package com.cepe.dao;

import com.cepe.database.DatabaseConnection;
import com.cepe.model.Ecole;
import com.cepe.model.Eleve;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EleveDAOImpl implements EleveDAO {

    @Override
    public Optional<Eleve> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM ELEVE WHERE numEleve = ?";
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
    public List<Eleve> findAll() throws SQLException {
        List<Eleve> list = new ArrayList<>();
        String sql = "SELECT * FROM ELEVE ORDER BY nom, prenom";
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
    public List<Eleve> findAllWithEcole() throws SQLException {
        List<Eleve> list = new ArrayList<>();
        String sql = """
            SELECT e.*, ec.numEcole as ec_num, ec.design as ec_design, ec.adresse as ec_adresse
            FROM ELEVE e
            JOIN ECOLE ec ON e.numEcole = ec.numEcole
            ORDER BY e.nom, e.prenom
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Eleve eleve = mapRow(rs);
                Ecole ecole = new Ecole(
                    rs.getInt("ec_num"),
                    rs.getString("ec_design"),
                    rs.getString("ec_adresse")
                );
                eleve.setEcole(ecole);
                list.add(eleve);
            }
        }
        return list;
    }

    @Override
    public Eleve save(Eleve eleve) throws SQLException {
        String sql = "INSERT INTO ELEVE (numEcole, nom, prenom, dateNaissance) VALUES (?, ?, ?, ?) RETURNING numEleve";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eleve.getNumEcole());
            ps.setString(2, eleve.getNom());
            ps.setString(3, eleve.getPrenom());
            ps.setDate(4, Date.valueOf(eleve.getDateNaissance()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    eleve.setNumEleve(rs.getInt(1));
                }
            }
        }
        return eleve;
    }

    @Override
    public void update(Eleve eleve) throws SQLException {
        String sql = "UPDATE ELEVE SET numEcole = ?, nom = ?, prenom = ?, dateNaissance = ? WHERE numEleve = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eleve.getNumEcole());
            ps.setString(2, eleve.getNom());
            ps.setString(3, eleve.getPrenom());
            ps.setDate(4, Date.valueOf(eleve.getDateNaissance()));
            ps.setInt(5, eleve.getNumEleve());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM ELEVE WHERE numEleve = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Eleve> findByEcole(int numEcole) throws SQLException {
        List<Eleve> list = new ArrayList<>();
        String sql = "SELECT * FROM ELEVE WHERE numEcole = ? ORDER BY nom, prenom";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numEcole);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Eleve> findByNomContaining(String keyword) throws SQLException {
        List<Eleve> list = new ArrayList<>();
        String sql = "SELECT * FROM ELEVE WHERE nom ILIKE ? OR prenom ILIKE ? ORDER BY nom, prenom";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Eleve mapRow(ResultSet rs) throws SQLException {
        return new Eleve(
            rs.getInt("numEleve"),
            rs.getInt("numEcole"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getDate("dateNaissance").toLocalDate()
        );
    }
}