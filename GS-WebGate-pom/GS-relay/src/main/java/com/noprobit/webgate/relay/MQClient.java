package com.noprobit.webgate.relay;

import java.io.*;
import java.net.*;

public class MQClient {
    private String host;
    private int port;
    private Socket socket;
    private boolean connected;

    public MQClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.connected = false;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            connected = true;
        } catch (ConnectException e) {
            throw new ConnectionException("Failed to connect to " + host + ":" + port, e);
        } catch (IOException e) {
            throw new ConnectionException("Connection error", e);
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
        } catch (IOException e) {
            throw new ConnectionException("Failed to disconnect", e);
        }
    }

    public void reconnect() {
        disconnect();
        connect();
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

}
