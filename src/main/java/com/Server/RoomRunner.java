package com.Server;

import com.common.Connection;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Thread.sleep;

public class RoomRunner implements Runnable {
    final List<Connection> clients;

    RoomRunner(List<Connection> _clients) {
        clients = _clients;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if(clients.isEmpty()) {
                    // todo replace with fancy semaphore stuff
                    sleep(5000);
                } else {
                    sleep(250);
                }
            } catch (InterruptedException e){
                    throw new RuntimeException(e);
            }
            synchronized (clients) {
                Set<Connection> disconnected = new HashSet<>();
                for (Connection client : clients) {
                    try {
                        if (client.getMessage() == 1) {
                            String message = client.pollMessage();
                            System.out.println(message);
                            for (Connection otherClient : clients) {
                                if (otherClient != client) {
                                    try {
                                        otherClient.sendMessage(message);
                                    } catch (SocketException e) {
                                        disconnected.add(otherClient);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Corruption in reading client, booting them out!");
                        disconnected.add(client);
                    }
                }

                if (!disconnected.isEmpty()) {
                    for (Connection otherClient : disconnected) {
                        try {
                            otherClient.close();
                            System.out.println("Gracefully closed client connection.");
                        } catch (IOException e) {
                            System.out.println("Failed to gracefully close client.");
                        } finally {
                            clients.remove(otherClient);
                        }
                    }
                }
            }
        }
    }
}
