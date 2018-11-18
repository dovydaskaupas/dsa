import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;

public class Main extends JFrame{

    private JTextArea txtAr_topiclist;
    private JTextField txt_username;
    private static JTextField texsttext;

    private String username, password, topicSelected;
    private int topicNumber;

    private JavaSpace space;
    
    Main(){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
            System.exit(1);
        }

        username = LoginScreen.getUsername();
        password = LoginScreen.getPassword();
        initInterface(username, password);

        java.util.Timer timer = new Timer();
        timer.schedule(new TopicSearcher(txtAr_topiclist), 0, 5000);
    }

    private void initInterface(String un, String pwd) {
        JFrame frame = new JFrame("Bulletin Board");

        addWindowFocusListener(new WindowAdapter() { // Check what this does.
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Container base_panel = frame.getContentPane();
        base_panel.setLayout(new BorderLayout());

        // Panel WEST
        JPanel jPanel_west = new JPanel();
        jPanel_west.setLayout(new BorderLayout());

        JLabel lbl_createEdit = new JLabel();
        lbl_createEdit.setText("Create / Join Topic");
        jPanel_west.add(lbl_createEdit, "North");

        JPanel jpw_centre = new JPanel();
        jpw_centre.setLayout(new FlowLayout());

        JLabel lbl_username = new JLabel();
        lbl_username.setText("User name: ");
        jpw_centre.add(lbl_username);

        txt_username = new JTextField();
        txt_username.setText("Logged in as: " + un + ", " + pwd);
        txt_username.setEditable(false);
        jpw_centre.add(txt_username);
        jPanel_west.add(jpw_centre, "West");

        JPanel jpw_south = new JPanel();
        jpw_south.setLayout(new FlowLayout());

        JButton btn_start = new JButton();
        btn_start.setText("Start");
        jpw_south.add(btn_start);

        JButton btn_join = new JButton();
        btn_join.setText("Join");
        jpw_south.add(btn_join);

        JButton btn_delete = new JButton();
        btn_delete.setText("Delete");
        jpw_south.add(btn_delete);
        jPanel_west.add(jpw_south, "South");


        // East panel.
        JPanel jPanel_east = new JPanel();
        jPanel_east.setLayout(new BorderLayout());

        JLabel lbl_topiclist = new JLabel();
        lbl_topiclist.setText("Topic list:");
        jPanel_east.add(lbl_topiclist, "North");

        txtAr_topiclist = new JTextArea();
        JScrollPane jScrollPane_topicList = new JScrollPane(txtAr_topiclist);
        txtAr_topiclist.setEditable(false);
        jPanel_east.add(jScrollPane_topicList, "Center");

        Dimension preferredSize = base_panel.getPreferredSize();
        preferredSize.width = preferredSize.width*20;
        base_panel.setPreferredSize(preferredSize);

        JPanel newpanel = new JPanel();
        newpanel.setLayout(new GridLayout(1, 2, 10, 10));

        texsttext = new JTextField(16);
        texsttext.setText("");
        newpanel.add(texsttext);

        JButton ok = new JButton();
        ok.setText("Post");
        newpanel.add(ok);


        frame.setResizable(false);
        base_panel.setPreferredSize(new Dimension(500, 200));
        base_panel.add(jPanel_west, "West");
        base_panel.add(jPanel_east, "Center");
        base_panel.add(newpanel, "South");
        frame.pack();
        frame.setVisible(true);

        btn_start.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                createTopic();
            }
        });

        btn_join.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                joinTopic();
            }
        });

        btn_delete.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                deleteTopic(evt);
            }
        });

        ok.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                okPressed();
            }
        });
    }

    private void createTopic(){
        // Getting a name of the topic to be created.
        topicSelected = JOptionPane.showInputDialog("Give your topic a name");

        if (topicSelected.equals("")){
            return;
        }
        if (topicSelected == null ){
            return;
        }
        try{
            // Receiving Topic Nr.
            QueueStatus template = new QueueStatus();
            QueueStatus topicStatus = (QueueStatus) space.take(template, null, 1000);
            topicNumber = topicStatus.nextTopic;

            // Creating / Writing a new topic to the space.
            QueueItem newTopic = new QueueItem(topicNumber, topicSelected, username, password, getTimestamp(), "", username);
            space.write(newTopic, null, Lease.FOREVER);


            TopicList listTemplate = new TopicList(topicNumber, topicSelected, username);
            space.write(listTemplate, null, Lease.FOREVER);

            // Incrementing Topic Nr.
            topicStatus.incrementTopicNr();
            space.write(topicStatus, null, Lease.FOREVER);

            //Calling Topic room GUI.
            new TopicRoom2(topicNumber, topicSelected, username, password, getTimestamp(), "", username);

            // Adding newly created topic to the existing list.
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    private  void okPressed(){

        try{
            QueueStatus template = new QueueStatus();
            QueueStatus topicStatus = (QueueStatus) space.take(template, null, Long.MAX_VALUE);
            topicNumber = topicStatus.nextTopic;

            String timestamp = getTimestamp();
            String comment = texsttext.getText();

            QueueItem newTopic = new QueueItem(topicNumber, "dog", username, password, timestamp, comment, username);
            space.write(newTopic, null, Lease.FOREVER);

            topicStatus.incrementTopicNr();
            space.write(topicStatus, null, Lease.FOREVER);
            appendToTextArea();

            // Open Topic Rooom chat
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void joinTopic(){
        topicSelected = JOptionPane.showInputDialog("Select a Topic to Join");
        String owner = null;

        try{
            QueueItem qiTemplate = new QueueItem();
            qiTemplate._topicName = topicSelected;  // THIS IS THE MAIN CHANGE.  Set the destination printer name in the template so as to retrieve only the correct print jobs
            QueueItem nextJob = (QueueItem) space.readIfExists(qiTemplate,null, 2000);
            if (nextJob == null) {
                JOptionPane.showMessageDialog(null, "This topic does not exist.", "Name Error!", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("Topic received");
                owner = nextJob._topicOwner;
            }

            QueueStatus template = new QueueStatus();
            QueueStatus topicStatus = (QueueStatus) space.take(template, null, 2000);
            topicNumber = topicStatus.nextTopic;

            String timestamp = getTimestamp();
            String comment = texsttext.getText();

            QueueItem newTopic = new QueueItem(topicNumber, topicSelected, username, password, timestamp, comment, owner);
            space.write(newTopic, null, Lease.FOREVER);

            topicStatus.incrementTopicNr();
            space.write(topicStatus, null, Lease.FOREVER);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void deleteTopic(ActionEvent event){
        topicSelected = JOptionPane.showInputDialog("Select a Topic to Delete");
    }

    private void appendToTextArea(){
        if (topicSelected != null){
            String appendix = topicNumber + ". " + topicSelected + ", " + "\n";
            ArrayList<String> topics = new ArrayList<>();
            if (!topics.contains(appendix)){
                topics.add(appendix);
                for (String topic : topics){
                    txtAr_topiclist.append(topic);
                }

            }
        }
    }

    public static String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf.format(timestamp)+"";
    }

    public static void main(String[] args){
        new Main();
    }

}
