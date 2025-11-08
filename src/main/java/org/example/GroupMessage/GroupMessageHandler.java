package org.example.GroupMessage;

import com.groupManagement.groupMessaging.*;
import org.example.DatabaseCreation.DatabaseCreation;

import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GroupMessageHandler {


    public static void insertIntoDatabase(groupMessagingSender sender){
        String query= """
                SELECT COUNT(*) from GroupMembers WHERE (memberName=? AND channelID=? AND roleID>0);
                """;

        try(Connection conn =DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1 , sender.clientName);
            ps.setInt(2 , sender.channelID);

            ResultSet rs=ps.executeQuery();

            int data = rs.getInt(1);
            if(data==0){
                return ;
            }

        }catch (Exception e){
            e.printStackTrace();
        }


        query= """
                Insert Into GroupMessage(channelID, messageText , imageURL ,messageSender) VALUES(?,?,?,?);
                """;

        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(query)){
            System.out.println("----> "+sender.channelID+" "+sender.messageText+" "+sender.imageURL);
            ps.setInt(1 , sender.channelID);
            ps.setString(2 , sender.messageText);
            ps.setString(3 , sender.imageURL);
            ps.setString(4 , sender.clientName);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void getMessages(groupMessagingRange range, ObjectOutputStream out) {
        String baseQuery =
                """
                SELECT messageID, channelID, messageText, imageURL,
                       strftime('%Y-%m-%d %H:%M:%S', time_sent) AS formattedCreationDate,
                       messageSender
                FROM GroupMessage
                WHERE (channelID = ?)
                """;

        String query;
        if (range.start == -1 && range.end == -1) {
            query = baseQuery + " ORDER BY channelID DESC LIMIT 15";
        } else if (range.start == -1) {
            query = baseQuery + " AND messageID > ? ORDER BY messageID LIMIT 15";
        } else if(range.end == -1){
            query = baseQuery + " AND messageID < ? ORDER BY messageID LIMIT 15";
        }else{
            System.out.println("this is the query");
            query=baseQuery + " AND messageID <= ? AND messageID >= ? ORDER BY messageID ";
        }

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, range.channelID);

            if (range.start == -1 && range.end != -1) {
                ps.setInt(2, range.end);
            } else if (range.start != -1  && range.end == -1) {
                ps.setInt(2, range.start);
            }else if(range.start!=-1 ){

                ps.setInt(2, range.end);
                ps.setInt(3, range.start);
            }

            try (ResultSet rs = ps.executeQuery()) {
                synchronized (out) {
                    while (rs.next()) {
                        System.out.println(rs.getString("messageSender"));
                        out.writeObject(new groupMessageDataReceiver(
                                rs.getString("messageSender"),
                                rs.getInt("messageID"),
                                rs.getInt("channelID"),
                                rs.getString("messageText"),
                                rs.getString("imageURL"),
                                rs.getString("formattedCreationDate")

                        ));
                        out.flush();
                    }

                    out.writeObject(new groupMessageDataEnder(null));
                    out.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void DeleteMessage(DeleteGroupMessage del){
        String query = """
                DELETE FROM GroupMessage WHERE messageID = ?;
                """;

        try(Connection conn =DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(query)){

            ps.setInt(1,del.messageID);
            ps.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void EditGroupMessageHandler(EditGroupMessage edit){
        String query = """
                Update GroupMessage set messageText=? , imageURL=? where messageID =?;
                """;

        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(query)){
            ps.setString(1,edit.messageText);
            ps.setString(2 , edit.imageURL);
            ps.setInt(3 , edit.messageID);
            ps.executeUpdate();



        }catch (Exception e ){
            e.printStackTrace();
        }
    }
}
