package iogenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import event.*;
import scheduler.*;
 
public class Main {
	
	/**
	 * Create and call the chain: Input file -> Driver -> Scheduler -> Executor -> Output files 
	 * @param args: 
	 */
	public static void main (String[] args) { 
		
		try {
		
		/*** Print current time ***/
		Date dNow = new Date( );
	    SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
	    System.out.println("----------------------------------\nCurrent Date: " + ft.format(dNow));
	    
	    Path currentRelativePath = Paths.get("");
	    String s = currentRelativePath.toAbsolutePath().toString();
	    System.out.println("Current relative path is: " + s);
	    
	    /*** Input and output ***/
	    // Set default values
	    String path = "iofiles/";
		String inputfile = "stream1.txt";
		String outputfile = "sequences.txt";		
		
		boolean realtime = true;
		boolean overlap = false;
	    int lastsec = 0;
		int window_length = 0;
		int window_slide = 0;	
		int algorithm = 4;
		double memory_limit = Double.MAX_VALUE;
		int cut_number = 1;
		int search_algorithm = 1;
				
		// Read input parameters
	    for (int i=0; i<args.length; i++){
			if (args[i].equals("-path")) 		path = args[++i];
			if (args[i].equals("-file")) 		inputfile = args[++i];
			if (args[i].equals("-realtime")) 	realtime = Integer.parseInt(args[++i]) == 1;
			if (args[i].equals("-overlap")) 	overlap = Integer.parseInt(args[++i]) == 1;
			if (args[i].equals("-sec")) 		lastsec = Integer.parseInt(args[++i]);
			if (args[i].equals("-wl")) 			window_length = Integer.parseInt(args[++i]);
			if (args[i].equals("-ws")) 			window_slide = Integer.parseInt(args[++i]);
			if (args[i].equals("-algo")) 		algorithm = Integer.parseInt(args[++i]);
			if (args[i].equals("-mem")) 		memory_limit = Double.parseDouble(args[++i]);
			if (args[i].equals("-cut")) 		cut_number = Integer.parseInt(args[++i]);
			if (args[i].equals("-search")) 		search_algorithm = Integer.parseInt(args[++i]);
		}
	    String input = path + inputfile;
	    OutputFileGenerator output = new OutputFileGenerator(path+outputfile); 
	    if (!overlap) {
	    	window_length = lastsec+1;
	    	window_slide = lastsec+1;
	    }
	    
	    // Print input parameters
	    System.out.println(	"Input file: " + inputfile +
	    					"\nReal time: " + realtime +
	    					"\nOverlapping window: " + overlap +
	    					"\nLast sec: " + lastsec +
	    					"\nWindow length: " + window_length + 
							"\nWindow slide: " + window_slide +
							"\nAlgorithm: " + algorithm +
							"\nMemory limit: " + memory_limit +
							"\nCut number: " + cut_number +
							"\nSearch algorithm: " + search_algorithm +
							"\n----------------------------------");

		/*** SHARED DATA STRUCTURES ***/		
		AtomicInteger driverProgress = new AtomicInteger(-1);	
		EventQueue eventqueue = new EventQueue(driverProgress);						
		CountDownLatch done = new CountDownLatch(1);
		long startOfSimulation = System.currentTimeMillis();
		AtomicLong processingTime = new AtomicLong(0);	
		AtomicInteger eventNumber = new AtomicInteger(0);
		AtomicInteger maxMemoryPerWindow = new AtomicInteger(0);
		
		/*** EXECUTORS ***/
		int number_of_executors = 3;
		ExecutorService executor = Executors.newFixedThreadPool(number_of_executors);
			
		/*** Create and start the event driver and the scheduler threads.
		 *   Driver reads from the file and writes into the event queue.
		 *   Scheduler reads from the event queue and submits event batches to the executor. ***/
		EventDriver driver = new EventDriver (input, realtime, lastsec, eventqueue, startOfSimulation, driverProgress, eventNumber);				
				
		Scheduler scheduler = new Scheduler (eventqueue, lastsec, window_length, window_slide, algorithm, memory_limit, cut_number, search_algorithm, 
				executor, driverProgress, done, processingTime, maxMemoryPerWindow, output);		
		
		Thread prodThread = new Thread(driver);
		prodThread.setPriority(10);
		prodThread.start();
		
		Thread consThread = new Thread(scheduler);
		consThread.setPriority(10);
		consThread.start();		
				
		/*** Wait till all input events are processed and terminate the executor ***/
		done.await();		
		executor.shutdown();	
		output.file.close();
		
		System.out.println(//"Event number: " + eventNumber.get() +
				"\nCPU: " + processingTime.get() +
				//"\nThroughput: " + eventNumber.get()/processingTime.get() +
				"\nMEM: " + maxMemoryPerWindow.get() + "\n");
				//"\nExecutor is done." +
				//"\nMain is done.");
			
		} catch (InterruptedException e) { e.printStackTrace(); }
		  catch (IOException e1) { e1.printStackTrace(); }
	}	
}