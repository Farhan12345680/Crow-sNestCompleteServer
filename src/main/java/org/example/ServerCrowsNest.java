package org.example;

import com.crowsnestfrontend.SerializedClasses.*;
import org.example.DatabaseCreation.DatabaseCreation;
import org.example.DatabaseCreation.InmemoryDataBaseCreation;
import org.example.UserAuthentication.AuthChecker;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerCrowsNest {
    public static final int PORT = 12345;

    public static final int SIGN_IN  = 0;
    public static final int SIGN_UP  = 1;
    public static final int UPLOAD_IMAGE = 4;
    public static final int LOGOUT   = 10;
    public static final int CURRENT_USER=3;
    public static final int GITHUB_LOGIN=100;

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Accepted connection from " + client.getRemoteSocketAddress());

                Thread.startVirtualThread(() -> handleClient(client));
            }
        }catch (Exception e){

        }
    }

    private static void handleClient(Socket client) {
        try ( ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
              ObjectInputStream  in  = new ObjectInputStream(client.getInputStream()) ) {

            boolean alive = true;
            while (alive) {
                try {
                    alive = dispatchRequest(client, in, out);
                }
                catch (EOFException eof) {

                    System.out.println("Client " + client.getRemoteSocketAddress() + " disconnected.");
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Connection error with " + client.getRemoteSocketAddress() + ":");
            e.printStackTrace();
        }
    }

    private static boolean dispatchRequest(Socket client,ObjectInputStream in,ObjectOutputStream out)
            throws IOException, ClassNotFoundException
    {
        ClientRequest req = (ClientRequest) in.readObject();
        int code = req.getRequestNumber();
        switch (code) {
            case SIGN_UP: {
                SignUpProfile prof = (SignUpProfile) in.readObject();

                if(InmemoryDataBaseCreation.isUserAlreadyActive(prof.getName())){
                    out.writeObject(new returnQuery(-1, "User is already active on the platform", ""));
                    out.flush();
                    return true;
                }
                if (AuthChecker.whetherIsUser(prof.getName())) {
                    out.writeObject(new returnQuery(-1, "Username already exists", ""));
                    out.flush();
                    return true;
                }

                AuthChecker.insertUser(prof.getName(), prof.getPassword());
                InmemoryDataBaseCreation.insertActiveUser(prof.getName());


                out.writeObject(new returnQuery(1, "User created successfully", ""));
                out.flush();
                ImageChanger img = (ImageChanger) in.readObject();
                DatabaseCreation.addImagetoTheTable(img.getImageURL(), img.getName());

                return true;
            }

            case SIGN_IN: {
                SignInProfile user = (SignInProfile) in.readObject();
                System.out.println("SIGN_IN request for " + user.getName());

                if(InmemoryDataBaseCreation.isUserAlreadyActive(user.getName())){
                    out.writeObject(new returnQuery(-1, "User is already active on the platform", ""));
                    return true;
                }
                if (!AuthChecker.whetherIsUser(user.getName())) {
                    out.writeObject(new returnQuery(-1, "User does not exist", ""));
                    return true;
                }
                if (!AuthChecker.doesPasswordMatch(user.getName(), user.getPassword())) {
                    out.writeObject(new returnQuery(-1, "Password doesn't match", ""));
                    return true;
                }

                InmemoryDataBaseCreation.insertActiveUser(user.getName());


                String imageURL = DatabaseCreation.getUserImageData(user.getName());
                out.writeObject(new returnQuery(1, "Login successful", imageURL));
                return true;
            }

            case CURRENT_USER:{
//                SignInProfile signIn=(SignInProfile) in.readObject();
//                var list = InmemoryDataBaseCreation.isUserAlreadyActive(signIn.getName());
//                System.out.println("this was called");
//                if (list != null) {
//                    System.out.println("do something");
//                }
//                return true; ??
            }

            case UPLOAD_IMAGE: {
                System.out.println("UPLOAD_IMAGE request from " + client.getRemoteSocketAddress());
                ImageChanger img = (ImageChanger) in.readObject();
                DatabaseCreation.addImagetoTheTable(img.getImageURL(), img.getName());
                out.writeObject(new returnQuery(1, "Image uploaded", ""));
                out.flush();
                return true;
            }

            case LOGOUT: {
                SignInProfile who = (SignInProfile) in.readObject();
                if(who.getName()==null){
                    return false;
                }


                InmemoryDataBaseCreation.removerActiveUser(who.getName());
                InmemoryDataBaseCreation.removeBusyUser(who.getName());


                return true;
            }

            case GITHUB_LOGIN:{
                githubLogin prof = (githubLogin) in.readObject();

                if(InmemoryDataBaseCreation.isUserAlreadyActive(prof.name)){
                    out.writeObject(new returnQuery(-1, "User is already active on the platform", ""));
                    out.flush();
                    return true;
                }


                AuthChecker.insertUser(prof.name, prof.password);
                InmemoryDataBaseCreation.insertActiveUser(prof.name);

                String url2=DatabaseCreation.addImageGithub(prof.name,prof.imageURL);
                System.out.println("this is the url-->"+url2);
                out.writeObject(new returnQuery(1, "User created successfully", url2));
                out.flush();
                try{
                    in.readObject();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

                DatabaseCreation.addImagetoTheTable(url2, prof.name);


            }
            default:
                System.err.println("Unknown request code: " + code);
                return true;
        }
    }
}
