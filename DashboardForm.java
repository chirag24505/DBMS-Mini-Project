package eventmgmt;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * ============================================================
 *  FORM 2 – DashboardForm.java
 *  Event Management System | Java Swing + MySQL
 * ============================================================
 *  - Live stats: Total Events, Upcoming, Completed, Participants
 *  - Full Events table with search and filter by status/type
 *  - Add / Edit / Delete events (CRUD)
 *  - Navigate to Venue form and Participant form
 * ============================================================
 */
public class DashboardForm extends JFrame {

    private final int    userId;
    private final String fullName, role;

    // Stats labels
    private JLabel lblTotalEvents, lblUpcoming, lblCompleted, lblParticipants;

    // Table
    private JTable tblEvents;
    private DefaultTableModel tblModel;

    // Search/Filter
    private JTextField   txtSearch;
    private JComboBox<String> cmbStatus, cmbType;

    // Buttons
    private JButton btnAdd, btnEdit, btnDelete, btnSearch,
                    btnParticipants, btnVenues, btnLogout, btnRefresh;
    private JLabel  lblStatus;

    public DashboardForm(int userId, String fullName, String role) {
        this.userId   = userId;
        this.fullName = fullName;
        this.role     = role;
        initComponents();
        loadStats();
        loadEvents(null, "All Status", "All Types");
        setTitle("Event Management System – Dashboard");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents() {
        getContentPane().setBackground(new Color(245, 240, 255));
        setLayout(null);

        // ── Top bar ──
        JPanel top = new JPanel(null);
        top.setBackground(new Color(74, 20, 140));
        top.setBounds(0, 0, 1050, 52);
        add(top);

        JLabel logo = new JLabel("🎪  EVENT MANAGEMENT – DASHBOARD");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(Color.WHITE);
        logo.setBounds(12, 14, 420, 24);
        top.add(logo);

        JLabel info = new JLabel("Logged in: " + fullName + "  [" + role.toUpperCase() + "]");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info.setForeground(new Color(200, 170, 255));
        info.setBounds(450, 17, 340, 18);
        top.add(info);

        btnLogout = topBtn("Logout", new Color(183, 28, 28));
        btnLogout.setBounds(960, 11, 80, 30);
        btnLogout.addActionListener(e -> { dispose(); new LoginForm().setVisible(true); });
        top.add(btnLogout);

        // ── Stats cards ──
        String[] titles = {"Total Events","Upcoming","Completed","Participants"};
        Color[]  colors = {new Color(74,20,140), new Color(13,71,161), new Color(27,94,32), new Color(183,28,28)};
        JLabel[] refs   = new JLabel[4];
        for (int i = 0; i < 4; i++) {
            JPanel card = new JPanel(null);
            card.setBackground(colors[i]);
            card.setBounds(10 + i * 257, 58, 245, 70);
            card.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
            add(card);
            JLabel lTitle = new JLabel(titles[i], SwingConstants.CENTER);
            lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lTitle.setForeground(new Color(220, 200, 255));
            lTitle.setBounds(0, 8, 245, 18);
            card.add(lTitle);
            refs[i] = new JLabel("--", SwingConstants.CENTER);
            refs[i].setFont(new Font("Segoe UI", Font.BOLD, 28));
            refs[i].setForeground(Color.WHITE);
            refs[i].setBounds(0, 28, 245, 34);
            card.add(refs[i]);
        }
        lblTotalEvents  = refs[0];
        lblUpcoming     = refs[1];
        lblCompleted    = refs[2];
        lblParticipants = refs[3];

        // ── Search row ──
        JLabel lSrch = new JLabel("Search:");
        lSrch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lSrch.setBounds(10, 138, 60, 26);
        add(lSrch);

        txtSearch = new JTextField();
        txtSearch.setBounds(70, 138, 200, 28);
        add(txtSearch);

        JLabel lSt = new JLabel("Status:");
        lSt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lSt.setBounds(280, 138, 55, 26);
        add(lSt);

        String[] statuses = {"All Status","Upcoming","Ongoing","Completed","Cancelled"};
        cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setBounds(336, 138, 130, 28);
        add(cmbStatus);

        JLabel lTy = new JLabel("Type:");
        lTy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lTy.setBounds(476, 138, 45, 26);
        add(lTy);

        String[] types = {"All Types","Technical","Cultural","Workshop","Seminar","Sports","Other"};
        cmbType = new JComboBox<>(types);
        cmbType.setBounds(522, 138, 130, 28);
        add(cmbType);

        btnSearch = pBtn("🔍 Search", new Color(74, 20, 140));
        btnSearch.setBounds(662, 138, 120, 28);
        btnSearch.addActionListener(e -> loadEvents(
            txtSearch.getText().trim(),
            (String)cmbStatus.getSelectedItem(),
            (String)cmbType.getSelectedItem()));
        add(btnSearch);

        btnRefresh = pBtn("🔄 Refresh", new Color(0, 100, 150));
        btnRefresh.setBounds(792, 138, 110, 28);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText(""); cmbStatus.setSelectedIndex(0); cmbType.setSelectedIndex(0);
            loadStats(); loadEvents(null, "All Status", "All Types");
        });
        add(btnRefresh);

