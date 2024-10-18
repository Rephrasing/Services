package io.github.rephrasing.services.impl.comms;

import io.github.rephrasing.services.api.Service;
import io.github.rephrasing.services.api.ServiceInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ServiceInfo(value = "Server-Socket-Service", async = true)
public class ServerSocketService extends Service {

    private final int port;
    private final Consumer<String> listenerFunction;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private ServerSocket socket;
    private Socket connection;

    public ServerSocketService(int port, Integer setSoTimeOut, TimeUnit timeoutTimeUnit, Consumer<String> listenerFunction) {
        this.port = port;
        this.listenerFunction = listenerFunction;
        try {
            this.socket = new ServerSocket(port, 50);
            this.socket.setSoTimeout((int) timeoutTimeUnit.toMillis(setSoTimeOut));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        executor.execute(()->{
            try {
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeUTF(message);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void start() {
        try {
            getLogger().info("Listening on port {}", this.port);
            this.connection = this.socket.accept();
            getLogger().info("Connected by client {}:{}", this.socket.getInetAddress().toString(), this.socket.getLocalPort());
            DataInputStream in = new DataInputStream(connection.getInputStream());
            while (!connection.isClosed()) {
                String cmd = in.readUTF();
                if (cmd.isEmpty()) continue;
                this.listenerFunction.accept(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void stop() {
        if (!socket.isBound()) {
            return;
        }

        executor.execute(()->{
            try {
                this.connection.close();
                this.socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            getLogger().info("Disconnected");
        });
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
