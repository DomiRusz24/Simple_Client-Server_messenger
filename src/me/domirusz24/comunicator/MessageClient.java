package me.domirusz24.comunicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MessageClient implements Main.SystemIn {

    public static boolean isRunning() {
        return Main.running;
    }

    private final InetAddress address;

    private final Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    public MessageClient(InetAddress address) throws IOException {
        this.address = address;
        socket = new Socket(address, Main.port);
        openMainClientSocket();
        Main.registerListener(this);
    }

    private void openMainClientSocket() {
        try {
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Failed connecting to server!");
            e.printStackTrace();
            return;
        }
        Thread server = new LoopThread(
                () -> {
                    try {
                        if (!socket.isClosed()) {
                            String line = in.readLine();
                            while (line != null && line.length() > 0 && !line.equals("ABORT")) {
                                if (line.equals("RICKROLL")) {
                                    Main.rickRoll();
                                } else if (line.equals("DISCOPEN")) {
                                    System.out.println("xD");
                                    Main.openDisc();
                                } else {
                                    System.out.println(line);
                                }
                                line = in.readLine();
                            }
                        }
                        close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    out.println("");
                    out.flush();
                    Main.unregisterListener(this);
                }
        );
        server.start();
    }

    private void close() {
        try {
            System.out.println("ABORTING!");
            in.close();
            out.close();
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public InetAddress getAddress() {
        return address;
    }

    @Override
    public void input(String string) {
        out.println(string);
        out.flush();
    }
}
