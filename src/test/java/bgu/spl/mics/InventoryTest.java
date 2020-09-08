package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class InventoryTest {
    Inventory inv;

    @BeforeEach
    public void setUp(){
        inv = Inventory.getInstance();
    }

    @Test
    public void test(){
        String[] gadgetArr ={"Sky Hook", "Geiger counter", "X-ray glasses", "Dagger shoe"};
        inv.load(gadgetArr);

        //test: get item
        assertFalse(inv.getItem("Electrical pen"));
        assertTrue(inv.getItem("Sky Hook"));

        //test: load
        for (String item : gadgetArr) {
            assertTrue(inv.getItem(item));
        }

        String[] items2 = {"Winnie the pooh", "Christopher Robin", "Piglet", "eeyore"};
        for (String item : items2) {
            assertFalse(inv.getItem(item));
        }
    }
}
