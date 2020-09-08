package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Report;
import bgu.spl.mics.application.passiveObjects.Squad;
import javafx.util.Pair;

import java.util.concurrent.CountDownLatch;

/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {

	private int serialNumber;
	private int myTime;
	private boolean itsOver;
	private CountDownLatch latch;

	public M(String name, int serialNumber, CountDownLatch latch) {
		super(name);
		this.serialNumber = serialNumber;
		myTime = 0;
		itsOver = false;
		this.latch = latch;
	}

	@Override
	protected void initialize() {
		//Creating a callback for MissionReceivedEvent
		Callback<MissionReceivedEvent> MRECallBack = new Callback<MissionReceivedEvent>() {
			@Override
			public void call(MissionReceivedEvent c) {
				//At the moment of receiving new mission we increase the total missions in the Diary.
				Diary.getInstance().incrementTotal();

				//if the time expired of the mission has already passed, we do not execute the mission
				if (myTime <= c.getMission().getTimeExpired())
				{
					//Creating new AgentsAvailableEvent and GadgetAvailableEvent to check for agents and the gadget we need for the mission
					AgentsAvailableEvent aaEvent = new AgentsAvailableEvent(c.getMission().getSerialAgentsNumbers(),c.getMission().getDuration(), c.getMission().getTimeExpired());
					GadgetAvailableEvent gaEvent = new GadgetAvailableEvent(c.getMission().getGadget());

					try {
						//we check if MoneyPenny found and acquired the needed agents
						Future<Pair<Integer, Boolean>> agentFuture = getSimplePublisher().sendEvent(aaEvent);
						Pair<Integer, Boolean> agentP = agentFuture.get();

						if (agentP != null && agentP.getValue()) {
							try {
								//now we check if Q found the needed gadget
								Future<Pair<Integer, Boolean>> gadgetFuture = getSimplePublisher().sendEvent(gaEvent);
								aaEvent.setGadgetFuture(gadgetFuture);
								Pair<Integer, Boolean> gadgetP = gadgetFuture.get();

								if(gadgetP != null)
								{
									//we inform MoneyPenny that Q has returned an answer, and we set M time to be Q time.
									aaEvent.setStatus(1);
									myTime = gadgetP.getKey();

									//if Q has found the gadget and the time still hasn't expired we report the mission in the Diary.
									if (gadgetP.getValue() && (myTime <= c.getMission().getTimeExpired()))
									{

										Report report = new Report();
										report.setMissionName(c.getMission().getMissionName());
										report.setM(serialNumber);
										report.setMoneypenny(agentP.getKey());
										report.setAgentsSerialNumbers(c.getMission().getSerialAgentsNumbers());
										report.setAgentsNames(Squad.getInstance().getAgentsNames(c.getMission().getSerialAgentsNumbers()));
										report.setGadgetName(c.getMission().getGadget());
										report.setTimeIssued(c.getMission().getTimeIssued());
										report.setQTime(gadgetP.getKey());
										report.setTimeCreated(myTime);

										Diary.getInstance().addReport(report);
										complete(c, true);
									}
									//if Q hasn't found the gadget or the expired time is over
									else {
										complete(c, false);
									}
								}
								//if Q wasn't around to find the gadget (probably because he got the last tick)
								else if(gadgetFuture.isDone())
								{
									//we inform MoneyPenny that the time is over, and also set it as a flag for M to know.
									aaEvent.setStatus(-1);
									itsOver = true;
								}
							}
							//there was no Q when we sent the event
							catch (IllegalArgumentException e)
							{
								//we inform MoneyPenny that the time is over, and also set it as a flag for M to know.
								aaEvent.setStatus(-1);
								itsOver = true;
							}
						}
						else {
							//for the second condition, future from MoneyPenny is false. in case of first condition, we just want to exit the program.
							complete(c, false);
						}
					}
					//there was no MoneyPenny when we sent the event
					catch (IllegalArgumentException e)
					{
						itsOver = true;
					}
				}

				//when the flag of itsOver is true, it means that one of subscribers that suppose to help M to complete the mission
				//has been unregister, and there is no one who can help M.
				//which means they got the last tick, and even though M didn't got that tick yet, she needs to terminate.
				if (itsOver)
				{
					terminate();
				}
			}
		};

		//M subscribe to MissionReceivedEvent with the callback above
		this.subscribeEvent(MissionReceivedEvent.class,MRECallBack);

		//Creating a callback for TickBroadcast
		Callback<TickBroadcast> TBCallback = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast c) {
				//every time M gets a tickBroadcast she needs to set her time to the most recent one.
				myTime = Integer.max(c.getTimeTick(), myTime);
			}
		};

		//M subscribe to TickBroadcast with the callback above
		this.subscribeBroadcast(TickBroadcast.class, TBCallback);

		//Creating a callback for LastTickBroadcast
		Callback<LastTickBroadcast> LTBCallback = new Callback<LastTickBroadcast>() {
			@Override
			public void call(LastTickBroadcast c) {
				//when we got the last tick M is terminated and the she unregister from the message broker.
				myTime = c.getEndTime();
				terminate();
			}
		};

		//M subscribe to LastTickBroadcast with the callback above
		this.subscribeBroadcast(LastTickBroadcast.class, LTBCallback);

		//when M finish initializing the latch go one down, so that the TimeService will initialized last
		latch.countDown();
	}

}
