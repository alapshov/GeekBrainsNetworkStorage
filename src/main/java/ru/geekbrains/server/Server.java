package ru.geekbrains.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket srv = new ServerSocket(8101);
        System.out.println("server started...");
        while (true) {
            Socket socket = srv.accept();
            Handler handler = new Handler(socket);
            new Thread(handler).start();
        }
    }
}
