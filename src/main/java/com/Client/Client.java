package com.Client;

import com.Client.UI.WindowManager;
import com.common.Connection;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class Client {
    public Connection con;
    public WindowManager windowManager;
    public String username;
    private static final String TRUSTSTORE_PATH = "client.truststore";
    private static final String TRUSTSTORE_PASSWORD = "arCoaWQXNGEIQfZfKzQkCp8kxFkmekjdt7Wkg9TTqyG5w";

    Client() throws IOException {
        windowManager = new WindowManager(this);
    }

    public void startConnection(String ip, int port) throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        con = new Connection(ip, port, TRUSTSTORE_PASSWORD, TRUSTSTORE_PATH);
        Thread messageThread = new Thread(() -> {
            try {
                while(true) {
                    if (con.getMessage() == 1) {
                        String fullMessage = con.pollMessage();
                        String user = fullMessage.split("%:%")[0];
                        String message = fullMessage.split("%:%")[1];
                        windowManager.addOtherMessage(user, message);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        messageThread.setDaemon(true);
        messageThread.start();
    }

    public void closeConnection() throws IOException {
        con.close();
        System.exit(0);
    }

    public static void main(String[] args) throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        System.setProperty( "apple.awt.application.appearance", "system" );
        Client client = new Client();
        client.username = client.windowManager.getUsername();
        String roomNum = client.windowManager.getRoom();
        System.out.println(roomNum);
        while(!"123".contains(roomNum)) {
            roomNum = client.windowManager.getRoom();
        }
        client.windowManager.window.setTitle("Room " + roomNum);
        client.startConnection("centerbeam.proxy.rlwy.net", 44320);
        client.con.sendMessage(String.valueOf(Integer.parseInt(roomNum)-1));
    }
}
