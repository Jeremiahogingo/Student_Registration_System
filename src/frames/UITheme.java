package frames;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

public class UITheme {
    // Color palette
    public static final Color PRIMARY = new Color(33, 150, 243);
    public static final Color PRIMARY_DARK = new Color(25, 118, 210);
    public static final Color SECONDARY = new Color(245, 245, 245);
    public static final Color BACKGROUND = new Color(250, 250, 250);
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color TEXT_FG = new Color(33, 33, 33);
    public static final Color BORDER_COLOR = new Color(224, 224, 224);
    public static final Color TABLE_HEADER_BG = new Color(63, 81, 181);
    public static final Color TABLE_HEADER_FG = Color.WHITE;
    public static final Color TABLE_ROW_ODD = Color.WHITE;
    public static final Color TABLE_ROW_EVEN = new Color(248, 248, 248);

    public static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);

    public static void applyTheme() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
            UIManager.put("nimbusBase", PRIMARY);
            UIManager.put("nimbusBlueGrey", PRIMARY_DARK);
            UIManager.put("control", BACKGROUND);
            UIManager.put("text", TEXT_FG);
        } catch (Exception e) {
            // fallback
        }
    }

    public static void styleButton(JButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleTitledPanel(JPanel panel, String title) {
        panel.setBackground(PANEL_BG);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 18),
                PRIMARY_DARK
        );
        panel.setBorder(border);
    }

    public static void styleTable(JTable table) {
        table.setFont(TEXT_FONT);
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(LABEL_FONT);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(TABLE_HEADER_FG);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW_ODD : TABLE_ROW_EVEN);
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });
    }

    public static void styleTextField(JTextField field) {
        field.setFont(TEXT_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(TEXT_FONT);
        combo.setBackground(PANEL_BG);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    }
}