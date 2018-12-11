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
    private JLabel lableTop, lableUs;
    private String loggedAs;

    CommentPublisher(String topic, JTextArea txtAr, JLabel lblTopic, JLabel lblUser, String loggedUser){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
            System.exit(1);
        }

        topicSelected = topic;
        txtArea = txtAr;
        lableTop = lblTopic;
        lableUs = lblUser;
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
     * Reads the space for comments and adds them all to the ArrayList.
     * Every item from the ArrayList is appended to the textArea.
     */
    private void checkSpace(){
        ArrayList<String> comments = new ArrayList<>();
        String comment;

        try {
            TopicItem tl = new TopicItem();
            tl._topicName = topicSelected;

            if (topicSelected == null) {
                return;
            }

            TopicItem result = (TopicItem) space.read(tl,null, 500);

            if (result == null){
                return;
            }

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
                String commentOwner = queueItem._commentOwner;
                String isPrivate = queueItem._isPrivate;

                // Toolbar labels.
                lableTop.setText("Topic Owner: "  + topicOwner + "  |  Topic Name: " + topicNumber+ "." + topicSelected);
                lableUs.setText("Logged as: " + loggedAs);

                // Determine the form and state of comment.
                if (!comm.equals("")){
                    if(isPrivate.equals("yes")){
                        if(loggedAs.equals(topicOwner) || loggedAs.equals(commentOwner)){
                            comment = "->" + ts + ", " + userName + " says: " + comm + "\n";
                        }else{
                            comment = "->" + ts + ", message is hidden." +"\n";
                        }
                    }else{
                        comment = "->" + ts + ", " + userName + " says: " + comm + "\n";
                    }
                }else{
                    if(comments.size() != 0){
                        comment = "->" + ts + ", " + userName + " joined the topic room." + "\n";
                    }else{
                        comment = "->" + ts + ", " + userName + " created the topic room." + "\n";
                    }
                }

                // Adds comment to the array if yet it does not exist there.
                if (!comments.contains(comment)){
                    comments.add(comment);
                }

                // Every comment from the array is appended on the JTextArea.
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
