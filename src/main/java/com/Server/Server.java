package com.Server;

import com.common.Connection;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    int nRooms = 3;
    SSLServerSocket serverSocket;
    private List<List<Connection>> rooms = new ArrayList<>();
    private ExecutorService pool = Executors.newFixedThreadPool(nRooms);
    // TLS Details
    public static final String[] protocols = new String[]{"TLSv1.3"};
    public static final String[] cipherSuites = new String[]{"TLS_AES_128_GCM_SHA256"};

    public void start(int port) throws IOException, InterruptedException {

        // Require a reference to the rooms so we can later add back in.
        for (int i = 0; i < nRooms; i++) {
            final List<Connection> newRoom = Collections.synchronizedList(new ArrayList<>());
            rooms.add(newRoom);
        }

        SSLServerSocketFactory socketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        serverSocket = (SSLServerSocket) socketFactory.createServerSocket(port);
        serverSocket.setEnabledProtocols(protocols);
        serverSocket.setEnabledCipherSuites(cipherSuites);

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
