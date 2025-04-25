package finalkaj;

import java.io.*;
import java.net.*;

public class Client {

    private static final String detectEnd = "<eom>";
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost", 5001);
        System.out.println("Client Connected at server Handshaking port " + s.getPort());

        System.out.println("Client’s communcation port " + s.getLocalPort());
        System.out.println("Client is Connected");
        System.out.println("Enter the messages that you want to send and send \"Exit\" to close the connection:");

         


        
        //DataInputStream input = new DataInputStream(s.getInputStream());
       // DataOutputStream output = new DataOutputStream(s.getOutputStream());


     PrintWriter  output = new PrintWriter(s.getOutputStream(), true);
      BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));

      BufferedReader read = new BufferedReader(new InputStreamReader(System.in));


        // Thread to read server responses
        BufferedReader finalInput = input;
        Thread responseThread = new Thread(() -> {
            try {
                String serverResponse;
                while ((serverResponse = finalInput.readLine()) != null) {
                    System.out.println(serverResponse);
                }
            } catch (IOException e) {
                System.out.println("Server disconnected.");
            }
        });
        responseThread.start();


      StringBuilder msgToSent=new StringBuilder();
      String l;




       // String strOfServer = "";

     

       // String str = "";
        while ((l=read.readLine())!=null) {
           
            if(l.equalsIgnoreCase("exit")){
                output.println("exit");
                break;
            }

            else if(l.equalsIgnoreCase("SEND")){
                if(msgToSent.length()>0){
                    
                    output.println(msgToSent.toString());
                  //  System.out.println("inside");
                    output.println(detectEnd);
                 //   System.out.println(detectEnd);
                    msgToSent.setLength(0);
                }
            }
            else{
                msgToSent.append(l).append("\n");
               
            }
           // output.writeUTF(str);
            //added this to listen to server
           // strOfServer = input.readUTF();
           // System.out.println("Server says : " + strOfServer);
        }

        output.close();
        read.close();
        s.close();
    }
}



// package finalkaj;

// import java.io.*;
// import java.net.*;

// public class Client {

//     private static final String detectEnd = "<eom>";

//     public static void main(String[] args) {
//         Socket s = null;
//         PrintWriter output = null;
//         BufferedReader input = null;
//         BufferedReader read = null;

//         try {
//             s = new Socket("localhost", 5001);
//             System.out.println("Client Connected at server Handshaking port " + s.getPort());
//             System.out.println("Client’s communication port " + s.getLocalPort());
//             System.out.println("Client is Connected");
//             System.out.println("Enter the messages that you want to send, type 'SEND' to send, and 'exit' to close the connection:");

//             output = new PrintWriter(s.getOutputStream(), true);
//             input = new BufferedReader(new InputStreamReader(s.getInputStream()));
//             read = new BufferedReader(new InputStreamReader(System.in));

//             // Thread to read server responses
//             BufferedReader finalInput = input;
//             Thread responseThread = new Thread(() -> {
//                 try {
//                     String serverResponse;
//                     while ((serverResponse = finalInput.readLine()) != null) {
//                         System.out.println(serverResponse);
//                     }
//                 } catch (IOException e) {
//                     System.out.println("Server disconnected.");
//                 }
//             });
//             responseThread.start();

//             StringBuilder msgToSent = new StringBuilder();
//             String line;

//             while ((line = read.readLine()) != null) {
//                 if (line.equalsIgnoreCase("exit")) {
//                     output.println("exit");
//                     break;
//                 } else if (line.equalsIgnoreCase("SEND")) {
//                     if (msgToSent.length() > 0) {
//                         output.println(msgToSent.toString());
//                         output.println(detectEnd);
//                         msgToSent.setLength(0);
//                     }
//                 } else {
//                     msgToSent.append(line).append("\n");
//                 }
//             }
//         } catch (IOException e) {
//             System.out.println("Error: " + e.getMessage());
//         } finally {
//             try {
//                 if (output != null) output.close();
//                 if (input != null) input.close();
//                 if (read != null) read.close();
//                 if (s != null) s.close();
//             } catch (IOException e) {
//                 System.out.println("Error closing resources: " + e.getMessage());
//             }
//         }
//     }
// }