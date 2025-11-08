package org.example.PostProvider;

import com.PostFile.*;
import org.example.DatabaseCreation.DatabaseCreation;

import java.io.*;
import java.sql.*;

public class postProviderHandler {

    public static int getTheHighestIndex(){
        int data=0;

        String selectString ="SELECT MAX(id) from Posts ";

        try(Connection conn= DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(selectString)){

            ResultSet rs=ps.executeQuery();

            while(rs.next()){
                data=rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("max id finding error");
        }

        return data;

    };

    public static void HandlePost(PostData post){

        Thread.startVirtualThread(()->{

           int index= getTheHighestIndex();

            File file=new File("PostFolder/"+post.clientName+(index+1)+".md");
            try{

                file.createNewFile();
                FileOutputStream stream = new FileOutputStream(file);
                stream.write(post.fileContent);
                stream.close();
            }catch (Exception e){
                System.out.println("markdown fileCreation problem ");
            }

            String selectString ="INSERT INTO Posts(id ,postedBy ,PostTitle,PostDescription ,PostPath) values (?,?,?,?,?)";

            try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(selectString)) {
                ps.setInt(1 ,index+1  );
                ps.setString(2 , post.clientName);
                ps.setString(3 , post.PostTitle);
                ps.setString(4 , post.PostDescription);
                ps.setString(5 ,file.getAbsolutePath());
                ps.executeUpdate();

            }catch (SQLException e){
                System.out.println("sql insertion problem ");
            }
        });
    }

