package bgu.spl.mics.application;

import bgu.spl.mics.ReadFromJson;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.Intelligence;
import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;
import bgu.spl.mics.application.subscribers.Q;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {
    public static void main(String[] args) {
		
		//a list to contain all threads we create.
        List<Thread> threads = new LinkedList<>();
		//counter for thread, to use with latch.
        int threadNum = 0;
		
		//try to read input json.
        try {
            ReadFromJson myReader = new ReadFromJson(args[0]);

            //Inventory
            Inventory.getInstance().load(myReader.createInventory());

            //Squad
            Squad.getInstance().load(myReader.createSquad());

			//in order to promise that time service thread will start last.
            CountDownLatch latch = new CountDownLatch(threadNum);

            //creates a thread for Q.
            Q toCreateQ = new Q("Q",latch);
            Thread q = new Thread(toCreateQ);
            threads.add(q);
            threadNum++;
            q.start();

            //creates threads for M, by the given number.
            for (int j = 1; j <= myReader.getM(); j++) {
                M toCreateM = new M("M", j,latch);
                Thread m = new Thread(toCreateM);
                threads.add(m);
                threadNum++;
                m.start();
            }

            //creates threads for MoneyPenny, by the given number.
            for (int j = 1; j <= myReader.getMoneypenny(); j++) {
                Moneypenny toCreateMP = new Moneypenny("Moneypenny", j, latch);
                Thread moneypenny = new Thread(toCreateMP);
                threads.add(moneypenny);
                threadNum++;
                moneypenny.start();
            }

            //creates threads for Intelligence, by the given number.
            for (int j = 0; j < myReader.getIntelligence(); j++) {
                MissionInfo[] missions = myReader.getMissionArrayBySource(j);
                Intelligence toCreateIntel = new Intelligence("Intelligence", j+1, missions.length, latch);
                //creates missions for the instance.
				toCreateIntel.setMissions(missions);
                Thread intelligence = new Thread(toCreateIntel);
                threads.add(intelligence);
                threadNum++;
                intelligence.start();
            }

			//makes sure that all threads will start and initialize themselves.
			latch.await();

            // creates a thread for TimeService
            TimeService timeToCreate = new TimeService(myReader.getTimeDuration(), "TimeService");
            Thread time = new Thread(timeToCreate);
            threads.add(time);
            time.start();

        } catch (Exception  e) {
            e.printStackTrace();
        }
	
	    //waiting for all the threads to finish before generating the output files.
        for (Thread thread  : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //generating the output files
        Inventory.getInstance().printToFile(args[1]);
        Diary.getInstance().printToFile(args[2]);
    }
}

