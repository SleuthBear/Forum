package com.Server;

import com.common.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class Server {
    int nRooms = 7;
    ServerSocket serverSocket;
    private List<List<Connection>> rooms = new ArrayList<>();
    private ExecutorService pool = Executors.newFixedThreadPool(nRooms);

    public void start(int port) throws IOException, InterruptedException {

        // Require a reference to the rooms so we can later add back in.
        for (int i = 0; i < nRooms; i++) {
            final List<Connection> newRoom = Collections.synchronizedList(new ArrayList<>());
            rooms.add(newRoom);
        }

        serverSocket = new ServerSocket(port);
        NewConnectionThread connectionThread = new NewConnectionThread(serverSocket, rooms);
        connectionThread.start();

        for (List<Connection> room : rooms) {
            pool.submit(new RoomRunner(room));
        }

    }
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();
        server.start( 3744);
    }
}
