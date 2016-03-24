package scotlandyard;

public enum Ticket {
    Bus, Taxi, Underground, Double, Secret;

    public static Ticket fromTransport(Transport route) {
        switch (route) {
            case Taxi:
                return Taxi;
            case Bus:
                return Bus;
            case Underground:
                return Underground;
            case Boat:
                return Secret;
            default:
                return Taxi;
        }
    }
}
