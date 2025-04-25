/* Server Side Code */
package farjana;
// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class server {

//     static ServerSocket ss = null;

//     public static ArrayList<ClientHandler> allClients = new ArrayList<>();

//     public static void main(String[] args) throws IOException {

//         try {

//             int port = 5001;
//             ss = new ServerSocket(port);
//             System.out.println("Server started on port: " + ss.getLocalPort());
//             System.out.println("Waiting for client connections...\n");

//             while (true) {

//                 try {
//                     Socket socket = ss.accept();
//                     System.out.println("client connected from port: " + socket.getPort());

//                     DataInputStream input = new DataInputStream(socket.getInputStream());
//                     DataOutputStream output = new DataOutputStream(socket.getOutputStream());

//                     ClientHandler ch = new ClientHandler(socket, input, output);
//                     allClients.add(ch);
//                     ch.start();

//                 } catch (IOException e) {
//                     System.out.println("Error accepting connection: " + e.getMessage());
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         } finally {
//             // if (ss != null && !ss.isClosed()) {
//             //     try {
//             //         ss.close();
//             //         System.out.println("Server socket closed");
//             //     } catch (IOException e) {
//             //         System.err.println("Error closing server socket: " + e.getMessage());
//             //     }
//             // }


//             // Thread to listen for shutdown command
//             Thread shutdownThread = new Thread(() -> {
//                 Scanner scanner = new Scanner(System.in);
//                 while (true) {
//                     String command = scanner.nextLine();
//                     if (command.equalsIgnoreCase("SHUTDOWN") && allClients.isEmpty()) {
//                         System.out.println("Shutting down server...");
//                         try {
//                             ss.close();
//                             System.exit(0);
//                         } catch (IOException e) {
//                             e.printStackTrace();
//                         }
//                     } else if (!allClients.isEmpty()) {
//                         System.out.println("Cannot shutdown: Clients are still connected.");
//                     }
//                 }
//             });
//             shutdownThread.start();
//         }
//     }

//     public static synchronized void removeClients(ClientHandler ch) {
//         allClients.remove(ch);
//         System.out.println("Total Active clients : " + allClients.size());
//     }

// }

// class ClientHandler extends Thread {

//     private Socket clientSocket;
//     private DataInputStream dis;
//     private DataOutputStream dos;
//     boolean isRun = false;

//     ClientHandler(Socket cs, DataInputStream i, DataOutputStream o) {
//         this.clientSocket = cs;
//         this.dis = i;
//         this.dos = o;
//     }

//     public void run() {
//         String readClient;

//         // Thread serverInputThread = new Thread(() -> {

//         //     if (Server.allClients.isEmpty() && isRun == true) {
//         //         System.out.println("No active clients. Server shutting down...");
//         //         try {
//         //             Server.ss.close();
//         //         } catch (IOException e) {
//         //             e.printStackTrace();
//         //         }
//         //         System.exit(0);
//         //     } else {
//         //         System.out.println("There are still active clients connected. Can't stopserver yet.");
//         //     }

//         // });
//         // serverInputThread.start();


        

//         try {
//             while (true) {

//                 readClient = dis.readUTF();
//                 System.out.println("Client says at port number " + clientSocket.getPort() + " " + readClient);
//                 if (readClient.equalsIgnoreCase("stop")) {
//                     System.out.println("Client disconnetced at port " + clientSocket.getPort());
//                     server.removeClients(this);
//                     break;
//                 }

//                 BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
//                 String strServer = read.readLine();
//                 isRun = true;
//                 //System.out.println(isRun);

                
//                 dos.writeUTF(strServer);
             
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }

//     }
// }







// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class server {
//     static ServerSocket ss = null;
//     static ArrayList<ClientHandler> allClients = new ArrayList<>();
//     static BufferedReader serverInput; // Shared console input for server responses

//     public static void main(String[] args) {
//         try {
//             int port = 5001;
//             ss = new ServerSocket(port);
//             System.out.println("Server started on port: " + ss.getLocalPort());
//             System.out.println("Waiting for client connections...\n");

//             // Initialize shared BufferedReader for server console input
//             serverInput = new BufferedReader(new InputStreamReader(System.in));

//             // Start shutdown thread concurrently
//             Thread shutdownThread = new Thread(() -> {
//                 Scanner scanner = new Scanner(System.in);
//                 while (true) {
//                     String command = scanner.nextLine();
//                     if (command.equalsIgnoreCase("SHUTDOWN")) {
//                         synchronized (allClients) {
//                             if (allClients.isEmpty()) {
//                                 System.out.println("Shutting down server...");
//                                 try {
//                                     ss.close();
//                                     serverInput.close();
//                                     System.out.println("Server socket closed");
//                                     System.exit(0);
//                                 } catch (IOException e) {
//                                     System.err.println("Error closing server socket: " + e.getMessage());
//                                 }
//                             } else {
//                                 System.out.println("Cannot shutdown: " + allClients.size() + " clients are still connected.");
//                             }
//                         }
//                     }
//                 }
//             });
//             shutdownThread.setDaemon(true);
//             shutdownThread.start();

