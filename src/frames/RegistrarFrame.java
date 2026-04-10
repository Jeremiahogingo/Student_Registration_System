package frames;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RegistrarFrame extends JFrame {
    private String registrarName;

    public RegistrarFrame(String userId, String name) {
        this.registrarName = name;
        setTitle("Registrar Dashboard - " + name);
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
        JLabel statusLabel = new JLabel("Logged in as: " + name + " (Registrar)");
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
        tabs.addTab("Add Course", createAddCoursePanel());
        tabs.addTab("Cancel Course", createCancelCoursePanel());
        tabs.addTab("Check Conflicts", createConflictsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private JPanel createAddCoursePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = new JTextField(15);
        UITheme.styleTextField(txtId);
        JTextField txtTitle = new JTextField(30);
        UITheme.styleTextField(txtTitle);
        JTextField txtDesc = new JTextField(40);
        UITheme.styleTextField(txtDesc);
        JTextField txtCredits = new JTextField(5);
        UITheme.styleTextField(txtCredits);
        JTextField txtFee = new JTextField(10);
        UITheme.styleTextField(txtFee);
        JComboBox<String> cmbSemester = new JComboBox<>(new String[]{"Fall2026", "Spring2027"});
        UITheme.styleComboBox(cmbSemester);
        JComboBox<String> cmbDay = new JComboBox<>(new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday"});
        UITheme.styleComboBox(cmbDay);
        JTextField txtStart = new JTextField(8);
        UITheme.styleTextField(txtStart);
        JTextField txtEnd = new JTextField(8);
        UITheme.styleTextField(txtEnd);
        JTextField txtRoom = new JTextField(10);
        UITheme.styleTextField(txtRoom);
        JButton btnAdd = new JButton("Add Course");
        UITheme.styleButton(btnAdd);

        int row = 0;
        addField(panel, gbc, "Course ID:", txtId, row++);
        addField(panel, gbc, "Title:", txtTitle, row++);
        addField(panel, gbc, "Description:", txtDesc, row++);
        addField(panel, gbc, "Credits:", txtCredits, row++);
        addField(panel, gbc, "Fee:", txtFee, row++);
        addField(panel, gbc, "Semester:", cmbSemester, row++);
        addField(panel, gbc, "Day:", cmbDay, row++);
        addField(panel, gbc, "Start Time (HH:MM):", txtStart, row++);
        addField(panel, gbc, "End Time (HH:MM):", txtEnd, row++);
        addField(panel, gbc, "Room:", txtRoom, row++);
        gbc.gridx = 1; gbc.gridy = row;
        panel.add(btnAdd, gbc);

        btnAdd.addActionListener(e -> {
            // Check for duplicate course ID
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement psCheck = conn.prepareStatement("SELECT course_id FROM courses WHERE course_id=?")) {
                psCheck.setString(1, txtId.getText().trim());
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Course ID already exists. Cannot add duplicate.");
                    return;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error checking duplicate: " + ex.getMessage());
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                // Insert into courses
                String sqlCourse = "INSERT INTO courses (course_id, title, description, credits, fee, status) VALUES (?,?,?,?,?,?)";
                PreparedStatement psCourse = conn.prepareStatement(sqlCourse);
                psCourse.setString(1, txtId.getText());
                psCourse.setString(2, txtTitle.getText());
                psCourse.setString(3, txtDesc.getText());
                psCourse.setInt(4, Integer.parseInt(txtCredits.getText()));
                psCourse.setDouble(5, Double.parseDouble(txtFee.getText()));
                psCourse.setString(6, "Active");
                psCourse.executeUpdate();

                // Insert into course_sections
                String sqlSection = "INSERT INTO course_sections (course_id, semester, day_of_week, start_time, end_time, room) VALUES (?,?,?,?,?,?)";
                PreparedStatement psSection = conn.prepareStatement(sqlSection);
                psSection.setString(1, txtId.getText());
                psSection.setString(2, (String) cmbSemester.getSelectedItem());
                psSection.setString(3, (String) cmbDay.getSelectedItem());
                psSection.setString(4, txtStart.getText());
                psSection.setString(5, txtEnd.getText());
                psSection.setString(6, txtRoom.getText());
                psSection.executeUpdate();

                JOptionPane.showMessageDialog(this, "Course added successfully.");
                // Clear fields
                txtId.setText("");
                txtTitle.setText("");
                txtDesc.setText("");
                txtCredits.setText("");
                txtFee.setText("");
                txtStart.setText("");
                txtEnd.setText("");
                txtRoom.setText("");
            } catch (SQLException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.LABEL_FONT);
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private JPanel createCancelCoursePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 50));
        panel.setBackground(UITheme.PANEL_BG);
        JComboBox<String> cmbCourse = new JComboBox<>();
        UITheme.styleComboBox(cmbCourse);
        cmbCourse.setPreferredSize(new Dimension(400, 40));
        JButton btnCancel = new JButton("Cancel Course");
        UITheme.styleButton(btnCancel);
        btnCancel.setBackground(Color.RED);
        loadCourses(cmbCourse);
        btnCancel.addActionListener(e -> {
            String course = (String) cmbCourse.getSelectedItem();
            if (course == null) return;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE courses SET status='Cancelled' WHERE course_id=?")) {
                ps.setString(1, course.split(" - ")[0]);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Course cancelled.");
                loadCourses(cmbCourse);
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        panel.add(new JLabel("Select Course:"));
        panel.add(cmbCourse);
        panel.add(btnCancel);
        return panel;
    }

    private void loadCourses(JComboBox<String> cmb) {
        cmb.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT course_id, title FROM courses WHERE status != 'Cancelled'")) {
            while (rs.next()) {
                cmb.addItem(rs.getString("course_id") + " - " + rs.getString("title"));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private JPanel createConflictsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.PANEL_BG);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        top.setBackground(UITheme.PANEL_BG);
        JLabel lblSem = new JLabel("Semester:");
        lblSem.setFont(UITheme.LABEL_FONT);
        JComboBox<String> cmbSemester = new JComboBox<>(new String[]{"Fall2026", "Spring2027"});
        UITheme.styleComboBox(cmbSemester);
        JButton btnFind = new JButton("Find Conflicts");
        UITheme.styleButton(btnFind);
        JTable table = new JTable();
        UITheme.styleTable(table);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Course 1", "Course 2", "Conflict Type"}, 0);
        table.setModel(model);
        btnFind.addActionListener(e -> {
            model.setRowCount(0);
            String sem = (String) cmbSemester.getSelectedItem();
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT a.course_id AS c1, b.course_id AS c2, 'Time/Room overlap' AS conflict " +
                         "FROM course_sections a, course_sections b " +
                         "WHERE a.semester=b.semester AND a.course_id < b.course_id " +
                         "AND a.day_of_week=b.day_of_week AND a.room=b.room " +
                         "AND ((a.start_time < b.end_time AND a.end_time > b.start_time))")) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString("c1"), rs.getString("c2"), rs.getString("conflict")});
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        top.add(lblSem);
        top.add(cmbSemester);
        top.add(btnFind);
        panel.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1100, 600));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
}