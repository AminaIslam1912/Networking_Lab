package task2;


import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    private static final String address = "10.33.26.195";   //localhost  10.33.26.195
    private static final int port = 5000;
    private static final int chunk = 500; 
    private static final double alpha = 0.125;
    private static final double beta = 0.25;
    private static final int windowSize = 4; 

    public static void main(String[] args) {
        try {
            System.out.println("Server is connected at port no: " + port);
            Socket socket = new Socket(address, port);
            System.out.println("Connected to server!");

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter file name to send: ");
            String fileName = console.readLine();
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("File not found: " + fileName);
                socket.close();
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[chunk];

            Map<Integer, byte[]> sentPackets = new HashMap<>();
            Map<Integer, Long> sendTimes = new HashMap<>();
            Map<Integer, Integer> dupACKCounter = new HashMap<>();

            int seq = 1; 
            int base = 1; 
            double estimatedRTT = 100;
            double devRTT = 0;
            double timeout = estimatedRTT;

            while (true) {
                while (seq < base + windowSize && fis.available() > 0) {
                    int read = fis.read(buffer);
                    if (read == -1) break;

                    byte[] packet = Arrays.copyOf(buffer, read);
                    sentPackets.put(seq, packet);

                    out.writeInt(seq);               
                    out.writeInt(read);             
                    out.write(packet);              
                    out.flush();

                    sendTimes.put(seq, System.currentTimeMillis());
                    System.out.println("Sending Packet " + seq + " with Seq# " + seq);
                    seq++;
                }

                socket.setSoTimeout((int) timeout);

                try {
                    int ack = in.readInt(); // Receive ACK
                    System.out.print("ACK " + ack + " received.");

                    if (ack >= base) {
                        long sampleRTT = System.currentTimeMillis() - sendTimes.get(ack);
                        estimatedRTT = (1 - alpha) * estimatedRTT + alpha * sampleRTT;
                        devRTT = (1 - beta) * devRTT + beta * Math.abs(sampleRTT - estimatedRTT);
                        timeout = estimatedRTT + 4 * devRTT;

                        System.out.printf(" RTT=%d ms. EstimatedRTT=%.1f ms, DevRTT=%.2f ms, Timeout=%.1f ms\n",
                                sampleRTT, estimatedRTT, devRTT, timeout);

                        
                        for (int i = base; i <= ack; i++) {
                            sentPackets.remove(i);
                            sendTimes.remove(i);
                            dupACKCounter.remove(i);
                        }
                        base = ack + 1; 
                    } else {
                        int count = dupACKCounter.getOrDefault(ack, 0) + 1;
                        dupACKCounter.put(ack, count);
                        System.out.println(" Received Duplicate ACK for Seq " + ack);

                        if (count == 3) {
                            System.out.println("- Packet " + (ack + 1) + " lost during transmission -");
                            System.out.println("Fast Retransmit Triggered for Packet " + (ack + 1));
                            if (sentPackets.containsKey(ack + 1)) {
                                out.writeInt(ack + 1);
                                out.writeInt(sentPackets.get(ack + 1).length);
                                out.write(sentPackets.get(ack + 1));
                                out.flush();
                                sendTimes.put(ack + 1, System.currentTimeMillis());
                                dupACKCounter.put(ack, 0); 
                            }
                        }
                    }

            
                    if (fis.available() == 0 && base == seq) {
                        break;
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout for packet " + base + ". Retransmitting...");
                    if (sentPackets.containsKey(base)) {
                        out.writeInt(base);
                        out.writeInt(sentPackets.get(base).length);
                        out.write(sentPackets.get(base));
                        out.flush();
                        sendTimes.put(base, System.currentTimeMillis());
                    }
                }
            }

            fis.close();
            in.close();
            out.close();
            socket.close();
            System.out.println("ACK " + (base - 1) + " received. All packets delivered successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}