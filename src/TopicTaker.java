import net.jini.space.JavaSpace;

import javax.swing.*;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Helper class, that is being called (from TopicRoom class, by Timer.scheduler) every X second/s,
 * to check if anything new has been posted to the Topic room.
 */
public class TopicTaker extends TimerTask {

    private JavaSpace space;
    private String topicSelected;
    private JTextArea txtArea;
    private JLabel lable;

    public TopicTaker(String topic, JTextArea txtAr, JLabel lbl){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the javaspace");
            System.exit(1);
        }

        topicSelected = topic;
        txtArea = txtAr;
        lable = lbl;
    }

    @Override
    public void run() {
        checkSpace();
    }

    private void checkSpace(){
        ArrayList<String> comments = new ArrayList<>();
        String comment = "";

        try {
            TopicList tl = new TopicList();
            tl._topicName = topicSelected;
            TopicList result = (TopicList) space.read(tl,null, 500);

            int commentAmount = result._commentNr;

            for(int i = 1; i < commentAmount + 1; i++){
                QueueItem template = new QueueItem();
                template._commentNr = i;
                QueueItem queueItem = (QueueItem) space.read(template,null, 500);

                    int topicNumber = queueItem._topicNumber;
                    String topic_name = queueItem._topicName;
                    String username = queueItem._userName;
                    String password = queueItem._password;
                    String ts = queueItem._timestamp;
                    String comm = queueItem._comment;
                    String topicOwner = queueItem._topicOwner;
                    int commentNumber = queueItem._commentNr;

                    lable.setText("Topic Owner: "  + topicOwner + "              " + "Topic Name: " + topicNumber+ "." + topicSelected);

                    if (!comm.equals("")){
                        comment = "->" + ts + ", " + username + " says: " + comm + commentNumber + "\n";
                    }else{
                        comment = "->" + ts + ", " + username + " joined the topic room." + commentNumber + "\n";
                    }

                    if (!comments.contains(comment)){
                        comments.add(comment);
                    }

                    txtArea.setText("");
                    for (String com : comments){
                        txtArea.append(com);
                    }

                    /*
                    txtArea.append("");
                    if (!comm.equals("")){
                        txtArea.append("->" + ts + ", " + username + " says: " + comm + commentNumber + "\n");
                    }else{
                        txtArea.append("->" + ts + ", " + username + " joined the topic room." + commentNumber + "\n");
                    }
                    */
                //}

            }

            /*
            if (queueItem == null) {
                //System.out.println("No Topics");
            } else {
                //System.out.println("Topic received");
                int topicNumber = queueItem._topicNumber;
                String topic_name = queueItem._topicName;
                String username = queueItem._userName;
                String password = queueItem._password;
                String ts = queueItem._timestamp;
                String comm = queueItem._comment;
                String topicOwner = queueItem._topicOwner;
                int commentNumber = queueItem._commentNr;

                lable.setText("Topic Owner: "  + topicOwner + "              " + "Topic Name: " + topicNumber+ "." + topicSelected);

                txtArea.append("");
                if (!comm.equals("")){
                    txtArea.append("->" + ts + ", " + username + " says: " + comm + commentNumber + "\n");
                }else{
                    txtArea.append("->" + ts + ", " + username + " joined the topic room." + commentNumber + "\n");
                }

            }*/
        }  catch ( Exception e) {
            e.printStackTrace();
        }
    }
}
