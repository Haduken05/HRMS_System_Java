package dataObject;

public class StatusCount {

    public final int pending;
    public final int approved;
    public final int disapproved;

    public StatusCount(int pending, int approved, int disapproved) {
        this.pending     = pending;
        this.approved    = approved;
        this.disapproved = disapproved;
    }
}