import java.io.*;
import java.net.*;
import java.util.*;

public class server {
    public static ServerSocket serverSocket = null;
    public static int port = 5000;

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReceiveBufferSize(1024); 
        System.out.println("The server started on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: /" + socket.getInetAddress().getHostAddress());

            ClientHandler clientHandler = new ClientHandler(socket);
            clientHandler.start();
        }
    }
}

class ClientHandler extends Thread {
    Socket socket;
    Map<Integer, byte[]> packetBuffer; 

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.packetBuffer = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            int expectedSeq = 1;
            Random rand = new Random();
            double lossProbability = 0.1; 

            while (true) {
                try {
                    int seq = in.readInt();
                    int len = in.readInt();
                    byte[] data = new byte[len];
                    in.readFully(data);

                    if (rand.nextDouble() < lossProbability) {
                        System.out.println("- Packet " + seq + " not received -");
                        continue;
                    }

                    if (seq == expectedSeq) {
                        System.out.println("Received Packet " + seq  + ". Sending ACK " + seq);
                        packetBuffer.put(seq, data);
                        expectedSeq++;

                
                        while (packetBuffer.containsKey(expectedSeq)) {
                            expectedSeq++;
                        }
                        
                        out.writeInt(expectedSeq - 1);
                        out.flush();
                    } else {
                        System.out.println("Received Packet " + seq + ". Sending Duplicate ACK " + (expectedSeq - 1));
                        packetBuffer.put(seq, data); 
                        out.writeInt(expectedSeq - 1); 
                        out.flush();
                    }

                } catch (EOFException e) {
                    break; 
                }
            }

            in.close();
            out.close();
            socket.close();
            System.out.println("Client disconnected.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



