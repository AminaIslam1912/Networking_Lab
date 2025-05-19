import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class server {

    private static int port = 8080;
    private static HttpServer httpServer = null;

    public static void main(String[] args) {

        try {
            server server = new server();
            server.runServer();

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Error is");
            e.printStackTrace();
        }
    }

    private void runServer() throws IOException {
        // instance of HttpServer
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        // create context of download and upload
        HttpContext downloadContext = httpServer.createContext("/download");
        HttpContext uploadContext = httpServer.createContext("/upload");

        // assign handlers
        downloadContext.setHandler(new DownloadHandler());
        uploadContext.setHandler(new UploadHandler());

        // thread pool to handle multiple clients
        httpServer.setExecutor(Executors.newFixedThreadPool(10));

        // start the server
        httpServer.start();
        System.out.println("Server started at port number " + port);
    }

    // custom DownloadHandler Class (GET_METHOD)
    public static class DownloadHandler implements HttpHandler {

        // GET METHOD
        OutputStream outputStream;
        String response;

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            if (!httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
                httpExchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            URI uri = httpExchange.getRequestURI();
            String query = uri.getRawQuery();

            if (query == null || !query.startsWith("filename=")) {
                httpExchange.sendResponseHeaders(400, -1); // Bad Request
                return;
            }
            // String fileName = query.substring(query.indexOf("=") + 1);

            String fileName = URLDecoder.decode(query.substring("filename=".length()), "UTF-8");
            File file = new File(fileName);

            // check if the file exixts on the server disk
            if (!file.exists() || file.isDirectory()) {
                String response = "File Not Found";
                httpExchange.sendResponseHeaders(404, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // Set headers
            httpExchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
            httpExchange.getResponseHeaders().add("Content-Disposition",
                    "attachment; filename=\"" + file.getName() + "\"");

            httpExchange.sendResponseHeaders(200, file.length());
            OutputStream os = httpExchange.getResponseBody();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            fis.close();
            os.close();

            System.out.println("Queried for " + fileName);
        }
    }

    // custom UploadHandler Class
    public static class UploadHandler implements HttpHandler {

        // post request
        String response;

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            if (!httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                httpExchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            // Create a new file to save the uploaded data. Name it upload_<timestamp> or
            // use another naming convention.
            // Generate file name using timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Headers headers = httpExchange.getRequestHeaders();

            String fileName = "upload_" + timestamp;
            // OPTIONAL: Extract filename from a custom header or later parse multipart if
            // needed
            if (headers.containsKey("X-Filename")) {
                fileName = headers.getFirst("X-Filename"); // e.g., user sends this custom header
            } else {
                fileName = "upload_" + timestamp + ".txt";
            }

            InputStream inputStream = httpExchange.getRequestBody();
            File file = new File(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            // Read data in chunks from the input and write to the file output.
            byte[] buffer = new byte[8192];
            int bytesRead;
            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            fileOutputStream.close();
            inputStream.close();

            // After saving: Send HTTP 200 OK.
            // Respond with a confirmation message including the filename.
            String responseMessage;
            int statusCode;

            if (totalBytes == 0) {
                // No file content sent
                responseMessage = "No file uploaded.";
                statusCode = 400; // Bad Request
                file.delete(); // Remove the empty file
            } else {
                // File received
                responseMessage = "File uploaded successfully as: " + fileName;
                statusCode = 200;
            }

            httpExchange.sendResponseHeaders(statusCode, responseMessage.length());
            OutputStream responseBody = httpExchange.getResponseBody();
            responseBody.write(responseMessage.getBytes());
            responseBody.close();
        }
    }

}