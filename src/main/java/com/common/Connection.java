package com.common;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static com.Server.Server.cipherSuites;
import static com.Server.Server.protocols;

public class Connection {
    private SSLSocket socket;
    private InputStream iByteStream;
    private Reader iCharStream;
    private OutputStream oStream; // This stream blocks, because not really an issue.

    public byte[] lengthBuffer = new byte[4];
    public char[] messageBuffer = new char[10240];
    public int lengthBytesRead = 0;
    public int bytesToRead = 0;
    public int bytesRead = 0;

    // Server-side initializer
    public Connection(SSLServerSocket serverSocket) throws IOException {
        socket = (SSLSocket) serverSocket.accept();
        oStream = socket.getOutputStream();
        iByteStream = socket.getInputStream();
        iCharStream = new InputStreamReader(iByteStream, "UTF-8");
    }

    // Client-side initializer
    public Connection(String ip, int port) throws IOException {
        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) socketFactory.createSocket(ip, port);
        socket.setEnabledProtocols(protocols);
        socket.setEnabledCipherSuites(cipherSuites);
        socket.startHandshake();
        oStream = socket.getOutputStream();
        iByteStream = socket.getInputStream();
        iCharStream = new InputStreamReader(iByteStream, "UTF-8");
    }

    public int getMessage() throws IOException {
        // Check if a message is available.
        if(iByteStream.available() == 0) return -1;

        System.out.println("Getting message");

        // get message length
        if(lengthBytesRead < 4) {
            int readIn = iByteStream.read(lengthBuffer, lengthBytesRead, 4 - lengthBytesRead);
            if(readIn == -1) return -1; // no info read, back out.
            lengthBytesRead += readIn;
            if(lengthBytesRead == 4) {
                bytesToRead = parseLengthBuffer();
            }
        }

        System.out.println("message is " + String.valueOf(bytesToRead) + " bytes");

        if(bytesToRead > 0) {
            int readIn = iCharStream.read(messageBuffer, bytesRead, bytesToRead-bytesRead);
            if(readIn == -1) return -1;

            bytesToRead -= readIn;
            bytesRead += readIn;
            if(bytesToRead == 0) return 1;

        }

        return 0;
    }


    /**
     *
     * @param msg The message that should be sent.
     * @throws IOException
     * This method will take a string, send its length as a single byte, then convert it to a byte stream.
     * This allows it to be sent using the custom TCP protocol.
     */
    public void sendMessage(String msg) throws IOException {
        int len = msg.length();
        byte[] lengthBytes = new byte[4];
        lengthBytes[0] = (byte) ((len >> 24) & 0xFF);
        lengthBytes[1] = (byte) ((len >> 16) & 0xFF);
        lengthBytes[2] = (byte) ((len >> 8) & 0xFF);
        lengthBytes[3] = (byte) (len & 0xFF);
        oStream.write(lengthBytes);
        oStream.write(msg.getBytes());
        System.out.println("msg sent\n");
    }

    public String pollMessage() {
        String message = String.valueOf(messageBuffer).substring(0, bytesRead);
        bytesRead = 0;
        lengthBytesRead = 0;
        return message;
    }

    int parseLengthBuffer() {
        return ((lengthBuffer[0] & 0xFF) << 24) | ((lengthBuffer[1] & 0xFF) << 16) | ((lengthBuffer[2] & 0xFF) << 8 ) |
                (lengthBuffer[3] & 0xFF);
    }

    public void close() throws IOException {
        iCharStream.close();
        iByteStream.close();
        oStream.close();
        socket.close();
    }
}
