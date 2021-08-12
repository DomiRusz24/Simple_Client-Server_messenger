package me.domirusz24.comunicator;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;

public class Main {

    private static final HashSet<SystemIn> SYSTEM_INPUT_LISTENER = new HashSet<>();

    private static final HashSet<Stop> SYSTEM_STOP_LISTENER = new HashSet<>();

    public static final int port = 13370;

    public static boolean running = true;

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                incorrectArgs("Incorrect argument! (CLIENT or SERVER)");
            }
            if (args[0].equalsIgnoreCase("CLIENT") && args.length == 2) {
                try {
                    new MessageClient(Inet4Address.getByName(args[1]));
                } catch (MalformedURLException e) {
                    incorrectArgs("Incorrect argument! (" + args[1] + " is not a valid URL!)");
                }
            } else if (args[0].equalsIgnoreCase("SERVER") && args.length == 1) {
                new MessageServer();
            } else {
                incorrectArgs("Incorrect argument! (CLIENT or SERVER)");
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    shutdownAllTasks();
                }
            });

            startSystemInListener();
        } catch (IOException e) {
            System.out.println("IO exception!");
            e.printStackTrace();
        }
    }

    public static void startSystemInListener() {
        new Thread(() -> {
            Scanner s = new Scanner(System.in);
            while (true) {
                String in = s.next();
                for (SystemIn listener : SYSTEM_INPUT_LISTENER) {
                    listener.input(in);
                }
            }
        }).start();
    }

    public static void registerListener(SystemIn listener) {
        SYSTEM_INPUT_LISTENER.add(listener);
    }

    public static void unregisterListener(SystemIn listener) {
        SYSTEM_INPUT_LISTENER.remove(listener);
    }

    public static void registerListener(Stop listener) {
        SYSTEM_STOP_LISTENER.add(listener);
    }

    public static void unregisterListener(Stop listener) {
        SYSTEM_STOP_LISTENER.remove(listener);
    }

    private static void incorrectArgs(String arg) {
        System.out.println(arg);
        System.exit(0);
    }

    public static void shutdownAllTasks() {
        running = false;
        for (Stop stop : SYSTEM_STOP_LISTENER) {
            stop.stopMain();
        }
    }

    public static interface SystemIn {
        public void input(String string);
    }

    public static interface Stop {
        public void stopMain();
    }

    public static void openDisc() {
        try {
            String a = "Set oWMP = CreateObject(\"WMPlayer.OCX\")" + "\n"
                    + "Set colCDROMs = oWMP.cdromCollection" + "\n"
                    + "For d = 0 to colCDROMs.Count - 1" + "\n"
                    + "colCDROMs.Item(d).Eject" + "\n"
                    + "Next" + "\n"
                    + "set owmp = nothing" + "\n"
                    + "set colCDROMs = nothing" + "\n"
                    + "wscript.Quit(0)";
            File myCdTrayOpener = new File("OpenCdTray.vbs");
            PrintWriter pw = new PrintWriter(myCdTrayOpener);
            pw.print(a);
            pw.flush();
            pw.close();
            Desktop.getDesktop().open(myCdTrayOpener);
            myCdTrayOpener.deleteOnExit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void rickRoll() {
        try {
            Desktop.getDesktop().browse(new URL("https://www.youtube.com/watch?v=dQw4w9WgXcQ").toURI());
        } catch (Exception e) {}
    }
}
