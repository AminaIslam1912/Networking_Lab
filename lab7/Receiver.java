

import java.io.*;
import java.net.*;
import java.util.*;

public class Receiver {
    public static void main(String[] args) throws Exception {
        int port = 5000;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Receiver started on port " + port);

        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress());

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        
        Set<Integer> packetsToDrop = new HashSet<>(Arrays.asList( 12 , 20, 30,100,67,38,80));
        int packetCounter = 0;

        String line;
        while ((line = in.readLine()) != null) {
            if (line.equals("END")) break;
            String[] packets = line.split(",");

            for (String packet : packets) {
                packetCounter++;

                if (packetsToDrop.contains(packetCounter)) {
                    
                    String prev = (packetCounter == 1) ? "NA" : "pkt" + (packetCounter - 1);
                    for (int j = 0; j < 3; j++) {
                        out.println("ACK:" + prev);
                        System.out.println("Sent: ACK:" + prev + " (Duplicate for LOST pkt" + packetCounter + ")");
                    }
                   
                } else {
                    out.println("ACK:" + packet);
                    System.out.println("Sent: ACK:" + packet);
                }
            }
        }

        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
        System.out.println("Receiver closed.");
    }
}

