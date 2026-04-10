package frames;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtUserId;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JLabel lblStatus;

    public LoginFrame() {
        UITheme.applyTheme();
        setTitle("Student Registration System - Login");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User ID
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUser = new JLabel("User ID:");
        lblUser.setFont(UITheme.LABEL_FONT);
        add(lblUser, gbc);
        gbc.gridx = 1;
        txtUserId = new JTextField(20);
        UITheme.styleTextField(txtUserId);
        add(txtUserId, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.LABEL_FONT);
        add(lblPass, gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        UITheme.styleTextField(txtPassword);
        add(txtPassword, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(UITheme.LABEL_FONT);
        add(lblRole, gbc);
        gbc.gridx = 1;
        cmbRole = new JComboBox<>(new String[]{"Advisor", "Registrar", "Admissions", "Enrolment", "Schools", "Bursar"});
        UITheme.styleComboBox(cmbRole);
        add(cmbRole, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton btnLogin = new JButton("LOGIN");
        UITheme.styleButton(btnLogin);
        btnLogin.addActionListener(this::authenticate);
        add(btnLogin, gbc);

        // Exit button
        gbc.gridy = 4;
        JButton btnExit = new JButton("EXIT");
        UITheme.styleButton(btnExit);
        btnExit.setBackground(Color.RED);
        btnExit.addActionListener(e -> System.exit(0));
        add(btnExit, gbc);

        // Status label
        gbc.gridy = 5;
        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.TEXT_FONT);
        lblStatus.setForeground(Color.RED);
        add(lblStatus, gbc);
    }

    private void authenticate(java.awt.event.ActionEvent e) {
        String userId = txtUserId.getText().trim();
        String password = new String(txtPassword.getPassword());
        String role = (String) cmbRole.getSelectedItem();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT full_name FROM users WHERE user_id=? AND password_hash=? AND role=?")) {
            ps.setString(1, userId);
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("full_name");
                openDashboard(role, userId, name);
                dispose();
            } else {
                lblStatus.setText("Invalid credentials or role mismatch.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            lblStatus.setText("Database error: " + ex.getMessage());
        }
    }

    private void openDashboard(String role, String userId, String name) {
        switch (role) {
            case "Advisor": new AdvisorFrame(userId, name).setVisible(true); break;
            case "Registrar": new RegistrarFrame(userId, name).setVisible(true); break;
            case "Admissions": new AdmissionsFrame(userId, name).setVisible(true); break;
            case "Enrolment": new EnrolmentFrame(userId, name).setVisible(true); break;
            case "Schools": new SchoolsFrame(userId, name).setVisible(true); break;
            case "Bursar": new BursarFrame(userId, name).setVisible(true); break;
        }
    }
}