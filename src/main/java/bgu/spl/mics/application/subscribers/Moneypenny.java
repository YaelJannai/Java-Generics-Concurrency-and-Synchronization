package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.passiveObjects.Squad;
import javafx.util.Pair;

import java.util.concurrent.CountDownLatch;

/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {

	private int serialNumber;
	private int myTime;
	private CountDownLatch latch;
	private boolean itsOver;

	public Moneypenny(String name, int serialNumber, CountDownLatch latch) {
		super(name);
		this.serialNumber = serialNumber;
		myTime = 0;
		this.latch = latch;
		itsOver = false;
	}

	@Override
	protected void initialize() {
		//Creating a callback for AgentsAvailableEvent
		Callback<AgentsAvailableEvent> AAECallBack = new Callback<AgentsAvailableEvent>() {
			@Override
			public void call(AgentsAvailableEvent c) {

				//if the flag that tell MoneyPenny that the time is over its false, then we can continue to execute the mission
				if(!itsOver){
					//trying to acquire the needed agents, and informing M about the result
					boolean ans = Squad.getInstance().getAgents(c.getAgents());
					Pair<Integer,Boolean> p = new Pair<>(serialNumber, ans);
					complete(c,p);
					//if all the agents that we need are acquired to this mission
					if(ans)
					{
						//when the status is 0 it means that Q didn't resolve the future yet
						int status = c.getStatus();
						//so we do sleep and check until he does.
						//MoneyPenny isn't suppose to sleep more than once, since Q is very fast.
						while (status == 0)
						{
							try {
								Thread.sleep(10);
								status = c.getStatus();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						//when the status is 1 it means that Q has resolved the future
						if(status == 1)
						{
							//we take the future that Q sent, and set MoneyPenny time to Q time because he is more updated
							Pair<Integer,Boolean> QResult = c.getGadgetFuture().get();
							myTime = QResult.getKey();

							//if the gadget exists and the time hasn't been expired yet
							if (QResult.getValue() && (myTime <= c.getMissionTimeExpired())){
								//MoneyPenny sends the agent to the mission
								// - means she sleep for the amount of time that mission is acquiring
								Squad.getInstance().sendAgents(c.getAgents(),c.getMissionDuration());
							}
							else
							{
								//the gadget isn't in the inventory or the time is expired we release the agents.
								Squad.getInstance().releaseAgents(c.getAgents());
							}
						}
						//when the status is -1 it means that Q isn't exist anymore
						//which means he got the last tick, and neither should MoneyPenny work anymore
						else {
							//so she release the agents she acquired and set the flag.
							Squad.getInstance().releaseAgents(c.getAgents());
							itsOver = true;
						}
					}
				}
				else
				{	//if its over the MoneyPenny doesn't work anymore, and tell M she can't acquire the agents
					Pair<Integer,Boolean> p = new Pair<>(serialNumber, false);
					complete(c,p);
				}
			}
		};

		//MoneyPenny subscribe to AgentsAvailableEvent with the callback above
		this.subscribeEvent(AgentsAvailableEvent.class, AAECallBack);

		//Creating a callback for TickBroadcast
		Callback<TickBroadcast> TBCallback = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast c) {
				//every time MoneyPenny gets a tickBroadcast she needs to set her time to the most recent one.
				myTime = Integer.max(c.getTimeTick(),myTime);
			}
		};

		//MoneyPenny subscribe to TickBroadcast with the callback above
		this.subscribeBroadcast(TickBroadcast.class, TBCallback);

		//Creating a callback for LastTickBroadcast
		Callback<LastTickBroadcast> LTBCallback = new Callback<LastTickBroadcast>() {
			@Override
			public void call(LastTickBroadcast c) {
				//when we got the last tick MoneyPenny is terminated and the she unregister from the message broker.
				myTime = c.getEndTime();
				terminate();
			}
		};

		//MoneyPenny subscribe to LastTickBroadcast with the callback above
		this.subscribeBroadcast(LastTickBroadcast.class, LTBCallback);

		//when MoneyPenny finish initializing the latch go one down, so that the TimeService will initialized last
		latch.countDown();
	}
}
