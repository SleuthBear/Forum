package com.Server;

import com.Client.Client;
import com.common.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.sql.SQLOutput;
import java.util.*;

import static java.lang.Thread.sleep;

public class Server {
    ServerSocket serverSocket;
    private List<Connection> clients = Collections.synchronizedList(new ArrayList<>());
    private List<String> roomNames = new ArrayList<>();
    private List<List<Connection>> rooms = new ArrayList<>();

    public void start(int port) throws IOException, InterruptedException {

        // Create 3 seperate rooms
        roomNames.add("Server 1");
        roomNames.add("Server 2");
        roomNames.add("Server 3");
        rooms.add(Collections.synchronizedList(new ArrayList<>()));
        rooms.add(Collections.synchronizedList(new ArrayList<>()));
        rooms.add(Collections.synchronizedList(new ArrayList<>()));

        serverSocket = new ServerSocket(port);
        NewConnectionThread connectionThread = new NewConnectionThread(serverSocket, clients);
        connectionThread.start();

        // One thread per room maybe??

        while(true) {

            synchronized(clients) {
                // Disconnected Clients
                Set<Connection> disconnected = new HashSet<>();
                for (Connection client : clients) {

                    if (client.getMessage() == 1) {

                        String message = client.pollMessage();
                        System.out.println(message);

                        // Client needs to be assigned to a room first.
                        if(!client.connected) {

                            client.connected = true;
                            client.room = Integer.parseInt(message);
                            rooms.get(client.room).add(client);
                            break;
                        }

                        for (Connection otherClient : rooms.get(client.room)) {

                            if (otherClient != client) {

                                try {
                                    System.out.println("the fuck");
                                    otherClient.sendMessage(message);
                                } catch(SocketException e) {
                                    disconnected.add(otherClient);
                                }
                            }
                        }
                    }
                }

                if(!disconnected.isEmpty()) {
                    for(Connection otherClient: disconnected) {
                        otherClient.close();
                        clients.remove(otherClient);
                        rooms.get(otherClient.room).remove(otherClient);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();
        server.start( 3744);
    }
}
