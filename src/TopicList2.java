import net.jini.core.entry.Entry;

import java.util.HashMap;

public class TopicList2 implements Entry {
    public HashMap<Integer, String> topiclist = new HashMap<>();

    public TopicList2(){
    }

    public TopicList2(int topicNr, String topicName){
        topiclist.put(topicNr, topicName);
    }

    public void addToList(int topicNr, String topicName){
        topiclist.put(topicNr, topicName);
    }
}
