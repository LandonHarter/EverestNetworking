package com.landonharter.everest.server;

import com.landonharter.everest.packet.Packet;
import com.landonharter.everest.packet.ServerPackets;

public class ServerSend {

    public ServerClient client;

    public ServerSend(ServerClient client) {
        this.client = client;
    }

    public void sendId() {
        Packet packet = new Packet(ServerPackets.ID.ordinal());
        packet.write(client.getId());
        client.sendData(packet);
    }

    public void disconnect() {
        Packet packet = new Packet(ServerPackets.ForceDisconnect.ordinal());
        client.sendData(packet);
    }

}
