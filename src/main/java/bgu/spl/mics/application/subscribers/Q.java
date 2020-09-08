package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.passiveObjects.Inventory;
import javafx.util.Pair;

import java.util.concurrent.CountDownLatch;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {

	private int myTime;
	private CountDownLatch latch;

	public Q(String name,CountDownLatch latch) {
		super(name);
		myTime = 0;
		this.latch = latch;
	}

	@Override
	protected void initialize() {

		//Creating a callback for GadgetAvailableEvent
		Callback<GadgetAvailableEvent> GAECallBack = new Callback<GadgetAvailableEvent>() {
			@Override
			public void call(GadgetAvailableEvent c) {
				//trying to get the needed gadget from the inventory
				boolean ans = Inventory.getInstance().getItem(c.getGadget());
				//we complete the event with the answer from the inventory and Q time.
				Pair<Integer,Boolean> p = new Pair<>(myTime,ans);
				complete(c,p);
			}
		};

		//Q subscribe to GadgetAvailableEvent with the callback above
		this.subscribeEvent(GadgetAvailableEvent.class, GAECallBack);

		//Creating a callback for TickBroadcast
		Callback<TickBroadcast> TBCallback = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast c) {
				//every time Q gets a tickBroadcast he needs to set his time to the most recent one.
				myTime = c.getTimeTick();
			}
		};

		//Q subscribe to TickBroadcast with the callback above
		this.subscribeBroadcast(TickBroadcast.class, TBCallback);

		//Creating a callback for LastTickBroadcast
		Callback<LastTickBroadcast> LTBCallback = new Callback<LastTickBroadcast>() {
			@Override
			public void call(LastTickBroadcast c) {
				//when we got the last tick Q is terminated and the he unregister from the message broker.
				myTime = c.getEndTime();
				terminate();
			}
		};

		//Q subscribe to LastTickBroadcast with the callback above
		this.subscribeBroadcast(LastTickBroadcast.class, LTBCallback);

		//when Q finish initializing the latch go one down, so that the TimeService will initialized last
		latch.countDown();
	}
}
