package ru.geekbrains.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private static final int BUFFER_SIZE = 8192;
    private byte[] buffer;
    private Path baseDir;
    public ListView<String> clientFiles;
    public ListView<String> serverFiles;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private void read() {
        try {
            while (true) {
                String command = inputStream.readUTF();
                if (command.equals("#list#")) {
                    serverFiles.getItems().clear();
                    int filesCount = inputStream.readInt();
                    for (int i = 0; i < filesCount; i++) {
                        String name = inputStream.readUTF();
                        Platform.runLater(() -> serverFiles.getItems().add(name));
                    }
                }
                if (command.equals("#up#")) {
                    long size = inputStream.readLong();
                    String file = serverFiles.getSelectionModel().getSelectedItem();
                    Path filePath = baseDir.resolve(file);
                    try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile())) {
                        for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int read = inputStream.read(buffer);
                            fileOutputStream.write(buffer, 0, read);
                        }
                    }
                    clientFiles.getItems().clear();
                    clientFiles.getItems().addAll(getFileNames());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<FileInfo> getClientFiles() throws IOException {
        return Files.list(baseDir)
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }

    private List<String> getFileNames() throws IOException {
        return Files.list(baseDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            baseDir = Paths.get("E:/Porgramming");
            buffer = new byte[BUFFER_SIZE];
            clientFiles.getItems().addAll(getFileNames());
            Socket socket = new Socket("localhost", 8101);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {

        String file = clientFiles.getSelectionModel().getSelectedItem();
        Path filePath = baseDir.resolve(file);
        outputStream.writeUTF("#upload#");
        outputStream.writeUTF(file);
        outputStream.writeLong(Files.size(filePath));
        outputStream.write(Files.readAllBytes(filePath));
        outputStream.flush();
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String file = serverFiles.getSelectionModel().getSelectedItem();
        outputStream.writeUTF("#download#");
        outputStream.writeUTF(file);
        outputStream.flush();
    }
}
