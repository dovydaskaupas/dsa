import net.jini.space.JavaSpace;

import javax.swing.*;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * A helper class, that is being called (from Main class, by Timer.scheduler) every X second/s,
 * to check if any TopicItem objects been added to the space. If so, they are printed to the
 * accessible topic list.
 */
public class TopicSearcher extends TimerTask {
    private JavaSpace space;
    private JTextArea textArea;

    TopicSearcher(JTextArea textAr){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
            System.exit(1);
        }

        textArea = textAr;
    }

    @Override
    public void run() {
        searchForTopics(textArea);
    }

    /**
     * Method that reads all TopicItem objects from the space, adds them to the array list, and shows as the Topic List to the user.
     * @param txtAr - text area to post topics on.
     */
    private void searchForTopics(JTextArea txtAr){
        // A check if any topics have been added.
        if (txtAr.getText().equals("")){
            txtAr.append("No topics at the moment. Write a new one!");
        }

        // Creating an array list to store topics.
        ArrayList<String> topics = new ArrayList<>();

        try{
            TopicItem template = new TopicItem();
            for(int i = 1; i < 15; i++){
                template._id = i;
                TopicItem topicsOnline = (TopicItem) space.readIfExists(template, null, 1000);
                if(topicsOnline != null){
                    int topicNr = topicsOnline._id;
                    String topicName = topicsOnline._topicName;
                    String topicOwner = topicsOnline._topicOwner;

                    // Create a format for a topic.
                    String topicTitle = topicNr + "." + topicName + ". Author: " + topicOwner + "\n";

                    // Check if it's on the array list, if not - it's being added to the array.
                    if (!topics.contains(topicTitle)){
                        topics.add(topicTitle);
                    }

                    // Every single item of the array is posted on the TextArea.
                    txtAr.setText("");
                    for (String topic : topics){
                        txtAr.append(topic);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
