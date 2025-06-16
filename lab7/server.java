package lab7;

// ===== TCPServer.java =====

import java.io.*;
import java.net.*;
import java.util.*;

// Server Side (Receiver)
public class server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started on port 5000");
        Socket socket = serverSocket.accept();
        System.out.println("Client connected: " + socket.getInetAddress());

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Random random = new Random();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.equals("END")) break;

            String[] packets = line.split(",");
            int lossIndex = random.nextInt(packets.length);
            String lostPacket = packets[lossIndex];

            for (int i = 0; i < packets.length; i++) {
                if (i == lossIndex) {
                    if (i == 0) {
                        out.println("ACK:NA");
                        out.println("ACK:NA");
                        out.println("ACK:NA");
                    } else {
                        String prev = packets[i - 1];
                        out.println("ACK:" + prev);
                        out.println("ACK:" + prev);
                        out.println("ACK:" + prev);
                    }
                } else {
                    out.println("ACK:" + packets[i]);
                }
            }
        }
        socket.close();
        serverSocket.close();
    }
}