    public static  void getPost(getPostRequest getPost , ObjectOutputStream objOut){
        System.out.println("starting range and the ending range is --->" +getPost.StartingRange+" "+getPost.EndingRange);

        Thread.startVirtualThread(()->{
            String selectStringWithOffset = "Select * from Posts  WHERE isDeletedBySender = 0 AND id>=? ORDER BY id DESC LIMIT 15  ";
            String selectStrignWithOutOffset="Select * from Posts WHERE isDeletedBySender = 0 ORDER BY id DESC LIMIT 15 ";
            String selectStrignNegetive ="Select * from Posts WHERE isDeletedBySender = 0 AND id<? ORDER BY id DESC LIMIT 15 ";
            String selectStrignRange ="Select * from Posts WHERE isDeletedBySender = 0 AND id<=? AND id>=? ORDER BY id DESC LIMIT 15 ";
            if(getPost.EndingRange!=-1 && getPost.StartingRange!=-1 ){
                System.out.println("called 0");
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStrignRange)) {
                    ps.setInt(1, getPost.EndingRange);
                    ps.setInt(2, getPost.StartingRange);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            File file = new File(rs.getString("PostPath"));
                            byte[] bytes = new byte[0];
                            try (FileInputStream fs = new FileInputStream(file)) {
                                bytes = fs.readAllBytes();
                            } catch (Exception e) {

                            }
                            objOut.writeObject(new getPostData(rs.getInt("id"),
                                    rs.getString("postedBy"),
                                    rs.getString("PostTitle"),
                                    rs.getString("PostDescription"),
                                    bytes,
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(getPost.EndingRange==-1 && getPost.StartingRange==-1 ){
                System.out.println("called 1");
                int index= getTheHighestIndex();
                index/=15;

                synchronized (objOut){
                    try {
                        objOut.writeObject(new maximumID("" , (index+1)*15 ));
                        objOut.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                System.out.println("this is the data call 9002w293w2390w98238923897w289");
                try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                    PreparedStatement ps = conn.prepareStatement(selectStrignWithOutOffset)){

                    ResultSet rs=ps.executeQuery();

                    while(rs.next()){
                        synchronized (objOut) {

                            File file = new File(rs.getString("PostPath"));
                            byte[] bytes=new byte[0];
                            try (FileInputStream fs = new FileInputStream(file) ){
                                bytes=fs.readAllBytes();
                            }catch (Exception e){

                            }
                            objOut.writeObject(new getPostData(rs.getInt("id"),
                                    rs.getString("postedBy"),
                                    rs.getString("PostTitle"),
                                    rs.getString("PostDescription"),
                                    bytes,
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if(getPost.EndingRange==-1){
                System.out.println("called 2");
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStrignNegetive)) {
                    ps.setInt(1, getPost.StartingRange);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            File file = new File(rs.getString("PostPath"));
                            byte[] bytes = new byte[0];
                            try (FileInputStream fs = new FileInputStream(file)) {
                                bytes = fs.readAllBytes();
                            } catch (Exception e) {

                            }
                            objOut.writeObject(new getPostData(rs.getInt("id"),
                                    rs.getString("postedBy"),
                                    rs.getString("PostTitle"),
                                    rs.getString("PostDescription"),
                                    bytes,
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
                    ps.setInt(1, getPost.EndingRange);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            File file = new File(rs.getString("PostPath"));
                            byte[] bytes = new byte[0];
                            try (FileInputStream fs = new FileInputStream(file)) {
                                bytes = fs.readAllBytes();
                            } catch (Exception e) {

                            }
                            objOut.writeObject(new getPostData(rs.getInt("id"),
                                    rs.getString("postedBy"),
                                    rs.getString("PostTitle"),
                                    rs.getString("PostDescription"),
                                    bytes,
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

    public static  void getMyPost(getMyPost getPost , ObjectOutputStream objOut){
        System.out.println("starting range and the ending range is --->" +getPost.StartingRange+" "+getPost.EndingRange);

        Thread.startVirtualThread(()->{
            String selectStringWithOffset = "Select * from Posts  WHERE isDeletedBySender = 0 AND id>=? AND postedBy=? ORDER BY id DESC LIMIT 15  ";
            String selectStrignWithOutOffset="Select * from Posts WHERE isDeletedBySender = 0  AND postedBy=? ORDER BY id DESC LIMIT 15 ";
            String selectStrignNegetive ="Select * from Posts WHERE isDeletedBySender = 0 AND id<?  AND postedBy=? ORDER BY id DESC LIMIT 15 ";
            String selectStrignRange ="Select * from Posts WHERE isDeletedBySender = 0 AND id<=? AND id>=?  AND postedBy=? ORDER BY id DESC LIMIT 15 ";
            if(getPost.EndingRange!=-1 && getPost.StartingRange!=-1 ){
                System.out.println("called 0");
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStrignRange)) {
                    ps.setInt(1, getPost.EndingRange);
                    ps.setInt(2, getPost.StartingRange);
                    ps.setString(3 ,getPost.clientName);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            File file = new File(rs.getString("PostPath"));
                            byte[] bytes = new byte[0];
                            try (FileInputStream fs = new FileInputStream(file)) {
                                bytes = fs.readAllBytes();
                            } catch (Exception e) {

                            }
                            objOut.writeObject(new getPostData(rs.getInt("id"),
                                    rs.getString("postedBy"),
                                    rs.getString("PostTitle"),
                                    rs.getString("PostDescription"),
                                    bytes,
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(getPost.EndingRange==-1 && getPost.StartingRange==-1 ){
                System.out.println("called 1");
                int index= getTheHighestIndex();
                index/=15;

                synchronized (objOut){
                    try {
                        objOut.writeObject(new maximumID("" , (index+1)*15 ));
                        objOut.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                System.out.println("this is the data call 9002w293w2390w98238923897w289");
                try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                    PreparedStatement ps = conn.prepareStatement(selectStrignWithOutOffset)){
                    ps.setString(1 ,getPost.clientName);
                    ResultSet rs=ps.executeQuery();

                    while(rs.next()){
                        synchronized (objOut) {

                            File file = new File(rs.getString("PostPath"));
                            byte[] bytes=new byte[0];
                            try (FileInputStream fs = new FileInputStream(file) ){
                                bytes=fs.readAllBytes();
                            }catch (Exception e){

                            }
                            objOut.writeObject(new getPostData(rs.getInt("id"),
                                    rs.getString("postedBy"),
                                    rs.getString("PostTitle"),
                                    rs.getString("PostDescription"),
                                    bytes,
                                    rs.getString("time")));
                            objOut.flush();

                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if(getPost.EndingRange==-1){
                System.out.println("called 2");
                try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                     PreparedStatement ps = conn.prepareStatement(selectStrignNegetive)) {
                    ps.setInt(1, getPost.StartingRange);
                    ps.setString(2 ,getPost.clientName);

                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            File file = new File(rs.getString("PostPath"));
                            byte[] bytes = new byte[0];
                            try (FileInputStream fs = new FileInputStream(file)) {
                                bytes = fs.readAllBytes();
                            } catch (Exception e) {

                            }
                            objOut.writeObject(new getPostData(rs.getInt("id"),
                                    rs.getString("postedBy"),
                                    rs.getString("PostTitle"),
                                    rs.getString("PostDescription"),
                                    bytes,
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
                    ps.setInt(1, getPost.EndingRange);
                    ps.setString(2 ,getPost.clientName);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        synchronized (objOut) {

                            File file = new File(rs.getString("PostPath"));
                            byte[] bytes = new byte[0];
                            try (FileInputStream fs = new FileInputStream(file)) {
                                bytes = fs.readAllBytes();
                            } catch (Exception e) {

                            }
                            objOut.writeObject(new getPostData(rs.getInt("id"),
                                    rs.getString("postedBy"),
                                    rs.getString("PostTitle"),
                                    rs.getString("PostDescription"),
                                    bytes,
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

    public static void deletePost(int id){
            String selectString = """
                    Update Posts SET isDeletedBySender=1 WHERE  id=?;
                    """;

            try(Connection conn =DriverManager.getConnection(DatabaseCreation.URL);
                PreparedStatement ps =conn.prepareStatement(selectString)){

                ps.setInt(1 , id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

        public static  void editPost(int id , EditFile file){
            System.out.println("Edit post request came from server ");
            String selectString = """
                    Update Posts SET PostTitle=? , PostDescription=?  WHERE  id=?;
                    """;

            String pathString = """
                   Select PostPath from Posts WHERE ID=?;
                    """;
            String path ="";

            try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                PreparedStatement ps = conn.prepareStatement(pathString)){

                ps.setInt(1 , id);

                ResultSet query =ps.executeQuery();

                while (query.next()){
                    path= query.getString(1);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            File file2= new File(path);

            try{
                file2.delete();

                file2.createNewFile();

                FileOutputStream stream = new FileOutputStream(file2);
                stream.write(file.fileContent);
                stream.close();

            }catch (Exception e){
                e.printStackTrace();
            }

            try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
                PreparedStatement ps = conn.prepareStatement(selectString)){

                ps.setString(1 , file.PostTitle);
                ps.setString(2  ,file.PostDescription);
                ps.setInt(3 , id);

                ps.executeUpdate();



            }catch (Exception e){
                e.printStackTrace();
            }

        }




}
