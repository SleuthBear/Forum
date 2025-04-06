package com.Server;

import com.common.Connection;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
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
    private static final String KEYSTORE_PATH = "server.keystore";
    private static final String KEYSTORE_PASSWORD = System.getenv("KEYSTORE_PASSWORD");

    public void start(int port) throws IOException, InterruptedException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
// Load the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Create key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        // Create and initialize the SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        // Require a reference to the rooms so we can later add back in.
        for (int i = 0; i < nRooms; i++) {
            final List<Connection> newRoom = Collections.synchronizedList(new ArrayList<>());
            rooms.add(newRoom);
        }

        SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket) socketFactory.createServerSocket(port);
        serverSocket.setEnabledProtocols(protocols);
        serverSocket.setEnabledCipherSuites(cipherSuites);

        NewConnectionThread connectionThread = new NewConnectionThread(serverSocket, rooms);
        connectionThread.start();

        for (List<Connection> room : rooms) {
            pool.submit(new RoomRunner(room));
        }

    }
    public static void main(String[] args) throws IOException, InterruptedException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        Server server = new Server();
        server.start( 3744);
    }
}
