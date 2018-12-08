import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Timer;

/**
 * This class serves as the main menu, allowing to Create, Join, Delete topics
 * and displays existing Topic List on the screen.
 */
public class Main extends JFrame{

    private JTextArea txtAr_topicList;
    private JTextField txt_userName;

    private String userName, password, topicSelected, topicName, topicOwner;
    private int topicNumber;

    private JavaSpace space;

    Main(){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
            System.exit(1);
        }

        userName = LoginScreen.getUsername();
        password = LoginScreen.getPassword();
        initInterface(userName, password);

        java.util.Timer timer = new Timer();
        timer.schedule(new TopicSearcher(txtAr_topicList), 0, 5000);
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
        //jpw_centre.setLayout(new GridLayout(4, 0, 10, 10));

        JLabel lbl_username = new JLabel();
        lbl_username.setText("User name: ");
        jpw_centre.add(lbl_username);

        txt_userName = new JTextField();
        txt_userName.setText("Logged in as: " + un + ", " + pwd);
        txt_userName.setEditable(false);
        jpw_centre.add(txt_userName);

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

        JLabel lbl_topicList = new JLabel();
        lbl_topicList.setText("Topic list:");
        jPanel_east.add(lbl_topicList, "North");

        txtAr_topicList = new JTextArea();
        JScrollPane jScrollPane_topicList = new JScrollPane(txtAr_topicList);
        txtAr_topicList.setEditable(false);
        jPanel_east.add(jScrollPane_topicList, "Center");

        Dimension preferredSize = base_panel.getPreferredSize();
        preferredSize.width = preferredSize.width * 20;
        base_panel.setPreferredSize(preferredSize);

        frame.setResizable(false);
        base_panel.setPreferredSize(new Dimension(500, 200));
        base_panel.add(jPanel_west, "West");
        base_panel.add(jPanel_east, "Center");
        frame.pack();
        frame.setVisible(true);

        btn_start.addActionListener (evt -> createTopic());

        btn_join.addActionListener (evt -> joinTopic());

        btn_delete.addActionListener (evt -> deleteTopic());
    }

    /**
     * Responsible for creating new topics. New topic creates new TopicItem object,
     * that stores topic id, name, comment number and owner's name. Comment number is incremented each time
     * someone posts a comment. It allows displaying comments for each topic correctly.
     */
    private void createTopic(){
        // Getting a name of the topic to be created.
        topicSelected = JOptionPane.showInputDialog("Give your topic a name");
        if (topicSelected == null){
            return;
        }
        if (topicSelected.length() >= 1){
            try{
                // Receiving Topic Nr.
                QueueStatus template = new QueueStatus();
                QueueStatus topicStatus = (QueueStatus) space.take(template, null, 1000);
                topicNumber = topicStatus.nextTopic;

                // Creating / Writing a new topic to the space.
                QueueItem newTopic = new QueueItem(topicNumber, topicSelected, userName, password, getTimestamp(), "", 1, userName, userName, "");
                space.write(newTopic, null, Lease.FOREVER);

                // Creating new Topic Item.
                TopicItem listTemplate = new TopicItem(topicNumber, topicSelected, userName, 1);
                space.write(listTemplate, null, Lease.FOREVER);

                // Incrementing Topic Nr and writing it to the space.
                topicStatus.incrementTopicNr();
                space.write(topicStatus, null, Lease.FOREVER);

                //Calling Topic room GUI.
                new TopicRoom(topicNumber, topicSelected, userName, password, userName);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Responsible for Join Topic functionality. Uses values returned by TopicItem object
     * in order to joining topics correctly.
     */
    private void joinTopic() {
        topicSelected = "";
        topicSelected = JOptionPane.showInputDialog("Type in the TOPIC NUMBER to Join");
        if (topicSelected != null){
            if(topicSelected.length() < 1){ return; }
            int inputReceived;

            try {
                inputReceived = Integer.parseInt(topicSelected);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Input must be a number!");
                return;
            }

            try {
                TopicItem template = new TopicItem();
                template._id = inputReceived;

                TopicItem topicToTake = (TopicItem) space.take(template, null, 900);
                topicToTake.incrementCommentNr();

                int topicNr = topicToTake._id;
                topicName = topicToTake._topicName;
                topicOwner = topicToTake._topicOwner;
                int commentNr = topicToTake._commentNr;

                QueueItem joinThis = new QueueItem(topicNr, topicName, userName, password, getTimestamp(), "", commentNr, topicOwner, userName, "");
                space.write(joinThis, null, Lease.FOREVER);

                space.write(topicToTake, null, Lease.FOREVER);

                new TopicRoom(topicNr, topicName, userName, password, topicOwner);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Topic does not exist");
            }
        }
    }

    /**
     * Takes objects from the space.
     */
    private void deleteTopic(){
        int inputReceived;

        // 1. Getting an id of a topic to delete.
        topicSelected = "";
        topicSelected = JOptionPane.showInputDialog("Type in the TOPIC NUMBER to Delete it");
        if (topicSelected != null){
            if(topicSelected.length() < 1){ return; }
            try {
                inputReceived = Integer.parseInt(topicSelected);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Input must be a number!");
                return;
            }

            // 2. Getting the number of topics that exist on the space.
            try {
                QueueItem temp = new QueueItem();
                temp._topicNumber = inputReceived;
                QueueItem retQI = (QueueItem) space.readIfExists(temp,null, 500);
                String retPwd = retQI._password;
                String retOwner = retQI._topicOwner;

                if(retPwd.equals(password) & retOwner.equals(userName)){
                    // 3. Taking objects from the space
                    try{
                        QueueStatus qsTemp = new QueueStatus();
                        qsTemp.nextTopic = inputReceived;
                        space.take(qsTemp, null, 500);

                        TopicItem tiTemp = new TopicItem();
                        tiTemp._id = inputReceived;
                        TopicItem removeTI = (TopicItem) space.take(tiTemp, null, 500);

                        int commentAmount = removeTI._commentNr;
                        String topicName = removeTI._topicName;

                        for(int i = 1; i < commentAmount + 1; i++) {
                            QueueItem qiTemp = new QueueItem();
                            qiTemp._topicName = topicName;
                            qiTemp._commentNr = i;
                            space.take(qiTemp, null, 500);
                        }
                    }catch(Exception e){
                        JOptionPane.showMessageDialog(null, "Topic does not exist");
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "You can only delete your own topics.");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets current time in format - HH.mm.ss.
     * @return - current time.
     */
    static String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf.format(timestamp)+"";
    }

    public static void main(String[] args){
        new Main();
    }
}