package frames;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdmissionsFrame extends JFrame {
    private String userName;

    public AdmissionsFrame(String userId, String name) {
        this.userName = name;
        setTitle("Admissions - " + name);
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        UITheme.applyTheme();
        setLayout(new BorderLayout());

        // ========== MENU BAR ==========
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(UITheme.TEXT_FONT);

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setFont(UITheme.TEXT_FONT);
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(UITheme.TEXT_FONT);
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // ========== STATUS BAR WITH LOGOUT BUTTON ==========
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(240, 240, 240));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Logged in as: " + name + " (Admissions)");
        statusLabel.setFont(UITheme.TEXT_FONT);
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout");
        UITheme.styleButton(btnLogout);
        btnLogout.addActionListener(e -> logout());
        statusBar.add(btnLogout, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);

        // ========== MAIN FORM PANEL ==========
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = new JTextField(20);
        UITheme.styleTextField(txtId);
        JTextField txtName = new JTextField(20);
        UITheme.styleTextField(txtName);
        JTextField txtDob = new JTextField(20);
        UITheme.styleTextField(txtDob);
        JTextField txtEmail = new JTextField(20);
        UITheme.styleTextField(txtEmail);
        JTextField txtPhone = new JTextField(20);
        UITheme.styleTextField(txtPhone);
        JTextField txtAddress = new JTextField(20);
        UITheme.styleTextField(txtAddress);
        JComboBox<String> cmbProgramme = new JComboBox<>(new String[]{"CS", "IT", "SE", "ECE", "ME"});
        UITheme.styleComboBox(cmbProgramme);
        JTextField txtAdmission = new JTextField(20);
        UITheme.styleTextField(txtAdmission);
        JComboBox<String> cmbStanding = new JComboBox<>(new String[]{"Good", "Probation"});
        UITheme.styleComboBox(cmbStanding);
        JButton btnAdd = new JButton("ADD STUDENT");
        UITheme.styleButton(btnAdd);
        JLabel lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.TEXT_FONT);
        lblStatus.setForeground(UITheme.PRIMARY);

        int row = 0;
        addRow(formPanel, gbc, "Student Number:", txtId, row++);
        addRow(formPanel, gbc, "Full Name:", txtName, row++);
        addRow(formPanel, gbc, "Date of Birth (YYYY-MM-DD):", txtDob, row++);
        addRow(formPanel, gbc, "Email:", txtEmail, row++);
        addRow(formPanel, gbc, "Phone:", txtPhone, row++);
        addRow(formPanel, gbc, "Address:", txtAddress, row++);
        addRow(formPanel, gbc, "Programme:", cmbProgramme, row++);
        addRow(formPanel, gbc, "Admission Date (YYYY-MM-DD):", txtAdmission, row++);
        addRow(formPanel, gbc, "Standing:", cmbStanding, row++);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(btnAdd, gbc);
        gbc.gridy = row + 1;
        formPanel.add(lblStatus, gbc);

        add(formPanel, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            // Check for duplicate student ID
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement checkPs = conn.prepareStatement("SELECT student_id FROM students WHERE student_id=?")) {
                checkPs.setString(1, txtId.getText().trim());
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    lblStatus.setText("Error: Student ID already exists!");
                    return;
                }
            } catch (SQLException ex) {
                lblStatus.setText("DB error during duplicate check.");
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO students (student_id, name, dob, email, phone, address, programme, admission_date, standing) VALUES (?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1, txtId.getText());
                ps.setString(2, txtName.getText());
                ps.setDate(3, Date.valueOf(txtDob.getText()));
                ps.setString(4, txtEmail.getText());
                ps.setString(5, txtPhone.getText());
                ps.setString(6, txtAddress.getText());
                ps.setString(7, (String) cmbProgramme.getSelectedItem());
                ps.setDate(8, Date.valueOf(txtAdmission.getText()));
                ps.setString(9, (String) cmbStanding.getSelectedItem());
                ps.executeUpdate();
                lblStatus.setText("Student added successfully.");
                // Clear fields
                txtId.setText("");
                txtName.setText("");
                txtDob.setText("");
                txtEmail.setText("");
                txtPhone.setText("");
                txtAddress.setText("");
                txtAdmission.setText("");
            } catch (SQLException ex) {
                lblStatus.setText("Error: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                lblStatus.setText("Invalid date format. Use YYYY-MM-DD");
            }
        });
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int row) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }
}