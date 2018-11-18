import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Timer;

public class TopicRoom2 extends JFrame {

    private static String topicName, userName, password, ownerName;
    private static int topicNumber;
    private JavaSpace space;
    private static JTextArea txtAr_chatArea;
    private static JLabel lbl_topicname;
    private JTextField txt_comment;

    TopicRoom2(int topicNr, String topic, String userN, String pwd, String timeS, String comm, String ownerN){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the Java Space");
            System.exit(1);
        }

        initInterface();
        topicNumber = topicNr;
        topicName = topic;
        userName = userN;
        password = pwd;
        // timeStamp = timeS; // redundant!?
        // comment = comm; // redundant!?
        ownerName = ownerN;

        // Runs Helpers.TopicTaker class every X seconds to update the Topic room's text_area.
        Timer timer = new Timer();
        timer.schedule(new TopicTaker(topicName, txtAr_chatArea, lbl_topicname), 0, 2000);
    }

    private void initInterface(){
        JFrame frame = new JFrame("Bulletin Board Login");

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

        lbl_topicname = new JLabel();
        jPanel_north.add(lbl_topicname);

        // Panel CENTRE
        JPanel jPanel_centre = new JPanel();
        jPanel_centre.setLayout(new BorderLayout());

        txtAr_chatArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(txtAr_chatArea);
        txtAr_chatArea.setEditable(false);
        jPanel_centre.add(jScrollPane, "Center");

        // Panel SOUTH
        JPanel jPanel_south = new JPanel();
        jPanel_south.setLayout(new GridLayout(1, 3, 10, 10));

        JLabel lbl_comment = new JLabel();
        lbl_comment.setText("Write a comment here:");
        jPanel_south.add(lbl_comment);

        txt_comment = new JTextField(16);
        txt_comment.setText("");
        jPanel_south.add(txt_comment);

        JButton btn_post = new JButton();
        btn_post.setText("Post");
        jPanel_south.add(btn_post);

        frame.setResizable(false);
        base_panel.setPreferredSize(new Dimension(500, 700));
        base_panel.add(jPanel_north, "North");
        base_panel.add(jPanel_centre, "Center");
        base_panel.add(jPanel_south, "South");

        frame.pack();
        frame.setVisible(true);

        btn_post.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                postComment(evt);
            }
        });
    }

    private void postComment(ActionEvent event){
        try{
            QueueItem temp = new QueueItem();
            String tn = temp._topicName;
            String to = temp._topicOwner;

            String comment = txt_comment.getText();
            if (!comment.equals("")){
                QueueItem newTopic = new QueueItem(topicNumber, topicName, userName, password, Main.getTimestamp(), comment, ownerName);
                space.write(newTopic, null, Lease.FOREVER);
                txt_comment.setText("");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){}
}
