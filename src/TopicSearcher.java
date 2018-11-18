import net.jini.space.JavaSpace;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
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
        //searchForTopics(textArea);
    }

    private void searchForTopics(JTextArea txtAr){
        try{

            TopicList2 template = new TopicList2();
            TopicList2 topicsOnline = (TopicList2) space.read(template, null, 1000);
            if(topicsOnline != null){
                HashMap<Integer, String> receivedList = topicsOnline.topiclist;
                for (int i = 0; i < 10; i++){
                    String topicName = receivedList.get(i);

                    String formatToAppend = i + "." + topicName + "\n"; // Create a format for topic.


                    ArrayList<String> topics = new ArrayList<>();

                    if (!topics.contains(formatToAppend)){
                        topics.add(formatToAppend);
                        //txtAr.append("");
                        for (String topic : topics){
                            //txtAr.setText("");
                            txtAr.append(topic);
                        }
                    }
                }
            }else{
                if (!txtAr.getText().equals("No topics found. Create a new one!")){
                    txtAr.append("No topics found. Create a new one!");
                }
            }

            /*TopicList template = new TopicList();
            for (int i = 0; i < 10; i++){
                template._id = i;
                TopicList topicsOnline = (TopicList) space.readIfExists(template, null,Long.MAX_VALUE);
                if(topicsOnline != null){
                    int topicNr = topicsOnline._id;
                    String topicName = topicsOnline._topicName;
                    String topicOwner = topicsOnline._topicOwner;

                    String topicTitle = topicNr + "." + topicName + ". Author: " + topicOwner + "\n"; // Create a format for topic.

                    ArrayList<String> topics = new ArrayList<>();

                    if (!topics.contains(topicTitle)){
                        topics.add(topicTitle);
                        //txtAr.append("");
                        for (String topic : topics){
                            //txtAr.setText("");
                            txtAr.append(topic);
                        }
                    }
                }else{
                    if (!txtAr.getText().equals("No topics found. Create a new one!")){
                        txtAr.append("No topics found. Create a new one!" + "\n");
                    }
                }
            }*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
