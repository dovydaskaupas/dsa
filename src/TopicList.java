import net.jini.core.entry.Entry;

/**
 * Additional space entry, that helps to check how many topics exist on the space.
 * Used by createTopic() in Main.
 */
public class TopicList implements Entry {
    public Integer _id;
    public String _topicName;
    public String _topicOwner;

    public TopicList(){
    }

    public TopicList(int id, String name, String owner){
        _id = id;
        _topicName = name;
        _topicOwner = owner;
    }
}