package com.landonharter.everest.server;

import com.landonharter.everest.packet.Packet;

public class ServerHandle {

    public ServerClient client;

    public ServerHandle(ServerClient client) {
        this.client = client;
    }

    public void clientChangeNickname(Packet packet) {
        client.setNickname(packet.readString());
    }

    public void clientDisconnect(Packet packet) {
        client.disconnect();
    }

}
