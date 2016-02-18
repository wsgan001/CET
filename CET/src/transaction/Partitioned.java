package transaction;

import iogenerator.OutputFileGenerator;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import event.*;
import optimizer.*;

public class Partitioned extends Transaction {
	
	Partitioning partitioning;
	int memory_limit;
	int search_algorithm;
	
	public Partitioned (ArrayList<Event> b, OutputFileGenerator o, CountDownLatch tn, AtomicLong pT, AtomicInteger mMPW, int ml, int sa) {
		super(b,o,tn,pT,mMPW);	
		memory_limit = ml;
		search_algorithm = sa;
	}

	public void run() {
		
		long start =  System.currentTimeMillis();		
		Partitioning rootPartitioning = Partitioning.constructRootPartitioning(batch);	
		Partitioner partitioner;
		if (search_algorithm==1) {
			partitioner = new Exhaustive();
			partitioning = partitioner.getPartitioning(rootPartitioning, memory_limit);
		} else {
		//if (search_algorithm==2) {
			partitioner = new BranchAndBound();
			partitioning = partitioner.getPartitioning(rootPartitioning, memory_limit);
		//} else {
			
		//}
		}
		//computeResults(graph.first_nodes);		
		long end =  System.currentTimeMillis();
		long processingDuration = end - start;
		processingTime.set(processingTime.get() + processingDuration);
		
		//writeOutput2File();
		transaction_number.countDown();
	}
}