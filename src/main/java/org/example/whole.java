package org.example;

import org.example.DatabaseCreation.DatabaseCreation;
import org.example.DatabaseCreation.InmemoryDataBaseCreation;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class whole {

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        DatabaseCreation.main(null);
        InmemoryDataBaseCreation.main(null);
        Thread.startVirtualThread(()->{
            try {
                SimpleHttpServer.main(null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        File file=new File("PostFolder/");
        try{
            file.mkdir();
        }catch (Exception e ){
            System.out.println("an exception occurred in PostFolder");
        }

        Thread firstThread=Thread.startVirtualThread(()->{
            try {
                ServerCrowsNest.main(null);
            } catch (Exception e) {
                System.out.println("an error occurred Thread1");
            }
        });
        Thread secondThread=Thread.startVirtualThread(()->{
            try {
                server2.main(null);

            } catch (Exception e) {
                System.out.println("an error occurred Thread2");
            }


        });
        firstThread.join();

    }
}
