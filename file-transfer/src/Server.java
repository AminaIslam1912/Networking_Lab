import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class Server {

    private static int port = 8080;
    private static HttpServer httpServer = null;

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.runServer();
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext downloadContext = httpServer.createContext("/download");
        HttpContext uploadContext = httpServer.createContext("/upload");

        HttpContext listContext = httpServer.createContext("/list-files");
        downloadContext.setHandler(new DownloadHandler());
        uploadContext.setHandler(new UploadHandler());

        listContext.setHandler(new ListFilesHandler());

        httpServer.setExecutor(Executors.newFixedThreadPool(10));
        httpServer.start();
        System.out.println("Server started at port number " + port);
    }

    public static class DownloadHandler implements HttpHandler {
        OutputStream outputStream;
        String response;

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if (!httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
                httpExchange.sendResponseHeaders(405, -1);
                return;
            }

            URI uri = httpExchange.getRequestURI();
            String query = uri.getRawQuery();

            if (query == null || !query.startsWith("filename=")) {
                httpExchange.sendResponseHeaders(400, -1);
                return;
            }

            String fileName = URLDecoder.decode(query.substring("filename=".length()), "UTF-8");
            File file = new File("data/" + fileName);

            if (!file.exists() || file.isDirectory()) {
                String response = "File Not Found";
                httpExchange.sendResponseHeaders(404, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

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

    public static class UploadHandler implements HttpHandler {
        String response;

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if (!httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                httpExchange.sendResponseHeaders(405, -1);
                return;
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Headers headers = httpExchange.getRequestHeaders();
            String fileName = "upload_" + timestamp;
            if (headers.containsKey("X-Filename")) {
                fileName = headers.getFirst("X-Filename");
                System.out.println("Using X-Filename: " + fileName);
            } else {
                fileName = "upload_" + timestamp;
                System.out.println("Using default filename: " + fileName);
            }

            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                fileName = "upload_" + timestamp + ".txt";
                System.out.println("Invalid X-Filename, using default: " + fileName);
            }

            InputStream inputStream = httpExchange.getRequestBody();
            File file = new File("data/" + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[8192];
            int bytesRead;
            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            fileOutputStream.close();
            inputStream.close();

            System.out.println("Received upload, total bytes: " + totalBytes);

            String responseMessage;
            int statusCode;

            if (totalBytes == 0) {
                responseMessage = "No file uploaded.";
                statusCode = 400;
                file.delete();
            } else {
                responseMessage = "File uploaded successfully as: " + fileName;
                statusCode = 200;
            }

            httpExchange.sendResponseHeaders(statusCode, responseMessage.length());
            OutputStream responseBody = httpExchange.getResponseBody();
            responseBody.write(responseMessage.getBytes());
            responseBody.close();
        }
    }

    public static class ListFilesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            File folder = new File("data");
            File[] listOfFiles = folder.listFiles();
            StringBuilder fileList = new StringBuilder();

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        fileList.append(file.getName()).append("\n");
                    }
                }
            }

            byte[] response = fileList.toString().getBytes();
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();

            System.out.println("List of files sent to client");
        }
    }
}