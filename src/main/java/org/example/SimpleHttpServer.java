package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.DatabaseCreation.InmemoryDataBaseCreation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleHttpServer {


    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/", new MyHandler());
        server.createContext("/show" , new ActiveUserHandler());
        server.createContext("/busy" ,new BusyUserHandler());
        server.createContext("/makeBusy", new makeBusyUserHandler() );
        server.createContext("/signOut" , new signOutHandler());
        server.createContext("/makeUNBUSY" , new removeActiveUserHandler());
        server.createContext("/removeBusy",new removeBusyUser());
        server.createContext("/signIn",new signINhandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);

    }


    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Thread.startVirtualThread(()->{
                String response = "Hello from HttpServer!";
                try {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        }
    }

    static class ActiveUserHandler implements  HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Thread.startVirtualThread(()->{

                String response = InmemoryDataBaseCreation.showAllActiveUsers();
                try {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        }
    }

    static class BusyUserHandler implements  HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Thread.startVirtualThread(()->{

                String response = InmemoryDataBaseCreation.showAllBusyUsers();
                try {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        }
    }
    static class signOutHandler implements HttpHandler{
           @Override
            public void handle(HttpExchange exchange)throws IOException{
               Thread.startVirtualThread(()->{
                   String str=exchange.getRequestURI().getQuery();
                   System.out.println("sign out request came here ----->"+str);
                   InmemoryDataBaseCreation.removerActiveUser(str);
                   InmemoryDataBaseCreation.removeBusyUser(str);
                   String response="<h1>removing this user from the active and busy pool</h1>";
                   try {
                       if(server2.payloadBlockingQueue.get(str)!=null){
                           server2.payloadBlockingQueue.remove(str);
                       }
                       exchange.sendResponseHeaders(200, response.getBytes().length);
                       OutputStream os = exchange.getResponseBody();
                       os.write(response.getBytes());
                       os.close();

                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }

               });
            }
    }
    static class makeBusyUserHandler implements  HttpHandler{
        @Override
        public void handle(HttpExchange exchange)throws IOException{
            Thread.startVirtualThread(()->{
                System.out.println("this busy url was hit");
                System.out.println("this data was sent to the ");
                    String val=exchange.getRequestURI().getQuery();
                    InmemoryDataBaseCreation.insertBusyUser(val);
                    String response="<h1>Ok this was hit "+val+"</h1>";

                    try {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }
    }
    static class removeActiveUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange)throws IOException{
            Thread.startVirtualThread(()->{
                String str=exchange.getRequestURI().getQuery();
                try{
                    InmemoryDataBaseCreation.removeBusyUser(str);

                }catch (Exception e){
                    e.printStackTrace();
                }
                String response="<h1>removing this user from the busy pool</h1>";
                try {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }
    }

    static class signINhandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange)throws IOException{
            Thread.startVirtualThread(()->{
                String str=exchange.getRequestURI().getQuery();
                if(InmemoryDataBaseCreation.isAlreadyActive(str)){
                    String response="<h1>ALREADY ACTIVE</h1>";

                    try {
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                InmemoryDataBaseCreation.insertActiveUser(str);

                String response="<h1>inserting this user into the active database</h1>";
                try {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }
    }

    static class removeBusyUser implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange)throws IOException{
            Thread.startVirtualThread(()->{
                String str=exchange.getRequestURI().getQuery();
                InmemoryDataBaseCreation.removeBusyUser(str);
                String response="<h1>removing busy User</h1>";
                System.out.println("removing users " +
                        "/1" +
                        "/2" +
                        "/3");
                try {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }
    }

}
