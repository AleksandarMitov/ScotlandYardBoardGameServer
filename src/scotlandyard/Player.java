package scotlandyard;

import java.util.List;

public interface Player {

    void notify(int location, List<Move> list, Integer token, Receiver receiver);

}
