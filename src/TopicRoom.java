import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TopicRoom extends JFrame {

    private String topicSelected, username, password, topicOwner;
    private JavaSpace space;
    private static final long TWO_SECONDS = 2 * 1000;
    private JTextArea txtAr_chatArea;
    private JLabel lbl_topicname;
    private JTextField txt_comment;
    private static String topicChosen;

    TopicRoom(){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the javaspace");
            System.exit(1);
        }

       // topicChosen = topic;
        //if(topicChosen == null){
        //    JOptionPane.showMessageDialog(null, "Argument is not here", "Name Error!", JOptionPane.ERROR_MESSAGE);

       // }
       // else{
      //      JOptionPane.showMessageDialog(null, "Argument IS here", "Name Error!", JOptionPane.ERROR_MESSAGE);

      //  }
        initInterface();
        //topicSelected = Main.getTopicSelected();
        connectTo();
    }

    private void initInterface(){
        topicSelected = JOptionPane.showInputDialog("Enter Name of Printer");
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

    public void connectTo(){
        while(true){
            System.out.println("1");
            try {
                QueueItem qiTemplate = new QueueItem();
                System.out.println("2");
                qiTemplate._topicName = topicSelected; // THIS IS THE MAIN CHANGE.  Set the destination printer name in the template so as to retrieve only the correct print jobs
                System.out.println("3");
                QueueItem nextJob = (QueueItem) space.take(qiTemplate,null, TWO_SECONDS);
                System.out.println("4");
                if (nextJob == null) {
                    // no print job was found, so sleep for a couple of seconds and try again
                    Thread.sleep(TWO_SECONDS);
                    System.out.println("No Topics");
                } else {
                    // we have a job to process
                    System.out.println("Topic rceived");
                    int nextJobNumber = nextJob._topicNumber;
                    username = nextJob._userName;
                    password = nextJob._password;
                    String comm = nextJob._comment;
                    String ts = nextJob._timestamp;
                    topicOwner = nextJob._topicOwner;


                    lbl_topicname.setText("Topic Owner:"  + topicOwner + ".           "+  "Topic Name: " + nextJobNumber+ ". " + topicSelected +".");

                    txtAr_chatArea.append("->" + ts + ", " + username + " says: " + comm + "\n");
                    //txtAr_chatArea.append("-> " + timestamp +"," + username +  "says: "+ comment + "\n");
                }
            }  catch ( Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void postComment(ActionEvent event){
        try{
            QueueStatus template = new QueueStatus();
            QueueStatus topicStatus = (QueueStatus) space.take(template, null, 2000);
            int topicNumber = topicStatus.nextTopic;

            String timestamp = Main.getTimestamp();
            String comment = txt_comment.getText();

            QueueItem newTopic = new QueueItem(topicNumber, topicSelected, username, password, timestamp, comment, topicOwner);
            space.write(newTopic, null, Lease.FOREVER);

            //topicStatus.incrementTopicNr();
            space.write(topicStatus, null, Lease.FOREVER);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        new TopicRoom();
    }
}
