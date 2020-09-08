package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class FutureTest {

    Future<Integer> f;
    @BeforeEach
    public void setUp(){
        f = new Future<>();
    }

    @Test
    public void test(){
        //test: is Done
        assertFalse(f.isDone());
        //test: Get Timeout
        Integer result = f.get(1000, TimeUnit.MILLISECONDS);
        if(result != null){
            assertTrue(f.isDone());
        } else {
            fail();
        }
        f.resolve(2);
        //test: Get
        assertEquals(2,f.get());


    }
}
