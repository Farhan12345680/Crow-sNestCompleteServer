package org.example.DatabaseCreation;

import com.crowsnestfrontend.SerializedClasses.*;
import com.crowsnestfrontend.SerializedClasses.Unfriend;
import com.crowsnestfrontend.SerializedClasses.makeUnblock;
import com.crowsnestfrontend.SerializedClasses.payLoadUsers;
import org.example.cloudinaryDefaults;

import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseCreation {
    public static final String URL = "jdbc:sqlite:main_database_page.db";


    public static void main(String[] args) throws SQLException {
        initializeSchema();
        System.out.println("Database setup complete.");
    }

    public static void initializeSchema() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS Profiles (
              name_id  TEXT PRIMARY KEY NOT NULL,
              password TEXT NOT NULL,
              image    TEXT,
              time DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS Contacts (
              owner_name TEXT NOT NULL,
              target     TEXT NOT NULL,
              requestType  INTEGER NOT NULL DEFAULT 0,
              time DATETIME DEFAULT CURRENT_TIMESTAMP,
              FOREIGN KEY(owner_name) REFERENCES Profiles(name_id) ON DELETE CASCADE,
              FOREIGN KEY(target)     REFERENCES Profiles(name_id) ON DELETE CASCADE,
              PRIMARY KEY(owner_name, target)
            );
        """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS GroupChannel(
                channelID INTEGER PRIMARY KEY AUTOINCREMENT,
                ChannelName TEXT NOT NULL,
                groupImageURL TEXT NOT NULL,
                groupDescription TEXT NOT NULL,
                groupCreationDate DATETIME DEFAULT CURRENT_TIMESTAMP,
                groupCreator TEXT NOT NULL,
                FOREIGN KEY(groupCreator) REFERENCES Profiles(name_id) ON DELETE CASCADE
                )
                """);
            stmt.execute("""
                Create Table if not exists GroupMembers(
                    memberID INTEGER PRIMARY KEY AUTOINCREMENT,
                    channelID INTEGER NOT NULL,
                    memberName TEXT NOT NULL,
                    roleID Integer NOT NULL,
                    MemberSince DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(memberName) REFERENCES Profiles(name_id) ON DELETE CASCADE,
                    FOREIGN KEY(channelID) REFERENCES GroupChannel(channelID) ON DELETE CASCADE
                    )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS GroupMessage(
                messageID INTEGER PRIMARY KEY AUTOINCREMENT,
                channelID INTEGER NOT NULL,
                messageText TEXT ,
                imageURL TEXT,
                replyMessageId INTEGER DEFAULT -1,
                time_sent DATETIME DEFAULT CURRENT_TIMESTAMP,
                messageSender Text NOT NULL,
                FOREIGN KEY(channelID) REFERENCES GroupChannel(channelID) ON DELETE CASCADE,
                FOREIGN KEY(messageSender) REFERENCES GroupMembers(memberID) ON DELETE CASCADE,
                FOREIGN KEY (replyMessageId) REFERENCES GroupMessage(messageID) ON DELETE CASCADE
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Messages (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  content TEXT NOT NULL,
                  isReplyTo INTEGER DEFAULT -1,
                  receiver TEXT NOT NULL,
                  sender TEXT NOT NULL,
                  messageType INTEGER DEFAULT 1,
                  imageURL TEXT  DEFAULT NULL,
                  time_sent DATETIME DEFAULT CURRENT_TIMESTAMP,
                  isSent INTEGER DEFAULT 1,
                  reaction INTEGER DEFAULT 0,
                  isDeletedByReceiver INTEGER DEFAULT 0,
                  isDeletedBySender INTEGER DEFAULT 0,
                  FOREIGN KEY(receiver) REFERENCES Profiles(name_id) ON DELETE CASCADE,
                  FOREIGN KEY(sender) REFERENCES Profiles(name_id) ON DELETE CASCADE,
                  FOREIGN KEY(isReplyTo) REFERENCES Messages(id) ON DELETE CASCADE
                );
        """);


            stmt.execute("""
            CREATE TABLE IF NOT EXISTS Call_Type (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              receiver TEXT NOT NULL,
              sender TEXT NOT NULL,
              time_sent DATETIME DEFAULT CURRENT_TIMESTAMP,
              call_type INTEGER NOT NULL,
              is_seen INTEGER DEFAULT 0,
              FOREIGN KEY(receiver) REFERENCES Profiles(name_id) ON DELETE CASCADE,
              FOREIGN KEY(sender) REFERENCES Profiles(name_id) ON DELETE CASCADE
            );
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS blockedUsers (
              name_id_sender TEXT NOT NULL,
              name_id_receiver TEXT NOT NULL,
              time DATETIME DEFAULT CURRENT_TIMESTAMP,
              FOREIGN KEY(name_id_sender) REFERENCES Profiles(name_id) ON DELETE CASCADE,
              FOREIGN KEY(name_id_receiver) REFERENCES Profiles(name_id) ON DELETE CASCADE
            );
        """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reactionTable(
                sender TEXT NOT NULL,
                receiver TEXT NOT NULL,
                id int NOT NULL PRIMARY KEY,
                reactionType int NOT NULL,
                time DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (sender) references Profiles(name_id) ON DELETE CASCADE,
                FOREIGN KEY (receiver) references Profiles(name_id) ON DELETE CASCADE
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS deletedMessage(
                deletedBy TEXT NOT NULL,
                messageId int NOT NULL,
                FOREIGN KEY (deletedBy) REFERENCES Profiles(name_id) ON DELETE CASCADE,
                FOREIGN KEY (messageId) REFERENCES Messages(id) ON DELETE CASCADE
                );
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Posts(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                isDeletedBySender INTEGER DEFAULT 0,
                postedBy TEXT NOT NULL ,
                PostTitle TEXT NOT NULL,
                PostDescription TEXT NOT NULL,
                PostPath TEXT NOT NULL,
                postComments TEXT DEFAULT NULL,
                time DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (postedBy) references Profiles(name_id) ON DELETE CASCADE
                );
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Comments(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                PostId INTEGER ,
                isDeletedBySender INTEGER DEFAULT 0,
                postedBy TEXT NOT NULL ,
                commentText TEXT NOT NULL,
                time DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (postedBy) references Profiles(name_id) ON DELETE CASCADE,
                FOREIGN KEY (PostId) references Posts(id) ON DELETE CASCADE
                );
                """);
            stmt.execute("CREATE INDEX IF NOT EXISTS blockedUserIndex ON blockedUsers(name_id_sender);");
            stmt.execute("CREATE INDEX IF NOT EXISTS blockedReceiverIndex ON blockedUsers(name_id_receiver);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contacts_owner ON Contacts(owner_name);");
            stmt.execute("CREATE INDEX IF NOT EXISTS indexTargetOwner on Contacts(target, requestType);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_recv_sndr ON Messages(receiver, sender);");
            stmt.execute("CREATE INDEX IF NOT EXISTS get_unseen_messages ON Messages(receiver, sender, isSent);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_reactionType ON Messages(id);");
        }
    }

    public static void addImagetoTheTable(String image_url, String name ) {
        String sql = "UPDATE Profiles SET image = ? ,time= CURRENT_TIMESTAMP WHERE name_id = ?;";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, image_url);
            ps.setString(2, name);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows updated: " + rowsAffected);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String addImageGithub(String name ,String url){
        AtomicReference<String> result= new AtomicReference<>("");
        String sql="Select image from Profiles WHERE name_id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            System.out.println(url);
            if(rs.next()){
                if(rs.getString(1)==null){
                    System.out.println("this is nul fiohfdfhdsaofodfref3erpferf" +
                            "hfhdfhdhfdhfdhfdhfhdfhdhf");
                    result.set(cloudinaryDefaults.githubToCloudinary(url));
                    System.out.println(result.get());
                }else{
                    result.set(rs.getString(1));

                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result.get();
    }

    public static String getUserImageData(String nameid) {
        String sql = "SELECT image FROM Profiles WHERE name_id = ?;";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nameid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("image");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static void makeRequests(String owner_name ,String target  )  {
        String ss= """
            INSERT INTO Contacts(owner_name , target ,requestType ) VALUES(? ,?,?);
            """;
        try(Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps =conn.prepareStatement(ss)){

            ps.setString(1 , owner_name);
            ps.setString(2 , target);
            ps.setInt(3,1);
            ps.executeUpdate();

        }catch (Exception e){
            System.out.println(e.getMessage()+" Error getting relationship getter" );
        }

    }

    public static  void deleteRequests(String owner_name , String target){
        String ss= """
            DELETE FROM Contacts WHERE owner_name=? AND target=?;
            """;
        try(Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps =conn.prepareStatement(ss)){

            ps.setString(1 , owner_name);
            ps.setString(2 , target);
            ps.executeUpdate();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


    public static void makeBlock(String clientName, String target){
        String selectString = """
            DELETE FROM Contacts WHERE owner_name=? AND target=?;
            """;

        String blockString = """
            INSERT INTO blockedUsers(name_id_sender , name_id_receiver) values (? ,?);
            """;
        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(selectString)
             ; PreparedStatement ps2 = conn.prepareStatement(selectString);
             PreparedStatement ps3 =conn.prepareStatement(blockString)) {
            ps.setString(1, clientName);
            ps.setString(2, target);
            ps.executeUpdate();

            ps2.setString(1,target);
            ps2.setString(2,clientName);
            ps2.executeUpdate();


            ps3.setString(1,clientName);
            ps3.setString(2,target);
            ps3.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static  void makeUNFriend(Unfriend unfriend){
        String ClientName  =unfriend.clientName;
        String name = unfriend.unfriendReceiver;

        String ss= """
            DELETE FROM Contacts WHERE owner_name=? AND target=?;
            """;

        try(Connection conn =DriverManager.getConnection(URL);
            PreparedStatement ps =conn.prepareStatement(ss)){
            ps.setString(1 , ClientName);
            ps.setString(2,name);

            ps.executeUpdate();


            ps.setString(1 ,name);
            ps.setString(2,ClientName);

            ps.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }


    public static Boolean blockedUserChecker(String clientName , String receiverName){
        String isBlockedUser= """
                SELECT COUNT(*) from blockedUsers WHERE name_id_sender=? AND name_id_receiver=?;
                """;
        try(Connection conn= DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(isBlockedUser)){

            ps.setString(1,receiverName);
            ps.setString(2 , clientName);

            ResultSet rs = ps.executeQuery();

            int data=rs.getInt(1);
            return data != 0;

        }catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }


    public static void makeUnBlock1(makeUnblock block1 , ObjectOutputStream out){
        String selectString = """
            DELETE FROM blockedUsers WHERE (name_id_sender=? AND name_id_receiver=?);
            """;

        String selectString2 = """
                Select * from Profiles WHERE name_id =?;
                """;
        System.out.println(block1.clientName +"<---this is the clientName | this is the receiver---> "+block1.receiver );
        try(Connection conn= DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(selectString);
            PreparedStatement ps2 = conn.prepareStatement(selectString2)){

            ps.setString(1 , block1.clientName);
            ps.setString(2 , block1.receiver);

            ps.executeUpdate();



            ps2.setString(1 , block1.receiver);

            ResultSet rs = ps2.executeQuery();

            while(rs.next()){
                String name =rs.getString("name_id");
                String image =rs.getString("image");
                synchronized (out){
                    out.writeObject(new payLoadUsers(block1.clientName ,new RequestUsers(name , image)));
                    out.flush();
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void showAllBlockUsers1(showAllBlockedUsers show ,ObjectOutputStream out){
        String query ="""
                SELECT prof.name_id , prof.image
                FROM blockedUsers bl
                JOIN Profiles prof ON prof.name_id=bl.name_id_receiver
                WHERE (bl.name_id_sender=?)
                """;

        try(Connection conn =DriverManager.getConnection(URL);
            PreparedStatement ps=conn.prepareStatement(query)){

            ps.setString(1 , show.clientName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                synchronized (out){
                    out.writeObject(new BlockedUser(rs.getString(1) ,rs.getString(2)));
                    out.flush();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
