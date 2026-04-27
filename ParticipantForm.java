package eventmgmt;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * ============================================================
 *  FORM 3 – ParticipantForm.java
 *  Event Management System | Java Swing + MySQL
 * ============================================================
 *  - Register new participants for an event
 *  - View, Search, Edit, Delete participants
 *  - Update attendance status
 *  - Shows total registered count
 * ============================================================
 */
public class ParticipantForm extends JFrame {

    private final int    userId, eventId;
    private final String fullName, role, eventName;
    private final DashboardForm dashboard;

    // Input fields
    private JTextField txtName, txtEmail, txtPhone, txtCollege, txtRegNo, txtSearch;
    private JComboBox<String> cmbStatus;

    // Table
    private JTable tblParticipants;
    private DefaultTableModel tblModel;

    // Labels
    private JLabel lblCount, lblStatusMsg;

    // Buttons
    private JButton btnRegister, btnUpdate, btnDelete, btnClear, btnSearch, btnBack;

    public ParticipantForm(int userId, String fullName, String role,
                           int eventId, String eventName, DashboardForm dashboard) {
        this.userId    = userId;
        this.fullName  = fullName;
        this.role      = role;
        this.eventId   = eventId;
        this.eventName = eventName;
        this.dashboard = dashboard;
        initComponents();
        loadParticipants(null);
        setTitle("Participants – " + eventName);
        setSize(1000, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents() {
        getContentPane().setBackground(new Color(232, 240, 254));
        setLayout(null);

        // ── Top bar ──
        JPanel top = new JPanel(null);
        top.setBackground(new Color(27, 94, 32));
        top.setBounds(0, 0, 1000, 52);
        add(top);

        JLabel logo = new JLabel("👥  PARTICIPANT MANAGEMENT");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(Color.WHITE);
        logo.setBounds(12, 14, 340, 24);
        top.add(logo);

        JLabel eInfo = new JLabel("Event: " + eventName);
        eInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eInfo.setForeground(new Color(180, 255, 180));
        eInfo.setBounds(370, 17, 440, 18);
        top.add(eInfo);

        btnBack = topBtn("◀ Back", new Color(100, 60, 10));
        btnBack.setBounds(912, 11, 78, 30);
        btnBack.addActionListener(e -> { dashboard.loadStats(); dashboard.loadEvents(null,"All Status","All Types"); dispose(); });
        top.add(btnBack);

        // ── Input Panel ──
        JPanel input = new JPanel(null);
        input.setBackground(new Color(27, 94, 32));
        input.setBounds(10, 60, 300, 470);
        input.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 100), 1));
        add(input);

        JLabel lHead = new JLabel("  REGISTER PARTICIPANT");
        lHead.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lHead.setForeground(Color.WHITE);
        lHead.setBounds(0, 0, 300, 26);
        lHead.setBackground(new Color(20, 70, 20));
        lHead.setOpaque(true);
        input.add(lHead);

        // Fields
        input.add(iLbl("Full Name *:", 36));
        txtName = iField(); txtName.setBounds(10, 54, 280, 28); input.add(txtName);

        input.add(iLbl("Email:", 92));
        txtEmail = iField(); txtEmail.setBounds(10, 109, 280, 28); input.add(txtEmail);

        input.add(iLbl("Phone:", 147));
        txtPhone = iField(); txtPhone.setBounds(10, 164, 280, 28); input.add(txtPhone);

        input.add(iLbl("College / Organization:", 202));
        txtCollege = iField(); txtCollege.setBounds(10, 219, 280, 28); input.add(txtCollege);

        input.add(iLbl("Reg. No (auto if blank):", 257));
        txtRegNo = iField(); txtRegNo.setBounds(10, 274, 280, 28); input.add(txtRegNo);

        input.add(iLbl("Status:", 313));
        String[] statuses = {"Registered","Attended","Absent","Cancelled"};
        cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbStatus.setBackground(new Color(21, 120, 40));
        cmbStatus.setForeground(Color.WHITE);
        cmbStatus.setBounds(10, 330, 280, 28);
        input.add(cmbStatus);

