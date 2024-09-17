package io.github.burymydeadhoreses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class Client {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;

    private final UUID id;

    public Client(Socket handler) throws IOException {
        id = UUID.randomUUID();
        socket = handler;

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(outputStream, true);
    }

    public void writeLine(String message) {
        CompletableFuture.runAsync(() -> writer.println(message));
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public Socket getSocket() {
        return socket;
    }

    public UUID getId() {
        return id;
    }
}

