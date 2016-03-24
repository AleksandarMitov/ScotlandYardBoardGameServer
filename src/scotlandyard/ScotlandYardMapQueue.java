package scotlandyard;

import java.util.*;
import java.util.concurrent.*;

public class ScotlandYardMapQueue<X, Y> implements MapQueue<X, Y> {

    private Map<X, Y> map;
    private Queue<Entry<X, Y>> queue;

    public ScotlandYardMapQueue() {
        this.map = new ConcurrentHashMap<X, Y>();
        this.queue = new ConcurrentLinkedQueue<Entry<X, Y>>();
    }

    public Entry<X, Y> pop() {
        Entry<X, Y> entry = queue.poll();
        if (entry != null) map.remove(entry.getKey());
        return entry;
    }

    public Entry<X, Y> peek() {
        return queue.peek();
    }

    public Y get(X key) {
        return map.get(key);
    }

    public void remove(X key) {
        Y value = map.get(key);
        map.remove(key);
        queue.remove(new ScotlandYardEntry<X, Y>(key, value));
    }

    public void put(X key, Y value) {
        map.put(key, value);
        queue.add(new ScotlandYardEntry<X, Y>(key, value));
    }

    class ScotlandYardEntry<X, Y> implements Entry<X, Y> {

        private X key;
        private Y value;

        public ScotlandYardEntry(X key, Y value) {
            this.key = key;
            this.value = value;
        }

        public X getKey() {
            return key;
        }

        public Y getValue() {
            return value;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ScotlandYardEntry) {
                ScotlandYardEntry entry = (ScotlandYardEntry) object;
                return entry.getKey().equals(this.getKey())
                        && entry.getValue().equals(this.getValue());
            }
            return false;
        }

    }

}
