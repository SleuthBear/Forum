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

    public void start(int port) throws IOException, InterruptedException {

        serverSocket = new ServerSocket(port);
        NewConnectionThread connectionThread = new NewConnectionThread(serverSocket, clients);
        connectionThread.start();
        while(true) {
            synchronized(clients) {
                Set<Connection> disconnected = new HashSet<>();
                for (Connection client : clients) {
                    if (client.getMessage() == 1) {
                        System.out.println("Received Message");
                        String message = client.pollMessage();
                        for (Connection otherClient : clients) {
                            if (otherClient != client) {
                                try {
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
