import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_URL = "http://localhost:8080";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            while (true) {
                System.out.println("\nChoose an option (or type 'exit' to quit):");
                System.out.println("1. Upload file");
                System.out.println("2. Download file");
                System.out.println("3. List available files on server");
                System.out.print("Your choice: ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting client. Goodbye!");
                    break;
                }

                int choice;
                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter 1, 2, 3, or 'exit'.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        System.out.println("Enter the full path of the file to upload (e.g., D:\\path\\to\\file.txt):");
                        System.out.println("Note: This must be a file, not a directory.");
                        String filePath = scanner.nextLine();
                        uploadFile(filePath);
                        break;
                    case 2:
                        System.out.println("Enter the filename to download (e.g., example.txt):");
                        String filename = scanner.nextLine();
                        System.out.println("Enter the full save path including filename (e.g., D:\\path\\to\\save\\file.txt):");
                        System.out.println("Note: This must include the filename, not just a directory.");
                        String savePath = scanner.nextLine();
                        downloadFile(filename, savePath);
                        break;
                    case 3:
                        listFiles();
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 1, 2, 3, or 'exit'.");
                }
            }
        } finally {
            scanner.close();
        }
    }

    private static void uploadFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                System.out.println("Invalid file path: File does not exist or is not a file.");
                return;
            }

            URL url = new URL(SERVER_URL + "/upload");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("X-Filename", file.getName());

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = con.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            int responseCode = con.getResponseCode();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? con.getErrorStream() : con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                if (responseCode == 200) {
                    System.out.println(response.toString());
                } else if (responseCode == 400) {
                    System.out.println("Upload failed: " + response.toString());
                } else if (responseCode == 405) {
                    System.out.println("Upload failed: Method not allowed.");
                } else {
                    System.out.println("Upload failed with response code: " + responseCode);
                }
            }
        } catch (IOException e) {
            System.err.println("Error during upload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void downloadFile(String filename, String savePath) {
        try {
            
            if (filename == null || filename.trim().isEmpty()) {
                System.out.println("Invalid filename: Filename cannot be empty.");
                return;
            }

            
            File saveFile = new File(savePath);
            File parentDir = saveFile.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                System.out.println("Invalid save path: Cannot create parent directories.");
                return;
            }

            URL url = new URL(SERVER_URL + "/download?filename=" + URLEncoder.encode(filename, "UTF-8"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = con.getInputStream();
                     FileOutputStream fos = new FileOutputStream(savePath)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    System.out.println("File downloaded successfully to " + savePath);
                }
            } else {
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                if (responseCode == 404) {
                    System.out.println("File not found on server: " + response.toString());
                } else if (responseCode == 400) {
                    System.out.println("Download failed: Invalid request. " + response.toString());
                } else if (responseCode == 405) {
                    System.out.println("Download failed: Method not allowed.");
                } else {
                    System.out.println("Download failed with response code: " + responseCode);
                }
            }
        } catch (IOException e) {
            System.err.println("Error during download: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void listFiles() {
        try {
            URL url = new URL(SERVER_URL + "/list-files");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String line;
                    System.out.println("Available files on server:");
                    while ((line = in.readLine()) != null) {
                        System.out.println("- " + line);
                    }
                }
            } else {
                System.out.println("Failed to retrieve file list. Server responded with code: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("Error retrieving file list: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
  

