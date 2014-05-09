package simproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class Simulator {
	
	public static final int NUMBER_OF_QUEUES = 5;
	public static final int NUMBER_OF_SERVERS = 3;
	public static final double STANDARD_CONNECTIVITY_PROB = 0.8;
	public static final double PACKET_ARRIVAL_PROB = 0.5;
	public static final double CORRELATION_FACTOR_A = -0.5;
	public static final double CORRELATION_FACTOR_B = 0.5;
	
	private int numberOfQueues;
	private int numberOfServers;
	private Connectivity[][] connectivityTable;
	private SimQueue[] queueList;
	private SimServer[] serverList;
	private CorrelatedUniformRandom packetRandomGenerator;
	public long totalPacketsArrived;
	public long totalPacketsAttempted;
	private int roundRobinQueueIndex;
	
	public Simulator(int noQueues, int noServers) {
		this.numberOfQueues = noQueues;
		this.numberOfServers = noServers;
		this.totalPacketsArrived = 0;
		this.totalPacketsAttempted = 0;
		this.roundRobinQueueIndex = 0;
		
		this.connectivityTable = new Connectivity[noQueues][noServers];
		for (int q = 0; q < noQueues; q++) {
			for (int s = 0; s < noServers; s++) {
				this.connectivityTable[q][s] = new Connectivity(q + 1, s + 1, Connectivity.NOT_CONNECTED);
			}
		}
		
		this.queueList = new SimQueue[noQueues];
		for (int sq = 0; sq < noQueues; sq++)
			this.queueList[sq] = new SimQueue(sq + 1);
		
		this.serverList = new SimServer[noServers];
		for (int server = 0; server < noServers; server++)
			this.serverList[server] = new SimServer(server + 1);
	}

	public Simulator() {
		this(NUMBER_OF_QUEUES, NUMBER_OF_SERVERS);
	}
	
	public void updateConnectivities(double prob) {
		
		for (SimServer server : this.serverList)
			server.resetConnections();
		for (SimQueue queue : this.queueList)
			queue.resetConnections();
		
		for (int q = 0; q < this.numberOfQueues; q++) {
			for (int s = 0; s < this.numberOfServers; s++) {
				if (StdRandom.bernoulli(prob) == true) {
					this.connectivityTable[q][s].status = 1;
					this.serverList[s].incrementConnection();
					this.queueList[q].incrementConnection();
				}
				else
					this.connectivityTable[q][s].status = 0;
			}
		}
	}
	
	public void recordOccupancies() {
		for (SimQueue queue : this.queueList)
			queue.recordOccupancy();
	}
	
	public double calculateAvgOccupancy(int times) {
		double netAvgOccupancy = 0;
		for (SimQueue queue : this.queueList)
			netAvgOccupancy += queue.calculateAvgOccupancy(times);
		return (netAvgOccupancy / this.numberOfQueues);
	}
	
	public HashSet<Integer> selectRandomServers() {
		HashSet<Integer> chosenServers = new HashSet<Integer>();
		for (int i = 0; i < this.numberOfServers; i++) {
			chosenServers.add((int) (1 + Math.random() * Simulator.NUMBER_OF_SERVERS));
		}
		return chosenServers;
	}
	
	public void scheduleRandomlySingleServer() {
		HashSet<Integer> selectedServers = this.selectRandomServers();
		int queueIndex = (int) (Math.random() * this.numberOfQueues);
		for (int server : selectedServers) {
			if ((this.connectivityTable[queueIndex][server - 1].status == 1) && (this.queueList[queueIndex].getRemainingPackets() > 0) && (!this.serverList[server - 1].isBusy())) {
				//System.out.println("Found connection: queue " + (q + 1) + " to server " + server);
				this.queueList[queueIndex].popPacket();
				this.serverList[server - 1].addServedQueue(queueIndex + 1);
				this.serverList[server - 1].goBusy();
			}
		}
	}
	
	public void scheduleRoundRobinSingleServer() {
		int queueIndex = (this.roundRobinQueueIndex + 1) % this.numberOfQueues;
		this.roundRobinQueueIndex = queueIndex;
		if ((this.connectivityTable[queueIndex][0].status == 1) && (this.queueList[queueIndex].getRemainingPackets() > 0)) {
			this.queueList[queueIndex].popPacket();
			this.serverList[0].addServedQueue(queueIndex + 1);
			this.serverList[0].goBusy();
		}
	}
	
	public void scheduleLCQSingleServer() {
		SimQueue[] sortedQueues = this.sortQueueList();
		for (SimQueue queue : sortedQueues) {
			int queueIndex = queue.getQueueNumber() - 1;
			if ((this.connectivityTable[queueIndex][0].status == 1) && (this.queueList[queueIndex].getRemainingPackets() > 0)) {
				this.queueList[queueIndex].popPacket();
				this.serverList[0].addServedQueue(queueIndex + 1);
				this.serverList[0].goBusy();
				break;
			}
		}
	}
	
	public void scheduleRandomly() {
		HashSet<Integer> selectedServers = this.selectRandomServers();
		int serversFree = selectedServers.size();
		for (int queueIndex = 0; queueIndex < this.numberOfQueues; queueIndex++) {
			for (int server : selectedServers) {
				if ((this.connectivityTable[queueIndex][server - 1].status == 1) && (this.queueList[queueIndex].getRemainingPackets() > 0) && (!this.serverList[server - 1].isBusy())) {
					//System.out.println("Found connection: queue " + (q + 1) + " to server " + server);
					this.queueList[queueIndex].popPacket();
					this.serverList[server - 1].addServedQueue(queueIndex + 1);
					this.serverList[server - 1].goBusy();
					//System.out.println(selectedServers.toString());
					serversFree--;
					//break;
				}
			}
			if (serversFree <= 0)
				break;
		}
	}
	
	public void scheduleASLCQ() {
		HashSet<Integer> selectedServers = this.selectRandomServers();
		int serversFree = selectedServers.size();
		int queueNumber;
		int queueIndex;
		int serverIndex;
		SimQueue[] sortedQueues = this.sortQueueList();
		for (int sortedQueueIndex = sortedQueues.length - 1; sortedQueueIndex >= 0; sortedQueueIndex--) {
			queueNumber = sortedQueues[sortedQueueIndex].getQueueNumber();
			queueIndex = queueNumber - 1;
			for (int server : selectedServers) {
				serverIndex = server - 1;
				if ((this.connectivityTable[queueIndex][serverIndex].status == 1) && (this.queueList[queueIndex].getRemainingPackets() > 0) && (!this.serverList[serverIndex].isBusy())) {
					//System.out.println("Found connection: queue " + (q + 1) + " to server " + server);
					this.queueList[queueIndex].popPacket();
					this.serverList[serverIndex].addServedQueue(queueNumber);
					this.serverList[serverIndex].goBusy();
					//System.out.println(selectedServers.toString());
					serversFree--;
					//break;
				}
			}
			if (serversFree <= 0)
				break;
		}
	}
	
	public void scheduleLCSFLCQ() {
		int queueNumber;
		int queueIndex;
		int serverNumber;
		int serverIndex;
		SimQueue[] sortedQueues = this.sortQueueList();
		SimServer[] sortedServers = this.sortServerList();
		int serversFree = sortedServers.length;
		for (int sortedQueueIndex = sortedQueues.length - 1; sortedQueueIndex >= 0; sortedQueueIndex--) {
			queueNumber = sortedQueues[sortedQueueIndex].getQueueNumber();
			queueIndex = queueNumber - 1;
			for (int sortedServerIndex = 0; sortedServerIndex < sortedServers.length; sortedServerIndex++) {
				serverNumber = sortedServers[sortedServerIndex].getServerNumber();
				serverIndex = serverNumber - 1;
				if ((this.connectivityTable[queueIndex][serverIndex].status == 1) && (this.queueList[queueIndex].getRemainingPackets() > 0) && (!this.serverList[serverIndex].isBusy())) {
					//System.out.println("Found connection: queue " + (q + 1) + " to server " + server);
					this.queueList[queueIndex].popPacket();
					this.serverList[serverIndex].addServedQueue(queueNumber);
					this.serverList[serverIndex].goBusy();
					//System.out.println(selectedServers.toString());
					serversFree--;
					//break;
				}
			}
			if (serversFree <= 0)
				break;
		}
	}
	
	public SimServer[] sortServerList() {
		SimServer[] sortedServers = Arrays.copyOf(this.serverList, this.numberOfServers);
		Arrays.sort(sortedServers, new ServerComparator());
		return sortedServers;
	}
	
	public SimQueue[] sortQueueList() {
		SimQueue[] sortedQueues = Arrays.copyOf(this.queueList, this.numberOfQueues);
		Arrays.sort(sortedQueues, new QueueComparator());
		return sortedQueues;
	}
	
	public void generatePackets(double packetArrivalProb) {
		int packetArrived = 0;
		for (int queue = 0; queue < this.numberOfQueues; queue++) {
			if (this.numberOfServers == 1)
				packetArrived = this.packetRandomGenerator.nextBernouliiRandom(packetArrivalProb);
			else if (this.numberOfServers > 1)
				packetArrived = this.packetRandomGenerator.nextBernouliiCorrelatedRandom(packetArrivalProb);
			this.totalPacketsAttempted++;
			if (packetArrived == 1) {
				this.queueList[queue].addPacket(new SimPacket());
				this.totalPacketsArrived++;
			}
		}
	}
	
	public void releaseServers() {
		for (SimServer server : this.serverList)
			server.goIdle();
	}
	
	public void runSimulation(int times, double connectivityProb, double packetArrivalProb, double aBound, double bBound) {
		this.refreshSimulation();
		this.packetRandomGenerator = new CorrelatedUniformRandom(aBound, bBound);
		this.generatePackets(packetArrivalProb);
		for (int timeSlot = 1; timeSlot <= times; timeSlot++) {
			this.updateConnectivities(connectivityProb);
			// Choose your scheduling policy here
			//this.scheduleRandomlySingleServer();
			//this.scheduleRoundRobinSingleServer();
			//this.scheduleLCQSingleServer();
			//this.scheduleRandomly();
			//this.scheduleASLCQ();
			this.scheduleLCSFLCQ();
			this.recordOccupancies();
			this.generatePackets(packetArrivalProb);
			this.releaseServers();
		}
	}
	
	public void refreshSimulation() {
		this.connectivityTable = new Connectivity[this.numberOfQueues][this.numberOfServers];
		for (int q = 0; q < this.numberOfQueues; q++) {
			for (int s = 0; s < this.numberOfServers; s++) {
				this.connectivityTable[q][s] = new Connectivity(q + 1, s + 1, Connectivity.NOT_CONNECTED);
			}
		}
		
		this.queueList = new SimQueue[this.numberOfQueues];
		for (int sq = 0; sq < this.numberOfQueues; sq++)
			this.queueList[sq] = new SimQueue(sq + 1);
		
		this.serverList = new SimServer[this.numberOfServers];
		for (int server = 0; server < this.numberOfServers; server++)
			this.serverList[server] = new SimServer(server + 1);
	}
	
	public void printConnectivityTable() {
		for (int q = 0; q < this.numberOfQueues; q++) {
			System.out.println();
			for (int s = 0; s < this.numberOfServers; s++) {
				System.out.print(this.connectivityTable[q][s].status + " ");
			}
		}
	}
	
	public void printServerList() {
		System.out.println();
		System.out.print("[ ");
		for (SimServer server : this.serverList) {
			System.out.print(server.getServerNumber() + ": " + server.getConnections() + ", ");
		}
		System.out.print("]\n");
	}
	
	public void printQueueList() {
		System.out.println();
		System.out.print("[ ");
		for (SimQueue queue : this.queueList) {
			System.out.print(queue.getQueueNumber() + ": " + queue.getLastAvgOccupancy() + ", ");
		}
		System.out.print("]\n");
	}
	
	public double calculateOccupancyAverage() {
		double count = 0;
		double average = 0;
		for (SimQueue queue : this.queueList) {
			for (long delay : queue.getDelayTimes()) {
				count++;
			}
		}
		average = count / 50000.0;
		return average;
	}
	
	public double calculateOccupancyConfidenceInterval(double average) {
		double totalVariance = 0;
		double count = 0;
		double variance = 0;
		double confidenceInterval = 0;
		for (SimQueue queue : this.queueList) {
			totalVariance += (queue.getLastAvgOccupancy() - average) * (queue.getLastAvgOccupancy() - average);
			count++;
		}
		variance = totalVariance / (count - 1);
		confidenceInterval = Math.sqrt(variance);
		return confidenceInterval;
	}
	
	public int getNumberOfQueues() {
		return numberOfQueues;
	}

	public void setNumberOfQueues(int numberOfQueues) {
		this.numberOfQueues = numberOfQueues;
	}

	public int getNumberOfServers() {
		return numberOfServers;
	}

	public void setNumberOfServers(int numberOfServers) {
		this.numberOfServers = numberOfServers;
	}

	public Connectivity[][] getConnectivityTable() {
		return connectivityTable;
	}

	public void setConnectivityTable(Connectivity[][] connectivityTable) {
		this.connectivityTable = connectivityTable;
	}

	public static double computeAverage(ArrayList<Double> list) {
		double total = 0;
		int count = 0;
		for (double d : list) {
			total += d;
			count++;
		}
		return (total/count);
	}
	
	public static void main(String[] args) {
		Simulator simulator = new Simulator();
		int runningTimes = 50000;
		for (int i = 0; i < 20; i++)
			simulator.runSimulation(runningTimes, 1, 0.08, 0.1, 0.1);
		ArrayList<Double> averagesDiffLambda = new ArrayList<Double>();
		ArrayList<Double> confidenceIntervalsDiffLambda = new ArrayList<Double>();
		for (int simBase = 1; simBase <= 10; simBase++) {
			ArrayList<Double> averages = new ArrayList<Double>();
			ArrayList<Double> confidenceIntervals = new ArrayList<Double>();
			for (int i = 0; i < 20; i++) {
				simulator = new Simulator();
				simulator.runSimulation(runningTimes, 0.5, 0.02 * simBase, 0.1, 0.1);
				double average = simulator.calculateAvgOccupancy(runningTimes);
				averages.add(average);
				confidenceIntervals.add(simulator.calculateOccupancyConfidenceInterval(average));
				double arrivalRate = ((double) simulator.totalPacketsArrived) / ((double) simulator.totalPacketsAttempted);
				//System.out.println("Packet arrival rate: " + simulator.totalPacketsArrived + "/" + simulator.totalPacketsAttempted + " = " + arrivalRate);
			}
			averagesDiffLambda.add(Simulator.computeAverage(averages));
			confidenceIntervalsDiffLambda.add(Simulator.computeAverage(confidenceIntervals));
		}
		String output = "Lambda\tAverage Occupancy\tConfidence Interval\n";
		StringBuilder outputBuilder = new StringBuilder(output);
		for (int i = 0; i < averagesDiffLambda.size(); i++) {
			outputBuilder.append(((i + 1) * 0.02) + "\t" + averagesDiffLambda.get(i) + "\t" + confidenceIntervalsDiffLambda.get(i) + "\n");
		}
		System.out.println(outputBuilder.toString());
	}
	
	
	private class ServerComparator implements Comparator<SimServer> {

		@Override
		public int compare(SimServer o1, SimServer o2) {
			if (o1.getConnections() > o2.getConnections())
				return 1;
			else if (o1.getConnections() == o2.getConnections())
				return 0;
			else
				return -1;
		}
	}
	
	private class QueueComparator implements Comparator<SimQueue> {

		@Override
		public int compare(SimQueue o1, SimQueue o2) {
			if (o1.getConnections() > o2.getConnections())
				return 1;
			else if (o1.getConnections() == o2.getConnections())
				return 0;
			else
				return -1;
		}
		
	}
}
