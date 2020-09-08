package bgu.spl.mics;

import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Q;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageBrokerTest {

    MessageBroker MB;


    @BeforeEach
    public void setUp(){
        MB = MessageBrokerImpl.getInstance();
    }

    @Test
    public void test(){
        CountDownLatch latch = new CountDownLatch(4);
        Subscriber subsQ = new Q("Q", latch);
        Subscriber subsM = new M("M", 1,latch);
        Event<String> event_string = new ExampleEvent("eventTST");
        Broadcast broadcast = new ExampleBroadcast("2");

        //test: Subscribe + send Event + await message
        MB.subscribeEvent(ExampleEvent.class, subsQ);
        MB.sendEvent(event_string);
        try {
            assertEquals(MB.awaitMessage(subsQ).getClass(), ExampleEvent.class);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        //test Broadcast + await message
        MB.subscribeBroadcast(ExampleBroadcast.class, subsM);
        MB.subscribeBroadcast(ExampleBroadcast.class, subsQ);
        MB.sendBroadcast(broadcast);
        try {
            assertEquals(MB.awaitMessage(subsM).getClass(), ExampleBroadcast.class);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        //test: Complete
        Future<String> future_string = MB.sendEvent(event_string);
        MB.complete(event_string, "1");
        assertTrue(future_string.isDone());

    }
}
