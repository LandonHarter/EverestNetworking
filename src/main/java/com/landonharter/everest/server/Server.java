package com.landonharter.everest.server;

import com.landonharter.everest.packet.Packet;
import com.landonharter.everest.packet.ServerPackets;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public final class Server {

    private static ServerSocket socket;

    public static List<ServerClient> clients = new ArrayList<>();
    private static int assignableID = 0;

    private static boolean open = false;
    private static Thread acceptThread;

    protected Server() { }

    public static void create(int port) {
        try {
            socket = new ServerSocket(port);

            open = true;

            acceptThread = new Thread(() -> {
                while (open) {
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
            sendToAll(new Packet(ServerPackets.ForceDisconnect));

            open = false;
            socket.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void sendTo(int client, Packet packet) {
        clients.get(client).sendData(packet);
    }

    public static void sendToAll(Packet packet) {
        for (ServerClient client : clients) {
            client.sendData(packet);
        }
    }

    private static void acceptClients() {
        Socket newClient = null;
        try {
            newClient = socket.accept();
        } catch (Exception e) {
            if (!open) {
                return;
            } else {
                System.err.println(e);
            }
        }

        ServerClient client = new ServerClient(newClient);
        client.claimId(assignableID);

        clients.add(client);
        assignableID++;

        client.getSend().sendId();
    }

    public static boolean isOpen() {
        return open;
    }

}
