// import java.io.*;
// import java.net.*;

// public class Server {
//     public static void main(String[] args) throws IOException {
//         ServerSocket ss = new ServerSocket(5001);
//         System.out.println("Server started on port: " + ss.getLocalPort());

//         while (true) {
//             Socket s = ss.accept();
//             System.out.println("Client connected: " + s.getInetAddress());
//             new Thread(new ClientHandler(s)).start();
//         }
//     }
// }

// class ClientHandler implements Runnable {
//     private Socket clientSocket;

//     public ClientHandler(Socket socket) {
//         this.clientSocket = socket;
//     }

//     public void run() {
//         try (
//             DataInputStream input = new DataInputStream(clientSocket.getInputStream());
//             DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
//         ) {
//             String message;
//             while (true) {
//                 message = input.readUTF();  // receive message from client
//                 System.out.println("Received from " + clientSocket.getPort() + ": " + message);

//                 // respond to this specific client
//                 String response = "Hello client [" + clientSocket.getPort() + "], server received: " + message;
//                 output.writeUTF(response);
//                 output.flush();
//             }
//         } catch (IOException e) {
//             System.out.println("Client " + clientSocket.getPort() + " disconnected.");
//         } finally {
//             try {
//                 clientSocket.close();
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }








import java.io.*;
import java.net.*;
import java.util.*;



class ValidUser {
    private String pin;
    private int balance;

    public ValidUser(String pin, int balance) {
        this.pin = pin;
        this.balance = balance;
    }

    public String getPin() {
        return pin;
    }

    public synchronized int getBalance() {
        return balance;
    }

    public synchronized boolean withdraw(int amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
}


public class Server {
  
    public static Map<String, ValidUser> userDB = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws IOException {
        
        userDB.put("1234", new ValidUser("4321", 5000));
        userDB.put("5678", new ValidUser("8765", 3000));
        userDB.put("9999", new ValidUser("1111", 10000));

        

        ServerSocket ss = new ServerSocket(5001);
        System.out.println("Bank Server started on port " + ss.getLocalPort());

        while (true) {
            Socket clientSocket = ss.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }
}

// public class Server {
//     public static void main(String[] args) throws IOException {
//         ServerSocket ss = new ServerSocket(5001);
//         System.out.println("Server started on port: " + ss.getLocalPort());

//         while (true) {
//             Socket s = ss.accept();
//             System.out.println("Client connected: " + s.getInetAddress());
//             new Thread(new ClientHandler(s)).start();
//         }
//     }
// }



class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean isvalid = false;
    private String currentCard = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    public void run() {
        try {
            while (true) {
                String msg = input.readUTF();
                System.out.println("Received from client: " + msg);

                String[] parts = msg.split(":");
                String response = "";

                switch (parts[0]) {
                    case "AUTH":
                        if (parts.length == 3) {
                            String card = parts[1];
                            String pin = parts[2];
                            //System.out.println("here");

                            ValidUser user = Server.userDB.get(card);
                            if (user != null && user.getPin().equals(pin)) {
                                isvalid = true;
                                currentCard = card;
                                response = "AUTH_OK";
                            } else {
                                response = "AUTH_FAIL";
                            }
                        } else {
                            //System.out.println("inside");
                            response = "INVALID_AUTH_FORMAT";
                        }
                        break;

                    case "WITHDRAW":
                        if (!isvalid) {
                            response = "AUTH_REQUIRED";
                            //System.out.println("helloo");
                        } else {
                            int amount = Integer.parseInt(parts[1]);
                            ValidUser user = Server.userDB.get(currentCard);
                            if (user.withdraw(amount)) {
                                response = "WITHDRAW_OK";
                            } else {
                                response = "INSUFFICIENT_FUNDS";
                            }
                        }
                        break;

                    case "BALANCE_REQ":
                        if (!isvalid) {
                            response = "AUTH_REQUIRED";
                        } else {
                            ValidUser user = Server.userDB.get(currentCard);
                            response = "BALANCE_RES:" + user.getBalance();
                        }
                        break;

                    default:
                        response = "Give valid commands";
                }

                output.writeUTF(response);
                output.flush();
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getPort());
        }

       
    }
}
