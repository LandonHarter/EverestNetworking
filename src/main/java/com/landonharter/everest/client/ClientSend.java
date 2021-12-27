package com.landonharter.everest.client;

import com.landonharter.everest.packet.ClientPackets;
import com.landonharter.everest.packet.Packet;

public class ClientSend {

    private Client client;

    public ClientSend(Client client) {
        this.client = client;
    }

    public void disconnect() {
        Packet disconnectPacket = new Packet(1);
        disconnectPacket.write("Disconnect");
        client.sendData(disconnectPacket);
    }

}
