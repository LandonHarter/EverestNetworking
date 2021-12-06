package com.landonharter.everest.client;

import com.landonharter.everest.packet.Packet;

public class ClientHandle {

    public Client client;

    public ClientHandle(Client client) {
        this.client = client;
    }

    public void ClaimID(Packet packet) {
        client.clientID = packet.ReadInt();
    }

    public void Disconnect() {
        client.Disconnect();
    }

}
