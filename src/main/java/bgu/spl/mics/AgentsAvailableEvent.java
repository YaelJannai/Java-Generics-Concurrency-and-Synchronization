package bgu.spl.mics;

import javafx.util.Pair;

import java.util.List;

public class AgentsAvailableEvent implements Event<Pair<Integer,Boolean>> {

    private List<String> agents;
    private int missionDuration;
    private int missionTimeExpired;
    private Future<Pair<Integer,Boolean>> gadgetFuture;

    private int status;

    /**
     *  Constructor of AgentsAvailableEvent
     * @param agents - a list of the needed agents for the mission
     * @param missionDuration - for how long the agents are suppose to be in the mission
     * @param missionTimeExpired - when the mission time is expired
     */
    public AgentsAvailableEvent(List<String> agents, int missionDuration, int missionTimeExpired )
    {
        this.agents = agents;
        this.missionDuration = missionDuration;
        this.missionTimeExpired = missionTimeExpired;

        //M gives also the future of the GadgetAvailableEvent so MoneyPenny will know if she needs to send the agents
        this.gadgetFuture = null;
        //-1 its over; 0 there isn't future yet; 1 there is a future
        this.status = 0;
    }

    public List<String> getAgents() {
        return agents;
    }

    public Future<Pair<Integer, Boolean>> getGadgetFuture() {
        return gadgetFuture;
    }

    public void setGadgetFuture(Future<Pair<Integer, Boolean>> gadgetFuture) {
        this.gadgetFuture = gadgetFuture;
    }

    public int getMissionDuration() {
        return missionDuration;
    }

    public int getMissionTimeExpired() {
        return missionTimeExpired;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
