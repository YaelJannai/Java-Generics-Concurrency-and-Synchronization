package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * This class handles all the reading and parsing of the json file.
 * We use it in order for prior preparartion for the threads we need to create.
 * in order to have all the data needed before the program start. 
*/
public class ReadFromJson {

   private JsonObject parser;
   private JsonObject services;

	/**
	 * this function gets the json file we want to read.
	 * creates a parser to go over the file and extract data from it.
	 * @param filePath	the path of the input fie.
	*/
	public ReadFromJson(String filePath) throws IOException {
		Reader jsonReader = Files.newBufferedReader(Paths.get(filePath));
        this.parser = JsonParser.parseReader(jsonReader).getAsJsonObject();
        this.services = parser.get("services").getAsJsonObject();
    }

	/**
	 * this function takes the data for creation of inventory, all the gadgets from the input.
	 * @return retrun the array of all gadgets in the agency.
	*/
    public String[] createInventory(){
        String[] gadgets = new String[parser.get("inventory").getAsJsonArray().size()];
        int i = 0;
        for (JsonElement gadget : parser.get("inventory").getAsJsonArray()) {
            String gadg = gadget.getAsString();
            gadgets[i] = gadg;
            i++;
        }
        return gadgets;
    }

	//gets the number of Ms to create.
    public int getM()
    {
        return services.get("M").getAsInt();
    }
	
	//gets the number of Moneypennys to create.
    public int getMoneypenny()
    {
        return services.get("Moneypenny").getAsInt();
    }

	//gets the number of Intelligences to create.
    public int getIntelligence()
    {
        return services.get("intelligence").getAsJsonArray().size();
    }

	//gets the total timeticks we'll have in the program.
    public int getTimeDuration(){
        return services.get("time").getAsInt();
    }

	//gets the total number of threads to create - +1 for Q.
    public int getTotal(){
        return (getM()+getIntelligence()+getMoneypenny()+1);
    }

	/**
	 * this function parse all the given missions for one intelligence instance.
	 * @param 	source is the instance number from the given intelligence 2 dimention array.
	 * @return retrun an aray of MissionInfo type, with the missions with all of their details.
	*/
    public MissionInfo[] getMissionArrayBySource(int source)
    {
        int i = 0;
		//gets the objects we want to parse in order to extract data on missions.
        JsonElement JSource = services.get("intelligence").getAsJsonArray().get(source);
        JsonObject sObj = JSource.getAsJsonObject();
        MissionInfo[]  missions = new MissionInfo[sObj.get("missions").getAsJsonArray().size()];
		
		//runs over each mission and gets all the details we need on it.
        for (JsonElement mission : sObj.get("missions").getAsJsonArray()) {
            JsonObject missObj = mission.getAsJsonObject();
            MissionInfo mi = new MissionInfo();
            mi.setMissionName(missObj.get("name").getAsString());
            mi.setGadget(missObj.get("gadget").getAsString());
            mi.setTimeIssued(missObj.get("timeIssued").getAsInt());
            mi.setTimeExpired(missObj.get("timeExpired").getAsInt());
            mi.setDuration(missObj.get("duration").getAsInt());

            List<String> agentsNumbers = new LinkedList<>();
            for (JsonElement agent : missObj.get("serialAgentsNumbers").getAsJsonArray()) {
                agentsNumbers.add(agent.getAsString());
            }

            mi.setSerialAgentsNumbers(agentsNumbers);
            missions[i] = mi;
            i++;
        }
        return missions;
    }

	/**
	 * this function creates the squad - parse the serial numbers of the agents the agency will have.
	 * @return return an array off agents created for the missions.
	*/
    public Agent[] createSquad()
    {
        Agent[] agents = new Agent[parser.get("squad").getAsJsonArray().size()];
        int i = 0;
        for (JsonElement agent : parser.get("squad").getAsJsonArray()) {
            JsonObject obj = agent.getAsJsonObject();
            Agent tmp = new Agent();
            tmp.setName(obj.get("name").getAsString());
            tmp.setSerialNumber(obj.get("serialNumber").getAsString());
            agents[i] = tmp;
            i++;
        }
        return agents;
    }


}
