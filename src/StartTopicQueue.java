import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;

import javax.swing.*;

public class StartTopicQueue {
    
    private static final long ONE_SECOND = 1000;  // one thousand milliseconds

    StartTopicQueue(){
        startTopicQueue();
    }

    private void startTopicQueue(){
        JavaSpace space = SpaceUtils.getSpace();

        if (space == null) {
            JOptionPane.showMessageDialog(null, "Failed to find the JavaSpace", null, JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        QueueStatus template = new QueueStatus();
        try {
            QueueStatus returnedObject = (QueueStatus)space.readIfExists(template,null, ONE_SECOND);
            if (returnedObject == null) {
                // there is no object in the space, so create one
                Transaction trcQueue = Main.getTransactionCreated(500).transaction;
                try {
                    QueueStatus qs = new QueueStatus(1);
                    space.write(qs, trcQueue, Lease.FOREVER);
                    System.out.println("QueueStatus object added to space");
                    trcQueue.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    trcQueue.abort();
                }
            } else {
                // there is already an object available, so don't create one
                System.out.println("QueueStatus object is already in the space");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new StartTopicQueue();
    }
}
