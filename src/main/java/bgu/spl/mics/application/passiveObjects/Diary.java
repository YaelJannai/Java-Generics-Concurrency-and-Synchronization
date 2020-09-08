package bgu.spl.mics.application.passiveObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Passive object representing the diary where all reports are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Diary {

	private final List<Report> reports;
	private int total;
	private static Diary instance = new Diary();

	/**
	 * Retrieves the single instance of this class.
	 */
	private Diary()
	{
		reports = new LinkedList<>();
		total = 0;
	}

	public static Diary getInstance() {
		return instance;
	}

	public List<Report> getReports() {
		return reports;
	}

	/**
	 * adds a report to the diary
	 * @param reportToAdd - the report to add
	 */
	public void addReport(Report reportToAdd){
		//synchronize the report's list so no one will change it while we add something to it.
		synchronized (reports){
			reports.add(reportToAdd);
		}
	}

	/**
	 *
	 * <p>
	 * Prints to a file name @filename a serialized object List<Report> which is a
	 * List of all the reports in the diary.
	 * This method is called by the main method in order to generate the output.
	 */
	public void printToFile(String filename) {
		//creates a gson so we can write to file.
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			Writer writer = Files.newBufferedWriter(Paths.get(filename));
			//writes to the file the reports list, meaning 'this', all of the diary data.
			gson.toJson(this, writer);
			//closes the gson json file.
			writer.close();
		} catch (IOException e) {
				e.printStackTrace();
			}
	}



	/**
	 * Gets the total number of received missions (executed / aborted) be all the M-instances.
	 * @return the total number of received missions (executed / aborted) be all the M-instances.
	 */
	public int getTotal(){
		return total;
	}

	/**
	 * Increments the total number of received missions by 1
	 */
	public synchronized void incrementTotal(){
		//synchronize total because it's an int and we don't want it to be changed wrong.
		total++;
	}
}