        // Status msg
        lblStatusMsg = new JLabel("");
        lblStatusMsg.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatusMsg.setForeground(Color.YELLOW);
        lblStatusMsg.setBounds(5, 368, 290, 20);
        input.add(lblStatusMsg);

        // Buttons
        btnRegister = gBtn("➕ Register", new Color(0, 120, 0));
        btnRegister.setBounds(10, 396, 130, 30);
        btnRegister.addActionListener(e -> registerParticipant());
        input.add(btnRegister);

        btnUpdate = gBtn("✏ Update", new Color(255, 140, 0));
        btnUpdate.setBounds(155, 396, 135, 30);
        btnUpdate.addActionListener(e -> updateParticipant());
        input.add(btnUpdate);

        btnDelete = gBtn("🗑 Delete", new Color(183, 28, 28));
        btnDelete.setBounds(10, 434, 130, 30);
        btnDelete.addActionListener(e -> deleteParticipant());
        input.add(btnDelete);

        btnClear = gBtn("Clear", new Color(80, 80, 80));
        btnClear.setBounds(155, 434, 135, 30);
        btnClear.addActionListener(e -> clearFields());
        input.add(btnClear);

        // ── Right panel — table ──
        JLabel lSrch = new JLabel("Search:");
        lSrch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lSrch.setBounds(320, 65, 60, 26);
        add(lSrch);

        txtSearch = new JTextField();
        txtSearch.setBounds(380, 65, 300, 28);
        add(txtSearch);

        btnSearch = gBtn("🔍 Search", new Color(74, 20, 140));
        btnSearch.setBounds(690, 65, 120, 28);
        btnSearch.addActionListener(e -> loadParticipants(txtSearch.getText().trim()));
        add(btnSearch);

        lblCount = new JLabel("Total: 0 participants");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCount.setForeground(new Color(27, 94, 32));
        lblCount.setBounds(820, 68, 170, 22);
        add(lblCount);

        // Table
        String[] cols = {"ID","Reg No","Name","Email","Phone","College","Status","Registered At"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblParticipants = new JTable(tblModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String s = tblModel.getValueAt(row, 6).toString();
                    if      (s.equals("Attended"))  c.setBackground(new Color(240, 255, 240));
                    else if (s.equals("Absent"))    c.setBackground(new Color(255, 245, 220));
                    else if (s.equals("Cancelled")) c.setBackground(new Color(255, 235, 235));
                    else                            c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        tblParticipants.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblParticipants.setRowHeight(24);
        tblParticipants.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblParticipants.getTableHeader().setBackground(new Color(27, 94, 32));
        tblParticipants.getTableHeader().setForeground(Color.WHITE);
        tblParticipants.setSelectionBackground(new Color(200, 240, 200));
        tblParticipants.setGridColor(new Color(190, 220, 190));

        int[] cw = {40, 90, 160, 160, 90, 150, 90, 130};
        for (int i = 0; i < cw.length; i++)
            tblParticipants.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        tblParticipants.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { fillFormFromRow(); }
        });

