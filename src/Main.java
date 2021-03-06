import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseMap;
import net.jini.core.lease.LeaseMapException;
import net.jini.core.transaction.*;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Timer;

/**
 * This class implements an interface and functionality for the main menu.
 * It allows to Create, Join, Delete, get notifications about topics, and displays Topic List that exists on the space.
 */
public class Main extends JFrame implements RemoteEventListener {

    private JTextArea txtAr_topicList;
    private JTextField txt_notification;
    private JButton btn_notify;

    private String userName, password, topicSelected;
    private boolean isNotifying = false;

    private final static String NEW_C = "newComm"; // Used for marshalled objects.
    private final static String DEL_C = "delComm";
    private final static int TEN_MINS = 1000 * 60 * 10;

    private JavaSpace space;
    private LeaseMap notifyCommentLM, notifyDeleteLM;

    Main(){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
            System.exit(1);
        }

        // Gets username and hashed password.
        userName = LoginScreen.getUsername();
        password = LoginScreen.getPassword();
        initInterface(userName, password);

        // Every 5 seconds searches for topics on the space.
        java.util.Timer timer = new Timer();
        timer.schedule(new TopicSearcher(txtAr_topicList), 0, 5000);
    }

    @Override // When topic is selected for notifications, it notifies abotu new messages or when topic is deleted.
    public void notify(RemoteEvent remoteEvent){
        String unMarshalled = null;

        // Getting marshalled object.
        MarshalledObject mo = remoteEvent.getRegistrationObject();
        try {
            unMarshalled = (String) mo.get();
            System.out.println("marshalled: " + unMarshalled);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 2 marshalled objects have been associated with corresponding notifications.
        if(unMarshalled != null){
            if(unMarshalled.equals(NEW_C)){
                txt_notification.setText("You have unread comments.");
            }
            if(unMarshalled.equals(DEL_C)){
                txt_notification.setText("Pinned topic has been deleted.");
            }
        }
    }

    // Interface.
    private void initInterface(String un, String pwd) {
        JFrame frame = new JFrame("Bulletin Board");

        addWindowFocusListener(new WindowAdapter() {
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

        JTextField txt_userName = new JTextField();
        txt_userName.setText("Logged in as: " + un + ", " + pwd);
        System.out.println(pwd);
        txt_userName.setEditable(false);
        jpw_centre.add(txt_userName);

        jPanel_west.add(jpw_centre, "West");

        JPanel jpw_south = new JPanel();
        jpw_south.setLayout(new FlowLayout());

        JButton btn_start = new JButton();
        btn_start.setText("Create");
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

        // South panel.
        JPanel jPanel_south = new JPanel();
        jPanel_south.setLayout(new GridLayout(1, 2));

        btn_notify = new JButton();
        btn_notify.setText("Notify");
        jPanel_south.add(btn_notify);

        txt_notification = new JTextField();
        txt_notification.setText("No notifications");
        txt_notification.setEditable(false);
        jPanel_south.add(txt_notification);

        // Frame
        frame.setResizable(false);
        base_panel.setPreferredSize(new Dimension(500, 225));
        base_panel.add(jPanel_west, "West");
        base_panel.add(jPanel_east, "Center");
        base_panel.add(jPanel_south, "South");

        frame.pack();
        frame.setVisible(true);

        btn_start.addActionListener (evt -> createTopic());

        btn_join.addActionListener (evt -> joinTopic());

        btn_delete.addActionListener (evt -> deleteTopic());

        btn_notify.addActionListener(evt -> enableNotifications());
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
        if (topicSelected.length() >= 1){ // If input is shorter than 1 length, then nothing happens.

            if (topicSelected.length() > 21){
                JOptionPane.showMessageDialog(null, "Topic name can only be 21 symbols long.", null, JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Creating a transaction for QueueStatus take/write, and QueueItem and TopicItem write operations.
            Transaction trcCreate = getTransactionCreated(1000).transaction;
            try{
                // Receiving Topic Nr.
                QueueStatus template = new QueueStatus();
                QueueStatus topicStatus = (QueueStatus) space.take(template, trcCreate, 800);
                int topicNumber = topicStatus.nextTopic;

                // Creating / Writing a new QueueItem to the space with values: id, topicName, userName, password, time, comment, commentNr, topicOwner, commentOwner, isMessagePrivate.
                QueueItem newTopic = new QueueItem(topicNumber, topicSelected, userName, password, getTimestamp(), "", 1, userName, userName, "");
                space.write(newTopic, trcCreate, Lease.FOREVER);

                // Creating a new TopicItem, with values: topicID, topicName, topicOwner, totalNumberOfComments.
                TopicItem listTemplate = new TopicItem(topicNumber, topicSelected, userName, 1);
                space.write(listTemplate, trcCreate, Lease.FOREVER);

                // Incrementing Topic Nr and writing it to the space.
                topicStatus.incrementTopicNr();
                space.write(topicStatus, trcCreate, Lease.FOREVER);

                //Calling Topic room GUI.
                new TopicRoom(topicNumber, topicSelected, userName, password, userName);
                trcCreate.commit(); // Commiting the transaction when all write/take operations are completed.
            }catch(Exception e){
                e.printStackTrace();
                try {
                    trcCreate.abort();
                } catch (UnknownTransactionException | CannotAbortException | RemoteException e1) {
                    JOptionPane.showMessageDialog(null, "Error creating topic", null, JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
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
                JOptionPane.showMessageDialog(null, "Input must be a number!", null, JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Transaction for TopicItem take/write, and QueueItem write operations.
            Transaction trcJoin = getTransactionCreated(800).transaction;
            try {
                TopicItem template = new TopicItem();
                template._id = inputReceived;

                // TopicItem's commentNumber value is incremented because when user joins the topic,
                // a comment informing everyone about new user joining the topic room is posted.
                TopicItem topicToTake = (TopicItem) space.take(template, null, 500);
                topicToTake.incrementCommentNr();

                int topicNr = topicToTake._id;
                String topicName = topicToTake._topicName;
                String topicOwner = topicToTake._topicOwner;
                int commentNr = topicToTake._commentNr;

                QueueItem joinThis = new QueueItem(topicNr, topicName, userName, password, getTimestamp(), "", commentNr, topicOwner, userName, "");
                space.write(joinThis, trcJoin, Lease.FOREVER);

                space.write(topicToTake, trcJoin, Lease.FOREVER);

                new TopicRoom(topicNr, topicName, userName, password, topicOwner);
                trcJoin.commit();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Topic does not exist");
                try {
                    trcJoin.abort();
                } catch (CannotAbortException | RemoteException | UnknownTransactionException e1) {
                    System.out.print("Failed to cancel the transaction.");
                }
            }
        }
    }

    /**
     * Deletes selected object by pulling it out from the space. Only the actual owner can delete his topics.
     * Real owner is determined by comparing logged-in user's userName and password (which is hashed),
     * with topic owner's userName and password.
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
                JOptionPane.showMessageDialog(null, "Input must be a number!", null, JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2.1 Getting the password and topic owner's name of a topic that is about to be deleted.
            try {
                QueueItem temp = new QueueItem();
                temp._topicNumber = inputReceived;
                QueueItem retQI = (QueueItem) space.readIfExists(temp,null, 500);
                String retPwd = retQI._password;
                String retOwner = retQI._topicOwner;

                // 2.2 If topic's password and owner name match with currently logged-in user's password and user name, then it proceeds to delete topic.
                if(retPwd.equals(password) & retOwner.equals(userName)){

                    // 3. Taking objects out from the space
                    Transaction trcDelete = getTransactionCreated(800).transaction;
                    try{
                        QueueStatus qsTemp = new QueueStatus();
                        qsTemp.nextTopic = inputReceived;
                        space.take(qsTemp, trcDelete, 500);

                        TopicItem tiTemp = new TopicItem();
                        tiTemp._id = inputReceived;
                        TopicItem removeTI = (TopicItem) space.take(tiTemp, null, 500);

                        int commentAmount = removeTI._commentNr;
                        String topicName = removeTI._topicName;

                        // Every QueueItem belonging for that topic is removed from the space
                        for(int i = 1; i < commentAmount + 1; i++) {
                            QueueItem qiTemp = new QueueItem();
                            qiTemp._topicName = topicName;
                            qiTemp._commentNr = i;
                            space.take(qiTemp, trcDelete, 500);
                        }

                        // 4. Create a reference of deleted object. This is only for notifying about deleted topic.
                        DeletedItem deletedItem = new DeletedItem(inputReceived, topicName);
                        space.write(deletedItem, trcDelete, TEN_MINS);
                        trcDelete.commit();
                    }catch(Exception e){
                        e.printStackTrace();
                        trcDelete.abort();
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "You can only delete your own topics.", null, JOptionPane.ERROR_MESSAGE);
                }
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Topic does not exist", null, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Enables / Disables receiving notifications about specified topic.
     * Notifies only about: new comments, deleted topic.
     */
    private void enableNotifications(){
        int inputReceived;

        // Part A - if !isNotifying - if this bool is false, it runs logic allowing to get notifications about specified topics.
        // if it's 'true' then it means it's already listening for notifications, and PART B of the method is being active.
        if (!isNotifying){
            // 1. Validation.
            topicSelected = "";
            topicSelected = JOptionPane.showInputDialog("Type in the TOPIC NUMBER to get notifications about it");
            if (topicSelected != null) {
                if (topicSelected.length() < 1) {
                    return;
                }
                try {
                    inputReceived = Integer.parseInt(topicSelected);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Input must be a number!", null, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // 1.2 - Checking if topic exists.
                TopicItem checkIfExists = new TopicItem();
                checkIfExists._id = inputReceived;
                try {
                    TopicItem result = (TopicItem) space.read(checkIfExists, null, 500);
                    if (result == null){
                        JOptionPane.showMessageDialog(null, "Topic does not exist", null, JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (UnusableEntryException | TransactionException | InterruptedException | RemoteException e) {
                    e.printStackTrace();
                }

                // 2. Changes name of NOTIFY button to Disable Notifications, and enabling to disable notifications of 2nd click..
                btn_notify.setText("Disable notifications");
                isNotifying = true;

                // 3. Enables listening for notifications.
                RemoteEventListener theStub = null;
                try {
                    Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(), false, true);
                    theStub = (RemoteEventListener) myDefaultExporter.export(this);

                } catch (ExportException e) {
                    e.printStackTrace();
                }

                try {
                    // 4.1 Notify about: TopicItem for message notifications; DeleteItem about delete notifications.
                    TopicItem template = new TopicItem();
                    template._id = inputReceived;

                    DeletedItem diTemp = new DeletedItem();
                    diTemp._id = inputReceived;

                    // 4.2 Creating the Marshalled Object to help to distinguish different actions.
                    MarshalledObject<String> moComment = null;
                    MarshalledObject<String> moDelete = null;
                    try {
                        moComment = new MarshalledObject<>(NEW_C);
                        moDelete = new MarshalledObject<>(DEL_C);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // 4.3 Notification leases are added to the lease map(s).
                    EventRegistration ev = space.notify(template, null, theStub, TEN_MINS, moComment);
                    notifyCommentLM = ev.getLease().createLeaseMap(TEN_MINS);

                    EventRegistration evDel = space.notify(diTemp, null, theStub, TEN_MINS, moDelete);
                    notifyDeleteLM = evDel.getLease().createLeaseMap(TEN_MINS);
                } catch (TransactionException | RemoteException e) {
                    e.printStackTrace();
                }
                return;
            }
        } // Part B - if true (it means notifications are enabled) it cancels leases of objects that wait for notification-triggering events to occur.
        if (isNotifying){
            try {
                notifyCommentLM.cancelAll();
                notifyDeleteLM.cancelAll();
            } catch (LeaseMapException | RemoteException e) {
                e.printStackTrace();
            }
            isNotifying = false;
            btn_notify.setText("Notify");
            txt_notification.setText("No notifications.");
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

    /**
     * Creates Transaction.Created object, required by createTopic / joinTopic / deleteTopic methods for secured writes / takes.
     * @param leaseTime - lease time given for transaction to happen.
     * @return - returns Transaction.Created object.
     */
    static Transaction.Created getTransactionCreated(int leaseTime){
        TransactionManager tManager = SpaceUtils.getManager();
        if (tManager == null){
            System.err.println("Failed to find the TransactionManager");
            System.exit(1);
        }

        Transaction.Created trc = null;
        try{
            trc = TransactionFactory.create(tManager, leaseTime);
        }catch(Exception e){
            e.printStackTrace();
        }
        return trc;
    }

    public static void main(String[] args){
        new Main();
    }
}