// package task;

// import java.io.*;
// import java.net.*;

// public class ChatClient {
//     private static final String SERVER_ADDRESS = "localhost";
//     private static final int SERVER_PORT = 5000;
//     private static final String END_OF_MESSAGE = "<EOM>";

//     public static void main(String[] args) {
//         Socket socket = null;
//         PrintWriter out = null;
//         BufferedReader in = null;
//         BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

//         try {
//             // Connect to server
//             socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
//             System.out.println("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
//             System.out.println("Enter messages (type 'SEND' to send multiline message, 'EXIT' to quit):");

//             // Initialize I/O streams
//             out = new PrintWriter(socket.getOutputStream(), true);
//             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//             // Thread to read server responses
//             BufferedReader finalIn = in;
//             Thread responseThread = new Thread(() -> {
//                 try {
//                     String response;
//                     while ((response = finalIn.readLine()) != null) {
//                         System.out.println(response);
//                     }
//                 } catch (IOException e) {
//                     System.out.println("Server disconnected.");
//                 }
//             });
//             responseThread.start();

//             // Buffer for multiline message
//             StringBuilder messageBuffer = new StringBuilder();
//             String userInput;

//             // Read user input
//             while ((userInput = consoleInput.readLine()) != null) {
//                 if (userInput.equalsIgnoreCase("EXIT")) {
//                     out.println("EXIT");
//                     break;
//                 } else if (userInput.equalsIgnoreCase("SEND")) {
//                     // Send the buffered message
//                     if (messageBuffer.length() > 0) {
//                         out.println(messageBuffer.toString());
//                         out.println(END_OF_MESSAGE); // Send delimiter
//                         messageBuffer.setLength(0); // Clear buffer
//                     }
//                 } else {
//                     // Append line to buffer
//                     messageBuffer.append(userInput).append("\n");
//                 }
//             }
//         } catch (IOException e) {
//             System.out.println("Error connecting to server: " + e.getMessage());
//         } finally {
//             try {
//                 if (out != null) out.close();
//                 if (in != null) in.close();
//                 if (socket != null) socket.close();
//                 consoleInput.close();
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }




package task;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final String END_OF_MESSAGE = "<EOM>";

    public static void main(String[] args) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

        try {
            // Connect to server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            System.out.println("Enter messages (type 'SEND' to send multiline message, 'EXIT' to quit):");

            // Initialize I/O streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Thread to read server responses
            BufferedReader finalIn = in;
            Thread responseThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = finalIn.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.out.println("Server disconnected.");
                }
            });
            responseThread.start();

            // Buffer for multiline message
            StringBuilder messageBuffer = new StringBuilder();
            String userInput;

            // Read user input
            while ((userInput = consoleInput.readLine()) != null) {
                if (userInput.equalsIgnoreCase("EXIT")) {
                    out.println("EXIT");
                    break;
                } else if (userInput.equalsIgnoreCase("SEND")) {
                    // Send the buffered message
                    if (messageBuffer.length() > 0) {
                        out.println(messageBuffer.toString());
                        out.println(END_OF_MESSAGE); // Send delimiter
                        messageBuffer.setLength(0); // Clear buffer
                    }
                } else {
                    // Append line to buffer
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
                consoleInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}