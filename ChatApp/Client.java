package ChatApp;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";//10.33.28.18
    private static final int SERVER_PORT = 5000;
    private static final String endDetect = "<EOM>";

    public static void main(String[] args) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            System.out.println("Enter messages (type 'SEND' to send message, 'EXIT' to quit):");

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            ReadServer rs = new ReadServer(in);
            rs.start();

            StringBuilder messageBuffer = new StringBuilder();
            String userInput;

            while ((userInput = read.readLine()) != null) {
                if (userInput.equalsIgnoreCase("EXIT")) {
                    out.println("EXIT");
                    break;
                } else if (userInput.equalsIgnoreCase("SEND")) {
                    if (messageBuffer.length() > 0) {
                        out.println(messageBuffer.toString());
                        out.println(endDetect);
                        messageBuffer.setLength(0); 
                    }
                } else {
                    messageBuffer.append(userInput).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


class ReadServer extends Thread{
    BufferedReader str;

    ReadServer(BufferedReader bf){
        this.str = bf;
    }

    @Override
    public void run(){
        try {
            String response;
            while ((response = str.readLine()) != null) {
                //System.out.println("hello");
                System.out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Server disconnected.");
        }
    }
}