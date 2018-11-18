import net.jini.space.JavaSpace;

import javax.swing.*;
import java.util.TimerTask;

/**
 * Helper class, that is being called (from TopicRoom2 class, by Timer.scheduler) every X second/s,
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
        try {
            QueueItem qiTemplate = new QueueItem();
            qiTemplate._topicName = topicSelected; // THIS IS THE MAIN CHANGE.  Set the destination printer name in the template so as to retrieve only the correct print jobs
            QueueItem nextJob = (QueueItem) space.take(qiTemplate,null, 950);

            if (nextJob == null) {
                System.out.println("No Topics");
            } else {
                System.out.println("Topic rceived");
                int nextJobNumber = nextJob._topicNumber;
                String topic_name = nextJob._topicName;
                String username = nextJob._userName;
                String password = nextJob._password;
                String ts = nextJob._timestamp;
                String comm = nextJob._comment;
                String topicOwner = nextJob._topicOwner;

                System.out.println("Job nr: "+ nextJobNumber+"");
                System.out.println("Username: "+ username);
                System.out.println("Topic name: "+topic_name);
                System.out.println("Password: "+password);
                System.out.println("Comment: "+comm);
                System.out.println("timestamp: "+ ts);
                System.out.println("owner: "+ topicOwner);

                lable.setText("Topic Owner: "  + topicOwner + "              " + "Topic Name: " + nextJobNumber+ "." + topicSelected);

                if (!comm.equals("")){
                    txtArea.append("->" + ts + ", " + username + " says: " + comm + "\n");
                }else{
                    txtArea.append("->" + ts + ", " + username + " joined the topic room." + "\n");
                }
            }
        }  catch ( Exception e) {
            e.printStackTrace();
        }
    }
}
