package finalkaj;

/* Server Side Code */

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket ss = null;
        try {
            
            int port = 5001;
            ss = new ServerSocket(port);
            System.out.println("Server started on port: " + ss.getLocalPort());
            System.out.println("Waiting for client connections...\n");

           

            while (true) {
                try {
                    Socket socket = ss.accept();
                    System.out.println("new client connected from port: " + socket.getPort());
                    
                  //  DataInputStream input = new DataInputStream(socket.getInputStream());
                  //  DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                  BufferedReader   input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                  PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                    
                    ClientHandler ch = new ClientHandler(socket, input, output);
                    ch.start();
                } catch (IOException e) {
                    System.out.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (ss != null && !ss.isClosed()) {
                try {
                    ss.close();
                    System.out.println("Server socket closed");
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }

}


class ClientHandler extends Thread {

    private Socket clientSocket;
    private BufferedReader br;
    private PrintWriter pw;
    private static final String detectEnd="<eom>";

    ClientHandler(Socket cs, BufferedReader i, PrintWriter o) {
        this.clientSocket = cs;
        this.br = i;
        this.pw = o;
    }

    public void run() {
      //  String clientString="";
       // String serverString = "Hello from server and client port is " + clientSocket.getPort();

        StringBuilder fullMsgClient=new StringBuilder();
        String l;


        try {
            while ((l=br.readLine())!=null) {
               // clientString = dis.readUTF();

                if (l.equalsIgnoreCase("exit")) {
                    System.out.println("Client [" + clientSocket.getPort() + "] disconnected.");
                    break;
                }

                if (l.equals(detectEnd)) {
                   // System.out.println(detectEnd);
                    if(fullMsgClient.length()>0){
                       // System.out.println("sese");
                       // String s=fullMsgClient.toString();
                       String msg=fullMsgClient.toString();
                        System.out.println("Client says at port number : " + clientSocket.getPort() + " " + msg);
                        pw.println("Server: Received -\n" + msg);
                        fullMsgClient.setLength(0);
                    }
                    else{
                        fullMsgClient.append(l).append("\n");
                     //   System.out.println("inside apped");
                    }
                }

                

                

                // response of server
              //  dos.writeUTF(serverString);

            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                br.close();
                pw.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }

    }
}


// package finalkaj;

// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class Server {

//     public static void main(String[] args) throws IOException {
//         ServerSocket ss = null;
//         try {
//             int port = 5001;
//             ss = new ServerSocket(port);
//             System.out.println("Server started on port: " + ss.getLocalPort());
//             System.out.println("Waiting for client connections...\n");

//             while (true) {
//                 try {
//                     Socket socket = ss.accept();
//                     System.out.println("New client connected from port: " + socket.getPort());

//                     BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                     PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

//                     ClientHandler ch = new ClientHandler(socket, input, output);
//                     ch.start();
//                 } catch (IOException e) {
//                     System.out.println("Error accepting connection: " + e.getMessage());
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         } finally {
//             if (ss != null && !ss.isClosed()) {
//                 try {
//                     ss.close();
//                     System.out.println("Server socket closed");
//                 } catch (IOException e) {
//                     System.err.println("Error closing server socket: " + e.getMessage());
//                 }
//             }
//         }
//     }
// }

// class ClientHandler extends Thread {

//     private Socket clientSocket;
//     private BufferedReader br;
//     private PrintWriter pw;
//     private static final String detectEnd = "<eom>";

//     ClientHandler(Socket cs, BufferedReader i, PrintWriter o) {
//         this.clientSocket = cs;
//         this.br = i;
//         this.pw = o;
//     }

//     public void run() {
//         StringBuilder fullMsgClient = new StringBuilder();
//         String line;

//         try {
//             while ((line = br.readLine()) != null) {
//                 if (line.equalsIgnoreCase("exit")) {
//                     System.out.println("Client [" + clientSocket.getPort() + "] disconnected.");
//                     break;
//                 }

//                 if (line.equals(detectEnd)) {
//                     if (fullMsgClient.length() > 0) {
//                         String message = fullMsgClient.toString();
//                         System.out.println("Client says at port number: " + clientSocket.getPort() + "\n" + message);
//                         // Send response to client
//                         pw.println("Server: Received -\n" + message);
//                         fullMsgClient.setLength(0);
//                     }
//                 } else {
//                     fullMsgClient.append(line).append("\n");
//                 }
//             }
//         } catch (IOException e) {
//             System.out.println("Error handling client: " + e.getMessage());
//         } finally {
//             try {
//                 br.close();
//                 pw.close();
//                 clientSocket.close();
//             } catch (IOException e) {
//                 System.out.println("Error closing resources: " + e.getMessage());
//             }
//         }
//     }
// }