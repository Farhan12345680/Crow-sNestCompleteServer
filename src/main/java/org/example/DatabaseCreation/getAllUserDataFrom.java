package org.example.DatabaseCreation;

import com.crowsnestfrontend.SerializedClasses.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;

import static org.example.server2.payloadBlockingQueue;

public class getAllUserDataFrom {

    public static void getUsers(String userName ,String time) {

        String getUserString = """
            SELECT name_id, image
            FROM Profiles
            WHERE name_id NOT IN (
                SELECT name_id_receiver FROM blockedUsers WHERE name_id_sender = ?
            ) AND name_id NOT IN (SELECT name_id_sender FROM blockedUsers WHERE name_id_receiver=?) AND time>?;
        """;
        String userCount= """
            SELECT COUNT(*)
            FROM Profiles
            WHERE name_id NOT IN (
                SELECT name_id_receiver FROM blockedUsers WHERE name_id_sender = ?
            ) AND name_id NOT IN (SELECT name_id_sender FROM blockedUsers WHERE name_id_receiver=?) AND time>?;
           """;
        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(getUserString);
             PreparedStatement ps2=conn.prepareStatement(userCount)) {

            ps.setString(1, userName);

            ps.setString(2,userName);

            ps.setString(3 , time);
            ResultSet rs = ps.executeQuery();

            ps2.setString(1, userName);

            ps2.setString(2,userName);

            ps2.setString(3 , time);

            ResultSet rs2= ps2.executeQuery();

            int u=rs2.getInt(1);

            for (int i=0;i<u;i++ ){
                rs.next();
                String targetUser = rs.getString(1);
                String image = rs.getString(2);

                if (targetUser == null || targetUser.equals(userName)) continue;

                payloadBlockingQueue.get(userName)
                        .add(new payLoadUsers(userName, new RequestUsers(targetUser, image)));

            }


        } catch (SQLException e) {
            System.err.println("DB error while getting users for: " + userName);
            e.printStackTrace();
        }
    }

    public static void getRequestingSituationOwner(String userName ,String time) {
        String databaseString = """
                SELECT target FROM Contacts WHERE owner_name=?  AND requestType=1 AND time >?;
                """;

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(databaseString)) {
            ps.setString(1, userName);
            ps.setString(2 , time);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (!payloadBlockingQueue.containsKey(userName)) {
                    System.err.println("No payload queue for user: " + userName + " — skipping updateStatus delivery.");
                    continue;
                }
                payloadBlockingQueue.get(userName)
                        .add(new updateStatus(userName, rs.getString(1), 10));
            }
        } catch (SQLException e) {
            System.err.println("DB error in getRequestingSituationOwner for: " + userName);
            e.printStackTrace();
        }
    }

    public static void getRequestingSituation(String userName ,String time) {
        String databaseString = """
                SELECT owner_name FROM Contacts WHERE target=? AND requestType=1 AND time>?;
                """;

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(databaseString)) {
            ps.setString(1, userName);
            ps.setString(2, time);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (!payloadBlockingQueue.containsKey(userName)) {
                    System.err.println("No payload queue for user: " + userName + " — skipping payloadFriendRequest delivery.");
                    continue;
                }
                payloadBlockingQueue.get(userName)
                        .add(new payloadFriendRequest(userName,
                                new pendingFriendRequest(rs.getString(1))));
            }
        } catch (SQLException e) {
            System.err.println("DB error in getRequestingSituation for: " + userName);
            e.printStackTrace();
        }
    }

    public static void updateFriendStatus(String owner_name, String target ) {
        String ss = """
                UPDATE Contacts
                SET requestType = 2 , time=CURRENT_TIMESTAMP
                WHERE owner_name =? AND target=?;""";

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(ss)) {
            ps.setString(1, target);
            ps.setString(2, owner_name);
            int updated = ps.executeUpdate();

            if (updated == 0) {
                System.err.println("No rows updated for updateFriendStatus(owner=" + owner_name + ", target=" + target + ")");
            }

            if (payloadBlockingQueue.containsKey(target)) {
                payloadBlockingQueue.get(target).add(new updateStatus(owner_name, target, 11));
            }
        } catch (SQLException e) {
            System.err.println("DB error in updateFriendStatus for owner: " + owner_name + ", target: " + target);
            e.printStackTrace();
        }
    }

    public static void showAllFriends(String owner_name ,String time) {
        String ss = """
                SELECT target FROM Contacts WHERE owner_name=? AND requestType=2 AND time>?;
                """;

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(ss)) {
            ps.setString(1, owner_name);
            ps.setString(2, time);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (payloadBlockingQueue.containsKey(owner_name)) {
                    payloadBlockingQueue.get(owner_name).add(new updateStatus(owner_name, rs.getString(1), 3));
                } else {
                    System.err.println("No payload queue for user: " + owner_name + " — skipping friend: " + rs.getString(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("DB error in showAllFriends (owner->target) for: " + owner_name);
            e.printStackTrace();
        }

        ss = """
            SELECT owner_name FROM Contacts WHERE target=? AND requestType=2 AND time>?;
            """;
        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(ss)) {
            ps.setString(1, owner_name);
            ps.setString(2 ,time);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (payloadBlockingQueue.containsKey(owner_name)) {
                    payloadBlockingQueue.get(owner_name).add(new updateStatus(owner_name, rs.getString(1), 3));
                } else {
                    System.err.println("No payload queue for user: " + owner_name + " — skipping friend: " + rs.getString(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("DB error in showAllFriends (target->owner) for: " + owner_name);
            e.printStackTrace();
        }

    }

    public static void allMessageReaction(String name , String time , ObjectOutputStream writer){

        System.out.println("this is the time --------------------------> "+time +" <-------------------this is the time");

        String ss= """
                SELECT id , reactionType from reactionTable WHERE
                (sender=? OR receiver=?) AND time>?;
                """;
        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(ss);
        ){

            ps.setString(1 , name );
            ps.setString(2,name);
            ps.setString(3 ,time );

            ResultSet rs=ps.executeQuery();
            while (rs.next()){
                System.out.println("Data found here");
                synchronized (writer){
                    writer.writeObject(new payloadMessageReaction(" "," ",rs.getInt(1),rs.getInt(2)));
                    writer.flush();
                }
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void makeBlockUser(String name, String utc, ObjectOutputStream finalWriter1) {
        String ss= """
                SELECT name_id_sender FROM blockedUsers WHERE name_id_receiver=? AND time>?;
                """;

        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(ss)){
            ps.setString(1 ,name);
            ps.setString(2 ,utc);

            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                finalWriter1.writeObject(new updateStatus(" " ,rs.getString("name_id_sender") , -144));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
