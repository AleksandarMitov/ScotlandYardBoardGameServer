package scotlandyard;

public interface MapQueue<X, Y> {

    public Entry<X, Y> pop();

    public Entry<X, Y> peek();

    public Y get(X key);

    public void remove(X key);

    public void put(X key, Y value);

    interface Entry<X, Y> {

        public X getKey();

        public Y getValue();

    }

}
