import java.awt.Component;
import java.awt.LayoutManager;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

public class s extends JFrame {
    private JLabel questionLabel;
    private JRadioButton[] options;
    private JButton nextButton;
    private ButtonGroup group;
    private int currentQuestionIndex = 0;
    int score = 0;
    private int quizDuration = 600; // in seconds (10 minutes)
    private JLabel timerLabel;
    private Timer quizTimer;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private String[][] questions = new String[][]{
            {"Question 1: What is the square root of 81?", "9", "7", "8", "6", "1"},
            {"Question 2: What is the largest mammal on Earth?", "Elephant", "Blue Whale", "Giraffe", "Hippopotamus", "2"}
    };

    static int id;

    public s() {
        this.setTitle("Online Examination");
        this.setSize(700, 1000);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(null);

        // Question label
        this.questionLabel = new JLabel();
        this.questionLabel.setBounds(10, 10, 680, 25);
        this.add(this.questionLabel);
        this.options = new JRadioButton[4];
        this.group = new ButtonGroup();
        for (int i = 0; i < 4; ++i) {
            this.options[i] = new JRadioButton();
            this.options[i].setBounds(10, 40 + i * 30, 680, 25);
            this.group.add(this.options[i]);
            this.add(this.options[i]);
        }
        this.nextButton = new JButton("Next");
        this.nextButton.setBounds(300, 200, 100, 25);
        this.add(this.nextButton);

        this.timerLabel = new JLabel("Time left: 10:00");
        this.timerLabel.setBounds(500, 10, 150, 25);
        this.add(this.timerLabel);

        this.displayQuestion();
        this.startTimer();
        this.nextButton.addActionListener(e -> {
            checkAnswer();
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.length) {
                displayQuestion();
            } else {
                showScore();
            }
        });
    }

    private void displayQuestion() {
        this.group.clearSelection();
        if (this.currentQuestionIndex < this.questions.length) {
            this.questionLabel.setText(this.questions[this.currentQuestionIndex][0]);
            for (int i = 0; i < 4; ++i) {
                this.options[i].setText(this.questions[this.currentQuestionIndex][i + 1]);
            }
        }
    }

    private void checkAnswer() {
        for (int i = 0; i < 4; i++) {
            if (this.options[i].isSelected()) {
                if (Integer.toString(i + 1).equals(this.questions[this.currentQuestionIndex][5])) {
                    this.score++;
                }
            }
        }
    }

    private void startTimer() {
        quizTimer = new Timer();
        quizTimer.scheduleAtFixedRate(new TimerTask() {
            int remainingTime = quizDuration;

            @Override
            public void run() {
                int minutes = remainingTime / 60;
                int seconds = remainingTime % 60;
                timerLabel.setText(String.format("Time left: %02d:%02d", minutes, seconds));
                remainingTime--;

                if (remainingTime < 0) {
                    quizTimer.cancel();
                    showScore();
                }
            }
        }, 0, 1000);
    }

    public void showScore() {
        try {
            String url = "jdbc:mysql://localhost:3306/std";
            String username = "root";
            String password = "shreesh2004";
            Connection con = DriverManager.getConnection(url, username, password);
            this.questionLabel.setText("Quiz finished!");
            Scanner sc = new Scanner(System.in);
            String query = "UPDATE students SET room = ? WHERE id = ?";
            System.out.println("Please enter your id: ");
            int id = sc.nextInt();

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, this.score);
            stmt.setInt(2, id);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Score updated successfully");
            } else {
                System.out.println("No student found with name: " + id);
            }
            stmt.close();
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.nextButton.setEnabled(false);
        for (JRadioButton option : this.options) {
            option.setEnabled(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new s().setVisible(true);
        });
    }
}
