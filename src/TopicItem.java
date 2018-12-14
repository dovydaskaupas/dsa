import net.jini.core.entry.Entry;

/**
 * Stores information about topics that exist on the space.
 */
public class TopicItem implements Entry {
    public Integer _id;
    public String _topicName;
    public String _topicOwner;
    public Integer _commentNr;

    public TopicItem(){
    }

    public TopicItem(int id, String name, String owner, int commentNr){
        _id = id;
        _topicName = name;
        _topicOwner = owner;
        _commentNr = commentNr;
    }

    public void incrementCommentNr(){ _commentNr ++; }
}
