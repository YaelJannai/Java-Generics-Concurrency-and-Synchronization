package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.MissionInfo;

public class MissionReceivedEvent implements Event<Boolean> {
   private MissionInfo mission;

    /**
     * The constructor of MissionReceivedEvent
     * @param mission - contains all the information of this mission
     */
   public MissionReceivedEvent(MissionInfo mission)
   {
       this.mission = mission;
   }

    public MissionInfo getMission() {
        return mission;
    }

}
