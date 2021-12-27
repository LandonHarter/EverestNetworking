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

    private ServerHandle handle;
    private ServerSend send;

    private Packet receivedData;
    private byte[] receiveBuffer;

    private boolean connected = false;
    private boolean hasClaimedId = false;

    private Hashtable<Integer, Consumer<Packet>> packetHandlers = new Hashtable<>();

    private int id;
    private String nickname;

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
            connected = false;
            hasClaimedId = false;

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
        return socket.getRemoteSocketAddress().toString();
    }

    private void initializePacketHandlers() {
        packetHandlers.put(ClientPackets.ChangeNickname.ordinal(), (Packet packet) -> {
            handle.clientChangeNickname(packet);
        });
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
                if (packetID != -1) {
                    System.err.println("Server: Received a packet with an unidentifiable ID");
                    System.err.println("Packet ID: " + packetID);
                } else {
                    System.out.println("Server: Received a packet with no ID");
                }
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

    public ServerSend getSend() {
        return send;
    }

    public ServerHandle getHandle() {
        return handle;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void claimId(int id) {
        if (hasClaimedId) {
            System.out.println("Failed to set ID");
            return;
        }

        this.id = id;
        hasClaimedId = true;
    }

}
