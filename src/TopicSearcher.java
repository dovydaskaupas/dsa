import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;
import net.jini.space.MatchSet;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimerTask;

/**
 * A helper class, that is being called (from Main class, by Timer.scheduler) every X second/s,
 * to check if any TopicItem objects been added to the space. If so, they are printed on the topic list.
 */
public class TopicSearcher extends TimerTask {
    private JTextArea textArea;

    TopicSearcher(JTextArea textAr){
        JavaSpace space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
            System.exit(1);
        }
        textArea = textAr;
    }

    @Override
    public void run() {
        findTopics(textArea);
    }

    /**
     * Method that reads all TopicItem objects from the space, adds them to the array list, and shows as the Topic List.
     * @param txtAr - text area to post topics on.
     */
    private void findTopics(JTextArea txtAr){
        txtAr.setText("");
        txtAr.append("No topics at the moment. Write a new one!");

        // Creating an array list to store topics.
        ArrayList<String> topics = new ArrayList<>();

        // JavaSpace05 is required to use Collections and MatchSet
        JavaSpace05 space = (JavaSpace05) SpaceUtils.getSpace();
        if (space == null) {
            System.err.println("JavaSpace not found.");
            System.exit(1);
        }

        // Creating a collection to store wild cards of TopicItem objects. Each topic has it's own TopicItem object.
        Collection<TopicItem> templates = new ArrayList<>();
        TopicItem template = new TopicItem();
        templates.add(template);

        try { // Checking matching contents
            MatchSet results = space.contents(templates, null, 5000, 100);
            // Looping while true. Value becomes false when 'result' returned = null.
            boolean isItem = true;
            while(isItem){
                // Getting data from each.
                TopicItem topicsOnline = (TopicItem) results.next();
                if(topicsOnline != null){
                    int topicNr = topicsOnline._id;
                    String topicName = topicsOnline._topicName;
                    String topicOwner = topicsOnline._topicOwner;

                    // Creating format for a topic.
                    String topicTitle = topicNr + "." + topicName + ". Author: " + topicOwner + "\n";

                    // Checking if it's on the array list, if not - it's being added to the array.
                    if (!topics.contains(topicTitle)){
                        topics.add(topicTitle);
                    }

                    // Every single item of the array is posted on the TextArea.
                    txtAr.setText("");
                    for (String topic : topics){
                        txtAr.append(topic);
                    }
                }else{
                    isItem = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}