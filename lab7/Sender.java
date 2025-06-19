

// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class Sender {
//     public static void main(String[] args) throws Exception {
//         String mode = args.length > 0 ? args[0].toUpperCase() : "TAHOE"; // TAHOE or RENO
//         int N = 10; // rounds
//         int cwnd = 1;
//         int ssthresh = 8;
//         int dupACKcount = 0;
//         String lastACK = "";
//         int pkt_in = 0;

//         Socket socket = new Socket("127.0.0.1", 5000);
//         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

//         System.out.println("== TCP " + mode + " Mode ==");

//         outer:
//         for (int round = 1; round <= N; round++) {
//             System.out.println("Round " + round + ": cwnd=" + cwnd + ", ssthresh=" + ssthresh);
//             List<String> pkts = new ArrayList<>();
//             for (int i = 0; i < cwnd; i++) pkts.add("pkt" + (pkt_in++));
//             System.out.println("Sent packets: " + String.join(",", pkts));
//             out.println(String.join(",", pkts));

//             boolean fastRetransmit = false;
//             for (int i = 0; i < pkts.size(); i++) {
//                 String ack = in.readLine();
//                 System.out.println("Received: " + ack);

//                 if (ack.equals(lastACK)) {
//                     dupACKcount++;
//                 } else {
//                     dupACKcount = 1;
//                     lastACK = ack;
//                 }

//                 if (dupACKcount == 3) {
//                     System.out.println("==> 3 Duplicate ACKs: Fast Retransmit triggered.");
//                     ssthresh = Math.max(cwnd / 2, 1);
//                     if (mode.equals("RENO")) {
//                         cwnd = ssthresh;
//                         System.out.println("TCP RENO Fast Recovery: cwnd -> " + cwnd);
//                     } else {
//                         cwnd = 1;
//                         System.out.println("TCP TAHOE Reset: cwnd -> 1");
//                     }
//                     fastRetransmit = true;
//                     dupACKcount = 0; 
                   
//                     continue outer;
//                 }
//             }

           
//             if (cwnd < ssthresh) {
//                 cwnd *= 2;
//                 System.out.println("Slow Start: cwnd -> " + cwnd);
//             } else {
//                 cwnd += 1;
//                 System.out.println("Congestion Avoidance: cwnd -> " + cwnd);
//             }
//         }
//         out.println("END");
//         socket.close();
//         System.out.println("Sender closed.");
//     }
// }


import java.io.*;
import java.net.*;
import java.util.*;

public class Sender {
    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0].toUpperCase() : "TAHOE"; // TAHOE or RENO
        int N = 30; // rounds
        int cwnd = 1;
        int ssthresh = 8;
        int dupACKcount = 0;
        String lastACK = "";
        int pkt_in = 0;

        Socket socket = new Socket("localhost", 5000);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("== TCP " + mode + " Mode ==");
        PrintWriter csvWriter = new PrintWriter(new FileWriter("log_" + mode.toLowerCase() + ".csv"));
        csvWriter.println("Round,cwnd,ssthresh,mode");

        outer:

        for (int round = 1; round <= N; round++) {
            csvWriter.println(round + "," + cwnd + "," + ssthresh + "," + mode);

            System.out.println("Round " + round + ": cwnd=" + cwnd + ", ssthresh=" + ssthresh);
            List<String> pkts = new ArrayList<>();
            for (int i = 0; i < cwnd; i++) pkts.add("pkt" + (pkt_in++));
            System.out.println("Sent packets: " + String.join(",", pkts));
            out.println(String.join(",", pkts));

            boolean fastRetransmit = false;
            for (int i = 0; i < pkts.size(); i++) {
                String ack = in.readLine();
                System.out.println("Received: " + ack);

                if (ack.equals(lastACK)) {
                    dupACKcount++;
                } else {
                    dupACKcount = 1;
                    lastACK = ack;
                }

                if (dupACKcount == 3) {
                    System.out.println("==> 3 Duplicate ACKs: Fast Retransmit triggered.");
                    ssthresh = Math.max(cwnd / 2, 1);
                    if (mode.equals("RENO")) {
                        cwnd = ssthresh;
                        System.out.println("TCP RENO Fast Recovery: cwnd -> " + cwnd);
                    } else {
                        cwnd = 1;
                        System.out.println("TCP TAHOE Reset: cwnd -> 1");
                    }
                    fastRetransmit = true;
                    dupACKcount = 0; 
                    continue outer;
                }
            }

            if (cwnd < ssthresh) {
                cwnd *= 2;
                System.out.println("Slow Start: cwnd -> " + cwnd);
            } else {
                cwnd += 1;
                System.out.println("Congestion Avoidance: cwnd -> " + cwnd);
            }
        }
        csvWriter.close();

        out.println("END");
        socket.close();
        System.out.println("Sender closed.");
    }
}