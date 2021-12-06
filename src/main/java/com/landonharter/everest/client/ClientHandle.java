package com.landonharter.everest.client;

import com.landonharter.everest.packet.Packet;

public class ClientHandle {

    public Client client;

    public ClientHandle(Client client) {
        this.client = client;
    }

    public void claimId(Packet packet) {
        client.clientID = packet.readInt();
    }

    public void disconnect() {
        client.disconnect();
    }

}
