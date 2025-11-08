package org.example.UserAuthentication;


import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class AuthChecker {
    private static final String URL = "jdbc:sqlite:main_database_page.db";

    public static boolean whetherIsUser(String name) {
        String sql = "SELECT 1 FROM Profiles WHERE name_id = ?;";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean doesPasswordMatch(String name, String password) {
        String sql = "SELECT password FROM Profiles WHERE name_id = ?;";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return BCrypt.checkpw(password, rs.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("Problem in Password checking"+e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void insertUser(String name, String password) {
        String sql = "INSERT INTO Profiles(name_id, password) VALUES(?, ?);";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, hashed);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
