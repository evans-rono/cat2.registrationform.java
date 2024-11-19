import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

class RegistrationForm {
    private JFrame frame;
    private JTextField nameField, mobileField, addressField;
    private JRadioButton maleButton, femaleButton;
    private JComboBox<Integer> dayBox, monthBox, yearBox;
    private JCheckBox termsCheckBox;
    private JTable detailsTable;
    private DefaultTableModel tableModel;

    // Database connection details
    private final String DB_URL = "jdbc:mysql://localhost:3306/registration_db";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "";

    public RegistrationForm() {
        frame = new JFrame("Registration Form");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Left panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"Name", "Mobile", "Gender", "DOB", "Address"}, 0);
        detailsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(detailsTable);
        leftPanel.add(new JLabel("Registered Users", JLabel.CENTER), BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        frame.add(leftPanel, BorderLayout.WEST);

        // Right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JLabel("Registration Form", JLabel.CENTER));

        // Name input
        rightPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        rightPanel.add(nameField);

        // Mobile input
        rightPanel.add(new JLabel("Mobile:"));
        mobileField = new JTextField();
        rightPanel.add(mobileField);

        // Gender input
        rightPanel.add(new JLabel("Gender:"));
        ButtonGroup genderGroup = new ButtonGroup();
        maleButton = new JRadioButton("Male");
        femaleButton = new JRadioButton("Female");
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);
        JPanel genderPanel = new JPanel();
        genderPanel.add(maleButton);
        genderPanel.add(femaleButton);
        rightPanel.add(genderPanel);

        // DOB input
        rightPanel.add(new JLabel("Date of Birth:"));
        JPanel dobPanel = new JPanel();
        dayBox = new JComboBox<>();
        monthBox = new JComboBox<>();
        yearBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) dayBox.addItem(i);
        for (int i = 1; i <= 12; i++) monthBox.addItem(i);
        for (int i = 1900; i <= 2024; i++) yearBox.addItem(i);
        dobPanel.add(dayBox);
        dobPanel.add(monthBox);
        dobPanel.add(yearBox);
        rightPanel.add(dobPanel);

        // Address input
        rightPanel.add(new JLabel("Address:"));
        addressField = new JTextField();
        rightPanel.add(addressField);

        // Terms checkbox
        termsCheckBox = new JCheckBox("I accept the terms and conditions");
        rightPanel.add(termsCheckBox);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("Submit");
        JButton resetButton = new JButton("Reset");
        buttonPanel.add(submitButton);
        buttonPanel.add(resetButton);
        rightPanel.add(buttonPanel);

        frame.add(rightPanel, BorderLayout.CENTER);

        // Button actions
        submitButton.addActionListener(new SubmitButtonActionListener());
        resetButton.addActionListener(e -> resetForm());

        loadRegisteredDetails();

        frame.setVisible(true);
    }

    private void resetForm() {
        nameField.setText("");
        mobileField.setText("");
        maleButton.setSelected(false);
        femaleButton.setSelected(false);
        dayBox.setSelectedIndex(0);
        monthBox.setSelectedIndex(0);
        yearBox.setSelectedIndex(0);
        addressField.setText("");
        termsCheckBox.setSelected(false);
    }

    private void loadRegisteredDetails() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users")) {

            tableModel.setRowCount(0);
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getString("name"),
                        resultSet.getString("mobile"),
                        resultSet.getString("gender"),
                        resultSet.getString("dob"),
                        resultSet.getString("address")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class SubmitButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!termsCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(frame, "You must accept the terms and conditions!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = nameField.getText();
            String mobile = mobileField.getText();
            String gender = maleButton.isSelected() ? "Male" : femaleButton.isSelected() ? "Female" : "";
            int day = (int) dayBox.getSelectedItem();
            int month = (int) monthBox.getSelectedItem();
            int year = (int) yearBox.getSelectedItem();
            String dob = String.format("%04d-%02d-%02d", year, month, day);
            String address = addressField.getText();

            if (name.isEmpty() || mobile.isEmpty() || gender.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO users (name, mobile, gender, dob, address) VALUES (?, ?, ?, ?, ?)")) {

                statement.setString(1, name);
                statement.setString(2, mobile);
                statement.setString(3, gender);
                statement.setString(4, dob);
                statement.setString(5, address);
                statement.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRegisteredDetails();
                resetForm();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error saving details to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegistrationForm::new);
    }
}