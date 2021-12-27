package examples.simple;

import com.landonharter.everest.client.Client;
import com.landonharter.everest.packet.Packet;
import com.landonharter.everest.server.Server;

public class Main {

    private static Client client;

    private static final int PORT = 444;

    public static void main(String[] args) {
        try {
            Start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void Start() throws InterruptedException {
        client = new Client("127.0.0.1", PORT);
        Server.create(PORT);
        client.connect();

        client.disconnect();
    }

}
