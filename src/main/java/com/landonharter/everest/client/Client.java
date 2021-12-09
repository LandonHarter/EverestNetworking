package com.landonharter.everest.client;

import com.landonharter.everest.packet.ClientPackets;
import com.landonharter.everest.packet.Packet;
import com.landonharter.everest.packet.ServerPackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Random;
import java.util.function.Consumer;

public class Client {

    private String connectedIP;
    private int connectedPort;

    private int clientID;
    private String clientNickname;

    private ClientHandle handle;
    private ClientSend send;

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    private boolean connected = true;
    private boolean hasClaimedId = false;

    private Packet receivedData;
    private byte[] receiveBuffer;
    private Hashtable<Integer, Consumer<Packet>> packetHandlers = new Hashtable<>();

    private Thread updateThread;

    public Client(String ip, int port) {
        connectedIP = ip;
        connectedPort = port;

        initializePacketHandlers();
    }

    public void connect() {
        try {
            socket = new Socket(connectedIP, connectedPort);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            handle = new ClientHandle(this);
            send = new ClientSend(this);

            receivedData = new Packet();
            receiveBuffer = new byte[4096];

            changeNickname("Client" + new Random().nextInt(100000));

            connected = true;
            updateThread = new Thread(() -> {
                while (connected) {
                    update();
                }
            });
            updateThread.start();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void update() {
        try {
            input.read(receiveBuffer, 0, receiveBuffer.length);
            int byteLength = receiveBuffer.length;

            if (byteLength <= 0) {
                disconnect();
                return;
            }
            receivedData.reset(handlePacket(receiveBuffer));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void disconnect() {
        try {
            connected = false;
            hasClaimedId = false;

            if (socket.isConnected()) {
                send.disconnect();
            }

            socket.close();
            connectedIP = null;
            connectedPort = -1;
            send = null;
            handle = null;
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void sendData(Packet packet) {
        try {
            packet.writeLength();
            output.write(packet.toArray(), 0, packet.length());
            output.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private boolean handlePacket(byte[] data) {
        int packetLength = 0;

        receivedData.setBytes(data);
        if (receivedData.unreadLength() >= 4) {
            packetLength = receivedData.readInt();
            if (packetLength <= 0) {
                return true;
            }
        }

        while (packetLength > 0 && packetLength <= receivedData.unreadLength()) {
            byte[] packetBytes = receivedData.readBytes(packetLength);

            Packet newPacket = new Packet(packetBytes);
            int packetID = newPacket.readInt();
            packetHandlers.getOrDefault(packetID, (Packet packet) -> {
                System.out.println("Received a packet with an unidentifiable packet ID");
                System.out.println("Packet ID: " + packetID);
            }).accept(newPacket);

            packetLength = 0;
            if (receivedData.unreadLength() >= 4) {
                packetLength = receivedData.readInt();
                if (packetLength <= 0) return true;
            }
        }

        if (packetLength <= 1) return true;

        return false;
    }

    private void initializePacketHandlers() {
        packetHandlers.put(ServerPackets.ID.ordinal(), (Packet packet) -> { handle.claimId(packet); });
        packetHandlers.put(ServerPackets.ForceDisconnect.ordinal(), (Packet packet) -> { handle.disconnect(); });
    }

    public String getConnectedIP() {
        return connectedIP;
    }

    public int getPort() {
        return connectedPort;
    }

    public Socket getSocket() {
        return socket;
    }

    public ClientHandle getHandle() {
        return handle;
    }

    public ClientSend getSend() {
        return send;
    }

    public int getId() {
        return clientID;
    }

    public String getNickname() {
        return clientNickname;
    }

    public void changeNickname(String newNickname) {
        Packet packet = new Packet(ClientPackets.ChangeNickname);
        packet.write(newNickname);
        sendData(packet);

        clientNickname = newNickname;
    }

    public void claimId(int id) {
        if (hasClaimedId) {
            System.out.println("Failed to set ID");
            return;
        }

        clientID = id;
        hasClaimedId = true;
    }

}
