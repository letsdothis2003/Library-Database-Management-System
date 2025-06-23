import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//USE THIS TO RUN THE ENTIRE PROGRAM
public class Login extends JFrame {

    private JTextField dbNameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // Constructor for our UI
    public Login() {
        setTitle("Database Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null); // Center the window on the screen
        setLayout(new BorderLayout(10, 10)); // Add some padding

        // Panel for input fields
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5)); // 2 rows, 2 columns, with gaps
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Inner padding

        JLabel dbNameLabel = new JLabel("Database Name:");
        dbNameField = new JTextField(20);
        dbNameField.setText("librarydb"); // Pre-fill with default for convenience
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        passwordField.setText("chyn331"); // Pre-fill with default for convenience

        inputPanel.add(dbNameLabel);
        inputPanel.add(dbNameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);

        // Panel for the login button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Login");
        buttonPanel.add(loginButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> attemptLogin());

        setVisible(true);
    }

   
    private void attemptLogin() {
        String dbName = dbNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String user = "root"; // Replace by actual root

     
        String url = "jdbc:mysql://localhost:3306/" + dbName;

        try {
            // Attempt to establish a connection to the database
            Connection connection = DriverManager.getConnection(url, user, password);
            connection.close(); // Close the connection if successful

            JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // If login is successful, hide the login window and launch the LibraryDB application
            this.dispose(); // Close the login window
            LibraryDB.main(null); // Call the main method of  LibraryDB class and call it a day

        } catch (SQLException ex) {
            // Display an error message if login fails
            JOptionPane.showMessageDialog(this,
                    "Login failed: " + ex.getMessage() + "\nPlease check database name and password.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }


     //RUN THIS IN VSCODE(You should see a small "run" and "debug buttons next to "public). You can also
     //run it the normal way by setting up configuratiuon through classpath. 
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create an instance of the  frame
        new Login();
    }
}