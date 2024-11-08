import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.time.LocalDateTime;

public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(8000), 0);
        server.createContext("/api/students", new StudentHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running on port 8000...");
    }

    static class StudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                String response = handleGetRequest(exchange);
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Method Not Allowed";
                exchange.sendResponseHeaders(405, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String handleGetRequest(HttpExchange exchange) {
            String studentName = exchange.getRequestURI().getPath().split("/")[3]; // Extract name from URL
            return checkStudentEligibility(studentName);
        }

        private String checkStudentEligibility(String studentName) {
            String url = "jdbc:mysql://localhost:3306/std";
            String username = "root";
            String password = "shreesh2004";
            StringBuilder response = new StringBuilder();

            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String queryDebarred = "SELECT * FROM students WHERE name = ? AND room = 'DEBARED'";
                PreparedStatement pstDebarred = conn.prepareStatement(queryDebarred);
                pstDebarred.setString(1, studentName);
                ResultSet resultSetDebarred = pstDebarred.executeQuery();

                if (resultSetDebarred.next()) {
                    response.append("\"status\": \"debarred\", \"message\": \"Student is debarred due to low attendance: ")
                            .append(studentName).append("\"}");
                } else {
                    String queryRoom = "SELECT room_no, seat_no FROM room WHERE id = (SELECT id FROM students WHERE name = ?)";
                    PreparedStatement pstRoom = conn.prepareStatement(queryRoom);
                    pstRoom.setString(1, studentName);
                    ResultSet resultSetRoom = pstRoom.executeQuery();

                    if (resultSetRoom.next()) {
                        String roomNo = resultSetRoom.getString("room_no");
                        String seatNo = resultSetRoom.getString("seat_no");

                        // Update start time
                        LocalDateTime now = LocalDateTime.now();
                        String updateQuery = "UPDATE students SET starttime = ? WHERE name = ?";
                        PreparedStatement pstUpdate = conn.prepareStatement(updateQuery);
                        pstUpdate.setString(1, String.valueOf(now));
                        pstUpdate.setString(2, studentName);
                        pstUpdate.executeUpdate();

                        response.append("{\"status\": \"allowed\", \"message\": \"Student is allowed to give exams.\", \"room_no\": \"")
                                .append(roomNo).append("\", \"seat_no\": \"").append(seatNo).append("\"}");
                    } else {
                        response.append("{\"status\": \"error\", \"message\": \"Room or seat not found for the student: ")
                                .append(studentName).append("\"}");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.append("{\"status\": \"error\", \"message\": \"Database error occurred.\"}");
            }
            return response.toString();
        }
    }
}
