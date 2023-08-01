package digital.hd.workshop.eventsourcing.common.es;

public class InvocationContext {

    private final String user;

    public InvocationContext(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}
