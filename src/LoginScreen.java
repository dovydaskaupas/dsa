import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginScreen extends JFrame {

    private boolean hasLetter = false, hasDigit = false;
    private static JTextField txt_username, txt_password;
    private JFrame frame;

    private LoginScreen(){
        initInterface();
    }

    private void initInterface(){
        frame = new JFrame("Bulletin Board Login");

        addWindowFocusListener(new WindowAdapter() { // Check what this does.
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Container base_panel = frame.getContentPane();
        base_panel.setLayout(new BorderLayout());

        // Panel NORTH
        JPanel jPanel_north = new JPanel();
        jPanel_north.setLayout(new FlowLayout());

        JLabel lbl_title = new JLabel();
        lbl_title.setText("Welcome to Bulletin Board");
        jPanel_north.add(lbl_title);

        JPanel jPanel_center = new JPanel();
        jPanel_center.setLayout(new GridLayout(2, 2, 10, 10));

        JLabel lbl_username = new JLabel();
        lbl_username.setText("User name: ");
        jPanel_center.add(lbl_username);

        txt_username = new JTextField();
        txt_username.setText("");
        txt_username.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                // Alphanumeric input check.
                char c = e.getKeyChar();
                if (Character.isLetter(c)) {
                    hasLetter = true;
                } else if (Character.isDigit(c)) {
                    hasDigit = true;
                } else {
                    //JOptionPane.showMessageDialog(null, "This symbol cannot be used for username.", "Symbol Error!", JOptionPane.ERROR_MESSAGE);
                    e.consume();
                }

                // Input length check.
                if (txt_username.getText().length() == 15) {
                    JOptionPane.showMessageDialog(null, "Username cannot be longer than 15 symbols.", "Length Error!", JOptionPane.ERROR_MESSAGE);
                    e.consume();
                }
            }
        });
        jPanel_center.add(txt_username);

        JLabel lbl_password = new JLabel();
        lbl_password.setText("Password: ");
        jPanel_center.add(lbl_password);

        txt_password = new JPasswordField();
        txt_password.setText("");
        txt_password.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isLetter(c)) {
                    hasLetter = true;
                } else if (Character.isDigit(c)) {
                    hasDigit = true;
                } else {
                    //JOptionPane.showMessageDialog(null, "This symbol cannot be used for password.", "Symbol Error!", JOptionPane.ERROR_MESSAGE);
                    e.consume();
                }

                if (txt_password.getText().length() == 15) {
                    JOptionPane.showMessageDialog(null, "Password cannot be longer than 15 symbols.", "Length Error!", JOptionPane.ERROR_MESSAGE);
                    e.consume();
                }
            }
        });
        jPanel_center.add(txt_password);

        JButton btn_login = new JButton();
        btn_login.setText("Login");

        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        base_panel.setPreferredSize(new Dimension(300, 100));
        base_panel.add(jPanel_north, "North");
        base_panel.add(jPanel_center, "Center");
        base_panel.add(btn_login, "South");
        frame.pack();
        frame.setVisible(true);


        btn_login.addActionListener (evt -> {
            new StartTopicQueue();
            login();
        });
    }

    private void login(){
        new Main();
        frame.setVisible(false);
    }

    static String getUsername(){
        return txt_username.getText();
    }

    static String getPassword(){
        return txt_password.getText();
    }

    public static void main(String[] args){
        new LoginScreen();
    }
}
