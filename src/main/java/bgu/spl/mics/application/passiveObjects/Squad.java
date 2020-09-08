package bgu.spl.mics.application.passiveObjects;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Squad {

	private Map<String, Agent> agents;
	//sigelton, so we create ont instance of it.
	private static Squad instance = new Squad();

	private Squad() {
		agents = new HashMap<>();
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Squad getInstance() {
		return instance;
	}

	/**
	 * Initializes the squad. This method adds all the agents to the squad.
	 * <p>
	 * @param agents 	Data structure containing all data necessary for initialization
	 * 						of the squad.
	 */
	public void load (Agent[] agents) {
		for (Agent agent : agents) {
			this.agents.putIfAbsent(agent.getSerialNumber(), agent);
		}
	}

	/**
	 * Releases agents.
	 */
	public void releaseAgents(List<String> serials){
		for (String number :serials)
		{
			//release agents from a mission, don't want anyone to touch him while we do it.
			synchronized (agents.get(number)) {
				agents.get(number).release();
				//want anyone who wated for the agent to know that he is free now.
				agents.get(number).notifyAll();
			}
		}
	}

	/**
	 * simulates executing a mission by calling sleep.
	 * @param time   milliseconds to sleep
	 */
	public void sendAgents(List<String> serials, int time){
		try
		{
			//sends the agents to a mission for a given time.
			Thread.sleep(time*100);
			for (String number : serials)
			{
				//after the given time releases the agents we took for the mission and notufy anyone whi needs to know.
				synchronized (agents.get(number)) {
					agents.get(number).release();
					agents.get(number).notifyAll();
				}
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * acquires an agent, i.e. holds the agent until the caller is done with it
	 * @param serials   the serial numbers of the agents
	 * @return ‘false’ if an agent of serialNumber ‘serial’ is missing, and ‘true’ otherwise
	 */
	public boolean getAgents(List<String> serials){
		serials.sort(String::compareTo);
		//checks if all the given serials exists in the agents map.
		for (String number : serials) {
			if (agents.get(number) == null) {
				return  false;
			}
		}
		//runs over all the given serials and tries to acquire them.
		for (String number : serials)
		{
			//synchronize each agent so no one will change him while we work on him.
			//if an agent is not available, waits until he is.
			synchronized (agents.get(number)) {
				while (!agents.get(number).isAvailable()) {
					try {
						agents.get(number).wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				//acquire the agent for a mission.
				agents.get(number).acquire();
			}
		}
		return true;
	}

    /**
     * gets the agents names
     * @param serials the serial numbers of the agents
     * @return a list of the names of the agents with the specified serials.
     */
    public List<String> getAgentsNames(List<String> serials){
		//creates a new list to contain agents names.
		List<String> names = new LinkedList<>();
		//runs over the given serials and get the names of the agents given.
		for (String number : serials)
		{
			names.add(agents.get(number).getName());
		}
	    return names;
    }

}
