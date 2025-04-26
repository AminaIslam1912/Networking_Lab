package lab4;




import java.io.*;
import java.net.*;

public class Client_14_36{
    private static final String SERVER_ADDRESS = "10.33.26.195"; // 10.33.28.18
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        BufferedReader br = null;
        BufferedReader keyboard = null;

        try {
            
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);

           
        dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            
            br = new BufferedReader(new InputStreamReader(dis));
            keyboard = new BufferedReader(new InputStreamReader(System.in));

            
            System.out.println("List of available files on the server:");
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("Enter the file name you want to download:")) {
                    System.out.println(); 
                    break; 
                }
                System.out.println(line); 
            }

            
            while (true) {
                System.out.println("Enter file name or 'EXIT':");
                String fileName = keyboard.readLine();

                if (fileName == null || fileName.equalsIgnoreCase("EXIT")) {
                    dos.writeBytes("EXIT\n");
                    dos.flush();
                    break;
                }

                
                dos.writeBytes(fileName + "\n");
                dos.flush();

                
                String response = br.readLine();
                if (response != null && response.trim().equals("File Found")) {
                    long fileSize = dis.readLong();
                    System.out.println("File size is " + fileSize);
                    System.out.println("File found. Starting download...");

                    FileOutputStream fos = new FileOutputStream("downloaded_" + fileName);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long remaining = fileSize;
                    while (remaining > 0
                            && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                    }

                    fos.close();
                    System.out.println("File downloaded successfully as: downloaded_" + fileName);
                } else if (response != null && response.trim().equals("File Not Found on Server.")) {
                    System.out.println("File Not Found on Server.");
                } else {
                    System.out.println("Unexpected server response: " + response);
                }
            }

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        } finally {
            try {
                if (dos != null) dos.close();
                if (br != null) br.close();
                if (dis != null) dis.close();
                if (keyboard != null) keyboard.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}