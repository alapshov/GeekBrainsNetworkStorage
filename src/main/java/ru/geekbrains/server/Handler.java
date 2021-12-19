package ru.geekbrains.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Handler implements Runnable {

    private static final int BUFFER_SIZE = 8192;
    private Path currentDir;
    private byte[] buffer;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private FileOutputStream fileOutputStream;

    public Handler(Socket socket) throws IOException {
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        currentDir = Paths.get("serverFiles");
        System.out.println("Client accepted");
        sendServerFiles();
        buffer = new byte[BUFFER_SIZE];
    }

    private List<String> getFileNames() throws IOException {
        return Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    private void sendServerFiles() throws IOException {
        outputStream.writeUTF("#list#");
        List<String> names = getFileNames();
        outputStream.writeInt(names.size());
        for (String name : names) {
            outputStream.writeUTF(name);
        }
        outputStream.flush();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = inputStream.readUTF();
                System.out.println("received command: " + command);
                switch (command) {
                    case "#upload#": {
                        String fileName = inputStream.readUTF();
                        long size = inputStream.readLong();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(currentDir.resolve(fileName).toFile())) {
                            for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                                int read = inputStream.read(buffer);
                                fileOutputStream.write(buffer, 0, read);
                            }
                        }
                        sendServerFiles();
                        break;
                    }
                    case "#download#": {
                        String fileName = inputStream.readUTF();
                        long size = Files.size(currentDir.resolve(fileName));
                        outputStream.writeUTF("#up#");
                        outputStream.writeLong(size);
                        try (FileInputStream fileInputStream = new FileInputStream(currentDir.resolve(fileName).toFile())) {
                            for (int i = 0; i < size; i++) {
                                int read = fileInputStream.read();
                                outputStream.writeInt(read);
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
