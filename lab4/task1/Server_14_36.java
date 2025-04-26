package lab4;
import java.io.*;
import java.net.*;

public class Server_14_36 {
    private static final int PORT = 5000;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket);

                    // Initialize input/output streams with the accepted socket
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

                    // Pass streams to client handler
                    ClientHandler clientHandler = new ClientHandler(in, writer, dos);
                    clientHandler.start();

                } catch (SocketException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ClientHandler extends Thread {
    private BufferedReader in;
    private BufferedWriter writer;
    private DataOutputStream dos;

    public ClientHandler(BufferedReader bufferedReader, BufferedWriter bufferedWriter,
            DataOutputStream dataOutputStream) {
        this.in = bufferedReader;
        this.writer = bufferedWriter;
        this.dos = dataOutputStream;
    }

    @Override
    public void run() {
        try {
            // writer.write("Enter the file name you want to download:\n");
            // writer.flush();

            // List available files in the current directory
            File dir = new File("."); // "." means current directory
            File[] filesList = dir.listFiles();

            writer.write("Available Files are:\n");
            for (File f : filesList) {
                if (f.isFile()) {
                    writer.write("- " + f.getName() + "\n");
                    //System.out.println("gui");
                }
            }
            writer.flush();
            System.out.println("gheee");

            // writer.write("Enter the file name you want to download:\n");
            // writer.flush();
            // System.out.println("yes");

            String fileName = in.readLine();
            File file = new File(fileName);

            if (file.exists() && file.isFile()) {
                writer.write("File Found\n");
                writer.flush();

                // long l = file.length();

                // Send file size
                dos.writeLong(file.length());
                dos.flush();

                // Send file contents
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                dos.flush();
                fis.close();

                System.out.println("File sent to client: " + fileName);

            } else {
                writer.write("File Not_Found!!\n");
                writer.flush();
            }

        } catch (IOException e) {
            System.out.println("Error handling Client: " + e.getMessage());
        } finally {
            try {
                in.close();
                writer.close();
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}