        JScrollPane scroll = new JScrollPane(tblParticipants);
        scroll.setBounds(318, 100, 672, 490);
        add(scroll);
    }

    // ── Load participants ─────────────────────────────────
    void loadParticipants(String keyword) {
        tblModel.setRowCount(0);
        try {
            String sql = "SELECT participant_id, registration_no, full_name, email, phone, " +
                         "college, status, registered_at FROM participants WHERE event_id=?";
            if (keyword != null && !keyword.isEmpty())
                sql += " AND (full_name LIKE ? OR registration_no LIKE ?)";
            sql += " ORDER BY registered_at DESC";

            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ps.setInt(1, eventId);
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(2, "%" + keyword + "%");
                ps.setString(3, "%" + keyword + "%");
            }
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                tblModel.addRow(new Object[]{
                    rs.getInt("participant_id"), rs.getString("registration_no"),
                    rs.getString("full_name"), rs.getString("email"),
                    rs.getString("phone"), rs.getString("college"),
                    rs.getString("status"), rs.getString("registered_at")
                });
            }
            rs.close(); ps.close();
            lblCount.setText("Total: " + count + " participant" + (count != 1 ? "s" : ""));
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Register participant ──────────────────────────────
    private void registerParticipant() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) { setStatus("⚠  Name is required.", Color.YELLOW); return; }

        // Auto-generate reg no if blank
        String regNo = txtRegNo.getText().trim();
        if (regNo.isEmpty()) {
            regNo = "EV" + eventId + "P" + System.currentTimeMillis() % 10000;
        }

        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "INSERT INTO participants (event_id,full_name,email,phone,college,registration_no,status) " +
                "VALUES(?,?,?,?,?,?,?)");
            ps.setInt(1, eventId); ps.setString(2, name);
            ps.setString(3, txtEmail.getText().trim());
            ps.setString(4, txtPhone.getText().trim());
            ps.setString(5, txtCollege.getText().trim());
            ps.setString(6, regNo);
            ps.setString(7, (String)cmbStatus.getSelectedItem());
            ps.executeUpdate(); ps.close();
            setStatus("✔  " + name + " registered. Reg No: " + regNo, Color.GREEN);
            clearFields(); loadParticipants(null);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate"))
                setStatus("✘  Registration No already exists.", Color.ORANGE);
            else showErr(ex.getMessage());
        }
    }

    // ── Update participant ────────────────────────────────
    private void updateParticipant() {
        int row = tblParticipants.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select a participant to update.", Color.YELLOW); return; }
        int pid = (int) tblModel.getValueAt(row, 0);
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "UPDATE participants SET full_name=?,email=?,phone=?,college=?,status=? WHERE participant_id=?");
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setString(4, txtCollege.getText().trim());
            ps.setString(5, (String)cmbStatus.getSelectedItem());
            ps.setInt(6, pid);
            ps.executeUpdate(); ps.close();
            setStatus("✔  Participant updated.", Color.GREEN);
            clearFields(); loadParticipants(null);
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Delete participant ────────────────────────────────
    private void deleteParticipant() {
        int row = tblParticipants.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select a participant to delete.", Color.YELLOW); return; }
        int    pid  = (int)    tblModel.getValueAt(row, 0);
        String name = (String) tblModel.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove " + name + " from this event?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "DELETE FROM participants WHERE participant_id=?");
            ps.setInt(1, pid); ps.executeUpdate(); ps.close();
            setStatus("✔  Participant removed.", Color.GREEN);
            loadParticipants(null);
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ── Fill form from row click ──────────────────────────
    private void fillFormFromRow() {
        int row = tblParticipants.getSelectedRow();
        if (row < 0) return;
        txtName.setText(tblModel.getValueAt(row, 2).toString());
        txtEmail.setText(tblModel.getValueAt(row, 3) != null ? tblModel.getValueAt(row, 3).toString() : "");
        txtPhone.setText(tblModel.getValueAt(row, 4) != null ? tblModel.getValueAt(row, 4).toString() : "");
        txtCollege.setText(tblModel.getValueAt(row, 5) != null ? tblModel.getValueAt(row, 5).toString() : "");
        txtRegNo.setText(tblModel.getValueAt(row, 1).toString());
        cmbStatus.setSelectedItem(tblModel.getValueAt(row, 6).toString());
        lblStatusMsg.setText("");
    }

    private void clearFields() {
        txtName.setText(""); txtEmail.setText(""); txtPhone.setText("");
        txtCollege.setText(""); txtRegNo.setText("");
        cmbStatus.setSelectedIndex(0); tblParticipants.clearSelection(); lblStatusMsg.setText("");
    }

    private void setStatus(String msg, Color c) { lblStatusMsg.setForeground(c); lblStatusMsg.setText(msg); }
    private void showErr(String msg) { JOptionPane.showMessageDialog(this, "Error: " + msg, "DB Error", JOptionPane.ERROR_MESSAGE); }

    private JLabel iLbl(String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(200, 240, 200));
        l.setBounds(10, y, 280, 16);
        return l;
    }
    private JTextField iField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(new Color(21, 120, 40));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 100)));
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
    private JButton gBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
