package optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import graph.*;

public class Exhaustive implements Partitioner {	
	
	public Partitioning getPartitioning (Partitioning root, int memory_limit) {
		
		Partitioning solution = new Partitioning(new ArrayList<Partition>());
		
		double minCPU = Integer.MAX_VALUE;
		ArrayList<Integer> memCosts = new ArrayList<Integer>();
		double maxHeapSize = 0;
				
		LinkedList<Partitioning> heap = new LinkedList<Partitioning>();
		heap.add(root);
		
		while (!heap.isEmpty()) {		
			
			// Get current node, compute its costs and store its memory cost
			Partitioning temp = heap.poll();
			double temp_cpu = temp.getCPUcost();
			double temp_mem = temp.getMEMcost();
			memCosts.add(new Double(temp_mem).intValue());
			
			System.out.println("Considered: " + temp.toString());
			
			// Update the best solution seen so far
			if (minCPU > temp_cpu && temp_mem <= memory_limit) {
				minCPU = temp_cpu;
				solution = temp;
				
				System.out.println("Best so far: " + solution.toString());
			}
			// Add children to the heap
			ArrayList<Partitioning> children = temp.getChildren();
			for (Partitioning child : children) {				
				if (!heap.contains(child)) heap.add(child);
			}
			// Update max heap size
			if (maxHeapSize < heap.size()) maxHeapSize = heap.size();
		}
		// Compute median memory cost
		int length = memCosts.size();
		Integer[] array = new Integer [length];
		array = memCosts.toArray(array);
		Arrays.sort(array);
		int median = array[length/2];
		
		System.out.println("Max heap size: " + maxHeapSize +
				"\nMedian memory cost: " + median);
		
		return solution;		
	}
}
