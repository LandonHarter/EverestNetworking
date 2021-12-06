package com.landonharter.everest.server;

import com.landonharter.everest.packet.ClientPackets;
import com.landonharter.everest.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.function.Consumer;

public class ServerClient {

    public Socket socket;
    public DataInputStream input;
    public DataOutputStream output;

    public ServerHandle handle;
    public ServerSend send;

    private Packet receivedData;
    private byte[] receiveBuffer;

    private boolean Connected = false;

    private Hashtable<Integer, Consumer<Packet>> packetHandlers = new Hashtable<>();

    public int id;

    private Thread updateThread;

    public ServerClient(Socket socket) {
        this.socket = socket;

        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            handle = new ServerHandle(this);
            send = new ServerSend(this);

            receivedData = new Packet();
            receiveBuffer = new byte[4096];

            initializePacketHandlers();

            Connected = true;

            updateThread = new Thread(() -> {
                while (Connected) {
                    update();
                }
            });
            updateThread.start();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void update() {
        try {
            input.read(receiveBuffer, 0, receiveBuffer.length);
            int byteLength = receiveBuffer.length;

            if (byteLength <= 0) {
                forceDisconnect();
                return;
            }
            receivedData.reset(HandlePacket(receiveBuffer));
        } catch (Exception e) {
            if (socket.isClosed()) return;
            else System.err.println(e);
        }
    }

    public void disconnect() {
        try {
            Connected = false;

            socket.close();
            Server.clients.remove(id);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void forceDisconnect() {
        send.disconnect();
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

    public String getIp() {
        return formatIP(socket.getRemoteSocketAddress().toString());
    }

    private String formatIP(String ip) {
        return ip.split("/")[1];
    }

    private void initializePacketHandlers() {
        packetHandlers.put(ClientPackets.Disconnect.ordinal(), (Packet packet) -> {
            handle.clientDisconnect(packet);
        });
    }

    private boolean HandlePacket(byte[] data) {
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
                System.out.println("Received a packet with an unidentifiable id");
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

}
