import net.jini.core.lease.Lease;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;
import java.util.Timer;

/**
 * Class that is responsible for implementing interface for TopicRoom,
 * and posting new comments.
 */
public class TopicRoom extends JFrame {

    private static String topicName, userName, password, ownerName;
    private static int topicNumber;
    private JavaSpace space;
    private static JTextArea txtAr_chatArea;
    private static JLabel lbl_topicName, lbl_userName;
    private JTextField txt_comment;
    private JCheckBox cb_privateMessaging;

    TopicRoom(int topicNr, String topic, String userN, String pwd, String ownerN){
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
        ownerName = ownerN;

        // Runs Helpers.CommentPublisher class every X seconds to update the Topic room's text_area.
        Timer timer = new Timer();
        timer.schedule(new CommentPublisher(topicName, txtAr_chatArea, lbl_topicName, lbl_userName, userName), 0, 2000);
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
        jPanel_north.setLayout(new GridLayout(2, 1));

        lbl_topicName = new JLabel();
        jPanel_north.add(lbl_topicName);

        lbl_userName = new JLabel();
        jPanel_north.add(lbl_userName);

        // Panel CENTRE
        JPanel jPanel_centre = new JPanel();
        jPanel_centre.setLayout(new BorderLayout());

        txtAr_chatArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(txtAr_chatArea);
        txtAr_chatArea.setEditable(false);
        jPanel_centre.add(jScrollPane, "Center");

        // Panel SOUTH
        JPanel jPanel_south = new JPanel();
        jPanel_south.setLayout(new GridLayout(2, 3, 10, 10));

        JLabel lbl_comment = new JLabel();
        lbl_comment.setText("Write a comment here:");
        jPanel_south.add(lbl_comment);

        txt_comment = new JTextField(16);
        txt_comment.setText("");
        jPanel_south.add(txt_comment);

        JButton btn_post = new JButton();
        btn_post.setText("Post");
        jPanel_south.add(btn_post);

        JLabel lbl_private = new JLabel();
        lbl_private.setText("Set Private:");
        jPanel_south.add(lbl_private);

        cb_privateMessaging = new JCheckBox();
        cb_privateMessaging.setSelected(false);
        jPanel_south.add(cb_privateMessaging);

        frame.setResizable(false);
        base_panel.setPreferredSize(new Dimension(550, 700));
        base_panel.add(jPanel_north, "North");
        base_panel.add(jPanel_centre, "Center");
        base_panel.add(jPanel_south, "South");

        frame.pack();
        frame.setVisible(true);

        btn_post.addActionListener (evt -> postComment());
    }

    /**
     * Method responsible for posting new comments. Also it determines whether comment is private or public,
     * and increments each comment number to make tracking comments easy.
     */
    private void postComment(){
        String isPrivate;
        Transaction trcComment = Main.getTransactionCreated(800).transaction;
        try{
            if (cb_privateMessaging.isSelected()){
                isPrivate = "yes";
            }else{
                isPrivate = "no";
            }

            String comment = txt_comment.getText();
            if (!comment.equals("")){
                // Takes the TopicItem object from the space by its id, then increments its comment number and passes it to the QueueItem object.
                // This helps to track individual comments.
                TopicItem topicList = new TopicItem();
                topicList._id = topicNumber;
                TopicItem result = (TopicItem) space.take(topicList, trcComment, 500);

                result.incrementCommentNr();
                int commentNr = result._commentNr;

                System.out.println("Topic nr: " + topicNumber);
                System.out.println("topic name: " + topicName);
                System.out.println("user nam: " + userName);
                System.out.println("pwd: " + password);
                System.out.println("comment" + comment);
                System.out.println("comnr: " + commentNr);
                System.out.println("ownr nm: " + ownerName);
                System.out.println("comment owner: " + userName);


                QueueItem newTopic = new QueueItem(topicNumber, topicName, userName, password, Main.getTimestamp(), comment, commentNr, ownerName, userName, isPrivate);
                space.write(newTopic, trcComment, Lease.FOREVER);

                space.write(result, trcComment, Lease.FOREVER);
                txt_comment.setText("");
                trcComment.commit();
            }
        }catch(Exception e){
            e.printStackTrace();
            try {
                trcComment.abort();
            } catch (UnknownTransactionException | CannotAbortException | RemoteException e1) {
                JOptionPane.showMessageDialog(null, "Problem occurred while posting comment!", null, JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        }
    }
    public static void main(String[] args){}
}
