package ChatApp;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static int clientCounter = 1; 

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            PrintWriter out;
            BufferedReader in;

            WorkOfServer workOfServer = new WorkOfServer(serverSocket);
            workOfServer.start();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket + " (Client " + clientCounter + ")");

                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientCounter++, in, out);
                    synchronized (clients) {
                        clients.add(clientHandler);
                    }
                    clientHandler.start();
                } catch (SocketException e) {
                    
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

    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client " + client.getClientId() + " disconnected. Active clients: " + clients.size());
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final int clientId; 
    private static final String endDetect = "<EOM>";

    public ClientHandler(Socket socket, int clientId, BufferedReader in, PrintWriter out) {
        this.socket = socket;
        this.clientId = clientId;
        this.in = in;
        this.out = out;
    }

    public int getClientId() {
        return clientId;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            
            StringBuilder messageBuilder = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("EXIT")) {
                    break;
                }
                if (line.equals(endDetect)) {
                    if (messageBuilder.length() > 0) {
                        String message = messageBuilder.toString();
                        System.out.println("Received from Client " + clientId + ":\n" + message);
                        // // Echo back to the client
                        out.println("Server: Received message from Client " + clientId + " successfully!!");
                        messageBuilder.setLength(0); // Clear builder for next message
                    }
                } else {
                    messageBuilder.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling Client " + clientId + ": " + e.getMessage());
        } finally {
            try {
                out.close();
                in.close();
                socket.close();
                Server.removeClient(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class WorkOfServer extends Thread {

    ServerSocket ss;
    Scanner scanner = null;

    WorkOfServer(ServerSocket ss) {
        this.ss = ss;
    }

    @Override
    public void run() {
        scanner = new Scanner(System.in);
        try {
            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("SHUTDOWN") && Server.clients.isEmpty()) {
                    System.out.println("Shutting down server...");
                    try {
                        ss.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break; 
                } else if (input.toUpperCase().startsWith("SEND ")) {
                    String[] parts = input.split(" ", 3);
                    if (parts.length < 3) {
                        System.out.println("Invalid command. Use: SEND <client_id> <message>");
                        continue;
                    }
                    try {
                        int clientId = Integer.parseInt(parts[1]);
                        String message = parts[2];
                        Server.sendMessageToClient(clientId, "Server: " + message);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid client ID. Use: SEND <client_id> <message>");
                    }
                } else if (!Server.clients.isEmpty()) {
                    System.out.println("Cannot shutdown: Clients are still connected.");
                } else {
                    System.out.println("Invalid command. Use 'SEND <client_id> <message>' or 'SHUTDOWN'");
                }
            }
        } finally {
            scanner.close();
        }
    }
    

   
}