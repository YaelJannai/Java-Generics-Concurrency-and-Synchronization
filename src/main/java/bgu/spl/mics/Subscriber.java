package bgu.spl.mics;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
/**
 * The Subscriber is an abstract class that any subscriber in the system
 * must extend. The abstract Subscriber class is responsible to get and
 * manipulate the singleton {@link MessageBroker} instance.
 * <p>
 * Derived classes of Subscriber should never directly touch the MessageBroker.
 * the derived class also supplies a {@link Callback} that should be called when
 * a message of the subscribed type was taken from the Subscriber
 * message-queue (see {@link MessageBroker#register(Subscriber)}
 * method). The abstract Subscriber stores this callback together with the
 * type of the message is related to.
 * 
 * Only private fields and methods may be added to this class.
 * <p>
 */
public abstract class Subscriber extends RunnableSubPub {
    private boolean terminated = false;
    private Map<Class<? extends Message>,Callback> callbackMap;

    /**
     * @param name the Subscriber nMame (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public Subscriber(String name) {
        super(name);
        //the callbackMap stores by message as a key the callback that go with this event as value
        callbackMap = new HashMap<>();
    }

    /**
     * Subscribes to events of type {@code type} with the callback
     * {@code callback}. This means two things:
     * 1. Subscribe to events in the singleton MessageBroker using the supplied
     * {@code type}
     * 2. Store the {@code callback} so that when events of type {@code type}
     * are received it will be called.
     * <p>
     * For a received message {@code m} of type {@code type = m.getClass()}
     * calling the callback {@code callback} means running the method
     * {@link Callback#call(java.lang.Object)} by calling
     * {@code callback.call(m)}.
     * <p>
     * @param <E>      The type of event to subscribe to.
     * @param <T>      The type of result expected for the subscribed event.
     * @param type     The {@link Class} representing the type of event to
     *                 subscribe to.
     * @param callback The callback that should be called when messages of type
     *                 {@code type} are taken from this Subscriber message
     *                 queue.
     */
    protected final <T, E extends Event<T>> void subscribeEvent(Class<E> type, Callback<E> callback) {
        //when subscribing to an event, we stores the callback with the event as a key in the callback map
        this.callbackMap.putIfAbsent(type,callback);
        //we tell the MessageBroker that we want to subscribe to this event
        MessageBrokerImpl.getInstance().subscribeEvent(type,this);
    }

    /**
     * Subscribes to broadcast message of type {@code type} with the callback
     * {@code callback}. This means two things:
     * 1. Subscribe to broadcast messages in the singleton MessageBroker using the
     * supplied {@code type}
     * 2. Store the {@code callback} so that when broadcast messages of type
     * {@code type} received it will be called.
     * <p>
     * For a received message {@code m} of type {@code type = m.getClass()}
     * calling the callback {@code callback} means running the method
     * {@link Callback#call(java.lang.Object)} by calling
     * {@code callback.call(m)}.
     * <p>
     * @param <B>      The type of broadcast message to subscribe to
     * @param type     The {@link Class} representing the type of broadcast
     *                 message to subscribe to.
     * @param callback The callback that should be called when messages of type
     *                 {@code type} are taken from this Subscriber message
     *                 queue.
     */
    protected final <B extends Broadcast> void subscribeBroadcast(Class<B> type, Callback<B> callback) {
        //when subscribing to a broadcast, we stores the callback with the broadcast as a key in the callback map
        this.callbackMap.putIfAbsent(type,callback);
        //we tell the MessageBroker that we want to subscribe to this broadcast
        MessageBrokerImpl.getInstance().subscribeBroadcast(type,this);
    }

    /**
     * Completes the received request {@code e} with the result {@code result}
     * using the MessageBroker.
     * <p>
     * @param <T>    The type of the expected result of the processed event
     *               {@code e}.
     * @param e      The event to complete.
     * @param result The result to resolve the relevant Future object.
     *               {@code e}.
     */
    protected final <T> void complete(Event<T> e, T result) {
        MessageBrokerImpl.getInstance().complete(e, result);
    }

    /**
     * Signals the event loop that it must terminate after handling the current
     * message.
     */
    protected final void terminate() {
        this.terminated = true;
    }

    /**
     * The entry point of the Subscriber. TODO: you must complete this code
     * otherwise you will end up in an infinite loop.
     */
    @Override
    public final void run() {
        //when starting to run, we first register to the MessageBroker
        MessageBrokerImpl.getInstance().register(this);
        initialize();
        while (!terminated) {
            try {
                //while the subscriber hasn't terminated it takes a new message from its queue in MessageBroker
                Message myMessage = MessageBrokerImpl.getInstance().awaitMessage(this);
                if (callbackMap.containsKey(myMessage.getClass()))
                {
                    //its calling to the callback that is assign to the given message
                    callbackMap.get(myMessage.getClass()).call(myMessage);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        //when the subscriber terminated, it unregister from the MessageBroker and finish running.
        MessageBrokerImpl.getInstance().unregister(this);
    }
}
