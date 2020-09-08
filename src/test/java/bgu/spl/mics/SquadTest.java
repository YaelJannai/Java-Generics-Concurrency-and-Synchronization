package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.Squad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SquadTest {

    Squad sq;

    @BeforeEach
    public void setUp() {

        sq = Squad.getInstance();

    }

    @Test
    public void test(){

        //test: load
        Agent a = new Agent();
        a.setSerialNumber("007");
        a.setName("James Bond");

        Agent b = new Agent();
        b.setSerialNumber("006");
        b.setName("Alec Trevelyan");

        Agent[] arr = {a,b};
        sq.load(arr);

        //test: get agents names
        List<String> serialList = new LinkedList<>();
        serialList.add(0,"006");
        serialList.add(1,"007");
        List<String> names = sq.getAgentsNames(serialList);

        List<String> wantedOutput = new LinkedList<>();
        wantedOutput.add(0, "Alec Trevelyan");
        wantedOutput.add(1, "James Bond");

        assertEquals(names.size(),wantedOutput.size());
        for (int i = 0; i < names.size(); i++) {
            assertEquals(wantedOutput.get(i),names.get(i));
        }

        //test: get agents
        assertTrue(sq.getAgents(serialList));
        serialList.add("008");
        assertFalse(sq.getAgents(serialList));
        serialList.remove("008");


        //test: send agents
        sq.sendAgents(serialList,1000);
        assertFalse(a.isAvailable());
        assertFalse(b.isAvailable());

        //test: release agents
        sq.releaseAgents(serialList);
        assertTrue(a.isAvailable());
        assertTrue(b.isAvailable());

    }
}
