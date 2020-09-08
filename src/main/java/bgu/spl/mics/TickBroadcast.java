package bgu.spl.mics;

public class TickBroadcast implements Broadcast {

    private int timeTick;

    /**
     * The constructor of TickBroadcast
     * @param tick - the time we are now in
     */
    public TickBroadcast(int tick){
        super();
        timeTick = tick;
    }

    public int getTimeTick() {
        return timeTick;
    }
}
