package bgu.spl.mics.application.passiveObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *  That's where Q holds his gadget (e.g. an explosive pen was used in GoldenEye, a geiger counter in Dr. No, etc).
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.O
 */
public class Inventory {
	private final List<String> gadgets;
	private static Inventory instance = new Inventory();

	private Inventory(){
		gadgets = new LinkedList<>();
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Inventory getInstance() {
		return instance;
	}

	/**
     * Initializes the inventory. This method adds all the items given to the gadget
     * inventory.
     * <p>
     * @param inventory 	Data structure containing all data necessary for initialization
     * 						of the inventory.
     */
	public void load (String[] inventory) {
		gadgets.addAll(Arrays.asList(inventory));
	}
	
	/**
     * acquires a gadget and returns 'true' if it exists.
     * <p>
     * @param gadget 		Name of the gadget to check if available
     * @return 	‘false’ if the gadget is missing, and ‘true’ otherwise
     */
	public boolean getItem(String gadget){
		//checks if the gadgets list contains a given gadget.
		if (gadgets.contains(gadget))
		{
			//synchronize the list so no one will change it while deletion.
			synchronized (gadgets)
			{
				gadgets.remove(gadget);
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * <p>
	 * Prints to a file name @filename a serialized object List<Gadget> which is a
	 * List of all the reports in the diary.
	 * This method is called by the main method in order to generate the output.
	 */
	public void printToFile(String filename){
		//synchronize the gadgets list, so it won't be changed while we print it.
		synchronized (gadgets){
			Gson gson = new GsonBuilder().create();
			try {
				//creates a gson and write to the file the gdgets left.
				Writer writer = Files.newBufferedWriter(Paths.get(filename));
				gson.toJson(gadgets, writer);
				writer.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
