package org.example.Messages;

import com.crowsnestfrontend.SerializedClasses.Message;
import com.crowsnestfrontend.SerializedClasses.MessagePayload;
import com.crowsnestfrontend.SerializedClasses.deleteMessage;
import com.crowsnestfrontend.SerializedClasses.payloadMessageReaction;
import org.example.DatabaseCreation.DatabaseCreation;
import org.example.server2;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;

public class MessageData {

    public static int makeMessage(String clientName, Message msg, int isSent,int isReplyTo ,int messageType , String imageURL) {
        String makeMessage = """
            INSERT INTO Messages(sender, receiver, content,isSent, isReplyTo, messageType,imageURL) VALUES (?,?,?, ?, ?, ?,?);
            """;

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(makeMessage, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, clientName);
            ps.setString(2, msg.name());
            ps.setString(3, msg.getText());
            ps.setInt(4, isSent);
            ps.setInt(5 , isReplyTo);
            ps.setInt(6,messageType);
            ps.setString(7, imageURL);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("Insert failed");
                return 0;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getDateTime(int id){
        String dateTime="";
        String select="SELECT time_sent from Messages WHERE id=?";

        try(Connection conn=DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps=conn.prepareStatement(select)){

            ps.setInt(1,id);
            ResultSet rs=ps.executeQuery();
            while (rs.next()){
                return rs.getString(1);
            }
        }catch (SQLException e){

        }


        return dateTime;
    }

    public static void getAllMessage(String name , String time , ObjectOutputStream writer){
        String selectString = """
                SELECT * FROM Messages WHERE (receiver=? OR sender=?) AND time_sent>? AND
                id;
                """;
        String countString = """
                SELECT count(*) FROM Messages WHERE (receiver=? OR sender=?) AND time_sent>?;
                """;
        try(Connection conn=DriverManager.getConnection(DatabaseCreation.URL);
        PreparedStatement ps =conn.prepareStatement(selectString);
        PreparedStatement ps2=conn.prepareStatement(countString)
        ){
            ps.setString(1 , name);
            ps.setString(2,name);
            ps.setString(3 , time);
            ResultSet rs=ps.executeQuery();

            ps2.setString(1,name);
            ps2.setString(2 ,name);
            ps2.setString(3 , time);
            ResultSet rs2=ps2.executeQuery();
            rs2.next();


            while(rs.next()){
                if(server2.payloadBlockingQueue.containsKey(name)){
                    int sender=rs.getInt("isDeletedBySender");
                    int receiver=rs.getInt("isDeletedByReceiver");
                    MessagePayload payload=null;
                    if(sender==1 || receiver==1){
                        payload=new MessagePayload(
                                rs.getString("sender"),
                                rs.getInt("id"),
                                rs.getString("receiver"),
                                sender,
                                receiver,
                                rs.getString("content"),
                                rs.getInt("messageType"),
                                rs.getString("imageURL")
                                );
                        System.out.println("this is the url that is being sent to the other side -->" +rs.getString("imageURL"));
                        payload.isReply=rs.getInt("isReplyTo");

                    }else{
                        payload=new MessagePayload(
                                rs.getString("sender"),
                                rs.getInt("id"),
                                rs.getString("content"),
                                rs.getInt("reaction"),
                                rs.getString("time_sent"),
                                rs.getInt("messageType"),
                                rs.getString("imageURL")
                        );
                        System.out.println("this is the url that is being sent to the other side -->" +rs.getString("imageURL"));

                        payload.sender=rs.getString("receiver");
                        payload.isReply=rs.getInt("isReplyTo");

                    }
                    synchronized (writer){
                        writer.writeObject(payload);
                        writer.flush();
                    }

                }

            }

        }catch (SQLException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static String returnMessageSender(int id, payloadMessageReaction msgReaction){
        String ss= """
                SELECT sender FROM Messages  WHERE id =?;
                """;
        String ss2= """
                INSERT INTO reactionTable(sender ,receiver , id ,reactionType ) VALUES (?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET reactionType=excluded.reactionType ,
                time=CURRENT_TIMESTAMP;
                """;
        try(Connection conn =DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(ss);
            PreparedStatement ps2= conn.prepareStatement(ss2)){

            ps.setInt(1,id);
            ResultSet rs= ps.executeQuery();
            rs.next();
            System.out.println(rs.getString(1));

            ps2.setString(1 , msgReaction.clientName);
            ps2.setString(2 ,msgReaction.receiver);
            ps2.setInt(3 , msgReaction.messageID);
            ps2.setInt(4 , msgReaction.reactionType);

            ps2.executeUpdate();
            System.out.println(rs.getString(1));
            return rs.getString(1);
        }catch (SQLException e){
            e.printStackTrace();
        }

        return "";
    }

    public static void deleteMessages(deleteMessage del){
        String updateString = """
                Select sender from  Messages WHERE  id=?;
                """;
        System.out.println("came all the way here");
        String data="";

        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL) ;
            PreparedStatement ps = conn.prepareStatement(updateString)){

            ps.setInt(1,del.messageID);

            ResultSet rs= ps.executeQuery();
            rs.next();
            data = rs.getString(1);

        }catch (Exception e){

        }
        String ss3= """
                UPDATE Messages  SET isDeletedByReceiver=1 ,time_sent=CURRENT_TIMESTAMP WHERE id=?;
                """;
        String ss4= """
                UPDATE Messages SET content='',time_sent=CURRENT_TIMESTAMP
                ,reaction=0,  isDeletedBySender=1 WHERE id=?;
                """;

        if(del.initiator.equals(data)){
            System.out.println("ss4 was called");
            try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL) ;
                PreparedStatement ps = conn.prepareStatement(ss4)){

                ps.setInt(1,del.messageID);
                ps.executeUpdate();


            }catch (Exception e){

            }
        }else{
            System.out.println("ss3 was called");
            try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL) ;
                PreparedStatement ps = conn.prepareStatement(ss3)){

                ps.setInt(1,del.messageID);
                ps.executeUpdate();


            }catch (Exception e){

            }
        }


    }

}
