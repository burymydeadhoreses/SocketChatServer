package io.github.burymydeadhoreses;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        var address = new InetSocketAddress("127.0.0.1", 25565);

        var server = new Server(address);

        server.setOnMessageAccept(Main::printClientMessage);
        server.setOnClientConnect(Main::printConnectedClient);
        server.setOnClientDisconnect(Main::printDisconnectedClient);

        server.listen();

        var scanner = new Scanner(System.in);

        scanner.nextLine();
    }

    static void printClientMessage(Client client, String message) {
        System.out.println(client.getId() + " : " + message);
    }

    static void printDisconnectedClient(Client client) {
        System.out.println(client.getId() + " disconnected");
    }

    static void printConnectedClient(Client client) {
        System.out.println(client.getId() + " connected");
    }
}