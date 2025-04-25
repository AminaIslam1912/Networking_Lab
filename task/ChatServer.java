// package task;
// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class ChatServer {
//     private static final int PORT = 5000;
//     private static ArrayList<ClientHandler> clients = new ArrayList<>();
//     private static ServerSocket serverSocket;

//     public static void main(String[] args) {
//         try {
//             serverSocket = new ServerSocket(PORT);
//             System.out.println("Server started on port " + PORT);

//             // Thread to listen for shutdown command
//             Thread shutdownThread = new Thread(() -> {
//                 Scanner scanner = new Scanner(System.in);
//                 while (true) {
//                     String command = scanner.nextLine();
//                     if (command.equalsIgnoreCase("SHUTDOWN") && clients.isEmpty()) {
//                         System.out.println("Shutting down server...");
//                         try {
//                             serverSocket.close();
//                             System.exit(0);
//                         } catch (IOException e) {
//                             e.printStackTrace();
//                         }
//                     } else if (!clients.isEmpty()) {
//                         System.out.println("Cannot shutdown: Clients are still connected.");
//                     }
//                 }
//             });
//             shutdownThread.start();

//             // Accept client connections
//             while (true) {
//                 try {
//                     Socket clientSocket = serverSocket.accept();
//                     System.out.println("New client connected: " + clientSocket);

//                     // Create a new client handler
//                     ClientHandler clientHandler = new ClientHandler(clientSocket);
//                     clients.add(clientHandler);
//                     clientHandler.start();
//                 } catch (SocketException e) {
//                     // Server socket closed
//                     break;
//                 }
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         } finally {
//             try {
//                 if (serverSocket != null && !serverSocket.isClosed()) {
//                     serverSocket.close();
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     // Remove a client from the list
//     public static synchronized void removeClient(ClientHandler client) {
//         clients.remove(client);
//         System.out.println("Client disconnected. Active clients: " + clients.size());
//     }
// }


//  class ClientHandler extends Thread {
//     private Socket socket;
//     private PrintWriter out;
//     private BufferedReader in;

//     private static final String END_OF_MESSAGE = "<EOM>";

//     public ClientHandler(Socket socket) {
//         this.socket = socket;
//     }

//     @Override
//     public void run() {
//         try {
//             // Initialize input and output streams
//             out = new PrintWriter(socket.getOutputStream(), true);
//             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//             StringBuilder messageBuilder = new StringBuilder();
//             String line;

//             // Read messages from client
//             while ((line = in.readLine()) != null) {
//                 if (line.equalsIgnoreCase("EXIT")) {
//                     break;
//                 }
//                 if (line.equals(END_OF_MESSAGE)) {
//                     // Process the complete message
//                     if (messageBuilder.length() > 0) {
//                         String message = messageBuilder.toString();
//                         System.out.println("Received from " + socket + ":\n" + message);
//                         out.println("Server: Received -\n" + message);
//                         messageBuilder.setLength(0); // Clear builder for next message
//                     }
//                 } else {
//                     // Accumulate lines until <EOM>
//                     messageBuilder.append(line).append("\n");
//                 }
//             }
//         } catch (IOException e) {
//             System.out.println("Error handling client: " + e.getMessage());
//         } finally {
//             try {
//                 // Clean up
//                 out.close();
//                 in.close();
//                 socket.close();
//                 ChatServer.removeClient(this);
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }

   
package task;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static int clientIdCounter = 1; // Counter for assigning client IDs

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

             PrintWriter out;
             BufferedReader in;

            // Thread to handle server terminal input
            Thread inputThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();
                    if (input.equalsIgnoreCase("SHUTDOWN") && clients.isEmpty()) {
                        System.out.println("Shutting down server...");
                        try {
                            serverSocket.close();
                            System.exit(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (input.toUpperCase().startsWith("SEND ")) {
                        // Parse command: SEND <client_id> <message>
                        String[] parts = input.split(" ", 3);
                        if (parts.length < 3) {
                            System.out.println("Invalid command. Use: SEND <client_id> <message>");
                            continue;
                        }
                        try {
                            int clientId = Integer.parseInt(parts[1]);
                            String message = parts[2];
                            sendMessageToClient(clientId, "Server: " + message);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid client ID. Use: SEND <client_id> <message>");
                        }
                    } else if (!clients.isEmpty()) {
                        System.out.println("Cannot shutdown: Clients are still connected.");
                    } else {
                        System.out.println("Invalid command. Use 'SEND <client_id> <message>' or 'SHUTDOWN'");
                    }
                }
            });
            inputThread.start();

            // Accept client connections
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket + " (Client " + clientIdCounter + ")");

                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    // Create a new client handler with a unique ID
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientIdCounter++,in,out);
                    synchronized (clients) {
                        clients.add(clientHandler);
                    }
                    clientHandler.start();
                } catch (SocketException e) {
                    // Server socket closed
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

    // Send a message to a specific client by ID
    public static void sendMessageToClient(int clientId, String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getClientId() == clientId) {
                    client.sendMessage(message);
                    System.out.println("Sent to Client " + clientId + ": " + message);
                    return;
                }
            }
            System.out.println("Client " + clientId + " not found.");
        }
    }

    // Remove a client from the list
    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client " + client.getClientId() + " disconnected. Active clients: " + clients.size());
    }
}





 class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final int clientId; // Unique client ID
    private static final String END_OF_MESSAGE = "<EOM>";

    public ClientHandler(Socket socket, int clientId,BufferedReader in,PrintWriter out) {
        this.socket = socket;
        this.clientId = clientId;
        this.in=in;
        this.out=out;
    }

    public int getClientId() {
        return clientId;
    }

    // Send a message to this client
    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            // Initialize input and output streams
           // out = new PrintWriter(socket.getOutputStream(), true);
           // in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            StringBuilder messageBuilder = new StringBuilder();
            String line;

            // Read messages from client
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("EXIT")) {
                    break;
                }
                if (line.equals(END_OF_MESSAGE)) {
                    // Process the complete message
                    if (messageBuilder.length() > 0) {
                        String message = messageBuilder.toString();
                        System.out.println("Received from Client " + clientId + ":\n" + message);
                        // Echo back to the client
                        // out.println("Server: Received from Client " + clientId + " -\n" + message);
                         out.println("Server: Received msg from Client " + clientId  + "successfully");
                        messageBuilder.setLength(0); // Clear builder for next message
                    }
                } else {
                    // Accumulate lines until <EOM>
                    messageBuilder.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling Client " + clientId + ": " + e.getMessage());
        } finally {
            try {
                // Clean up
                out.close();
                in.close();
                socket.close();
                ChatServer.removeClient(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}