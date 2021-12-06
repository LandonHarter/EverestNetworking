package com.landonharter.everest.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public final class Server {

    private static ServerSocket socket;

    public static List<ServerClient> clients = new ArrayList<>();
    private static int assignableID = 0;

    public static boolean Open = false;
    private static Thread acceptThread;

    protected Server() {}

    public static void create(int port) {
        try {
            socket = new ServerSocket(port);

            Open = true;

            acceptThread = new Thread(() -> {
                while (Open) {
                    acceptClients();
                }
            });
            acceptThread.start();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void close() {
        try {
            Open = false;
            socket.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private static void acceptClients() {
        Socket newClient = null;
        try {
            newClient = socket.accept();
        } catch (Exception e) {
            if (!Open) {
                return;
            } else {
                System.err.println(e);
            }
        }
        ServerClient client = new ServerClient(newClient);
        client.id = assignableID;

        clients.add(client);
        assignableID++;

        client.send.sendId();
    }

}
