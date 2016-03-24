package swing.application;

import scotlandyard.*;

import java.util.concurrent.*;

/**
 * A class to allow communication between Threads.
 */

public class ThreadCommunicator {

    private BlockingQueue<Packet> eventQueue;
    private BlockingQueue<Packet> updateQueue;

    /**
     * Constructor for ThreadCommunicator, creates BlockingQueues.
     */
    public ThreadCommunicator() {
        eventQueue = new ArrayBlockingQueue<Packet>(1024);
        updateQueue = new ArrayBlockingQueue<Packet>(1024);
    }

    public Packet takeEvent() {
        try {
            return eventQueue.take();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        return null;
    }

    public Packet takeUpdate() {
        try {
            return updateQueue.take();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        return null;
    }

    /**
     * Puts an event id and object onto the event queue
     * in accordance with our protocol.
     *
     * @param id the String id of this event
     * @param object the object for this event
     */
    public void putEvent(String id, Object object) {
        try {
            eventQueue.put(new Packet(id, object));
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    /**
     * Puts an update id and object onto the update queue
     * in accordance with our protocol.
     *
     * @param id the String id of this update
     * @param object the object for this update
     */
    public void putUpdate(String id, Object object) {
        try {
            updateQueue.put(new Packet(id, object));
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    /**
     * Clears the event queue
     */
    public void clearEvents() {
        eventQueue.clear();
    }

    /**
     * Clears the update queue
     */
    public void clearUpdates() {
        updateQueue.clear();
    }

    public class Packet {

        private String id;
        private Object object;

        public Packet(String id, Object object) {
            this.id = id;
            this.object = object;
        }

        public String getId() {
            return id;
        }

        public Object getObject() {
            return object;
        }

    }

}