//             // Main server loop to accept clients
//             while (true) {
//                 try {
//                     Socket socket = ss.accept();
//                     System.out.println("Client connected from port: " + socket.getPort());

//                     DataInputStream input = new DataInputStream(socket.getInputStream());
//                     DataOutputStream output = new DataOutputStream(socket.getOutputStream());

//                     ClientHandler ch = new ClientHandler(socket, input, output);
//                     synchronized (allClients) {
//                         allClients.add(ch);
//                     }
//                     ch.start();
//                 } catch (IOException e) {
//                     if (ss.isClosed()) {
//                         System.out.println("Server socket closed, stopping accept loop.");
//                         break;
//                     }
//                     System.out.println("Error accepting connection: " + e.getMessage());
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         } finally {
//             if (ss != null && !ss.isClosed()) {
//                 try {
//                     ss.close();
//                     System.out.println("Server socket closed in finally block");
//                 } catch (IOException e) {
//                     System.err.println("Error closing server socket: " + e.getMessage());
//                 }
//             }
//         }
//     }

//     public static synchronized void removeClients(ClientHandler ch) {
//         allClients.remove(ch);
//         System.out.println("Total Active clients: " + allClients.size());
//     }
// }

// class ClientHandler extends Thread {
//     private Socket clientSocket;
//     private DataInputStream dis;
//     private DataOutputStream dos;

//     ClientHandler(Socket cs, DataInputStream i, DataOutputStream o) {
//         this.clientSocket = cs;
//         this.dis = i;
//         this.dos = o;
//     }

//     @Override
//     public void run() {
//         try {
//             while (true) {
//                 // Read client message
//                 String readClient = dis.readUTF();
//                 System.out.println("Client says at port " + clientSocket.getPort() + ": " + readClient);

//                 if (readClient.equalsIgnoreCase("stop")) {
//                     System.out.println("Client disconnected at port " + clientSocket.getPort());
//                     break;
//                 }

//                 // Prompt server user for response
//                 System.out.print("Enter server response for client at port " + clientSocket.getPort() + ": ");
//                 String strServer = server.serverInput.readLine();
//                 if (strServer != null && !strServer.trim().isEmpty()) {
//                     dos.writeUTF(strServer);
//                     dos.flush(); // Ensure the message is sent immediately
//                 } else {
//                     dos.writeUTF("Server: No response entered.");
//                     dos.flush();
//                 }
//             }
//         } catch (IOException e) {
//             System.out.println("Client at port " + clientSocket.getPort() + " disconnected abruptly: " + e.getMessage());
//         } finally {
//             try {
//                 dis.close();
//                 dos.close();
//                 clientSocket.close();
//                 server.removeClients(this);
//             } catch (IOException e) {
//                 System.err.println("Error closing client resources: " + e.getMessage());
//             }
//         }
//     }
// }




// import java.io.*;
// import java.net.*;
// import java.util.*;
// import java.util.concurrent.LinkedBlockingQueue;

// public class server {
//     static ServerSocket ss = null;
//     static ArrayList<ClientHandler> allClients = new ArrayList<>();
//     static BufferedReader serverInput;
//     static LinkedBlockingQueue<ResponseRequest> responseQueue = new LinkedBlockingQueue<>();

//     public static void main(String[] args) {
//         try {
//             int port = 5001;
//             ss = new ServerSocket(port);
//             System.out.println("Server started on port: " + ss.getLocalPort());
//             System.out.println("Waiting for client connections...\n");

//             // Initialize shared BufferedReader for server console input
//             serverInput = new BufferedReader(new InputStreamReader(System.in));

//             // Start a thread to handle server responses from the queue
//             Thread responseHandlerThread = new Thread(() -> {
//                 while (true) {
//                     try {
//                         // Take a response request from the queue
//                         ResponseRequest request = responseQueue.take();
//                         System.out.print("Enter server response for client at port " + request.clientPort + ": ");
//                         String strServer = serverInput.readLine();
//                         // Only send a response if the input is not empty
//                         if (strServer != null && !strServer.trim().isEmpty()) {
//                             request.handler.sendResponse(strServer);
//                         }
//                     } catch (InterruptedException e) {
//                         System.out.println("Response handler interrupted: " + e.getMessage());
//                         break;
//                     } catch (IOException e) {
//                         System.out.println("Error sending response: " + e.getMessage());
//                     }
//                 }
//             });
//             responseHandlerThread.setDaemon(true);
//             responseHandlerThread.start();

//             // Start shutdown thread
//             Thread shutdownThread = new Thread(() -> {
//                 Scanner scanner = new Scanner(System.in);
//                 while (true) {
//                     String command = scanner.nextLine();
//                     if (command.equalsIgnoreCase("SHUTDOWN")) {
//                         synchronized (allClients) {
//                             if (allClients.isEmpty()) {
//                                 System.out.println("Shutting down server...");
//                                 try {
//                                     ss.close();
//                                     serverInput.close();
//                                     System.out.println("Server socket closed");
//                                     System.exit(0);
//                                 } catch (IOException e) {
//                                     System.err.println("Error closing server socket: " + e.getMessage());
//                                 }
//                             } else {
//                                 System.out.println("Cannot shutdown: " + allClients.size() + " clients are still connected.");
//                             }
//                         }
//                     }
//                 }
//             });
//             shutdownThread.setDaemon(true);
//             shutdownThread.start();

//             // Main server loop to accept clients
//             while (true) {
//                 try {
//                     Socket socket = ss.accept();
//                     System.out.println("Client connected from port: " + socket.getPort());

//                     DataInputStream input = new DataInputStream(socket.getInputStream());
//                     DataOutputStream output = new DataOutputStream(socket.getOutputStream());

//                     ClientHandler ch = new ClientHandler(socket, input, output);
//                     synchronized (allClients) {
//                         allClients.add(ch);
//                     }
//                     ch.start();
//                 } catch (IOException e) {
//                     if (ss.isClosed()) {
//                         System.out.println("Server socket closed, stopping accept loop.");
//                         break;
//                     }
//                     System.out.println("Error accepting connection: " + e.getMessage());
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         } finally {
//             if (ss != null && !ss.isClosed()) {
//                 try {
//                     ss.close();
//                     System.out.println("Server socket closed in finally block");
//                 } catch (IOException e) {
//                     System.err.println("Error closing server socket: " + e.getMessage());
//                 }
//             }
//         }
//     }

//     public static synchronized void removeClients(ClientHandler ch) {
//         allClients.remove(ch);
//         System.out.println("Total Active clients: " + allClients.size());
//     }
// }

// // Class to represent a response request
// class ResponseRequest {
//     ClientHandler handler;
//     int clientPort;

//     ResponseRequest(ClientHandler handler, int clientPort) {
//         this.handler = handler;
//         this.clientPort = clientPort;
//     }
// }

// class ClientHandler extends Thread {
//     private Socket clientSocket;
//     private DataInputStream dis;
//     private DataOutputStream dos;

//     ClientHandler(Socket cs, DataInputStream i, DataOutputStream o) {
//         this.clientSocket = cs;
//         this.dis = i;
//         this.dos = o;
//     }

//     // Method to send response to client
//     void sendResponse(String message) throws IOException {
//         dos.writeUTF(message);
//         dos.flush();
//     }

//     @Override
//     public void run() {
//         try {
//             while (true) {
//                 // Read client message
//                 String readClient = dis.readUTF();
//                 System.out.println("Client says at port " + clientSocket.getPort() + ": " + readClient);

//                 if (readClient.equalsIgnoreCase("stop")) {
//                     System.out.println("Client disconnected at port " + clientSocket.getPort());
//                     break;
//                 }

//                 // Add a response request to the queue
//                 server.responseQueue.put(new ResponseRequest(this, clientSocket.getPort()));
//             }
//         } catch (IOException e) {
//             System.out.println("Client at port " + clientSocket.getPort() + " disconnected abruptly: " + e.getMessage());
//         } catch (InterruptedException e) {
//             System.out.println("Interrupted while adding to response queue: " + e.getMessage());
//         } finally {
//             try {
//                 dis.close();
//                 dos.close();
//                 clientSocket.close();
//                 server.removeClients(this);
//             } catch (IOException e) {
//                 System.err.println("Error closing client resources: " + e.getMessage());
//             }
//         }
//     }
// }



import java.io.*;
import java.net.*;
import java.util.*;

public class server {
    private static final int PORT = 5000;
    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static int clientIdCounter = 1; 

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
                    System.out.println("New client connected: " + clientSocket + " (Client " + clientIdCounter + ")");

                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientIdCounter++, in, out);
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
    private static final String END_OF_MESSAGE = "<EOM>";

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
                if (line.equals(END_OF_MESSAGE)) {
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
                server.removeClient(this);
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
                if (input.equalsIgnoreCase("SHUTDOWN") && server.clients.isEmpty()) {
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
                        server.sendMessageToClient(clientId, "Server: " + message);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid client ID. Use: SEND <client_id> <message>");
                    }
                } else if (!server.clients.isEmpty()) {
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