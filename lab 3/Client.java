import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try (
                Socket socket = new Socket("localhost", 5001);
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                DataInputStream input = new DataInputStream(socket.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {
            System.out.println("Client connected on port " + socket.getPort());
            System.out.println("Enter messages (AUTH:<card_no>:<pin>, WITHDRAW:<amount>, ):");

            String message;
            while (true) {
                message = reader.readLine();
                if (message.equalsIgnoreCase("exit"))
                    break;

                output.writeUTF(message); // send to server
                output.flush();

                String response = input.readUTF(); // receive from server
                System.out.println("Server says: " + response);
                if (response != null) {
                    output.writeUTF("ACK"); // send to server
                    output.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
