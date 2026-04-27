package eventmgmt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * ============================================================
 *  FORM 1 – LoginForm.java
 *  Event Management System | Java Swing + MySQL
 * ============================================================
 */
public class LoginForm extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox      chkShow;
    private JLabel         lblStatus;
    private JButton        btnLogin, btnClear, btnExit;

    public LoginForm() {
        initComponents();
        setTitle("Event Management System – Login");
        setSize(450, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        getContentPane().setBackground(new Color(74, 20, 140));
        setLayout(null);

        // ── Top Banner ──
        JPanel banner = new JPanel(null);
        banner.setBackground(new Color(55, 10, 110));
        banner.setBounds(0, 0, 450, 75);
        add(banner);

        JLabel icon = new JLabel("🎪", SwingConstants.LEFT);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        icon.setBounds(15, 18, 45, 40);
        banner.add(icon);

        JLabel title = new JLabel("EVENT MANAGEMENT SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBounds(68, 12, 370, 28);
        banner.add(title);

        JLabel sub = new JLabel("SNJB College of Engineering – Admin Portal");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        sub.setForeground(new Color(200, 170, 255));
        sub.setBounds(68, 40, 370, 20);
        banner.add(sub);

        // ── Card ──
        JPanel card = new JPanel(null);
        card.setBackground(new Color(90, 30, 160));
        card.setBounds(30, 88, 390, 255);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 130, 255), 1));
        add(card);

        JLabel lHead = new JLabel("LOGIN TO CONTINUE", SwingConstants.CENTER);
        lHead.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lHead.setForeground(new Color(220, 190, 255));
        lHead.setBounds(0, 12, 390, 22);
        card.add(lHead);

        // Username
        addCardLabel(card, "Username :", 50);
        txtUsername = cardField(); txtUsername.setBounds(150, 50, 210, 30); card.add(txtUsername);

        // Password
        addCardLabel(card, "Password :", 97);
        txtPassword = new JPasswordField();
        styleField(txtPassword);
        txtPassword.setBounds(150, 97, 210, 30); card.add(txtPassword);

        // Show password
        chkShow = new JCheckBox("Show Password");
        chkShow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkShow.setForeground(new Color(200, 170, 255));
        chkShow.setBackground(new Color(90, 30, 160));
        chkShow.setBounds(150, 133, 150, 22);
        chkShow.addActionListener(e ->
            txtPassword.setEchoChar(chkShow.isSelected() ? (char)0 : '●'));
        card.add(chkShow);

        // Status
        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.YELLOW);
        lblStatus.setBounds(0, 162, 390, 22);
        card.add(lblStatus);

        // Buttons
        btnLogin = btn("Login", new Color(123, 31, 162));
        btnLogin.setBounds(25, 195, 100, 34);
        btnLogin.addActionListener(e -> doLogin());
        card.add(btnLogin);

        btnClear = btn("Clear", new Color(80, 80, 100));
        btnClear.setBounds(140, 195, 100, 34);
        btnClear.addActionListener(e -> {
            txtUsername.setText(""); txtPassword.setText("");
            lblStatus.setText(""); txtUsername.requestFocus();
        });
        card.add(btnClear);

        btnExit = btn("Exit", new Color(183, 28, 28));
        btnExit.setBounds(255, 195, 100, 34);
        btnExit.addActionListener(e -> System.exit(0));
        card.add(btnExit);

        // Footer
        JLabel footer = new JLabel("Mini Project | Java + MySQL | Domain: Event Management", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(180, 150, 220));
        footer.setBounds(0, 368, 450, 18);
        add(footer);

        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
    }

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            setStatus("⚠  Enter username and password.", Color.YELLOW); return;
        }
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT user_id, full_name, role FROM users WHERE username=? AND password=?");
            ps.setString(1, user); ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int    uid  = rs.getInt("user_id");
                String name = rs.getString("full_name");
                String role = rs.getString("role");
                setStatus("✔  Welcome, " + name + "!", Color.GREEN);
                Timer t = new Timer(800, ev -> {
                    dispose();
                    new DashboardForm(uid, name, role).setVisible(true);
                });
                t.setRepeats(false); t.start();
            } else {
                setStatus("✘  Invalid username or password.", Color.YELLOW);
            }
            rs.close(); ps.close();
        } catch (SQLException ex) {
            setStatus("DB Error: " + ex.getMessage(), Color.ORANGE);
        }
    }

    private void addCardLabel(JPanel p, String text, int y) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(210, 180, 255));
        l.setBounds(15, y, 125, 28); p.add(l);
    }
    private JTextField cardField() {
        JTextField f = new JTextField(); styleField(f); return f;
    }
    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(new Color(110, 50, 180));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createLineBorder(new Color(180, 130, 255)));
    }
    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void setStatus(String msg, Color c) { lblStatus.setForeground(c); lblStatus.setText(msg); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
