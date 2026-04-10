package frames;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EnrolmentFrame extends JFrame {
    private String userName;

    public EnrolmentFrame(String userId, String name) {
        this.userName = name;
        setTitle("Enrolment Department - " + name);
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        UITheme.applyTheme();
        setLayout(new BorderLayout());

        // Menu bar
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

        // Status bar with logout button
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(240, 240, 240));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Logged in as: " + name + " (Enrolment)");
        statusLabel.setFont(UITheme.TEXT_FONT);
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout");
        UITheme.styleButton(btnLogout);
        btnLogout.addActionListener(e -> logout());
        statusBar.add(btnLogout, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.LABEL_FONT);
        tabs.setBackground(UITheme.PANEL_BG);
        tabs.addTab("Dropout Report", createDropoutPanel());
        tabs.addTab("Update Student Info", createUpdatePanel());
        tabs.addTab("Graduation Check", createGraduationPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private JPanel createDropoutPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.PANEL_BG);
        JButton btnGenerate = new JButton("Generate Dropout Report");
        UITheme.styleButton(btnGenerate);
        JTable table = new JTable();
        UITheme.styleTable(table);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Student ID", "Name", "Last Semester"}, 0);
        table.setModel(model);
        btnGenerate.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT s.student_id, s.name, MAX(r.semester) as last_sem " +
                         "FROM students s LEFT JOIN registrations r ON s.student_id=r.student_id " +
                         "GROUP BY s.student_id " +
                         "HAVING last_sem IS NULL OR last_sem < 'Spring2026'")) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString("student_id"), rs.getString("name"), rs.getString("last_sem")});
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        panel.add(btnGenerate, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1100, 600));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtSearch = new JTextField(15);
        UITheme.styleTextField(txtSearch);
        JButton btnLoad = new JButton("Load");
        UITheme.styleButton(btnLoad);
        JTextField txtName = new JTextField(20);
        UITheme.styleTextField(txtName);
        JTextField txtEmail = new JTextField(20);
        UITheme.styleTextField(txtEmail);
        JTextField txtPhone = new JTextField(20);
        UITheme.styleTextField(txtPhone);
        JTextField txtAddress = new JTextField(20);
        UITheme.styleTextField(txtAddress);
        JComboBox<String> cmbProgramme = new JComboBox<>(new String[]{"CS", "IT", "SE", "ECE", "ME"});
        UITheme.styleComboBox(cmbProgramme);
        JComboBox<String> cmbStanding = new JComboBox<>(new String[]{"Good", "Probation"});
        UITheme.styleComboBox(cmbStanding);
        JButton btnUpdate = new JButton("UPDATE");
        UITheme.styleButton(btnUpdate);
        btnUpdate.setBackground(new Color(0, 150, 0));
        JLabel lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.TEXT_FONT);

        int row = 0;
        addRow(panel, gbc, "Student Number:", txtSearch, row); 
        gbc.gridx = 2; panel.add(btnLoad, gbc); row++;
        addRow(panel, gbc, "Name:", txtName, row++);
        addRow(panel, gbc, "Email:", txtEmail, row++);
        addRow(panel, gbc, "Phone:", txtPhone, row++);
        addRow(panel, gbc, "Address:", txtAddress, row++);
        addRow(panel, gbc, "Programme:", cmbProgramme, row++);
        addRow(panel, gbc, "Standing:", cmbStanding, row++);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        panel.add(btnUpdate, gbc);
        gbc.gridy = row + 1;
        panel.add(lblStatus, gbc);

        btnLoad.addActionListener(e -> {
            String sid = txtSearch.getText().trim();
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT name, email, phone, address, programme, standing FROM students WHERE student_id=?")) {
                ps.setString(1, sid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtName.setText(rs.getString("name"));
                    txtEmail.setText(rs.getString("email"));
                    txtPhone.setText(rs.getString("phone"));
                    txtAddress.setText(rs.getString("address"));
                    cmbProgramme.setSelectedItem(rs.getString("programme"));
                    cmbStanding.setSelectedItem(rs.getString("standing"));
                } else {
                    lblStatus.setText("Student not found.");
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        btnUpdate.addActionListener(e -> {
            String sid = txtSearch.getText().trim();
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE students SET name=?, email=?, phone=?, address=?, programme=?, standing=? WHERE student_id=?")) {
                ps.setString(1, txtName.getText());
                ps.setString(2, txtEmail.getText());
                ps.setString(3, txtPhone.getText());
                ps.setString(4, txtAddress.getText());
                ps.setString(5, (String) cmbProgramme.getSelectedItem());
                ps.setString(6, (String) cmbStanding.getSelectedItem());
                ps.setString(7, sid);
                int rows = ps.executeUpdate();
                if (rows > 0) lblStatus.setText("Updated successfully.");
                else lblStatus.setText("Student not found.");
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int row) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = row; panel.add(lbl, gbc);
        gbc.gridx = 1; panel.add(comp, gbc);
    }

    private JPanel createGraduationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.PANEL_BG);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        top.setBackground(UITheme.PANEL_BG);
        JLabel lblStudent = new JLabel("Student Number:");
        lblStudent.setFont(UITheme.LABEL_FONT);
        JTextField txtStudentId = new JTextField(15);
        UITheme.styleTextField(txtStudentId);
        JButton btnCheck = new JButton("Check Graduation");
        UITheme.styleButton(btnCheck);
        JTextArea taResult = new JTextArea(15, 50);
        taResult.setEditable(false);
        taResult.setFont(UITheme.TEXT_FONT);
        btnCheck.addActionListener(e -> {
            String sid = txtStudentId.getText().trim();
            taResult.setText("");
            try (Connection conn = DBConnection.getConnection()) {
                // Get student's programme
                String progSql = "SELECT programme FROM students WHERE student_id=?";
                PreparedStatement psProg = conn.prepareStatement(progSql);
                psProg.setString(1, sid);
                ResultSet rsProg = psProg.executeQuery();
                if (!rsProg.next()) {
                    taResult.setText("Student not found.");
                    return;
                }
                String programme = rsProg.getString("programme");

                // Get required credits from programmes table
                String reqSql = "SELECT required_credits FROM programmes WHERE programme_code=?";
                PreparedStatement psReq = conn.prepareStatement(reqSql);
                psReq.setString(1, programme);
                ResultSet rsReq = psReq.executeQuery();
                int required = 120; // default
                if (rsReq.next()) {
                    required = rsReq.getInt("required_credits");
                }

                // Calculate earned credits from passed courses (grade A-D)
                String creditsSql = "SELECT SUM(c.credits) FROM registrations r JOIN courses c ON r.course_id=c.course_id WHERE r.student_id=? AND r.grade IN ('A','B','C','D')";
                PreparedStatement psCred = conn.prepareStatement(creditsSql);
                psCred.setString(1, sid);
                ResultSet rsCred = psCred.executeQuery();
                rsCred.next();
                int earned = rsCred.getInt(1);
                if (earned >= required) {
                    taResult.append("Eligible for graduation.\n");
                    taResult.append("Programme: " + programme + "\n");
                    taResult.append("Required credits: " + required + "\n");
                    taResult.append("Earned credits: " + earned + "\n");
                } else {
                    taResult.append("NOT eligible for graduation.\n");
                    taResult.append("Programme: " + programme + "\n");
                    taResult.append("Required credits: " + required + "\n");
                    taResult.append("Earned credits: " + earned + "\n");
                    taResult.append("Need " + (required - earned) + " more credits.\n");
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        top.add(lblStudent);
        top.add(txtStudentId);
        top.add(btnCheck);
        panel.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(taResult);
        scroll.setPreferredSize(new Dimension(1100, 500));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
}