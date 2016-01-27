package transaction;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import event.*;

/** 
 * A transaction has an event batch, start of simulation and transaction number. 
 * @author Olga Poppe
 */
public abstract class Transaction implements Runnable {
	
	ArrayList<Event> batch;	
	long startOfSimulation;
	public CountDownLatch transaction_number;
	PrintWriter out;
	
	public Transaction (ArrayList<Event> b, long start, PrintWriter o) {		
		batch = b;		
		startOfSimulation = start;		
		out = o;
	}	

}
