
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.net.*;

public class client {
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("10.33.2.226", 5001);
        System.out.println("Client Connected at server Handshaking port " + s.

                getPort());

        System.out.println("Client’s communcation port " + s.getLocalPort());
        System.out.println("Client is Connected");
        System.out.println("Enter the messages that you want to send and send \"STOP\" to close the connection:");

        DataOutputStream output = new DataOutputStream(s.getOutputStream());
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in

        ));


        
        DataInputStream input = new DataInputStream(s.getInputStream());
        String strOfServer = "";

     

        String str = "";
        while (true) {
            str = read.readLine();
            if(str.equals("STOP")){
                break;
            }
            output.writeUTF(str);
            //addede this to listen to server
            strOfServer = input.readUTF();
            System.out.println("Server says : " + strOfServer);
        }

        output.close();
        read.close();
        s.close();
    }
}
