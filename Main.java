import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {
    static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    static Scanner scanner;

    public Main() {
    }

    public static void main(String[] args) {
        jconnect j = new jconnect();
        j.UpdateData();

        try {
            String url = "jdbc:mysql://localhost:3306/std";
            String username = "root";
            String password = "shreesh2004";
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            System.out.println("Enter the student's name:");
            String studentName = scanner.nextLine();

            String queryDebarred = "SELECT * FROM students WHERE name = ? AND room = 'DEBARED'";
            PreparedStatement pstDebarred = conn.prepareStatement(queryDebarred);
            pstDebarred.setString(1, studentName);
            ResultSet resultSetDebarred = pstDebarred.executeQuery();

            ResultSet resultSetRoom = null;
            PreparedStatement pstRoom = null;
            if (resultSetDebarred.next()) {
                System.out.println("Student is debarred due to low attendance: " + studentName);
            } else {
                System.out.println("Student is allowed to give exams. Your Quiz starts now");

                String queryRoom = "SELECT room_no, seat_no FROM room WHERE id = (SELECT id FROM students WHERE name = ?)";
                pstRoom = conn.prepareStatement(queryRoom);
                pstRoom.setString(1, studentName);
                resultSetRoom = pstRoom.executeQuery();

                if (resultSetRoom.next()) {
                    String roomNo = resultSetRoom.getString("room_no");
                    String seatNo = resultSetRoom.getString("seat_no");
                    System.out.println("Your allocated room number is: " + roomNo);
                    System.out.println("Your allocated seat number is: " + seatNo);
                } else {
                    System.out.println("Room or seat not found for the student: " + studentName);
                }

                LocalDateTime now = LocalDateTime.now();
                String updateQuery = "UPDATE students SET starttime = ? WHERE name = ?";
                PreparedStatement pstUpdate = conn.prepareStatement(updateQuery);
                pstUpdate.setString(1, String.valueOf(now));
                pstUpdate.setString(2, studentName);
                pstUpdate.execute();
                s examWindow = new s();
                examWindow.setVisible(true);
                examWindow.showScore();
            }

            resultSetDebarred.close();
            resultSetRoom.close();
            pstDebarred.close();
            pstRoom.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static {
        scanner = new Scanner(System.in);
    }
}
