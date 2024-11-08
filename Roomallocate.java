import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;

public class Roomallocate {
    String[] branches = {"cse", "ece", "mech"};
    String[] displayNames = {"CSE", "ECE", "MECH"};

    public HashMap<Integer, String> getStudentsFromBranch(String branch) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/std";
        String username = "root";
        String password = "shreesh2004";
        HashMap<Integer, String> students = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT id, name FROM " + branch.toLowerCase())) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                students.put(id, name);
            }
        }

        return students;
    }

    public HashMap<Integer, String>[] createMixedStudentsMap() throws SQLException {
        HashMap<Integer, String>[] mixedStudentsMaps = new HashMap[4];
        HashMap<Integer, String> cseStudents = getStudentsFromBranch("CSE");
        HashMap<Integer, String> eceStudents = getStudentsFromBranch("ECE");
        HashMap<Integer, String> mechStudents = getStudentsFromBranch("MECH");

        for (int i = 0; i < mixedStudentsMaps.length; i++) {
            mixedStudentsMaps[i] = new HashMap<>();
        }

        HashSet<Integer> assignedStudents = new HashSet<>();
        fillMixedStudents(mixedStudentsMaps[0], cseStudents, eceStudents, 15, 15, assignedStudents);
        fillMixedStudents(mixedStudentsMaps[1], mechStudents, eceStudents, 15, 15, assignedStudents);
        fillMixedStudents(mixedStudentsMaps[2], cseStudents, mechStudents, 15, 15, assignedStudents);
        fillMixedStudents(mixedStudentsMaps[3], eceStudents, cseStudents, 15, 15, assignedStudents);

        return mixedStudentsMaps;
    }

    private void fillMixedStudents(HashMap<Integer, String> mixedMap, HashMap<Integer, String> firstGroup,
                                   HashMap<Integer, String> secondGroup, int firstCount, int secondCount,
                                   HashSet<Integer> assignedStudents) {
        int count = 0;

        for (Integer id : firstGroup.keySet()) {
            if (count < firstCount && !assignedStudents.contains(id)) {
                mixedMap.put(id, firstGroup.get(id));
                assignedStudents.add(id);
                count++;
            }
        }

        count = 0;

        for (Integer id : secondGroup.keySet()) {
            if (count < secondCount && !assignedStudents.contains(id)) {
                mixedMap.put(id, secondGroup.get(id));
                assignedStudents.add(id);
                count++;
            }
        }
    }

    public void saveRoomAllocationsToDatabase(HashMap<Integer, String>[] mixedStudentsMaps) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/std";
        String username = "root";
        String password = "shreesh2004";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String insertSQL = "INSERT INTO room (id, name, branch, room_no, seat_no) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                for (int room = 0; room < mixedStudentsMaps.length; room++) {
                    int row = 1, col = 1;

                    for (HashMap.Entry<Integer, String> entry : mixedStudentsMaps[room].entrySet()) {
                        int id = entry.getKey();
                        String name = entry.getValue();
                        String branch = branches[room % branches.length];
                        String seatNo = "R" + row + "C" + col;

                        pstmt.setInt(1, id);
                        pstmt.setString(2, name);
                        pstmt.setString(3, branch.toUpperCase());
                        pstmt.setInt(4, room + 1);
                        pstmt.setString(5, seatNo);

                        pstmt.addBatch();

                        col++;
                        if (col == 6) {
                            col = 1;
                            row++;
                        }
                    }

                    pstmt.executeBatch();
                }
            }
        }
    }

    public static void main(String[] args) {
        Roomallocate allocator = new Roomallocate();
        try {
            HashMap<Integer, String>[] mixedStudentsMaps = allocator.createMixedStudentsMap();
            allocator.saveRoomAllocationsToDatabase(mixedStudentsMaps);

            for (int room = 0; room < mixedStudentsMaps.length; room++) {
                System.out.println("Room " + (room + 1) + ":");
                int row = 1, col = 1;

                for (HashMap.Entry<Integer, String> entry : mixedStudentsMaps[room].entrySet()) {
                    System.out.println("ID: " + entry.getKey() + ", Name: " + entry.getValue() + " Seat: " +
                            "R" + row + "C" + col);
                    col++;
                    if (col == 6) {
                        col = 1;
                        row++;
                    }
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
