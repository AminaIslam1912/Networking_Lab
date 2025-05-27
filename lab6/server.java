import java.io.*;
import java.net.*;
import java.util.Random;

public class server {
    public static ServerSocket serverSocket = null;
    public static int port = 5000;

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port number: " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            socket.setReceiveBufferSize(1024); // Simulate receive window

            System.out.println("Client connected: " + socket.getInetAddress());

            ClientHandler clientHandler = new ClientHandler(socket);
            clientHandler.start();
        }
    }
}

class ClientHandler extends Thread {
    Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            int expectedSeq = 1;
            Random rand = new Random();

            while (true) {
                try {
                    int seq = in.readInt();
                    int len = in.readInt();
                    byte[] data = new byte[len];
                    in.readFully(data);

                    // Simulate packet loss
                    if (rand.nextDouble() < 0.2) {
                        System.out.println("Packet " + seq + " lost.");
                        continue;
                    }

                    if (seq == expectedSeq) {
                        System.out.println("Received Packet " + seq + ". Sending ACK " + seq);
                        expectedSeq++;
                    } else {
                        System.out.println("Received Packet " + seq + ". Out of order. Sending Duplicate ACK " + (expectedSeq - 1));
                    }

                    // Send cumulative ACK
                    out.writeInt(expectedSeq - 1);
                    out.flush();

                } catch (EOFException e) {
                    break; // End of file transmission
                }
            }

            in.close();
            out.close();
            socket.close();
            System.out.println("Connection closed with client.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
