package com.floteo.dao;

import com.floteo.model.Vehicle;
import com.floteo.model.VehicleStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VehicleDao {

    private final Connection conn;

    public VehicleDao(Connection conn) {
        this.conn = conn;
    }

    public Vehicle create(
            String plate,
            String type,
            String brand,
            String model,
            int mileage,
            LocalDate acquisitionDate,
            VehicleStatus status
    ) throws SQLException {

        String sql = """
            INSERT INTO vehicle
              (plate, type, brand, model, mileage, acquisition_date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id, plate, type, brand, model, mileage, acquisition_date, status
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.setString(2, type);
            ps.setString(3, brand);
            ps.setString(4, model);
            ps.setInt(5, mileage);
            ps.setDate(6, Date.valueOf(acquisitionDate));
            ps.setString(7, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        }
    }

    public Optional<Vehicle> findById(long id) throws SQLException {
        String sql = """
            SELECT id, plate, type, brand, model, mileage, acquisition_date, status
            FROM vehicle
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public Optional<Vehicle> findByPlate(String plate) throws SQLException {
        String sql = """
            SELECT id, plate, type, brand, model, mileage, acquisition_date, status
            FROM vehicle
            WHERE plate = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public List<Vehicle> findAll() throws SQLException {
        String sql = """
            SELECT id, plate, type, brand, model, mileage, acquisition_date, status
            FROM vehicle
            ORDER BY plate
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Vehicle> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

    public List<String> findAllTypeLabels() throws SQLException {
        String sql = """
        SELECT label
        FROM vehicle_type
        ORDER BY label
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<String> out = new ArrayList<>();
            while (rs.next()) out.add(rs.getString("label"));
            return out;
        }
    }



    public List<Vehicle> findByStatus(VehicleStatus status) throws SQLException {
        String sql = """
            SELECT id, plate, type, brand, model, mileage, acquisition_date, status
            FROM vehicle
            WHERE status = ?
            ORDER BY plate
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                List<Vehicle> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    public boolean updateStatus(long id, VehicleStatus status) throws SQLException {
        String sql = "UPDATE vehicle SET status = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM vehicle WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private static Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getLong("id"),
                rs.getString("plate"),
                rs.getString("type"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("mileage"),
                rs.getDate("acquisition_date").toLocalDate(),
                VehicleStatus.valueOf(rs.getString("status"))
        );
    }

    public List<Vehicle> findAvailableByText(String q) throws SQLException {
        String sql = """
        SELECT id, plate, type, brand, model, mileage, acquisition_date, status
        FROM vehicle
        WHERE status = 'DISPONIBLE'
          AND (? = '' OR LOWER(plate) LIKE LOWER(?) OR LOWER(brand) LIKE LOWER(?) OR LOWER(model) LIKE LOWER(?))
        ORDER BY plate
        """;

        String text = (q == null) ? "" : q.trim();
        String like = "%" + text + "%";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, text);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);

            try (ResultSet rs = ps.executeQuery()) {
                List<Vehicle> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }
    public boolean update(
            long id,
            String plate,
            String type,
            String brand,
            String model,
            int mileage,
            VehicleStatus status
    ) throws SQLException {

        String sql = """
        UPDATE vehicle
        SET plate = ?, type = ?, brand = ?, model = ?, mileage = ?, status = ?
        WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.setString(2, type);
            ps.setString(3, brand);
            ps.setString(4, model);
            ps.setInt(5, mileage);
            ps.setString(6, status.name());
            ps.setLong(7, id);
            return ps.executeUpdate() == 1;
        }
    }


}
