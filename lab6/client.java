import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    private static final String address="localhost";
    private static final int port = 5000;
    private static final int chunk = 500; // packet size
    private static final double alpha = 0.125;
    private static final double beta = 0.25;

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

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[chunk];

            Map<Integer, byte[]> sentPackets = new HashMap<>();
            Map<Integer, Long> sendTimes = new HashMap<>();
            Map<Integer, Integer> dupACKCounter = new HashMap<>();

            int seq = 1;
            double estimatedRTT = 100;
            double devRTT = 0;
            double timeout = estimatedRTT;

            int lastAck = 0;

            while (true) {
                if (!sentPackets.containsKey(seq)) {
                    int read = fis.read(buffer);
                    if (read == -1) break;

                    byte[] packet = Arrays.copyOf(buffer, read);
                    sentPackets.put(seq, packet);

                    out.writeInt(seq);               // send sequence number
                    out.writeInt(read);             // send length
                    out.write(packet);              // send data
                    out.flush();

                    sendTimes.put(seq, System.currentTimeMillis());

                    System.out.println("Sending Packet " + seq + " with Seq# " + seq);
                }

                socket.setSoTimeout((int) timeout);

                try {
                    int ack = in.readInt(); // receive ACK
                    System.out.println("ACK " + ack + " received.");

                    if (ack == lastAck) {
                        int count = dupACKCounter.getOrDefault(ack, 0) + 1;
                        dupACKCounter.put(ack, count);

                        if (count == 3) {
                            System.out.println("Received Duplicate ACK for Seq " + ack + " three times.");
                            System.out.println("Fast Retransmit Triggered for Packet " + (ack + 1));
                            out.writeInt(ack + 1);
                            out.writeInt(sentPackets.get(ack + 1).length);
                            out.write(sentPackets.get(ack + 1));
                            out.flush();
                            sendTimes.put(ack + 1, System.currentTimeMillis());
                            dupACKCounter.put(ack, 0); // reset
                        }
                    } else {
                        long sampleRTT = System.currentTimeMillis() - sendTimes.get(ack);
                        estimatedRTT = (1 - alpha) * estimatedRTT + alpha * sampleRTT;
                        devRTT = (1 - beta) * devRTT + beta * Math.abs(sampleRTT - estimatedRTT);
                        timeout = estimatedRTT + 4 * devRTT;

                        System.out.println("RTT = " + sampleRTT + "ms. EstimatedRTT = " + estimatedRTT +
                                "ms, Timeout = " + timeout + "ms");

                        lastAck = ack;
                        seq = ack + 1;
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout for packet " + seq + ". Retransmitting...");
                    out.writeInt(seq);
                    out.writeInt(sentPackets.get(seq).length);
                    out.write(sentPackets.get(seq));
                    out.flush();
                    sendTimes.put(seq, System.currentTimeMillis());
                }
            }

            fis.close();
            in.close();
            out.close();
            socket.close();
            System.out.println("File sent successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
