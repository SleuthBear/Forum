package com.Server;

import com.common.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class NewConnectionThread extends Thread {
    ServerSocket serverSocket;
    final List<Connection> waitingRoom = new ArrayList<>();
    List<List<Connection>> rooms;

    NewConnectionThread(ServerSocket _serverSocket, List<List<Connection>> _rooms) {
        serverSocket = _serverSocket;
        rooms = _rooms;
    }

    private void assignRooms() {
        List<Connection> processed = new ArrayList<>();
        for(Connection client : waitingRoom) {
            try {
                if (client.getMessage() == 1) {
                    String message = client.pollMessage();
                    try {
                        int roomNum = Integer.parseInt(message);
                        synchronized(rooms.get(roomNum)) {
                            rooms.get(roomNum).add(client);
                            System.out.println("Allocated client to room " + message);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Client failed to join a room -FailurePoint1-");
                    } finally {
                        processed.add(client);
                    }
                }
            } catch (IOException e) {
                try {
                    client.close();
                } catch (IOException ex) {
                    System.out.println("Failed to close corrupted client. -FailurePoint2-");
                    throw new RuntimeException(ex);
                }
                processed.add(client);
            }
        }
        for(Connection client : processed) {
            waitingRoom.remove(client);
        }
    }

    @Override
    public void run() {
        Thread waitingThread = null;

        System.out.println("Looking for connections.");
        while (true) {
            // Try and register a new client
            try {
                Connection client = new Connection(serverSocket);
                synchronized (waitingRoom) {
                    waitingRoom.add(client);
                }

                if (waitingThread == null || !waitingThread.isAlive()) {
                    waitingThread = new Thread( () -> {
                        while(!waitingRoom.isEmpty()) {
                            synchronized(waitingRoom) { // synchronize so we don't add a new client mid-loop.
                                assignRooms();
                            }
                        }
                    });
                    waitingThread.start();
                }

            } catch (SocketTimeoutException e) {
                // No client connected
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}