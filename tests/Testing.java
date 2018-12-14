import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;
import net.jini.space.MatchSet;
import org.junit.Test;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test class that conducts tests on functionality of:
 * - Creating new topics
 * - Deleting topics
 * - Notifying about new topics.
 * - Retrieving objects from the space.
 */
public class Testing implements RemoteEventListener {

    private JavaSpace space = SpaceUtils.getSpace();
    private Transaction trcTestNotify = Main.getTransactionCreated(HALF_A_SECOND * 2).transaction; // 1 second lease is given for transaction.
    private final static int HALF_A_SECOND = 500;

    // Values used for MarshalledObject as well as for NotifyTest result.
    private final static String NOTIFY_QS = "qs";
    private final static String NOTIFY_QI = "qi";
    private final static String NOTIFY_TI = "ti";

    // test fields
    private int topicNr = 0;
    private String topicName = "topic";
    private String userName = "userName";
    private String password = "password";
    private String timeStamp = "time";
    private String comment = "comment";
    private int commentNumber = 0;
    private String topicOwner = "topicOwner";
    private String commentOwner = "commentOwner";
    private String isPrivate = "yes";

    /**
     * Test of logic used in createTopic() method. For this test 10 QueueStatus / QueueItem / TopicItem objects are written to space,
     * with specified Field values + i. Then, each object is taken from space and each returned value is matched with expected value.
     */
    @Test
    public void testCreateTopic() {
        try{
            // Writes 10 QueueStatus / QueueItem / TopicItem objects to space.
            writeTenObjectsToSpace();

            for(int i = 1; i < 11; i++){
                // Taking objects out from space.
                QueueStatus templateQS = new QueueStatus();
                templateQS.nextTopic = i;
                QueueStatus resultQS = (QueueStatus) space.take(templateQS, null, HALF_A_SECOND);

                QueueItem templateQI = new QueueItem();
                templateQI._topicNumber = i;
                QueueItem resultQI = (QueueItem) space.take(templateQI, null, HALF_A_SECOND);

                TopicItem templateTI = new TopicItem();
                templateTI._id = i;
                TopicItem resultTI = (TopicItem) space.take(templateTI, null, HALF_A_SECOND);

                // asserting notNull for object ids 1 to 10;
                assertNotNull(resultQS);
                assertNotNull(resultQI);
                assertNotNull(resultTI);

                // asserting Equals for topic ids 1 to 10 for:
                // A. QueueStatus
                int statusN = resultQS.nextTopic;
                assertEquals(topicNr + i, statusN);

                // B. QueueItem
                int itemN = resultQI._topicNumber;
                int commentN = resultQI._commentNr;
                assertEquals(topicNr + i, itemN);
                assertEquals(topicName + i, resultQI._topicName);
                assertEquals(password + i, resultQI._password);
                assertEquals(timeStamp + i, resultQI._timestamp);
                assertEquals(comment + i, resultQI._comment);
                assertEquals(commentNumber + i, commentN);
                assertEquals(topicOwner + i, resultQI._topicOwner);
                assertEquals(commentOwner + i, resultQI._commentOwner);
                assertEquals(isPrivate + i, resultQI._isPrivate);

                // C. TopicItem
                int topicN = resultTI._id;
                int commentNr = resultTI._commentNr;
                assertEquals(topicNr + i, topicN);
                assertEquals(topicName + i, resultTI._topicName);
                assertEquals(topicOwner + i, resultTI._topicOwner);
                assertEquals(commentNumber + i, commentNr);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test of logic used in deleteTopic() method. For this test 10 QueueStatus / QueueItem / TopicItem objects are written to space, and
     * then taken out immediately. Next, looping again through the same object ID's and asserting for null, which is expected to be true,
     * because these objects had been taken out.
     */
    @Test
    public void testDeleteTopic() {
        QueueStatus qsToDelete = new QueueStatus();
        QueueItem qiToDelete = new QueueItem();
        TopicItem tiToDelete = new TopicItem();

        // Part A - 10 objects of each class are written and taken out from space.
        writeTenObjectsToSpace();

        try {
            for(int i = 1; i < 11; i++){
                qsToDelete.nextTopic = i;
                space.take(qsToDelete, null, HALF_A_SECOND);

                qiToDelete._topicNumber = i;
                space.take(qiToDelete, null, HALF_A_SECOND);

                tiToDelete._id = i;
                space.take(tiToDelete, null, HALF_A_SECOND);
            }
        } catch (TransactionException | RemoteException | InterruptedException | UnusableEntryException e) {
            e.printStackTrace();
        }

        // Part B - Looping again through the same object id's, that are expected to be null, and asserting whether it's true.
        try {
            for(int i = 1; i < 11; i++){
                qsToDelete.nextTopic = i;
                QueueStatus resultQS = (QueueStatus) space.take(qsToDelete, null, HALF_A_SECOND);

                qiToDelete._topicNumber = i;
                QueueItem resultQI = (QueueItem) space.take(qiToDelete, null, HALF_A_SECOND);

                tiToDelete._id = i;
                TopicItem resultTI = (TopicItem) space.take(tiToDelete, null, HALF_A_SECOND);

                assertNull(resultQS);
                assertNull(resultQI);
                assertNull(resultTI);
            }
        } catch (UnusableEntryException | TransactionException | InterruptedException | RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Test of logic used in enableNotifications() method. For this test a notification is set to QueueStatus / QueueItem / TopicItem objects.
     * Each of them has a MarshalledObject value assigned which allows to run a different logic for particular MarshalledObject returned in notify() method.
     * Transaction trcTestNotify is also set to write / take operations during this test.
     *
     * For this test it is expected that each object will trigger different IF statement, where unique value will be assigned to special variable,
     * which is equal to expected variable. Both will be tested in assertEquals() method, which should return true if notify was successful.
     */
    @Test
    public void testNotify() {
        // Assigning theStub object.
        RemoteEventListener theStub = null;
        try {
            Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(), false, true);
            theStub = (RemoteEventListener) myDefaultExporter.export(this);

        } catch (ExportException e) {
            e.printStackTrace();
        }

        // Creating templates of objects to notify about (Objects with id - 1).
        QueueStatus notifyQS = new QueueStatus();
        QueueItem notifyQI = new QueueItem();
        TopicItem notifyTI = new TopicItem();
        notifyQS.nextTopic = 1;
        notifyQI._topicNumber = 1;
        notifyTI._id = 1;

        // Creating the Marshalled Object to help to distinguish notifications.
        MarshalledObject<String> moQS = null;
        MarshalledObject<String> moQI = null;
        MarshalledObject<String> moTI = null;
        try {
            moQS = new MarshalledObject<>(NOTIFY_QS);
            moQI = new MarshalledObject<>(NOTIFY_QI);
            moTI = new MarshalledObject<>(NOTIFY_TI);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            space.notify(notifyQS, null, theStub, HALF_A_SECOND, moQS);
            space.notify(notifyQI, null, theStub, HALF_A_SECOND, moQI);
            space.notify(notifyTI, null, theStub, HALF_A_SECOND, moTI);

        } catch (TransactionException | RemoteException e) {
            e.printStackTrace();
        }

        // Writing created objects to the space, which should trigger the notifications for each of them in notify() method below.
        QueueStatus templateQS = new QueueStatus(1);
        QueueItem templateQI = new QueueItem(1, topicName, userName, password, timeStamp, comment, commentNumber, topicOwner, commentOwner, isPrivate);
        TopicItem templateTI = new TopicItem(1, topicName, topicOwner, commentNumber);

        try {
            space.write(templateQS, trcTestNotify, HALF_A_SECOND);
            space.write(templateQI, trcTestNotify, HALF_A_SECOND);
            space.write(templateTI, trcTestNotify, HALF_A_SECOND);
        } catch (TransactionException | RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notify(RemoteEvent remoteEvent) {
        String unMarshalled = null;
        // Getting marshalled objects.
        MarshalledObject mo = remoteEvent.getRegistrationObject();
        try {
            unMarshalled = (String) mo.get();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        String qsNotifyResult = null;
        String qiNotifyResult = null;
        String tiNotifyResult = null;

        // 3 marshalled objects have been associated with test variables.
        if(unMarshalled != null){
            if(unMarshalled.equals(NOTIFY_QS)){
                System.out.println(NOTIFY_QS);
                qsNotifyResult = "QueueStatus notification test was successful";
            }
            if(unMarshalled.equals(NOTIFY_QI)){
                qiNotifyResult = "QueueItem notification test was successful";
            }
            if(unMarshalled.equals(NOTIFY_TI)){
                tiNotifyResult = "TopicItem notification test was successful";
            }
        }
        // Asserting for match.
        assertEquals("QueueStatus notification test was successful", qsNotifyResult);
        System.out.println("Expected: QueueStatus notification test was successful, actual: " + qsNotifyResult);
        assertEquals("QueueItem notification test was successful", qiNotifyResult);
        assertEquals("TopicItem notification test was successful", tiNotifyResult);

        // Written objects are taken out from the space. Failure to do so would trigger the transaction.
        QueueStatus tempQS = new QueueStatus();
        QueueItem tempQI = new QueueItem();
        TopicItem tempTI = new TopicItem();
        tempQS.nextTopic = 1;
        tempQI._topicNumber = 1;
        tempTI._id = 1;
        try {
            space.take(tempQS, trcTestNotify, HALF_A_SECOND);
            space.take(tempQI, trcTestNotify, HALF_A_SECOND);
            space.take(tempTI, trcTestNotify, HALF_A_SECOND);
            trcTestNotify.commit();
        } catch (UnusableEntryException | TransactionException | InterruptedException | RemoteException e) {
            e.printStackTrace();
            try {
                trcTestNotify.abort();
            } catch (UnknownTransactionException | CannotAbortException | RemoteException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Test of functionality used for searching topics on space.
     */
    @Test
    public void testRetrievingFromSpace(){
        for(int i = 1; i < 11; i++){
            try {
                TopicItem templateTI = new TopicItem(i, topicName + i, topicOwner + i, commentNumber + i);
                space.write(templateTI, null, HALF_A_SECOND);
            } catch (TransactionException | RemoteException e) {
                e.printStackTrace();
            }
        }

        JavaSpace05 space = (JavaSpace05) SpaceUtils.getSpace();
        // Creating a collection to store wild cards of TopicItem objects. Each topic has it's own TopicItem object.
        Collection<TopicItem> templates = new ArrayList<>();

        TopicItem template = new TopicItem();
        templates.add(template);

        try {
            MatchSet results = space.contents(templates, null, HALF_A_SECOND, 100);
            // Looping while true. Value becomes false when 'result' returned = null.
            boolean isItem = true;
            int count = 0;
            while(isItem){
                TopicItem topicsOnline = (TopicItem) results.next();
                if(topicsOnline != null){
                    count++;
                    int topicID = topicsOnline._id;
                    String tName = topicsOnline._topicName;
                    String tOwner = topicsOnline._topicOwner;

                    // Asserting whether returned items match the expected ones.
                    assertEquals(topicNr + count, topicID);
                    assertEquals(topicName + count, tName);
                    assertEquals(topicOwner + count, tOwner);

                    // Asserting not equals with COUNT value removed.
                    assertNotEquals(topicNr, topicID);
                    assertNotEquals(topicName, tName);
                    assertNotEquals(topicOwner, tOwner);


                }else{
                    isItem = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that writes 10 QueueStatus / QueueItem / TopicItem objects to space, used to avoid repetition.
     */
    private void writeTenObjectsToSpace(){
        try{
            for(int i = 1; i < 11; i++){
                QueueStatus templateQS = new QueueStatus(topicNr + i);
                space.write(templateQS, null, HALF_A_SECOND);

                QueueItem templateQI = new QueueItem(topicNr + i, topicName+ i, userName + i, password + i, timeStamp + i, comment + i, commentNumber + i, topicOwner + i, commentOwner + i, isPrivate + i);
                space.write(templateQI, null, HALF_A_SECOND);

                TopicItem templateTI = new TopicItem(topicNr + i, topicName + i, topicOwner + i, commentNumber + i);
                space.write(templateTI, null, HALF_A_SECOND);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}