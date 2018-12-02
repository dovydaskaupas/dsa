import net.jini.space.JavaSpace;

import javax.swing.*;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Helper class, that is being called (from TopicRoom class, by Timer.scheduler) every X second/s,
 * to check if anything new has been posted to the Topic room.
 */
public class CommentPublisher extends TimerTask {

    private JavaSpace space;
    private String topicSelected;
    private JTextArea txtArea;
    private JLabel lable;
    private String loggedAs;

    CommentPublisher(String topic, JTextArea txtAr, JLabel lbl, String loggedUser){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
            System.exit(1);
        }

        topicSelected = topic;
        txtArea = txtAr;
        lable = lbl;
        loggedAs = loggedUser;
    }

    /**
     * Every X seconds runs checkSpace() method checking for new comments.
     */
    @Override
    public void run() {
        checkSpace();
    }

    /**
     * When called, reads the space for comments and adds all to the ArrayList.
     * Every item from the ArrayList is appended to the textArea.
     */
    private void checkSpace(){
        ArrayList<String> comments = new ArrayList<>();
        String comment;

        try {
            TopicList tl = new TopicList();
            tl._topicName = topicSelected;
            TopicList result = (TopicList) space.read(tl,null, 500);

            int topicNumber = result._id;
            String topicOwner = result._topicOwner;
            int commentAmount = result._commentNr;

            for(int i = 1; i < commentAmount + 1; i++){
                QueueItem template = new QueueItem();
                template._topicName = topicSelected;
                template._commentNr = i;
                QueueItem queueItem = (QueueItem) space.read(template,null, 500);

                String userName = queueItem._userName;
                String ts = queueItem._timestamp;
                String comm = queueItem._comment;
                int commentNumber = queueItem._commentNr;

                lable.setText("Topic Owner: "  + topicOwner + "  |  Topic Name: " + topicNumber+ "." + topicSelected + "  |  " + "Logged as: " + loggedAs);

                if (!comm.equals("")){
                    comment = "->" + ts + ", " + userName + " says: " + comm + commentNumber + "\n";
                }else{
                    if(comments.size() != 0){
                        comment = "->" + ts + ", " + userName + " joined the topic room." + commentNumber + "\n";
                    }else{
                        comment = "->" + ts + ", " + userName + " created the topic room." + commentNumber + "\n";
                    }
                }

                if (!comments.contains(comment)){
                    comments.add(comment);
                }

                txtArea.setText("");
                for (String com : comments){
                    txtArea.append(com);
                }
            }
        }  catch ( Exception e) {
            e.printStackTrace();
        }
    }
}
