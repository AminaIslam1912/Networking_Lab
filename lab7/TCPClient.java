package lab7;

// ===== TCPClient.java =====

import java.io.*;
import java.net.*;
import java.util.*;

// Client Side (Sender)
public class TCPClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        int cwnd = 1;
        int ssthresh = 8;
        int dupACKcount = 0;
        String lastACK = "";
        int pktIndex = 0;
        String mode = "TAHOE"; // or "RENO"

        System.out.println("== TCP " + mode + " Mode ==");

        for (int round = 1; round <= 10; round++) {
            System.out.println("Round " + round + ": cwnd = " + cwnd + ", ssthresh = " + ssthresh);

            List<String> sentPkts = new ArrayList<>();
            for (int i = 0; i < cwnd; i++) {
                sentPkts.add("pkt" + pktIndex++);
            }
            out.println(String.join(",", sentPkts));

            List<String> acks = new ArrayList<>();
            for (int i = 0; i < cwnd; i++) {
                String ack = in.readLine();
                System.out.println("Received: " + ack);
                acks.add(ack);
            }

            boolean dupACK = false;
            for (String ack : acks) {
                if (ack.equals(lastACK)) {
                    dupACKcount++;
                    if (dupACKcount == 3) {
                        System.out.println("==> 3 Duplicate ACKs: Fast Retransmit triggered.");
                        ssthresh = cwnd / 2;
                        if (mode.equals("RENO")) {
                            cwnd = ssthresh;
                            System.out.println("TCP RENO Fast Recovery: cwnd -> " + cwnd);
                        } else {
                            cwnd = 1;
                            System.out.println("TCP TAHOE Reset: cwnd -> 1");
                        }
                        dupACKcount = 0;
                        dupACK = true;
                        break;
                    }
                } else {
                    lastACK = ack;
                    dupACKcount = 1;
                }
            }
            if (dupACK) continue;

            if (cwnd < ssthresh) {
                cwnd *= 2;
                System.out.println("Slow Start: cwnd -> " + cwnd);
            } else {
                cwnd += 1;
                System.out.println("Congestion Avoidance: cwnd -> " + cwnd);
            }
        }
        out.println("END");
        socket.close();
    }
}

