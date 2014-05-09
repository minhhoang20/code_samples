package simproject;

import java.util.LinkedList;

public class SimQueue {
	
	private int queueNumber;
	private int connections;
	private int remainingPackets;
	private long totalOccupancy;
	private double lastAvgOccupancy;
	private LinkedList<SimPacket> packets;
	private LinkedList<Long> delayTimes;
	
	public SimQueue(int number) {
		this.queueNumber = number;
		this.connections = 0;
		this.remainingPackets = 0;
		this.packets = new LinkedList<SimPacket>();
		this.delayTimes = new LinkedList<Long>();
		this.totalOccupancy = 0;
		this.lastAvgOccupancy = 0;
	}
	
	public void incrementConnection() {
		this.connections++;
	}
	
	public void decrementConnection() {
		this.connections--;
	}
	
	public void resetConnections() {
		this.connections = 0;
	}
	
	public void packetProcessed() {
		this.remainingPackets--;
	}

	// This function allows adding 0
	public void addPacket(SimPacket p) {
		this.remainingPackets++;
		this.packets.add(p);
	}
	
	public SimPacket popPacket() {
		this.remainingPackets--;
		SimPacket poppedPacket = this.packets.poll();
		this.delayTimes.add(poppedPacket.finish());
		return poppedPacket;
	}
	
	public void recordOccupancy() {
		this.totalOccupancy += this.remainingPackets;
	}
	
	public double calculateAvgOccupancy(int times) {
		this.lastAvgOccupancy = (((double) this.totalOccupancy) / ((double) times));
		return this.lastAvgOccupancy;
	}

	public int getQueueNumber() {
		return queueNumber;
	}
	
	public int getConnections() {
		return connections;
	}

	public int getRemainingPackets() {
		return remainingPackets;
	}
	
	public LinkedList<Long> getDelayTimes() {
		return this.delayTimes;
	}

	public double getLastAvgOccupancy() {
		return lastAvgOccupancy;
	}
	
	
}
