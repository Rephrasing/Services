package io.github.rephrasing.services.impl.comms;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.github.rephrasing.services.api.Service;
import io.github.rephrasing.services.api.ServiceInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ServiceInfo(value = "Client-Socket-Service", async = true)
public class ClientSocketService extends Service {

    private final String address;
    private final int port;
    private final Consumer<JsonElement> listenerFunction;
    private final int soTimeoutMillis;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Gson gson;

    private Socket socket;

    public ClientSocketService(String address, int port, int soTimeOut, TimeUnit timeoutTimeUnit, Gson gson, Consumer<JsonElement> listenerFunction) {
        this.address = address;
        this.port = port;
        this.listenerFunction = listenerFunction;
        this.gson = gson;
        this.soTimeoutMillis = (int)timeoutTimeUnit.toMillis(soTimeOut);
    }

    public void sendMessage(JsonElement message) {
        if (!isConnected()) {
            throw new IllegalArgumentException("cannot send a message without on an inactive service");
        }
        executor.execute(()->{
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(this.gson.toJson(message));
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void start() {
        try {
            this.socket = new Socket(address, port);
            this.socket.setSoTimeout(soTimeoutMillis);
            getLogger().info("Connected to server {}:{}", this.socket.getInetAddress().toString(), this.socket.getLocalPort());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            while (!socket.isClosed()) {
                String cmd = in.readUTF();
                if (cmd.isEmpty()) continue;
                this.listenerFunction.accept(this.gson.fromJson(cmd, JsonElement.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void stop() {
        if (!isConnected()) return;
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return this.socket != null;
    }
}
