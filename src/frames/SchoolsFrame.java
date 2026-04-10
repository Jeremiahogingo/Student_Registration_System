package frames;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SchoolsFrame extends JFrame {
    private String userName;

    public SchoolsFrame(String userId, String name) {
        this.userName = name;
        setTitle("Schools Dashboard - " + name);
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
        JLabel statusLabel = new JLabel("Logged in as: " + name + " (Schools)");
        statusLabel.setFont(UITheme.TEXT_FONT);
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout");
        UITheme.styleButton(btnLogout);
        btnLogout.addActionListener(e -> logout());
        statusBar.add(btnLogout, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.LABEL_FONT);
        tabbedPane.setBackground(UITheme.PANEL_BG);
        tabbedPane.addTab("Enrolments", createEnrolmentsPanel());
        tabbedPane.addTab("Class List", createClassListPanel());
        tabbedPane.addTab("Transcript", createTranscriptPanel());
        tabbedPane.addTab("Student Info", createStudentInfoPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ---------- Connection to schools_dashboard ----------
    private Connection getSchoolsConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/student_registration";
        String user = "root";
        String password = "Jerrylion39";
        return DriverManager.getConnection(url, user, password);
    }
    // -------------------------------------------------------

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private JPanel createEnrolmentsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 30));
        panel.setBackground(UITheme.PANEL_BG);
        JLabel progLabel = new JLabel("Programme:");
        progLabel.setFont(UITheme.LABEL_FONT);
        JComboBox<String> cmbProgramme = new JComboBox<>(new String[]{"CS", "IT", "SE", "ECE", "ME"});
        UITheme.styleComboBox(cmbProgramme);
        JButton btnShow = new JButton("Show Enrolment Count");
        UITheme.styleButton(btnShow);
        JLabel lblCount = new JLabel("0");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblCount.setForeground(UITheme.PRIMARY);
        btnShow.addActionListener(e -> {
            String prog = (String) cmbProgramme.getSelectedItem();
            try (Connection conn = getSchoolsConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM students WHERE programme=?")) {
                ps.setString(1, prog);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) lblCount.setText(rs.getString(1));
            } catch (SQLException ex) { 
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(progLabel);
        panel.add(cmbProgramme);
        panel.add(btnShow);
        panel.add(new JLabel("Total Enrolled:"));
        panel.add(lblCount);
        return panel;
    }

    private JPanel createClassListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.PANEL_BG);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        top.setBackground(UITheme.PANEL_BG);
        JLabel lblCourse = new JLabel("Course ID:");
        lblCourse.setFont(UITheme.LABEL_FONT);
        JTextField txtCourseId = new JTextField(15);
        UITheme.styleTextField(txtCourseId);
        JButton btnList = new JButton("Generate Class List");
        UITheme.styleButton(btnList);
        JTable table = new JTable();
        UITheme.styleTable(table);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Student ID", "Name", "Programme"}, 0);
        table.setModel(model);
        btnList.addActionListener(e -> {
            String course = txtCourseId.getText().trim();
            model.setRowCount(0);
            try (Connection conn = getSchoolsConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT s.student_id, s.name, s.programme FROM registrations r JOIN students s ON r.student_id=s.student_id WHERE r.course_id=? AND r.semester=?")) {
                ps.setString(1, course);
                ps.setString(2, "Fall2026");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3)});
                }
                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(panel, "No students found for course " + course + " in Fall2026", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) { 
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        top.add(lblCourse);
        top.add(txtCourseId);
        top.add(btnList);
        panel.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1100, 600));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTranscriptPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.PANEL_BG);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        top.setBackground(UITheme.PANEL_BG);
        JLabel lblStudent = new JLabel("Student Number:");
        lblStudent.setFont(UITheme.LABEL_FONT);
        JTextField txtStudentId = new JTextField(15);
        UITheme.styleTextField(txtStudentId);
        JButton btnTranscript = new JButton("Show Transcript");
        UITheme.styleButton(btnTranscript);
        JTable table = new JTable();
        UITheme.styleTable(table);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Course", "Grade", "Semester", "Credits"}, 0);
        table.setModel(model);
        btnTranscript.addActionListener(e -> {
            String sid = txtStudentId.getText().trim();
            model.setRowCount(0);
            try (Connection conn = getSchoolsConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT c.title, r.grade, r.semester, c.credits FROM registrations r JOIN courses c ON r.course_id=c.course_id WHERE r.student_id=? AND r.status='Approved' AND r.grade IS NOT NULL")) {
                ps.setString(1, sid);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4)});
                }
                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(panel, "No transcript found for student " + sid, "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) { 
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        top.add(lblStudent);
        top.add(txtStudentId);
        top.add(btnTranscript);
        panel.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1100, 600));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStudentInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblSearch = new JLabel("Student Number:");
        lblSearch.setFont(UITheme.LABEL_FONT);
        JTextField txtSearch = new JTextField(20);
        UITheme.styleTextField(txtSearch);
        JButton btnSearch = new JButton("Search");
        UITheme.styleButton(btnSearch);
        JLabel lblName = new JLabel();
        lblName.setFont(UITheme.TEXT_FONT);
        JLabel lblEmail = new JLabel();
        lblEmail.setFont(UITheme.TEXT_FONT);
        JLabel lblPhone = new JLabel();
        lblPhone.setFont(UITheme.TEXT_FONT);
        JLabel lblAddress = new JLabel();
        lblAddress.setFont(UITheme.TEXT_FONT);
        JLabel lblProgramme = new JLabel();
        lblProgramme.setFont(UITheme.TEXT_FONT);
        btnSearch.addActionListener(e -> {
            String sid = txtSearch.getText().trim();
            try (Connection conn = getSchoolsConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT name, email, phone, address, programme FROM students WHERE student_id=?")) {
                ps.setString(1, sid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    lblName.setText("Name: " + rs.getString("name"));
                    lblEmail.setText("Email: " + rs.getString("email"));
                    lblPhone.setText("Phone: " + rs.getString("phone"));
                    lblAddress.setText("Address: " + rs.getString("address"));
                    lblProgramme.setText("Programme: " + rs.getString("programme"));
                } else {
                    lblName.setText("Student not found.");
                    lblEmail.setText("");
                    lblPhone.setText("");
                    lblAddress.setText("");
                    lblProgramme.setText("");
                }
            } catch (SQLException ex) { 
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblSearch, gbc);
        gbc.gridx = 1;
        panel.add(txtSearch, gbc);
        gbc.gridx = 2;
        panel.add(btnSearch, gbc);
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 3;
        panel.add(lblName, gbc);
        gbc.gridy = 2;
        panel.add(lblEmail, gbc);
        gbc.gridy = 3;
        panel.add(lblPhone, gbc);
        gbc.gridy = 4;
        panel.add(lblAddress, gbc);
        gbc.gridy = 5;
        panel.add(lblProgramme, gbc);
        return panel;
    }
}