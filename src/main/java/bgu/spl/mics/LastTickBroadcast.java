package bgu.spl.mics;

public class LastTickBroadcast implements Broadcast {

    private int endTime;

    /**
     * The constructor of LastTickBroadcast
     * @param endTime - the tick of this broadcast
     */
    public LastTickBroadcast(int endTime){
        super();
        this.endTime = endTime;
    }

    public int getEndTime() {
        return endTime;
    }
}
