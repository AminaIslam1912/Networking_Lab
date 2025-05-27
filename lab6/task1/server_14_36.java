import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class server_14_36 {
    public static ServerSocket serverSocket = null;
    public static Socket socket = null;
    public static int port = 5000;

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port number, " + port);

        while (true) {
            socket = serverSocket.accept();

           
            socket.setReceiveBufferSize(1024); // 1 KB window

            System.out.println("Client connected : " + socket.getInetAddress());

            
            ClientHandler clientHandler = new ClientHandler(socket);
            clientHandler.start();
        }
        //serverSocket.close();
    }
}

class ClientHandler extends Thread {
    Socket socket;
    final int windowSize = 1024;  

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
           
            InputStream input = socket.getInputStream();
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytes = 0;
            int windowReceivedBytes = 0; 

           
            while ((bytesRead = input.read(buffer)) != -1) {
                totalBytes += bytesRead;
                windowReceivedBytes += bytesRead;
                int temp = totalBytes % windowSize;
                int remaining = (temp == 0) ? 0 : windowSize - temp;
                //int remaining = windowSize - temp;

                //if (windowReceivedBytes >= 1024) windowReceivedBytes = 0;  

                System.out.println("Received " + bytesRead + " bytes. Total = " + totalBytes);
                System.out.flush(); // Ensure output is visible

                //int remaining = 1024 % totalBytes;
               
                String ack = "ACK: Received up to byte " + totalBytes + " remaining window size is " + remaining + "\n";
               
                output.writeUTF(ack);
                output.flush();
                System.out.println("Client disconnected.");

            }

            
            input.close();
            output.close();
            socket.close();
            System.out.println("Connection closed with client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
