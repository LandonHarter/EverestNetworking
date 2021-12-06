package com.landonharter.everest.client;

import com.landonharter.everest.packet.Packet;
import com.landonharter.everest.packet.ServerPackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.function.Consumer;

public class Client {

    private String connectedIP;
    private int connectedPort;
    public int clientID;

    private ClientHandle handle;
    private ClientSend send;

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    private boolean Connected = true;

    private Packet receivedData;
    private byte[] receiveBuffer;
    private Hashtable<Integer, Consumer<Packet>> packetHandlers = new Hashtable<>();

    private Thread updateThread;

    public Client() {
        initializePacketHandlers();
    }

    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            handle = new ClientHandle(this);
            send = new ClientSend(this);

            connectedIP = ip;
            connectedPort = port;

            receivedData = new Packet();
            receiveBuffer = new byte[4096];

            Connected = true;

            updateThread = new Thread(() -> {
                while (Connected) update();
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
            Connected = false;

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

}
