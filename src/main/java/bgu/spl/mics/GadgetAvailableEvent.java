package bgu.spl.mics;

import javafx.util.Pair;

public class GadgetAvailableEvent implements Event<Pair<Integer,Boolean>> {
    private String gadget;

    /**
     *  The constructor of GadgetAvailableEvent
     * @param gadget - the gadget needed for the mission
     */
    public GadgetAvailableEvent(String gadget){
        this.gadget = gadget;
    }

    public String getGadget() {
        return gadget;
    }

}
