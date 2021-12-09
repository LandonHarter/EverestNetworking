package com.landonharter.everest.examples.simple;

import com.landonharter.everest.client.Client;
import com.landonharter.everest.server.Server;

import java.util.Scanner;

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

        System.out.println("Client connected.");

        Thread.sleep(3000);

        client.disconnect();
        System.out.println("Client disconnected.");

        Thread.sleep(3000);

        Server.close();
        System.out.println("Server closed.");
    }

}
