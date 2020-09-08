package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.concurrent.CountDownLatch;


/**
 * A Publisher\Subscriber.
 * Holds a list of Info objects and sends them
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {

	private MissionInfo[] missions;
	private int myTime;
	private int serialNumber;
	private int missionsNum;
	private CountDownLatch latch;

	public Intelligence(String name, int serialNumber, int missionsNum, CountDownLatch latch) {
		super(name);
		this.serialNumber = serialNumber;
		this.missionsNum = missionsNum;
		missions = new MissionInfo[this.missionsNum];
		myTime = 0;
		this.latch = latch;
	}

	//this method is call from the main when it sets the mission array for this intelligence
	//(this method is called only once when the intelligence is created)
	public void setMissions(MissionInfo[] missions) {
		this.missions = missions;
	}

	@Override
	protected void initialize() {
		//Creating a callback for TickBroadcast
		Callback<TickBroadcast> TBCallBack = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast t) {
				//every time Intelligence gets a tickBroadcast she needs to set her time to the most recent one.
				myTime = t.getTimeTick();

				//we run over all the missions in the mission array and check if their time issued is compatible with the time of the tick broadcast
				for (MissionInfo missionInfo : missions) {
					if (missionInfo.getTimeIssued() == t.getTimeTick()) {
						//Intelligence creates a new MissionReceivedEvent and sends it to M
						MissionReceivedEvent mrEvent = new MissionReceivedEvent(missionInfo);
						Future<Boolean> missionFuture = getSimplePublisher().sendEvent(mrEvent);
					}
				}
			}
		};

		//Intelligence subscribe to TickBroadcast with the callback above
		this.subscribeBroadcast(TickBroadcast.class,TBCallBack);

		//Creating a callback for LastTickBroadcast
		Callback<LastTickBroadcast> LTBCallback = new Callback<LastTickBroadcast>() {
			@Override
			public void call(LastTickBroadcast c) {
				//when we got the last tick Intelligence is terminated and the she unregister from the message broker.
				myTime = c.getEndTime();
				terminate();
			}
		};

		//Intelligence subscribe to LastTickBroadcast with the callback above
		this.subscribeBroadcast(LastTickBroadcast.class, LTBCallback);

		//when Intelligence finish initializing the latch go one down, so that the TimeService will initialized last
		latch.countDown();
	}
}
