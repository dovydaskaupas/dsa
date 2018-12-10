import net.jini.core.entry.Entry;

/**
 * Object to reference deleted QueueItem.
 */
public class DeletedItem implements Entry {
    public Integer _id;
    public String _topicName;

    public DeletedItem(){
    }

    public DeletedItem(int id, String topicName){
        _id = id;
        _topicName = topicName;
    }
}
