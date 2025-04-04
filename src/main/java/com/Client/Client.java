package com.Client;

import com.Client.UI.WindowManager;
import com.common.Connection;
import java.io.*;

public class Client {
    public Connection con;
    public WindowManager windowManager;
    public String username;

    Client() throws IOException {
        windowManager = new WindowManager(this);
    }

    public void startConnection(String ip, int port) throws IOException {
        con = new Connection(ip, port);
        Thread messageThread = new Thread(() -> {
            try {
                while(true) {
                    if (con.getMessage() == 1) {
                        System.out.println("Received Message");
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

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.username = client.windowManager.getUsername();
        String roomNum = client.windowManager.getRoom();
        System.out.println(roomNum);
        while(!"012".contains(roomNum)) {
            roomNum = client.windowManager.getRoom();
        }
        client.windowManager.window.setTitle("Room " + roomNum);
        client.startConnection("127.0.0.1", 3744);
        client.con.sendMessage(roomNum);
    }


}
