package com.noprobit.mq;

import java.io.*;
import java.net.*;

public class MQServer {
    private int port;
    private MQServerStatus status;
    private ServerSocket serverSocket;

    public MQServer(int port) {
        this.port = port;
        this.status = MQServerStatus.STOPPED;
    }

    public void start() {
        if (status == MQServerStatus.RUNNING) {
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            status = MQServerStatus.RUNNING;

            // Start accepting connections in separate thread
            new Thread(() -> acceptConnections()).start();
        } catch (BindException e) {
            throw new PortUnavailableException("Port " + port + " already in use", e);
        } catch (IOException e) {
            status = MQServerStatus.ERROR;
            throw new RuntimeException("Failed to start MQ Server", e);
        }
    }

    public void shutdown() {
        if (status == MQServerStatus.STOPPED) {
            return;
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            status = MQServerStatus.STOPPED;
        } catch (IOException e) {
            status = MQServerStatus.ERROR;
            throw new RuntimeException("Failed to shutdown MQ Server", e);
        }
    }

    private void acceptConnections() {
        while (status == MQServerStatus.RUNNING && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                // Handle client connection in separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (SocketException e) {
                // Server closed, exit loop
                break;
            } catch (IOException e) {
                if (status == MQServerStatus.RUNNING) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        // TODO: Implement client handling
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return status == MQServerStatus.RUNNING;
    }

    public MQServerStatus getStatus() {
        return status;
    }

    public int getPort() {
        return port;
    }

    public String getInfo() {
        return String.format("MQServer{port=%d, status=%s}", port, status);
    }

}
