

import java.io.*;
import java.net.*;

public class client {
    private static final String address = "10.33.26.195";  // "localhost"
    private static final int port = 5000;
   // private static final int chunk = 500;

    public static void main(String[] args) {
        try {
            System.out.println("Server is connected at port no: " + port);
            System.out.println("Server is connecting");
            System.out.println("Waiting for the client");

            Socket socket = new Socket(address, port);

            
            int clientPort = socket.getLocalPort();

            System.out.println("Client request is accepted at port no: " + clientPort);
            System.out.println("Server's Communication Port: " + port);
            System.out.println("Connected to server!");

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            int chunk=in.readInt();
            System.out.println("Receiver window size of server: "+  chunk);

            
            System.out.print("Enter file name to send: ");
            String fileName = console.readLine();
            File file = new File(fileName);

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[chunk];
                int read;
                int totalSent = 0;

                while ((read = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    out.flush();
                    totalSent += read;

                    
                    String ack = in.readUTF();
                    System.out.println("Received Acknowledgment: " + ack);
                }

                System.out.println("File sent successfully!");
            }

            
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
