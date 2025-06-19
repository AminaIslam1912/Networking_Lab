import pandas as pd
import matplotlib.pyplot as plt

# Load CSV files
tahoe_df = pd.read_csv("log_tahoe.csv")
reno_df = pd.read_csv("log_reno.csv")

plt.figure(figsize=(12, 7))

# Plot cwnd
plt.plot(tahoe_df["Round"], tahoe_df["cwnd"], label="TCP Tahoe - cwnd", marker='o', color='blue')
plt.plot(reno_df["Round"], reno_df["cwnd"], label="TCP Reno - cwnd", marker='s', color='green')

# Plot ssthresh (optional but recommended)
plt.plot(tahoe_df["Round"], tahoe_df["ssthresh"], label="TCP Tahoe - ssthresh", linestyle='--', color='purple')
plt.plot(reno_df["Round"], reno_df["ssthresh"], label="TCP Reno - ssthresh", linestyle='--', color='brown')

# Highlight points where ssthresh changes
tahoe_ssthresh_changes = tahoe_df[tahoe_df["ssthresh"].diff() != 0]
reno_ssthresh_changes = reno_df[reno_df["ssthresh"].diff() != 0]

plt.scatter(tahoe_ssthresh_changes["Round"], tahoe_ssthresh_changes["cwnd"], color='red', marker='x', label="Tahoe ssthresh change")
plt.scatter(reno_ssthresh_changes["Round"], reno_ssthresh_changes["cwnd"], color='orange', marker='x', label="Reno ssthresh change")

# Labels and grid
plt.title("TCP Tahoe vs TCP Reno - cwnd and ssthresh Over Time")
plt.xlabel("Round")
plt.ylabel("Congestion Window Size (cwnd)")
plt.legend()
plt.grid(True)

plt.savefig("tcp_tahoe_vs_reno_with_ssthresh.png")
plt.show()
