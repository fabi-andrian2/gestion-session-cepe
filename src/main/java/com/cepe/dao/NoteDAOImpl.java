package com.cepe.dao;

import com.cepe.database.DatabaseConnection;
import com.cepe.model.Ecole;
import com.cepe.model.Eleve;
import com.cepe.model.Matiere;
import com.cepe.model.Note;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteDAOImpl implements NoteDAO {

    @Override
    public Optional<Note> findById(String anneeScolaire, int numEleve, int numMat) throws SQLException {
        String sql = "SELECT * FROM NOTE WHERE anneeScolaire = ? AND numEleve = ? AND numMat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, anneeScolaire);
            ps.setInt(2, numEleve);
            ps.setInt(3, numMat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Note> findAll() throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT * FROM NOTE ORDER BY anneeScolaire, numEleve, numMat";
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
    public List<Note> findByEleve(int numEleve) throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT * FROM NOTE WHERE numEleve = ? ORDER BY anneeScolaire, numMat";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numEleve);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Note> findByEleveAndAnnee(int numEleve, String anneeScolaire) throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT * FROM NOTE WHERE numEleve = ? AND anneeScolaire = ? ORDER BY numMat";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numEleve);
            ps.setString(2, anneeScolaire);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Note> findByAnneeScolaire(String anneeScolaire) throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT * FROM NOTE WHERE anneeScolaire = ? ORDER BY numEleve, numMat";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, anneeScolaire);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Note> findAllWithDetails() throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = """
            SELECT 
                n.anneeScolaire, n.numEleve, n.numMat, n.note,
                e.numEcole, e.nom, e.prenom, e.dateNaissance,
                m.designMat, m.coef,
                ec.design as ec_design, ec.adresse as ec_adresse
            FROM NOTE n
            JOIN ELEVE e ON n.numEleve = e.numEleve
            JOIN MATIERE m ON n.numMat = m.numMat
            JOIN ECOLE ec ON e.numEcole = ec.numEcole
            ORDER BY n.anneeScolaire, e.nom, e.prenom, m.numMat
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Note note = mapRow(rs);

                Eleve eleve = new Eleve(
                    rs.getInt("numEleve"),
                    rs.getInt("numEcole"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getDate("dateNaissance").toLocalDate()
                );

                Ecole ecole = new Ecole(
                    rs.getInt("numEcole"),
                    rs.getString("ec_design"),
                    rs.getString("ec_adresse")
                );
                eleve.setEcole(ecole);

                Matiere matiere = new Matiere(
                    rs.getInt("numMat"),
                    rs.getString("designMat"),
                    rs.getInt("coef")
                );

                note.setEleve(eleve);
                note.setMatiere(matiere);
                list.add(note);
            }
        }
        return list;
    }

    @Override
    public List<String> findAnneesScolaires() throws SQLException {
        List<String> annees = new ArrayList<>();
        String sql = "SELECT DISTINCT anneeScolaire FROM NOTE ORDER BY anneeScolaire DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                annees.add(rs.getString("anneeScolaire"));
            }
        }
        return annees;
    }

    @Override
    public Note save(Note note) throws SQLException {
        String sql = "INSERT INTO NOTE (anneeScolaire, numEleve, numMat, note) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, note.getAnneeScolaire());
            ps.setInt(2, note.getNumEleve());
            ps.setInt(3, note.getNumMat());
            ps.setBigDecimal(4, note.getNote());
            ps.executeUpdate();
        }
        return note;
    }

    @Override
    public void update(Note note) throws SQLException {
        String sql = "UPDATE NOTE SET note = ? WHERE anneeScolaire = ? AND numEleve = ? AND numMat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, note.getNote());
            ps.setString(2, note.getAnneeScolaire());
            ps.setInt(3, note.getNumEleve());
            ps.setInt(4, note.getNumMat());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(String anneeScolaire, int numEleve, int numMat) throws SQLException {
        String sql = "DELETE FROM NOTE WHERE anneeScolaire = ? AND numEleve = ? AND numMat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, anneeScolaire);
            ps.setInt(2, numEleve);
            ps.setInt(3, numMat);
            ps.executeUpdate();
        }
    }

    private Note mapRow(ResultSet rs) throws SQLException {
        return new Note(
            rs.getString("anneeScolaire"),
            rs.getInt("numEleve"),
            rs.getInt("numMat"),
            rs.getBigDecimal("note")
        );
    }
}