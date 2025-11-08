package org.example.PostProvider;

import com.PostFile.postDataSendingFinished;
import com.comment.*;
import org.example.DatabaseCreation.DatabaseCreation;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;

public class commentProvider {
    public static int getTheHighestIndex(){
        int data=0;

        String selectString ="SELECT MAX(id) from Comments";

        try(Connection conn= DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(selectString)){

            ResultSet rs=ps.executeQuery();

            while(rs.next()){
                data=rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        return data;

    };

    public static void HandlePost(commentData comment){

        Thread.startVirtualThread(()->{

            int index= getTheHighestIndex();



            String selectString ="INSERT INTO Comments(id ,PostId,postedBy ,commentText) values (?,?,?,?)";

            try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                PreparedStatement ps = conn.prepareStatement(selectString)) {
                ps.setInt(1 ,index+1  );
                ps.setInt(2 , comment.PostID);
                ps.setString(3 , comment.clientName);
                ps.setString(4 , comment.CommentDescription);

                ps.executeUpdate();

            }catch (SQLException e){e.printStackTrace();
                System.out.println("sql comment insertion problem ");
            }
        });
    }

    public static void deleteComment(int id){
        String selectString = """
                    Update Comments SET isDeletedBySender=1 WHERE  id=?;
                    """;

        try(Connection conn =DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps =conn.prepareStatement(selectString)){

            ps.setInt(1 , id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public static  void editComment(int id , commentEdit file){
        System.out.println("Edit comment request came from server ");
        String selectString = """
                    Update Comments SET commentText=? WHERE  id=?;
                    """;


        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(selectString)){

            ps.setString(1 , file.commentData);
            ps.setInt(2  ,file.commentID);

            ps.executeUpdate();



        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static  void getMyComments(commentDataRequestSenderBased getPost , ObjectOutputStream objOut){

        Thread.startVirtualThread(()->{
            String selectStringWithOffset = "Select * from Comments  WHERE isDeletedBySender = 0 AND PostId=? AND id>=? AND postedBy=? ORDER BY id DESC LIMIT 15  ";
            String selectStrignWithOutOffset="Select * from Comments WHERE isDeletedBySender = 0 AND PostId=?  AND postedBy=? ORDER BY id DESC LIMIT 15 ";
            String selectStrignNegetive ="Select * from Posts Comments isDeletedBySender = 0 AND id<? AND PostId=?  AND postedBy=? ORDER BY id DESC LIMIT 15 ";

            String selectStrignRange ="Select * from Posts Comments isDeletedBySender = 0 AND id<=? AND PostId=?  AND id>=?  AND postedBy=? ORDER BY id DESC LIMIT 15 ";

            if(getPost.start!=-1 && getPost.end!=-1 ){

                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStrignRange)) {
                    ps.setInt(1, getPost.end);
                    ps.setInt(2, getPost.PostID);
                    ps.setInt(3, getPost.start);
                    ps.setString(4 ,getPost.clientName);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            objOut.writeObject(new commentDataReturn(
                                    rs.getString("postedBy"),
                                    rs.getInt("PostId"),
                                    rs.getInt("id"),
                                    rs.getString("commentText"),
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(getPost.end==-1 && getPost.start==-1 ){

                try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                    PreparedStatement ps = conn.prepareStatement(selectStrignWithOutOffset)){


                    ps.setInt(1, getPost.PostID);

                    ps.setString(2 ,getPost.clientName);


                    ResultSet rs=ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            objOut.writeObject(new commentDataReturn(
                                    rs.getString("postedBy"),
                                    rs.getInt("PostId"),
                                    rs.getInt("id"),
                                    rs.getString("commentText"),
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if(getPost.end==-1){
                System.out.println("called 2");
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStrignNegetive)) {
                    ps.setInt(1, getPost.start);
                    ps.setInt(2 ,getPost.PostID);
                    ps.setString(3,getPost.clientName);

                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            objOut.writeObject(new commentDataReturn(
                                    rs.getString("postedBy"),
                                    rs.getInt("PostId"),
                                    rs.getInt("id"),
                                    rs.getString("commentText"),
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStringWithOffset)) {
                    ps.setInt(1, getPost.PostID);
                    ps.setInt(2 ,getPost.end);
                    ps.setString(3 , getPost.clientName);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            objOut.writeObject(new commentDataReturn(
                                    rs.getString("postedBy"),
                                    rs.getInt("PostId"),
                                    rs.getInt("id"),
                                    rs.getString("commentText"),
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            synchronized (objOut){
                try {
                    objOut.writeObject(new postDataSendingFinished());objOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public static  void getPost(commentDataRequest getPost , ObjectOutputStream objOut){
        System.out.println("select comment called ");
        System.out.println(getPost.start +" "+getPost.end +" "+getPost.PostID);
        Thread.startVirtualThread(()->{
            String selectStringWithOffset = "Select * from Comments  WHERE isDeletedBySender = 0 AND PostId=? AND id>=?  ORDER BY id DESC LIMIT 15  ";
            String selectStrignWithOutOffset="Select * from Comments WHERE isDeletedBySender = 0 AND PostId=?   ORDER BY id DESC LIMIT 15 ";
            String selectStrignNegetive ="Select * from  Comments where isDeletedBySender = 0 AND id<? AND PostId=? ORDER BY id DESC LIMIT 15 ";

            String selectStrignRange ="Select * from  Comments where isDeletedBySender = 0 AND id<=? AND PostId=?  AND id>=? ORDER BY id DESC LIMIT 15 ";

            if(getPost.start!=-1 && getPost.end!=-1 ){
                System.out.println("A request came through here ");
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStrignRange)) {
                    ps.setInt(1, getPost.end);
                    ps.setInt(2, getPost.PostID);
                    ps.setInt(3, getPost.start);

                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {

                        synchronized (objOut) {
                            objOut.writeObject(new commentDataReturn(
                                    rs.getString("postedBy"),
                                    rs.getInt("PostId"),
                                    rs.getInt("id"),
                                    rs.getString("commentText"),
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                    synchronized (objOut) {
                        objOut.writeObject(new commentDataSendingEnder(
                                rs.getString("postedBy"),
                                rs.getInt("PostId")));
                        objOut.flush();

                    }
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(getPost.end==-1 && getPost.start==-1 ){

                try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                    PreparedStatement ps = conn.prepareStatement(selectStrignWithOutOffset)){

                    ps.setInt(1, getPost.PostID);

                    ResultSet rs=ps.executeQuery();

                    while (rs.next()) {
                        System.out.println("1");
                        System.out.println("this comment is sent --->" +rs.getString("postedBy")+" "+rs.getString("commentText"));

                        synchronized (objOut) {

                            objOut.writeObject(new commentDataReturn(
                                    rs.getString("postedBy"),
                                    rs.getInt("PostId"),
                                    rs.getInt("id"),
                                    rs.getString("commentText"),
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                    synchronized (objOut) {
                        objOut.writeObject(new commentDataSendingEnder(
                                rs.getString("postedBy"),
                                rs.getInt("PostId")));
                        objOut.flush();

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if(getPost.end==-1){
                System.out.println("called 2");
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStrignNegetive)) {
                    ps.setInt(1, getPost.start);
                    ps.setInt(2 ,getPost.PostID);


                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            objOut.writeObject(new commentDataReturn(
                                    rs.getString("postedBy"),
                                    rs.getInt("PostId"),
                                    rs.getInt("id"),
                                    rs.getString("commentText"),
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }
                    synchronized (objOut) {
                        objOut.writeObject(new commentDataSendingEnder(
                                rs.getString("postedBy"),
                                rs.getInt("PostId")));
                        objOut.flush();

                    }
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStringWithOffset)) {
                    ps.setInt(1, getPost.PostID);
                    ps.setInt(2 ,getPost.end);

                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            objOut.writeObject(new commentDataReturn(
                                    rs.getString("postedBy"),
                                    rs.getInt("PostId"),
                                    rs.getInt("id"),
                                    rs.getString("commentText"),
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }
                    synchronized (objOut) {
                        objOut.writeObject(new commentDataSendingEnder(
                                rs.getString("postedBy"),
                                rs.getInt("PostId")));
                        objOut.flush();

                    }
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            synchronized (objOut){
                try {
                    objOut.writeObject(new postDataSendingFinished());objOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
