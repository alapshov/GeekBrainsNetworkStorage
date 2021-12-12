package ru.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Handler implements Runnable {

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Handler(Socket socket) throws IOException {
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Client accepted");
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = inputStream.readUTF();
                System.out.println("received command: " + command);
                switch (command) {
                    case "getFile": {
                        outputStream.writeUTF("Enter file name...");
                        String fileName = inputStream.readUTF();
                        outputStream.writeUTF("File: " + fileName);
                        break;
                    }
                    case "getListFiles":
                        outputStream.writeUTF("List: {File1, File2, File3}");
                        break;
                    case "putFile": {
                        outputStream.writeUTF("Enter file name...");
                        String fileName = inputStream.readUTF();
                        outputStream.writeUTF("Enter file size...");
                        long size = inputStream.readLong();
                        outputStream.writeUTF("Upload: file" + fileName + "uploaded, size: " + size);
                        break;
                    }
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
