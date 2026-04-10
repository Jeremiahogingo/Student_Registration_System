package frames;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class AdvisorFrame extends JFrame {
    private JTextField txtStudentId, txtCourseId;
    private JTextArea taCourseDesc;
    private JLabel lblEligibility, lblSchedule;
    private JTable tblCurrentReg;
    private DefaultTableModel tableModel;
    private String advisorId, advisorName;

    public AdvisorFrame(String advisorId, String advisorName) {
        this.advisorId = advisorId;
        this.advisorName = advisorName;
        setTitle("Advisor Dashboard - " + advisorName);
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        UITheme.applyTheme();
        setLayout(new BorderLayout(10, 10));

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

        // ========== TOP PANEL (Student + Course + Lookup + Refresh) ==========
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        topPanel.setBackground(UITheme.PANEL_BG);
        
        JLabel lblStudent = new JLabel("Student Number:");
        lblStudent.setFont(UITheme.LABEL_FONT);
        topPanel.add(lblStudent);
        txtStudentId = new JTextField(15);
        UITheme.styleTextField(txtStudentId);
        topPanel.add(txtStudentId);
        
        // Refresh button for current registrations
        JButton btnRefresh = new JButton("Refresh Registrations");
        UITheme.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadCurrentRegistrations());
        topPanel.add(btnRefresh);
        
        JLabel lblCourse = new JLabel("Course ID:");
        lblCourse.setFont(UITheme.LABEL_FONT);
        topPanel.add(lblCourse);
        txtCourseId = new JTextField(10);
        UITheme.styleTextField(txtCourseId);
        topPanel.add(txtCourseId);
        
        JButton btnLookup = new JButton("Lookup Course");
        UITheme.styleButton(btnLookup);
        btnLookup.addActionListener(e -> lookupCourse());
        topPanel.add(btnLookup);
        
        add(topPanel, BorderLayout.NORTH);

        // ========== CENTER PANEL (eligibility checks) ==========
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(UITheme.PANEL_BG);
        UITheme.styleTitledPanel(centerPanel, "Course Approval Checks");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblDesc = new JLabel("Course Description:");
        lblDesc.setFont(UITheme.LABEL_FONT);
        centerPanel.add(lblDesc, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        taCourseDesc = new JTextArea(5, 40);
        taCourseDesc.setEditable(false);
        taCourseDesc.setFont(UITheme.TEXT_FONT);
        JScrollPane descScroll = new JScrollPane(taCourseDesc);
        descScroll.setPreferredSize(new Dimension(600, 120));
        centerPanel.add(descScroll, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JButton btnEligibility = new JButton("Check Eligibility & Pre-reqs");
        UITheme.styleButton(btnEligibility);
        btnEligibility.addActionListener(e -> checkEligibility());
        centerPanel.add(btnEligibility, gbc);
        gbc.gridx = 1;
        lblEligibility = new JLabel("Not checked");
        lblEligibility.setFont(UITheme.TEXT_FONT);
        centerPanel.add(lblEligibility, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JButton btnSchedule = new JButton("Check Schedule Conflicts");
        UITheme.styleButton(btnSchedule);
        btnSchedule.addActionListener(e -> checkSchedule());
        centerPanel.add(btnSchedule, gbc);
        gbc.gridx = 1;
        lblSchedule = new JLabel("Not checked");
        lblSchedule.setFont(UITheme.TEXT_FONT);
        centerPanel.add(lblSchedule, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // ========== BOTTOM AREA (table + save button + status bar) ==========
        JPanel bottomArea = new JPanel(new BorderLayout(10, 10));
        bottomArea.setBackground(UITheme.PANEL_BG);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(UITheme.PANEL_BG);
        UITheme.styleTitledPanel(tablePanel, "Current Registrations");
        tableModel = new DefaultTableModel(new String[]{"Course ID", "Title", "Day", "Time", "Status"}, 0);
        tblCurrentReg = new JTable(tableModel);
        UITheme.styleTable(tblCurrentReg);
        JScrollPane tableScroll = new JScrollPane(tblCurrentReg);
        tableScroll.setPreferredSize(new Dimension(1100, 250));
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        JButton btnSave = new JButton("SAVE REGISTRATION");
        UITheme.styleButton(btnSave);
        btnSave.addActionListener(e -> saveRegistration());
        tablePanel.add(btnSave, BorderLayout.SOUTH);

        bottomArea.add(tablePanel, BorderLayout.CENTER);

        // Status bar (with logout button)
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(240, 240, 240));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Logged in as: " + advisorName + " (Advisor)");
        statusLabel.setFont(UITheme.TEXT_FONT);
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout");
        UITheme.styleButton(btnLogout);
        btnLogout.addActionListener(e -> logout());
        statusBar.add(btnLogout, BorderLayout.EAST);
        
        bottomArea.add(statusBar, BorderLayout.SOUTH);

        add(bottomArea, BorderLayout.SOUTH);

        // Load current registrations when student ID is entered and focus lost
        txtStudentId.addActionListener(e -> loadCurrentRegistrations());
        txtStudentId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                loadCurrentRegistrations();
            }
        });
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // ========== REGISTRATION PERIOD CHECK ==========
    private boolean isRegistrationOpen(String semester) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM registration_periods WHERE semester=? AND start_date <= CURDATE() AND end_date >= CURDATE() AND is_active=TRUE")) {
            ps.setString(1, semester);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("isRegistrationOpen error: " + e.getMessage());
            return false;
        }
    }

    private void lookupCourse() {
        String courseId = txtCourseId.getText().trim();
        if (courseId.isEmpty()) {
            taCourseDesc.setText("Please enter a Course ID.");
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT description FROM courses WHERE course_id=?")) {
            ps.setString(1, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String desc = rs.getString("description");
                if (desc == null || desc.trim().isEmpty()) {
                    taCourseDesc.setText("No description available for this course.");
                } else {
                    taCourseDesc.setText(desc);
                }
            } else {
                taCourseDesc.setText("Course not found. Check Course ID.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            taCourseDesc.setText("Database error: " + ex.getMessage());
        }
    }

    private void checkEligibility() {
        String studentId = txtStudentId.getText().trim();
        String courseId = txtCourseId.getText().trim();
        if (studentId.isEmpty() || courseId.isEmpty()) {
            lblEligibility.setText("Enter student and course.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            // Academic standing
            String standingSql = "SELECT standing FROM students WHERE student_id=?";
            PreparedStatement psStand = conn.prepareStatement(standingSql);
            psStand.setString(1, studentId);
            ResultSet rsStand = psStand.executeQuery();
            if (!rsStand.next()) {
                lblEligibility.setText("Student not found.");
                return;
            }
            String standing = rsStand.getString("standing");
            if (!"Good".equalsIgnoreCase(standing)) {
                lblEligibility.setText("Not eligible: Academic standing is " + standing);
                return;
            }

            // Prerequisites
            String prereqSql = "SELECT prerequisite_course_id FROM prerequisites WHERE course_id=?";
            PreparedStatement psPre = conn.prepareStatement(prereqSql);
            psPre.setString(1, courseId);
            ResultSet rsPre = psPre.executeQuery();
            boolean missing = false;
            while (rsPre.next()) {
                String prereq = rsPre.getString("prerequisite_course_id");
                String checkSql = "SELECT grade FROM registrations WHERE student_id=? AND course_id=? AND grade IN ('A','B','C','D')";
                PreparedStatement psCheck = conn.prepareStatement(checkSql);
                psCheck.setString(1, studentId);
                psCheck.setString(2, prereq);
                ResultSet rsCheck = psCheck.executeQuery();
                if (!rsCheck.next()) {
                    missing = true;
                    lblEligibility.setText("Missing prerequisite: " + prereq);
                    break;
                }
            }
            if (!missing) lblEligibility.setText("Eligible (standing good, prerequisites met)");
        } catch (SQLException ex) {
            ex.printStackTrace();
            lblEligibility.setText("DB error.");
        }
    }

    private void checkSchedule() {
        String studentId = txtStudentId.getText().trim();
        String courseId = txtCourseId.getText().trim();
        if (studentId.isEmpty() || courseId.isEmpty()) {
            lblSchedule.setText("Enter student and course.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String reqSql = "SELECT day_of_week, start_time, end_time FROM course_sections WHERE course_id=? AND semester=?";
            PreparedStatement psReq = conn.prepareStatement(reqSql);
            psReq.setString(1, courseId);
            psReq.setString(2, "Fall2026");
            ResultSet rsReq = psReq.executeQuery();
            if (!rsReq.next()) {
                lblSchedule.setText("Course section not found for current semester.");
                return;
            }
            String reqDay = rsReq.getString("day_of_week");
            String reqStart = rsReq.getString("start_time");
            String reqEnd = rsReq.getString("end_time");

            String existingSql = "SELECT cs.day_of_week, cs.start_time, cs.end_time FROM registrations r " +
                    "JOIN course_sections cs ON r.course_id=cs.course_id AND r.semester=cs.semester " +
                    "WHERE r.student_id=? AND r.semester=?";
            PreparedStatement psExist = conn.prepareStatement(existingSql);
            psExist.setString(1, studentId);
            psExist.setString(2, "Fall2026");
            ResultSet rsExist = psExist.executeQuery();
            boolean conflict = false;
            while (rsExist.next()) {
                String existDay = rsExist.getString("day_of_week");
                String existStart = rsExist.getString("start_time");
                String existEnd = rsExist.getString("end_time");
                if (existDay.equals(reqDay) && timeOverlap(reqStart, reqEnd, existStart, existEnd)) {
                    conflict = true;
                    lblSchedule.setText("Conflict with existing course on " + existDay);
                    break;
                }
            }
            if (!conflict) lblSchedule.setText("No schedule conflict.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            lblSchedule.setText("DB error.");
        }
    }

    private boolean timeOverlap(String s1, String e1, String s2, String e2) {
        return s1.compareTo(e2) < 0 && s2.compareTo(e1) < 0;
    }

    private void saveRegistration() {
        String studentId = txtStudentId.getText().trim();
        String courseId = txtCourseId.getText().trim();
        if (studentId.isEmpty() || courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter student and course.");
            return;
        }

        String currentSemester = "Fall2026";
        if (!isRegistrationOpen(currentSemester)) {
            JOptionPane.showMessageDialog(this, "Registration period is closed for " + currentSemester);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check for duplicate registration
            PreparedStatement psDup = conn.prepareStatement(
                    "SELECT COUNT(*) FROM registrations WHERE student_id=? AND course_id=? AND semester=?");
            psDup.setString(1, studentId);
            psDup.setString(2, courseId);
            psDup.setString(3, currentSemester);
            ResultSet rsDup = psDup.executeQuery();
            rsDup.next();
            if (rsDup.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Student already registered for this course in " + currentSemester);
                return;
            }

            // Insert registration
            String insertReg = "INSERT INTO registrations (student_id, course_id, semester, status, advisor_id) VALUES (?,?,?,?,?)";
            PreparedStatement psReg = conn.prepareStatement(insertReg);
            psReg.setString(1, studentId);
            psReg.setString(2, courseId);
            psReg.setString(3, currentSemester);
            psReg.setString(4, "Approved");
            psReg.setString(5, advisorId);
            psReg.executeUpdate();

            // Generate bill
            String getFee = "SELECT fee FROM courses WHERE course_id=?";
            PreparedStatement psFee = conn.prepareStatement(getFee);
            psFee.setString(1, courseId);
            ResultSet rsFee = psFee.executeQuery();
            double fee = rsFee.next() ? rsFee.getDouble("fee") : 0.0;
            String insertBill = "INSERT INTO bills (student_id, semester, amount, paid_status, generated_date) VALUES (?,?,?,?,?)";
            PreparedStatement psBill = conn.prepareStatement(insertBill);
            psBill.setString(1, studentId);
            psBill.setString(2, currentSemester);
            psBill.setDouble(3, fee);
            psBill.setString(4, "Unpaid");
            psBill.setDate(5, Date.valueOf(LocalDate.now()));
            psBill.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registration saved. Bill printed in Bursar's office.");
            loadCurrentRegistrations();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }

    private void loadCurrentRegistrations() {
        String studentId = txtStudentId.getText().trim();
        if (studentId.isEmpty()) {
            tableModel.setRowCount(0);
            return;
        }
        tableModel.setRowCount(0);
        System.out.println("Loading registrations for student: " + studentId); // Debug
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT r.course_id, c.title, cs.day_of_week, cs.start_time, r.status " +
                     "FROM registrations r " +
                     "JOIN courses c ON r.course_id = c.course_id " +
                     "LEFT JOIN course_sections cs ON r.course_id = cs.course_id AND r.semester = cs.semester " +
                     "WHERE r.student_id = ? AND r.semester = ?")) {
            ps.setString(1, studentId);
            ps.setString(2, "Fall2026");
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                tableModel.addRow(new Object[]{
                        rs.getString("course_id"),
                        rs.getString("title"),
                        rs.getString("day_of_week") != null ? rs.getString("day_of_week") : "TBD",
                        rs.getString("start_time") != null ? rs.getString("start_time") : "TBD",
                        rs.getString("status")
                });
            }
            System.out.println("Found " + count + " registrations.");
            if (count == 0) {
                JOptionPane.showMessageDialog(this, "No current registrations found for student " + studentId, "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading registrations: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}