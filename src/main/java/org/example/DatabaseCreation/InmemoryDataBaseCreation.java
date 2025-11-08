package org.example.DatabaseCreation;

import java.sql.*;

public class InmemoryDataBaseCreation {
    public static final String URL = "jdbc:sqlite:file::memory:?cache=shared";
    public static void main(String[] args) throws SQLException {
        initializeSchema();
        System.out.println("Database setup complete.");
    }
    public static void initializeSchema() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            System.out.println("problem active -->"+stmt.execute("""
    CREATE TABLE IF NOT EXISTS ActiveUsers (
        name_id TEXT PRIMARY KEY NOT NULL
    );
"""));

            System.out.println("problem busy-->"+ stmt.execute("""
    CREATE TABLE IF NOT EXISTS BusyUsers (
        name_id TEXT PRIMARY KEY NOT NULL,
        FOREIGN KEY (name_id) REFERENCES ActiveUsers (name_id)
    );
"""));

        }
    }



    public static void insertActiveUser(String name ){
        String updateStatement = "INSERT INTO ActiveUsers(name_id) values(?)";

        try(Connection conn= DriverManager.getConnection(URL);
            PreparedStatement ps= conn.prepareStatement(updateStatement)){

            ps.setString(1 , name);

            ps.executeUpdate();



        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    public static boolean  isAlreadyActive(String name ){
        String updateStatement = "SELECT COUNT(*) FROM ActiveUsers WHERE name_id=?";

        try(Connection conn= DriverManager.getConnection(URL);
            PreparedStatement ps= conn.prepareStatement(updateStatement)){

            ps.setString(1 , name);

            ResultSet rs=ps.executeQuery();

            while(rs.next()){
                if(rs.getInt(1)==1){
                    return true;
                }
            }


        }catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }



    public static void insertBusyUser(String name ){
        String updateStatement = "INSERT INTO BusyUsers(name_id) values(?)";

        try(Connection conn= DriverManager.getConnection(URL);
            PreparedStatement ps= conn.prepareStatement(updateStatement)){

            ps.setString(1 , name);

            ps.executeUpdate();



        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void removerActiveUser(String name ){
        String deleteUserString= """
                DELETE FROM ActiveUsers WHERE name_id=?;
                """;

        try(Connection conn= DriverManager.getConnection(URL);
            PreparedStatement ps= conn.prepareStatement(deleteUserString)){

            ps.setString(1 , name);

            ps.executeUpdate();



        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    public static void removeBusyUser(String name ){
        String deleteUserString= """
                DELETE FROM BusyUsers WHERE name_id=?;
                """;

        try(Connection conn= DriverManager.getConnection(URL);
            PreparedStatement ps= conn.prepareStatement(deleteUserString)){

            ps.setString(1 , name);

            ps.executeUpdate();



        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    public static boolean isUserAlreadyActive(String name){
        String activeUserString= """
                SELECT COUNT(*) FROM ActiveUsers WHERE name_id=?;
                """;

        try(Connection conn= DriverManager.getConnection(URL);
            PreparedStatement ps= conn.prepareStatement(activeUserString)){

            ps.setString(1 , name);

            ResultSet rs=ps.executeQuery();
            rs.next();
            System.out.println(rs.getInt(1));
            return rs.getInt(1) == 1;


        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isUserBusy(String name){
        String activeUserString= """
                SELECT COUNT(*) FROM BusyUsers WHERE name_id=?;
                """;

        try(Connection conn= DriverManager.getConnection(URL);
            PreparedStatement ps= conn.prepareStatement(activeUserString)){

            ps.setString(1 , name);

            ResultSet rs=ps.executeQuery();
            rs.next();

            return rs.getInt(1)==1;


        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static String showAllActiveUsers(){
        StringBuilder builder=new StringBuilder("<h1>these are all the active users<h1>") ;
        String showAllUserString = """
                Select name_id from ActiveUsers;
                """;
        try(Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps =conn.prepareStatement(showAllUserString)){

            ResultSet rs =ps.executeQuery();

            while(rs.next()){
                builder.append("<p>"+rs.getString(1) +"</p>");
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static String showAllBusyUsers(){
        StringBuilder builder=new StringBuilder("<h1>these are all the busy users<h1>") ;
        String showAllUserString = """
                Select name_id from BusyUsers;
                """;
        try(Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps =conn.prepareStatement(showAllUserString)){

            ResultSet rs =ps.executeQuery();

            while(rs.next()){
                builder.append("<p>"+rs.getString(1) +"</p>");
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        return builder.toString();
    }
}
