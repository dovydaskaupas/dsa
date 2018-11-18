import net.jini.space.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BulletinBoard extends JFrame{
    private JPanel panel_base;
    private JLabel lbl_title;
    private JTextField txt_username;
    private JTextField txt_topicname;
    private JButton btn_submit_topic;
    private JPanel panel_top;
    private JPanel panel_left;
    private JPanel panel_right;
    private JPanel panel_bottom;
    private JLabel lbl_username;
    private JLabel lbl_topicname;
    private JLabel lbl_addnew_title;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JavaSpace space;


    public BulletinBoard(){
        super("Bulletin Board");

        btn_submit_topic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                submitTopic();
            }
        });
    }
    private void initComponents(){
    setTitle("Bulletin Board");

    }


    private void submitTopic() {
        space = SpaceUtils.getSpace();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("BulletinBoard");
        frame.setContentPane(new BulletinBoard().panel_base);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
