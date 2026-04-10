package frames;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BursarFrame extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private String userName;

    public BursarFrame(String userId, String name) {
        this.userName = name;
        setTitle("Bursar Office - " + name);
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        UITheme.applyTheme();
        setLayout(new BorderLayout(10, 10));

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
        JLabel statusLabel = new JLabel("Logged in as: " + name + " (Bursar)");
        statusLabel.setFont(UITheme.TEXT_FONT);
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout");
        UITheme.styleButton(btnLogout);
        btnLogout.addActionListener(e -> logout());
        statusBar.add(btnLogout, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);

        // Top search panel
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        top.setBackground(UITheme.PANEL_BG);
        JLabel lblSem = new JLabel("Semester:");
        lblSem.setFont(UITheme.LABEL_FONT);
        JComboBox<String> cmbSemester = new JComboBox<>(new String[]{"Fall2026", "Spring2027"});
        UITheme.styleComboBox(cmbSemester);
        JLabel lblStudent = new JLabel("Student Number (optional):");
        lblStudent.setFont(UITheme.LABEL_FONT);
        JTextField txtStudentId = new JTextField(15);
        UITheme.styleTextField(txtStudentId);
        JButton btnSearch = new JButton("Search");
        UITheme.styleButton(btnSearch);
        top.add(lblSem);
        top.add(cmbSemester);
        top.add(lblStudent);
        top.add(txtStudentId);
        top.add(btnSearch);
        add(top, BorderLayout.NORTH);

        // Bill table
        tableModel = new DefaultTableModel(new String[]{"Bill ID", "Student Name", "Amount", "Paid Status", "Date"}, 0);
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1100, 600));
        add(scroll, BorderLayout.CENTER);

        // Print button
        JButton btnPrint = new JButton("PRINT SELECTED BILL");
        UITheme.styleButton(btnPrint);
        btnPrint.addActionListener(e -> printBill());
        add(btnPrint, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> {
            String sem = (String) cmbSemester.getSelectedItem();
            String sid = txtStudentId.getText().trim();
            loadBills(sem, sid);
        });
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void loadBills(String semester, String studentId) {
        tableModel.setRowCount(0);
        String sql = "SELECT b.bill_id, s.name, b.amount, b.paid_status, b.generated_date " +
                     "FROM bills b JOIN students s ON b.student_id=s.student_id WHERE b.semester=?";
        if (!studentId.isEmpty()) sql += " AND b.student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester);
            if (!studentId.isEmpty()) ps.setString(2, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("bill_id"),
                        rs.getString("name"),
                        rs.getDouble("amount"),
                        rs.getString("paid_status"),
                        rs.getDate("generated_date")
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void printBill() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a bill to print.");
            return;
        }
        int billId = (int) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);
        double amount = (double) tableModel.getValueAt(selectedRow, 2);
        String content = "=== BILL ===\nBill ID: " + billId + "\nStudent: " + studentName + "\nAmount: $" + amount + "\nDate: " + new java.util.Date();
        JTextArea printArea = new JTextArea(content);
        printArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        JOptionPane.showMessageDialog(this, new JScrollPane(printArea), "Print Preview", JOptionPane.INFORMATION_MESSAGE);
    }
}