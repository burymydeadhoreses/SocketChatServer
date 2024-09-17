package io.github.burymydeadhoreses;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Server {
    private final ServerSocket serverSocket;
    private final List<Client> clients = new ArrayList<>();

    private BiConsumer<Client, String> onMessageAccept;
    private Consumer<Client> onClientConnect;
    private Consumer<Client> onClientDisconnect;

    public Server(InetSocketAddress address) throws IOException {
        this.serverSocket = new ServerSocket(address.getPort(), 50, address.getAddress());
    }

    public void setOnMessageAccept(BiConsumer<Client, String> onMessageAccept) {
        this.onMessageAccept = onMessageAccept;
    }

    public void setOnClientConnect(Consumer<Client> onClientConnect) {
        this.onClientConnect = onClientConnect;
    }

    public void setOnClientDisconnect(Consumer<Client> onClientDisconnect) {
        this.onClientDisconnect = onClientDisconnect;
    }

    public void listen() {
        CompletableFuture.runAsync(() -> {
            try {
                accept();
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        });
    }

    private void accept() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            CompletableFuture.runAsync(() -> {
                try {
                    acceptClient(clientSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void acceptClient(Socket clientSocket) throws IOException {
        Client client = new Client(clientSocket);

        synchronized (clients) {
            clients.add(client);
            for (Client user : clients) {
                if (!user.getId().equals(client.getId())) {
                    user.writeLine(client.getId() + " connected");
                }
            }
        }

        if (onClientConnect != null) {
            onClientConnect.accept(client);
        }

        acceptMessage(client);
    }

    private void acceptMessage(Client client) {
        CompletableFuture.runAsync(() -> {
            while (client.getSocket().isConnected()) {
                String message = null;
                try {
                    message = client.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (message == null)
                    return;

                if (message.startsWith("/")) {
                    acceptCommand(client, message.replace("/", ""));
                    continue;
                }

                if (onMessageAccept != null) {
                    onMessageAccept.accept(client, message);
                }

                synchronized (clients) {
                    for (Client user : clients) {
                        if (!user.getId().equals(client.getId())) {
                            user.writeLine(user.getId() + " : " + message);
                        }
                    }
                }
            }

            removeClient(client);
        });
    }

    private void acceptCommand(Client client, String message) {
        String[] args = message.split(" ");

        boolean result;
        if (args[0].equalsIgnoreCase("PM")) {
            result = privateMessage(client, args[1], args[2]);
        } else {
            result = false;
        }
    }

    private boolean privateMessage(Client sender, String destinationId, String message) {
        Client client;
        synchronized (clients) {
            client = clients.stream()
                    .filter(c -> c.getId().equals(UUID.fromString(destinationId)))
                    .findFirst()
                    .orElse(null);
        }

        if (client == null) {
            sender.writeLine("No such client connected");
            return false;
        }

        client.writeLine("(Private message from: " + sender.getId() + ") : " + message);
        return true;
    }

    private void removeClient(Client client) {
        synchronized (clients) {
            clients.remove(client);
            for (Client user : clients) {
                if (!user.getId().equals(client.getId()))
                    user.writeLine(user.getId() + " disconnected");
            }
        }

        if (onClientDisconnect != null)
            onClientDisconnect.accept(client);
    }
}