        // ── Events table ──
        String[] cols = {"ID","Event Name","Type","Date","Time","Venue","Budget (₹)","Status","Participants"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEvents = new JTable(tblModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String s = tblModel.getValueAt(row, 7).toString();
                    if      (s.equals("Upcoming"))  c.setBackground(new Color(240,248,255));
                    else if (s.equals("Completed")) c.setBackground(new Color(240,255,240));
                    else if (s.equals("Cancelled")) c.setBackground(new Color(255,240,240));
                    else                            c.setBackground(new Color(255,255,235));
                }
                return c;
            }
        };
        tblEvents.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblEvents.setRowHeight(24);
        tblEvents.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblEvents.getTableHeader().setBackground(new Color(74, 20, 140));
        tblEvents.getTableHeader().setForeground(Color.WHITE);
        tblEvents.setSelectionBackground(new Color(200, 180, 255));
        tblEvents.setGridColor(new Color(210, 200, 230));

        int[] cw = {40, 200, 100, 90, 70, 140, 90, 90, 90};
        for (int i = 0; i < cw.length; i++)
            tblEvents.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        JScrollPane scroll = new JScrollPane(tblEvents);
        scroll.setBounds(10, 174, 1030, 330);
        add(scroll);

        // ── Status label ──
        lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatus.setForeground(new Color(74, 20, 140));
        lblStatus.setBounds(10, 510, 400, 20);
        add(lblStatus);

        // ── Bottom buttons ──
        btnAdd = pBtn("➕ Add Event", new Color(74, 20, 140));
        btnAdd.setBounds(10, 535, 140, 36);
        btnAdd.addActionListener(e -> openEventForm(false));
        add(btnAdd);

        btnEdit = pBtn("✏ Edit Event", new Color(255, 140, 0));
        btnEdit.setBounds(160, 535, 140, 36);
        btnEdit.addActionListener(e -> openEventForm(true));
        add(btnEdit);

        btnDelete = pBtn("🗑 Delete Event", new Color(183, 28, 28));
        btnDelete.setBounds(310, 535, 145, 36);
        btnDelete.addActionListener(e -> deleteEvent());
        add(btnDelete);

        btnParticipants = pBtn("👥 Participants", new Color(27, 94, 32));
        btnParticipants.setBounds(465, 535, 145, 36);
        btnParticipants.addActionListener(e -> openParticipants());
        add(btnParticipants);

        btnVenues = pBtn("🏛 Manage Venues", new Color(13, 71, 161));
        btnVenues.setBounds(620, 535, 160, 36);
        btnVenues.addActionListener(e -> new VenueForm(userId, fullName, role).setVisible(true));
        add(btnVenues);
    }

    // ── Load stats ────────────────────────────────────────
    void loadStats() {
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet r1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM events");
            r1.next(); lblTotalEvents.setText(String.valueOf(r1.getInt(1))); r1.close();

            ResultSet r2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM events WHERE status='Upcoming'");
            r2.next(); lblUpcoming.setText(String.valueOf(r2.getInt(1))); r2.close();

            ResultSet r3 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM events WHERE status='Completed'");
            r3.next(); lblCompleted.setText(String.valueOf(r3.getInt(1))); r3.close();

            ResultSet r4 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM participants");
            r4.next(); lblParticipants.setText(String.valueOf(r4.getInt(1))); r4.close();
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Load events ───────────────────────────────────────
    void loadEvents(String keyword, String status, String type) {
        tblModel.setRowCount(0);
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT e.event_id, e.event_name, e.event_type, e.event_date, e.event_time, " +
                "v.venue_name, e.budget, e.status, " +
                "(SELECT COUNT(*) FROM participants p WHERE p.event_id=e.event_id) AS pcount " +
                "FROM events e LEFT JOIN venues v ON e.venue_id=v.venue_id WHERE 1=1");
            if (keyword != null && !keyword.isEmpty())
                sql.append(" AND e.event_name LIKE ?");
            if (status != null && !status.equals("All Status"))
                sql.append(" AND e.status=?");
            if (type != null && !type.equals("All Types"))
                sql.append(" AND e.event_type=?");
            sql.append(" ORDER BY e.event_date DESC");

            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql.toString());
            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) ps.setString(idx++, "%" + keyword + "%");
            if (status != null && !status.equals("All Status")) ps.setString(idx++, status);
            if (type   != null && !type.equals("All Types"))   ps.setString(idx, type);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tblModel.addRow(new Object[]{
                    rs.getInt("event_id"), rs.getString("event_name"),
                    rs.getString("event_type"), rs.getString("event_date"),
                    rs.getString("event_time"), rs.getString("venue_name"),
                    String.format("%.0f", rs.getDouble("budget")),
                    rs.getString("status"), rs.getInt("pcount")
                });
            }
            rs.close(); ps.close();
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Add / Edit event dialog ───────────────────────────
    private void openEventForm(boolean isEdit) {
        int eid = -1;
        String[] cur = {"","","","","","0","Upcoming"};
        if (isEdit) {
            int row = tblEvents.getSelectedRow();
            if (row < 0) { setStatus("⚠  Select an event to edit.", Color.ORANGE); return; }
            eid    = (int) tblModel.getValueAt(row, 0);
            cur[0] = tblModel.getValueAt(row, 1).toString();
            cur[1] = tblModel.getValueAt(row, 2).toString();
            cur[2] = tblModel.getValueAt(row, 3).toString();
            cur[3] = tblModel.getValueAt(row, 4) != null ? tblModel.getValueAt(row, 4).toString() : "";
            cur[5] = tblModel.getValueAt(row, 6).toString();
            cur[6] = tblModel.getValueAt(row, 7).toString();
        }

        JTextField tfName  = new JTextField(cur[0]);
        String[] types = {"Technical","Cultural","Workshop","Seminar","Sports","Other"};
        JComboBox<String> cbType = new JComboBox<>(types);
        cbType.setSelectedItem(cur[1]);
        JTextField tfDate  = new JTextField(cur[2]);
        JTextField tfTime  = new JTextField(cur[3].isEmpty() ? "09:00:00" : cur[3]);
        JTextField tfBudget= new JTextField(cur[5]);
        String[] stats = {"Upcoming","Ongoing","Completed","Cancelled"};
        JComboBox<String> cbStatus = new JComboBox<>(stats);
        cbStatus.setSelectedItem(cur[6]);
        JTextField tfDesc  = new JTextField();
        JTextField tfVenue = new JTextField("1");

        Object[] fields = {
            "Event Name *:", tfName,
            "Event Type *:", cbType,
            "Date (YYYY-MM-DD) *:", tfDate,
            "Time (HH:MM:SS):", tfTime,
            "Venue ID:", tfVenue,
            "Budget (₹):", tfBudget,
            "Status:", cbStatus,
            "Description:", tfDesc
        };

        int res = JOptionPane.showConfirmDialog(this, fields,
            isEdit ? "Edit Event" : "Add New Event", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps;
            if (isEdit) {
                ps = conn.prepareStatement(
                    "UPDATE events SET event_name=?,event_type=?,event_date=?,event_time=?," +
                    "venue_id=?,budget=?,status=?,description=? WHERE event_id=?");
                ps.setString(1, tfName.getText()); ps.setString(2, (String)cbType.getSelectedItem());
                ps.setString(3, tfDate.getText());  ps.setString(4, tfTime.getText());
                ps.setInt(5, Integer.parseInt(tfVenue.getText().trim()));
                ps.setDouble(6, Double.parseDouble(tfBudget.getText().trim()));
                ps.setString(7, (String)cbStatus.getSelectedItem());
                ps.setString(8, tfDesc.getText()); ps.setInt(9, eid);
            } else {
                ps = conn.prepareStatement(
                    "INSERT INTO events (event_name,event_type,event_date,event_time,venue_id," +
                    "organizer_id,budget,status,description) VALUES(?,?,?,?,?,?,?,?,?)");
                ps.setString(1, tfName.getText()); ps.setString(2, (String)cbType.getSelectedItem());
                ps.setString(3, tfDate.getText());  ps.setString(4, tfTime.getText());
                ps.setInt(5, Integer.parseInt(tfVenue.getText().trim()));
                ps.setInt(6, userId);
                ps.setDouble(7, Double.parseDouble(tfBudget.getText().trim()));
                ps.setString(8, (String)cbStatus.getSelectedItem());
                ps.setString(9, tfDesc.getText());
            }
            ps.executeUpdate(); ps.close();
            setStatus("✔  Event " + (isEdit ? "updated" : "added") + " successfully.", new Color(0, 120, 0));
            loadStats(); loadEvents(null, "All Status", "All Types");
        } catch (Exception ex) { showErr(ex.getMessage()); }
    }

    // ── Delete event ──────────────────────────────────────
    private void deleteEvent() {
        int row = tblEvents.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select an event to delete.", Color.ORANGE); return; }
        int    eid  = (int) tblModel.getValueAt(row, 0);
        String name = tblModel.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete event: " + name + "?\nThis will also delete all participants.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement p1 = conn.prepareStatement("DELETE FROM participants WHERE event_id=?");
            p1.setInt(1, eid); p1.executeUpdate(); p1.close();
            PreparedStatement p2 = conn.prepareStatement("DELETE FROM events WHERE event_id=?");
            p2.setInt(1, eid); p2.executeUpdate(); p2.close();
            setStatus("✔  Event deleted.", new Color(0, 120, 0));
            loadStats(); loadEvents(null, "All Status", "All Types");
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Open participants form ────────────────────────────
    private void openParticipants() {
        int row = tblEvents.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select an event first."); return; }
        int    eid   = (int) tblModel.getValueAt(row, 0);
        String ename = tblModel.getValueAt(row, 1).toString();
        new ParticipantForm(userId, fullName, role, eid, ename, this).setVisible(true);
    }

    private void setStatus(String msg, Color c) { lblStatus.setForeground(c); lblStatus.setText(msg); }
    private void showErr(String msg) { JOptionPane.showMessageDialog(this, "Error: " + msg, "DB Error", JOptionPane.ERROR_MESSAGE); }

    private JButton topBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JButton pBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
