import net.jini.core.entry.Entry;

public class QueueItem implements Entry {
    public Integer _topicNumber;
    public String _topicName;
    public String _userName;
    public String _password;
    public String _timestamp;
    public String _comment;
    public String _topicOwner;

    public QueueItem(){
    }

    public QueueItem(int topicNumber, String topicName, String userName, String password, String timestamp, String comment, String topicOwner){
        _topicNumber = topicNumber;
        _topicName = topicName;
        _userName = userName;
        _password = password;
        _timestamp = timestamp;
        _comment = comment;
        _topicOwner = topicOwner;
    }
}