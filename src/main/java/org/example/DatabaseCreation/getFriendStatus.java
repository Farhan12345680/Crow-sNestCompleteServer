package org.example.DatabaseCreation;

import com.crowsnestfrontend.SerializedClasses.RequestUsers;
import com.crowsnestfrontend.SerializedClasses.returnQuery;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;

public class getFriendStatus {

    public static void getUsers(String userName , ObjectOutputStream writer ,int status){

        String getUserString="SELECT name_id, image FROM Profiles WHERE name_id NOT IN ( SELECT name_id_receiver FROM blockedUsers WHERE name_id_sender = ?);";
        String getUserCount="SELECT COUNT(*) FROM Profiles WHERE name_id NOT IN ( SELECT name_id_receiver FROM blockedUsers WHERE name_id_sender = ?);";

        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps =conn.prepareStatement(getUserString);
            PreparedStatement ps1=conn.prepareStatement(getUserCount);

        ){

            ps.setString(1 ,userName);
            ps.setInt(2,status);
            ResultSet rs =ps.executeQuery();

            ps1.setString(1 , userName);
            ps1.setInt(2,status);
            ResultSet rs1=ps1.executeQuery();

            if (rs1.next()) {
                writer.writeObject(new returnQuery(rs1.getInt(1) , " " ,""));
            }


            while(rs.next()){
                if (rs.getString(1).equals(userName)){
                    continue;
                }

                writer.writeObject(new RequestUsers(rs.getString(1) , ""));

            }

        }catch (SQLException | IOException e) {
            System.out.println("------>"+e.getMessage());
//            throw new RuntimeException(e);
        }
    }

}
