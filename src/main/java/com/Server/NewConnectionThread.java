package com.Server;

import com.common.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.List;

public class NewConnectionThread extends Thread {
    ServerSocket serverSocket;
    List<Connection> clients;
    NewConnectionThread(ServerSocket _serverSocket, List<Connection> _clients) {
        serverSocket = _serverSocket;
        clients = _clients;
    }

    @Override
    public void run() {
        System.out.println("Looking for connections.");
        while (true) {
            // Try and register a new client
            try {
                Connection client = new Connection(serverSocket);
                synchronized (clients) {
                    clients.add(client);
                }
                System.out.println("New client added");
            } catch (SocketTimeoutException e) {
                // No client connected
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}