package eventmgmt;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * ============================================================
 *  FORM 4 – VenueForm.java
 *  Event Management System | Java Swing + MySQL
 * ============================================================
 *  Full CRUD on venues table:
 *  Add / Edit / Delete / Search venues
 *  Shows which events are assigned to each venue
 * ============================================================
 */
public class VenueForm extends JFrame {

    private final int    userId;
    private final String fullName, role;

    // Input fields
    private JTextField txtVenueName, txtLocation, txtCapacity, txtContact, txtSearch;

    // Table
    private JTable tblVenues;
    private DefaultTableModel tblModel;

    // Buttons
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnBack;
    private JLabel  lblStatus;

    public VenueForm(int userId, String fullName, String role) {
        this.userId   = userId;
        this.fullName = fullName;
        this.role     = role;
        initComponents();
        loadVenues(null);
        setTitle("Event Management System – Venue Management");
        setSize(960, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents() {
        getContentPane().setBackground(new Color(240, 245, 255));
        setLayout(null);

        // ── Top bar ──
        JPanel top = new JPanel(null);
        top.setBackground(new Color(13, 71, 161));
        top.setBounds(0, 0, 960, 52);
        add(top);

        JLabel logo = new JLabel("🏛  VENUE MANAGEMENT");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(Color.WHITE);
        logo.setBounds(12, 14, 300, 24);
        top.add(logo);

        JLabel info = new JLabel("Manage venues for events | User: " + fullName);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info.setForeground(new Color(173, 216, 230));
        info.setBounds(330, 17, 420, 18);
        top.add(info);

        btnBack = topBtn("◀ Back", new Color(100, 50, 10));
        btnBack.setBounds(872, 11, 78, 30);
        btnBack.addActionListener(e -> dispose());
        top.add(btnBack);

        // ── Input Panel ──
        JPanel input = new JPanel(null);
        input.setBackground(new Color(13, 71, 161));
        input.setBounds(10, 60, 270, 430);
        input.setBorder(BorderFactory.createLineBorder(new Color(100, 160, 255), 1));
        add(input);

        JLabel lHead = new JLabel("  VENUE DETAILS");
        lHead.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lHead.setForeground(Color.WHITE);
        lHead.setBounds(0, 0, 270, 28);
        lHead.setBackground(new Color(10, 55, 130));
        lHead.setOpaque(true);
        input.add(lHead);

        // Fields
        input.add(vLbl("Venue Name *:", 38));
        txtVenueName = vField(); txtVenueName.setBounds(10, 56, 250, 28); input.add(txtVenueName);

        input.add(vLbl("Location:", 95));
        txtLocation = vField(); txtLocation.setBounds(10, 113, 250, 28); input.add(txtLocation);

        input.add(vLbl("Capacity:", 152));
        txtCapacity = vField(); txtCapacity.setBounds(10, 170, 250, 28); input.add(txtCapacity);

        input.add(vLbl("Contact No:", 209));
        txtContact = vField(); txtContact.setBounds(10, 227, 250, 28); input.add(txtContact);

        // Status
        lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatus.setForeground(Color.YELLOW);
        lblStatus.setBounds(5, 268, 260, 20);
        input.add(lblStatus);

        // Buttons
        btnAdd = vBtn("➕ Add Venue", new Color(0, 150, 136));
        btnAdd.setBounds(10, 296, 120, 32);
        btnAdd.addActionListener(e -> addVenue());
        input.add(btnAdd);

        btnUpdate = vBtn("✏ Update", new Color(255, 140, 0));
        btnUpdate.setBounds(140, 296, 120, 32);
        btnUpdate.addActionListener(e -> updateVenue());
        input.add(btnUpdate);

        btnDelete = vBtn("🗑 Delete", new Color(183, 28, 28));
        btnDelete.setBounds(10, 336, 120, 32);
        btnDelete.addActionListener(e -> deleteVenue());
        input.add(btnDelete);

        btnClear = vBtn("Clear", new Color(80, 80, 120));
        btnClear.setBounds(140, 336, 120, 32);
        btnClear.addActionListener(e -> clearFields());
        input.add(btnClear);

        // ── Right panel — Table ──
        JLabel lSrch = new JLabel("Search:");
        lSrch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lSrch.setBounds(290, 65, 60, 26);
        add(lSrch);

        txtSearch = new JTextField();
        txtSearch.setBounds(350, 65, 300, 28);
        add(txtSearch);

        btnSearch = vBtn("🔍 Search", new Color(74, 20, 140));
        btnSearch.setBounds(660, 65, 120, 28);
        btnSearch.addActionListener(e -> loadVenues(txtSearch.getText().trim()));
        add(btnSearch);

        // Table
        String[] cols = {"ID","Venue Name","Location","Capacity","Contact","Events Using"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblVenues = new JTable(tblModel);
        tblVenues.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblVenues.setRowHeight(26);
        tblVenues.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblVenues.getTableHeader().setBackground(new Color(13, 71, 161));
        tblVenues.getTableHeader().setForeground(Color.WHITE);
        tblVenues.setSelectionBackground(new Color(173, 216, 230));
        tblVenues.setGridColor(new Color(200, 215, 240));
        tblVenues.setRowSelectionAllowed(true);

        int[] cw = {40, 200, 200, 80, 120, 110};
        for (int i = 0; i < cw.length; i++)
            tblVenues.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        // Click row to fill form
        tblVenues.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { fillFormFromRow(); }
        });

        JScrollPane scroll = new JScrollPane(tblVenues);
        scroll.setBounds(288, 100, 662, 440);
        add(scroll);

        // Footer
        JLabel footer = new JLabel("Event Management System | Venue Module | Java + MySQL", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(100, 130, 170));
        footer.setBounds(0, 558, 960, 16);
        add(footer);
    }

    // ── Load venues ───────────────────────────────────────
    void loadVenues(String keyword) {
        tblModel.setRowCount(0);
        try {
            String sql = "SELECT v.venue_id, v.venue_name, v.location, v.capacity, v.contact, " +
                         "(SELECT COUNT(*) FROM events e WHERE e.venue_id=v.venue_id) AS event_count " +
                         "FROM venues v WHERE 1=1";
            if (keyword != null && !keyword.isEmpty())
                sql += " AND (v.venue_name LIKE ? OR v.location LIKE ?)";
            sql += " ORDER BY v.venue_name";

            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(1, "%" + keyword + "%");
                ps.setString(2, "%" + keyword + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tblModel.addRow(new Object[]{
                    rs.getInt("venue_id"), rs.getString("venue_name"),
                    rs.getString("location"), rs.getInt("capacity"),
                    rs.getString("contact"), rs.getInt("event_count")
                });
            }
            rs.close(); ps.close();
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Add venue ─────────────────────────────────────────
    private void addVenue() {
        String name = txtVenueName.getText().trim();
        if (name.isEmpty()) { setStatus("⚠  Venue Name is required.", Color.YELLOW); return; }
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "INSERT INTO venues (venue_name, location, capacity, contact) VALUES(?,?,?,?)");
            ps.setString(1, name);
            ps.setString(2, txtLocation.getText().trim());
            int cap = 100;
            try { cap = Integer.parseInt(txtCapacity.getText().trim()); } catch (Exception ignored) {}
            ps.setInt(3, cap);
            ps.setString(4, txtContact.getText().trim());
            ps.executeUpdate(); ps.close();
            setStatus("✔  Venue added successfully.", Color.GREEN);
            clearFields(); loadVenues(null);
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Update venue ──────────────────────────────────────
    private void updateVenue() {
        int row = tblVenues.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select a venue to update.", Color.YELLOW); return; }
        int vid = (int) tblModel.getValueAt(row, 0);
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "UPDATE venues SET venue_name=?, location=?, capacity=?, contact=? WHERE venue_id=?");
            ps.setString(1, txtVenueName.getText().trim());
            ps.setString(2, txtLocation.getText().trim());
            int cap = 100;
            try { cap = Integer.parseInt(txtCapacity.getText().trim()); } catch (Exception ignored) {}
            ps.setInt(3, cap);
            ps.setString(4, txtContact.getText().trim());
            ps.setInt(5, vid);
            ps.executeUpdate(); ps.close();
            setStatus("✔  Venue updated.", Color.GREEN);
            clearFields(); loadVenues(null);
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Delete venue ──────────────────────────────────────
    private void deleteVenue() {
        int row = tblVenues.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select a venue to delete.", Color.YELLOW); return; }
        int    vid   = (int)    tblModel.getValueAt(row, 0);
        String vname = (String) tblModel.getValueAt(row, 1);
        int    uses  = (int)    tblModel.getValueAt(row, 5);
        if (uses > 0) {
            setStatus("✘  Cannot delete — " + uses + " event(s) use this venue.", Color.ORANGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete venue: " + vname + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "DELETE FROM venues WHERE venue_id=?");
            ps.setInt(1, vid); ps.executeUpdate(); ps.close();
            setStatus("✔  Venue deleted.", Color.GREEN);
            clearFields(); loadVenues(null);
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Fill form from row ────────────────────────────────
    private void fillFormFromRow() {
        int row = tblVenues.getSelectedRow();
        if (row < 0) return;
        txtVenueName.setText(tblModel.getValueAt(row, 1).toString());
        txtLocation.setText(tblModel.getValueAt(row, 2) != null ? tblModel.getValueAt(row, 2).toString() : "");
        txtCapacity.setText(tblModel.getValueAt(row, 3).toString());
        txtContact.setText(tblModel.getValueAt(row, 4) != null ? tblModel.getValueAt(row, 4).toString() : "");
        lblStatus.setText("");
    }

    private void clearFields() {
        txtVenueName.setText(""); txtLocation.setText("");
        txtCapacity.setText(""); txtContact.setText("");
        tblVenues.clearSelection(); lblStatus.setText("");
    }

    private void setStatus(String msg, Color c) { lblStatus.setForeground(c); lblStatus.setText(msg); }
    private void showErr(String msg) { JOptionPane.showMessageDialog(this, "Error: " + msg, "DB Error", JOptionPane.ERROR_MESSAGE); }

    private JLabel vLbl(String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(200, 220, 255));
        l.setBounds(10, y, 250, 17);
        return l;
    }
    private JTextField vField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(new Color(21, 91, 180));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createLineBorder(new Color(100, 160, 255)));
        return f;
    }
    private JButton topBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JButton vBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
