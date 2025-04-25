// package farjana;

// import java.io.*;
// import java.net.*;

// public class client {
//     public static void main(String[] args) throws IOException {
//         Socket s = new Socket("localhost", 5001);
//         System.out.println("Client Connected at server Handshaking port " + s.getPort());

//         System.out.println("Client’s communcation port " + s.getLocalPort());
//         System.out.println("Client is Connected");
//         System.out.println("Enter the messages that you want to send and send \"Exit\" to close the connection:");

//         BufferedReader read = new BufferedReader(new InputStreamReader(System.in));


        
//         DataInputStream input = new DataInputStream(s.getInputStream());
//         DataOutputStream output = new DataOutputStream(s.getOutputStream());


        


//         String strOfServer = "";

     

//         String str = "";
//         while (true) {
//             str = read.readLine();
//             if(str.equalsIgnoreCase("stop")){
//                 output.writeUTF(str);
//                 break;
//             }
//             output.writeUTF(str);
//             //added this to listen to server
//             strOfServer = input.readUTF();
//             System.out.println("Server says : " + strOfServer);
//         }

//         output.close();
//         read.close();
//         s.close();
//     }
// }


package farjana;

// import java.io.*;
// import java.net.*;

// public class client {
//     public static void main(String[] args) {
//         Socket s = null;
//         DataInputStream input = null;
//         DataOutputStream output = null;
//         BufferedReader read = null;

//         try {
//             s = new Socket("localhost", 5001);
//             System.out.println("Client Connected at server Handshaking port " + s.getPort());
//             System.out.println("Client’s communication port " + s.getLocalPort());
//             System.out.println("Client is Connected");
//             System.out.println("Enter the messages that you want to send and send \"stop\" to close the connection:");

//             input = new DataInputStream(s.getInputStream());
//             output = new DataOutputStream(s.getOutputStream());
//             read = new BufferedReader(new InputStreamReader(System.in));

//             String str = "";
//             while (true) {
//                 str = read.readLine();
//                 output.writeUTF(str);
//                 if (str.equalsIgnoreCase("stop")) {
//                     break;
//                 }
//                 try {
//                     String strOfServer = input.readUTF();
//                     System.out.println("Server says: " + strOfServer);
//                 } catch (IOException e) {
//                     System.out.println("Server disconnected: " + e.getMessage());
//                     break;
//                 }
//             }
//         } catch (IOException e) {
//             System.err.println("Error connecting to server or communicating: " + e.getMessage());
//         } finally {
//             try {
//                 if (output != null) output.close();
//                 if (input != null) input.close();
//                 if (read != null) read.close();
//                 if (s != null && !s.isClosed()) s.close();
//                 System.out.println("Client resources closed");
//             } catch (IOException e) {
//                 System.err.println("Error closing client resources: " + e.getMessage());
//             }
//         }
//     }
// }



import java.io.*;
import java.net.*;

public class client {
    private static final String SERVER_ADDRESS = "localhost";//10.33.28.18
    private static final int SERVER_PORT = 5000;
    private static final String END_OF_MESSAGE = "<EOM>";

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
                        out.println(END_OF_MESSAGE);
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
                //System.out.println("hahahaha");
                System.out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Server disconnected.");
        }
    }
}