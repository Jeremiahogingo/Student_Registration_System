package frames;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentFrame extends JFrame {
    private String studentId, studentName;

    public StudentFrame(String studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
        setTitle("Student Portal - " + studentName);
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
        JLabel statusLabel = new JLabel("Logged in as: " + studentName + " (Student)");
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
        tabs.addTab("Current Schedule", createSchedulePanel());
        tabs.addTab("Transcript", createTranscriptPanel());
        tabs.addTab("Bills", createBillsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.PANEL_BG);
        JTable table = new JTable();
        UITheme.styleTable(table);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Course ID", "Title", "Day", "Start", "End", "Room"}, 0);
        table.setModel(model);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT r.course_id, c.title, cs.day_of_week, cs.start_time, cs.end_time, cs.room " +
                     "FROM registrations r JOIN courses c ON r.course_id=c.course_id " +
                     "JOIN course_sections cs ON r.course_id=cs.course_id AND r.semester=cs.semester " +
                     "WHERE r.student_id=? AND r.semester=?")) {
            ps.setString(1, studentId);
            ps.setString(2, "Fall2026");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("course_id"),
                        rs.getString("title"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("room")
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTranscriptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.PANEL_BG);
        JTable table = new JTable();
        UITheme.styleTable(table);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Course", "Grade", "Semester", "Credits"}, 0);
        table.setModel(model);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT c.title, r.grade, r.semester, c.credits FROM registrations r JOIN courses c ON r.course_id=c.course_id WHERE r.student_id=? AND r.status='Approved'")) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4)});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBillsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.PANEL_BG);
        JTable table = new JTable();
        UITheme.styleTable(table);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Bill ID", "Semester", "Amount", "Paid Status", "Date"}, 0);
        table.setModel(model);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT bill_id, semester, amount, paid_status, generated_date FROM bills WHERE student_id=? ORDER BY generated_date DESC")) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("bill_id"), rs.getString("semester"), rs.getDouble("amount"), rs.getString("paid_status"), rs.getDate("generated_date")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}