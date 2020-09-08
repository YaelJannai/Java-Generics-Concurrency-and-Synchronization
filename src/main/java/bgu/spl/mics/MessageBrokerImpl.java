package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
@SuppressWarnings("unchecked")
/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {

	// MessageBrokerImpl is a singelton, creates one instance for it/
	private static MessageBrokerImpl instance = new MessageBrokerImpl();

	//map of subscribers, and for each subscriber a queue for it's messages.
	private Map<Subscriber, BlockingQueue<Message>> queueMap;
	//map of events and for each event a list of anyone who registered for this kind of event.
	private Map<Class<? extends Event>, ConcurrentLinkedQueue<Subscriber>> eventMap;
	//map of broadcasts and for each broadcast a list of anyone who registered for this kind of event.
	private Map<Class<? extends Broadcast>, ConcurrentLinkedQueue<Subscriber>> broadcastMap;
	//map for events and for each event it's specific future that was created.
	private Map<Event<?>, Future> eventFutureMap;

	private MessageBrokerImpl() {
		queueMap = new ConcurrentHashMap<>();
		eventMap = new ConcurrentHashMap<>();
		broadcastMap = new ConcurrentHashMap<>();
		eventFutureMap = new ConcurrentHashMap<>();
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static MessageBroker getInstance() {
		return instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
		// if this event type is not already in the hash map, creates a queue for ir and insert the event and it's queue to the map.
		if (!eventMap.containsKey(type)) {
			ConcurrentLinkedQueue<Subscriber> bq = new ConcurrentLinkedQueue<>();
			eventMap.putIfAbsent(type, bq);
		}
		// if the hash map contain this type of event we don't want anyone to touch the event's queue while we change it
		//and enter a new subscriber to the queue.
		synchronized (eventMap.get(type)) {
			eventMap.get(type).add(m);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
		// if this broadcast is not already in the hash map, creates a queue for ir and insert the briadcast and it's queue to the map.
		if (!broadcastMap.containsKey(type)) {
			ConcurrentLinkedQueue<Subscriber> bq = new ConcurrentLinkedQueue<>();
			broadcastMap.putIfAbsent(type, bq);
		}
		// if the hash map contain this type of broadcast we don't want anyone to touch it's queue while we change it
		//and enter a new subscriber to the queue.
		synchronized (broadcastMap.get(type)) {
			broadcastMap.get(type).add(m);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// if the map of future events already coantains the future oa this event, we resolve it and then delete from map, we won't need this future again.
		if (eventFutureMap.containsKey(e)) {
			synchronized (eventFutureMap.get(e))
			{
				eventFutureMap.get(e).resolve(result);
				eventFutureMap.remove(e);
			}
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		//creates a temp copy of the broadcast subscribers queue, so it won't change while we work on it.
		ConcurrentLinkedQueue<Subscriber> subscribers;
		subscribers = broadcastMap.get(b.getClass());
		//if the broadcastMap doesn't contain this broadcast type, we don't want to do anything.
		if (!broadcastMap.containsKey(b.getClass())) {
			return;
		}
		// if the broadcast subscribers queue isn't empty, we'll run on the copy
		if (!subscribers.isEmpty()){
			subscribers.forEach((subscriber -> {
				//for each subscriber we'll check if he registeredto this broadcast, if he didn't - we will not do anything
				if (!queueMap.containsKey(subscriber)) {
					return;
				}
				// add the broadcast to the subscriber messages queue.
				queueMap.get(subscriber).add(b);
			}));
		}
	}



	@Override
	public <T> Future<T> sendEvent(Event<T> e) throws IllegalArgumentException{
		//checks if the map of all events contains the given one, if not, ,throws exception.
		if (!eventMap.containsKey(e.getClass())) {
			throw new IllegalArgumentException("No one is subscribed to this event");
		}
		//synchronise the subscribers queue of this event. so no one will change it while we take someone out of it.
		synchronized (eventMap.get(e.getClass())){
			//if the given event queue isn't empty deals with it, else - throws exception.
			if (!eventMap.get(e.getClass()).isEmpty()){
				//creates a new future for the event to use when it's finished
				Future<T> future = new Future<>();
				//pull out the first subscriber int the queue - to maintain round robin
				Subscriber subscriber = eventMap.get(e.getClass()).poll();
				//throws exception if the queue of subscribers doesn't recognize him - shouldn't happen
				if (!queueMap.containsKey(subscriber)) {
					throw new IllegalArgumentException("No one is subscribed to this event");
				}
				//and the given event to the queue of the subscriber we pulled out, and eneters the future created to the futures map. 
				queueMap.get(subscriber).add(e);
				eventFutureMap.putIfAbsent(e, future);
				//adds the subsciber we took out at the end of the queue - to maintain round robin.
				eventMap.get(e.getClass()).add(subscriber);
				return future;
			}
			else {
				throw new IllegalArgumentException("No one is subscribed to this event");
			}
		}
	}


	@Override
	public void register(Subscriber m) {
		//for a new subsciber, creates a new messages queue and enter them to the map.
		if(!queueMap.containsKey(m))
		{
			BlockingQueue<Message> qm = new LinkedBlockingQueue<>();
			queueMap.putIfAbsent(m,qm);
		}
	}

	@Override
	public void unregister(Subscriber m) {
		//for a given subscriber, find him in the subscribers map.
		if(queueMap.containsKey(m))
		{
			//for each messagein this subsciber queue - check if it's an event - if so, if it has a future created.
			for (Message message: queueMap.get(m) ) {
				if(message instanceof Event)
				{
					if(eventFutureMap.containsKey(message))
					{
						//find the future of this event in the future map, synchronize it so no one touch it during resolve.
						synchronized (eventFutureMap.get(message))
						{
							//resolve with null, because the subscriber won't handle these events after he is unregistered, and we want to 'pass it forward'.
							eventFutureMap.get(message).resolve(null);
							eventFutureMap.remove(message);
						}
					}
				}
			}
			queueMap.remove(m);

			// runs over events map and delete the subsciber from all the events he registered to.
			for(Map.Entry<Class<? extends Event>, ConcurrentLinkedQueue<Subscriber>> item : eventMap.entrySet())
			{
				//synchronize so no none will touch the event's queue while we delete from it.
				synchronized (item.getValue())
				{
					item.getValue().remove(m);
				}
			}
			
			// runs over broadcast map and delete the subsciber from all the broadcasts he registered to.
			for(Map.Entry<Class<? extends Broadcast>, ConcurrentLinkedQueue<Subscriber>> item : broadcastMap.entrySet())
			{
				//synchronize so no none will touch the broadcast's queue while we delete from it.
				synchronized (item.getValue())
				{
					item.getValue().remove(m);
				}
			}
		}
	}

	@Override
	public Message awaitMessage(Subscriber m) throws InterruptedException {
		//takes a message out of a given subsciber queue.
		if (!queueMap.containsKey(m)) {
			throw new InterruptedException();
		} else {
			return queueMap.get(m).take();
		}
	}
}
