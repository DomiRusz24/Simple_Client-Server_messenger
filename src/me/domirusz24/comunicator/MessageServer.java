package me.domirusz24.comunicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class MessageServer implements Main.SystemIn {

    private HashSet<ClientHandler> clients = new HashSet<>();

    private final ServerSocket socket;

    public static boolean isRunning() {
        return Main.running;
    }

    public MessageServer() throws IOException {
        socket = new ServerSocket(Main.port);
        openMainServerSocket();
        Main.registerListener((Main.SystemIn) this);
    }

    private void openMainServerSocket() {
        Thread server = new LoopThread(
                () -> {
                    try {
                        new ClientHandler(socket.accept());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    abort();
                }
        );
        server.start();
    }

    private void abort() {
        System.out.println("Aborting...");
        for (ClientHandler client : clients) {
            client.sendServer("ABORT!");
            client.sendCommand("ABORT");
        }
    }

    @Override
    public void input(String line) {
        if (line.equals("ABORT")) {
            abort();
            System.exit(0);
        } else if (line.equals("DISCOPEN")) {
            for (ClientHandler c : clients) {
                c.sendCommand("DISCOPEN");
            }
            System.out.println("xD");
            Main.openDisc();
        } else if (line.equals("RICKROLL")) {
            for (ClientHandler c : clients) {
                c.sendCommand("RICKROLL");
            }
            Main.rickRoll();
        } else {
            String ip = socket.getInetAddress().getHostAddress();
            System.out.println(ip + " >> " + line);
            for (ClientHandler c : clients) {
                c.send(ip, line);
            }
        }
    }


    public class ClientHandler extends Thread {

        private final Socket client;

        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket client) {
            this.client = client;
            clients.add(this);
            try {
                out = new PrintWriter(client.getOutputStream());
            } catch (IOException e) {
                System.out.println("Failed connecting " + client.getInetAddress().getCanonicalHostName() + "!");
                e.printStackTrace();
                return;
            }
            this.start();
        }

        public void send(String sender, String message) {
            out.println(sender + " >> " + message);
            out.flush();
        }

        public void send(String message) {
            out.println(message);
            out.flush();
        }

        public void sendServer(String message) {
            out.println("SERVER >> " + message);
            out.flush();
        }

        public void sendCommand(String command) {
            out.println(command);
            out.flush();
        }

        private void close() {
            clients.remove(this);
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String ip = client.getInetAddress().getCanonicalHostName();
            for (ClientHandler c : clients) {
                c.sendServer(ip + " has disconnected!");
            }
        }

        @Override
        public void run() {
            try {
                for (ClientHandler c : clients) {
                    c.send("[LOG] " + client.getInetAddress().getCanonicalHostName() + " connected");
                }
                System.out.println("[LOG] " + client.getInetAddress().getCanonicalHostName() + " connected");
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                if (client.isClosed()) {
                    close();
                    return;
                }
                String line = in.readLine();
                String ip = client.getInetAddress().getCanonicalHostName();
                while(line != null && line.length() > 0) {
                    System.out.println('\n' + ip + " >> " + line);
                    for (ClientHandler c : clients) {
                        c.send(ip, line);
                    }
                    out.flush();
                    if (!socket.isClosed()) {
                        line = in.readLine();
                    } else {
                        break;
                    }
                }
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
