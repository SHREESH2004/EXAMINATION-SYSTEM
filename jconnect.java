

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class jconnect {
    public jconnect() {
    }

    public static void main(String[] args) {
        try {
            String url = "jdbc:mysql://localhost:3306/std";
            String username = "root";
            String password = "shreesh2004";
            Connection con = DriverManager.getConnection(url, username, password);
            System.out.println("Connected Succesfully");
            String query = "show tables";
            Statement stm = con.createStatement();
            stm.execute(query);
            System.out.println(stm);
            con.close();
        } catch (SQLException var7) {
            SQLException e = var7;
            throw new RuntimeException(e);
        }
    }

    public void readData() {
        try {
            String url = "jdbc:mysql://localhost:3306/std";
            String username = "root";
            String password = "shreesh2004";
            Connection con = DriverManager.getConnection(url, username, password);
            System.out.println("Connected Succesfully");
            String query = "select * from students";
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            System.out.println("Read sucessfully");

            while(rs.next()) {
                System.out.println("id=" + rs.getInt(1));
                System.out.println("name=" + rs.getString(2));
                System.out.println("branch=" + rs.getString(3));
            }

        } catch (SQLException var8) {
            SQLException e = var8;
            throw new RuntimeException(e);
        }
    }

    public void UpdateData() {
        try {
            String url = "jdbc:mysql://localhost:3306/std";
            String username = "root";
            String password = "shreesh2004";
            Connection con = DriverManager.getConnection(url, username, password);
            System.out.println("Connected Succesfully");
            String query = "UPDATE Students SET Room = ? WHERE Attendance < ?";
            PreparedStatement pstm = con.prepareStatement(query);
            pstm.setString(1, "DEBARED");
            pstm.setInt(2, 80);
            pstm.execute();
        } catch (SQLException var7) {
            SQLException e = var7;
            throw new RuntimeException(e);
        }
    }
}
