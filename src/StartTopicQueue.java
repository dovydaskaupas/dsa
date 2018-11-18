import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class StartTopicQueue {

    
    private static final long ONE_SECOND = 1000;  // one thousand milliseconds

    public static void main(String args[]) {
        System.out.println("Z");
        JavaSpace space = SpaceUtils.getSpace();

        if (space == null) {
            System.err.println("Failed to find the Java Space");
            System.exit(1);
        }

        QueueStatus template = new QueueStatus();
        try {
            System.out.println("B");
            QueueStatus returnedObject = (QueueStatus)space.readIfExists(template,null, ONE_SECOND);
            if (returnedObject == null) {
                // there is no object in the space, so create one
                try {
                    System.out.println("C");
                    QueueStatus qs = new QueueStatus(1);
                    space.write(qs, null, Lease.FOREVER);
                    System.out.println("QueueStatus object added to space");
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // there is already an object available, so don't create one
                System.out.println("QueueStatus object is already in the space");
                System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
