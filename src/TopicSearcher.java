import net.jini.space.JavaSpace;

import javax.swing.*;
import java.util.ArrayList;
import java.util.TimerTask;

public class TopicSearcher extends TimerTask {
    private JavaSpace space;
    private JTextArea textArea;

    TopicSearcher(JTextArea textAr){
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the javaspace");
            System.exit(1);
        }

        textArea = textAr;
    }

    @Override
    public void run() {
        searchForTopics(textArea);
    }

    private void searchForTopics(JTextArea txtAr){
        ArrayList<String> topics = new ArrayList<>();
        try{
            TopicList template = new TopicList();
            for(int i = 1; i < 15; i++){
                template._id = i;
                TopicList topicsOnline = (TopicList) space.readIfExists(template, null, 1000);
                if(topicsOnline != null){
                    int topicNr = topicsOnline._id;
                    String topicName = topicsOnline._topicName;
                    String topicOwner = topicsOnline._topicOwner;
                    String topicTitle = topicNr + "." + topicName + ". Author: " + topicOwner + "\n"; // Create a format for topic.

                    if (!topics.contains(topicTitle)){
                        topics.add(topicTitle);
                    }

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
