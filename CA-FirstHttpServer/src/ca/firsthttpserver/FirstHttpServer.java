package ca.firsthttpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Lars Mortensen
 */
public class FirstHttpServer {

    static int port = 8080;
    static String ip = "127.0.0.1";
    static String contentFolder = "public/";

    public static void main(String[] args) throws Exception {
        if (args.length == 3) {
            port = Integer.parseInt(args[1]);
            ip = args[0];
            contentFolder = args[2];
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(ip, port), 0);
        server.createContext("/welcome", new RequestHandler());
        server.createContext("/headers", new RequestHandler2());
        server.createContext("/pages", new RequestHandler3());
        server.createContext("/Parameters", new RequestHandler4());
        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("Server started, listening on port: " + port);
    }

    static class RequestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            StringBuilder sb = new StringBuilder();
            String response = "Welcome to my very first almost home made Web Server :-)";
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>My fancy website</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");
            sb.append("<h2> Welcome to my very first home made web server :-*</h2>\n");
            sb.append("</body>\n");
            sb.append("</html>\n");
            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response); //What happens if we use a println instead of print --> Explain
            }
        }
    }

    static class RequestHandler2 implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>My fancy website</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");
            sb.append("<table border=\"1\">\n");
            sb.append("<tr><th>Header</th><th>Value</th></tr>\n");

            Headers heads = he.getRequestHeaders();
            for (Map.Entry<String, List<String>> entry : heads.entrySet()) {
                sb.append("<tr><td>" + entry.getKey() + "</td><td>" + entry.getValue() + "</td></tr>\n");
            }
            sb.append("</table>\n");
            sb.append("</body>\n");
            sb.append("</html>\n");

            String response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response); //What happens if we use a println instead of print --> Explain
            }

        }
    }

    static class RequestHandler3 implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String filepath = he.getRequestURI().toString();
            String path = contentFolder + filepath.replaceAll("/pages", "");
            File file = new File(path);
            byte[] bytesToSend = new byte[(int) file.length()];
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytesToSend, 0, bytesToSend.length);
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            String contentType = getMime(filepath);
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", contentType);
            he.sendResponseHeaders(200, bytesToSend.length);
            try (OutputStream os = he.getResponseBody()) {
                os.write(bytesToSend, 0, bytesToSend.length);
            }
        }

        private String getMime(String s) {
            String contentType;
            int dot = s.indexOf(".");
            String filetype = s.substring(dot + 1, s.length());
            switch (filetype) {
                case "jpg":
                case "gif":
                case "png":
                case "jpeg":
                case "bmp":
                    contentType = "image";
                    break;
                case "pdf":
                case "zip":
                    contentType = "application";
                    break;
                case "css":
                case "html":
                case "javascript":
                case "rtf":
                case "xml":
                    contentType = "text";
                    break;
                default:
                    contentType = "text";
            }
            return contentType + "/" + filetype;
        }
    }

    static class RequestHandler4 implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {

            StringBuilder sb = new StringBuilder();
            String response;
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title></title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");
            sb.append("<p>Method is:  \n" + he.getRequestMethod());
            sb.append("<p>Get-Parameters:  \n" + he.getRequestURI().getQuery());
            sb.append("</br>");
            Scanner scan = new Scanner(he.getRequestBody());
            while (scan.hasNext()) {
                sb.append("Request body, with Post-parameters: " + scan.nextLine());
                sb.append("</br>");
            }
            sb.append("</body>\n");
            sb.append("</html>\n");
            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response); //What happens if we use a println instead of print --> Explain
            }
        }
    }
}
