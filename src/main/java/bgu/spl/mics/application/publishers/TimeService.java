package bgu.spl.mics.application.publishers;

import bgu.spl.mics.LastTickBroadcast;
import bgu.spl.mics.Publisher;
import bgu.spl.mics.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this Publisher.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other subscribers about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends Publisher {
	private int duration;
	private final int TOTAL_TIME;

	public TimeService(int duration, String name){
		super(name);
		this.duration = duration;
		TOTAL_TIME = duration;
	}

	@Override
	protected void initialize()
	{
		//Because all the other threads are using latch the Time Service only will be initialized after they are all finished.

		//Creating a timer
		Timer timer = new Timer();
		TimerTask tickTack = new TimerTask() {
			@Override
			public void run() {
				//in every timer tick, if we didn't get to the final tick
				if(duration > 0)
				{
					//The Time Service sends a new TickBroadcast with the time
					TickBroadcast tb = new TickBroadcast(TOTAL_TIME-duration);
					getSimplePublisher().sendBroadcast(tb);

					//and then decreasing the duration by 1 time-tick
					duration -= 1;
				}
				//if we did get to the final tick
				else {
					//The Time Service sends a LastTickBroadcast with the time
					LastTickBroadcast ltb = new LastTickBroadcast(TOTAL_TIME);
					getSimplePublisher().sendBroadcast(ltb);

					//and then stopping the timer.
					timer.cancel();
				}
			}
		};

		//scheduling the timer to work since the beginning with 100 millisecond of period (1 time-tick)
		timer.schedule(tickTack,0, 100);
	}

	@Override
	public void run() {
		initialize();
	}
}
