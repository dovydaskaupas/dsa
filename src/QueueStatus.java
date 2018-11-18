import net.jini.core.entry.Entry;

public class QueueStatus implements Entry {
    public Integer nextTopic;
    public QueueStatus(){

    }

    public QueueStatus(int n){
        nextTopic = n;
    }

    public void incrementTopicNr(){ nextTopic ++; }
}