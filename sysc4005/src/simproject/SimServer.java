package simproject;

import java.util.LinkedList;

public class SimServer {
	private int serverNumber;
	private int connections;
	private LinkedList<Integer> queuesServed;
	private boolean isBusy;
	
	public SimServer(int number) {
		this.serverNumber = number;
		this.connections = 0;
		this.isBusy = false;
		this.queuesServed = new LinkedList<Integer>();
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
	
	public void addServedQueue(int queueNumber) {
		this.queuesServed.add(queueNumber);
	}

	public boolean isBusy() {
		return this.isBusy;
	}
	
	public void goBusy() {
		this.isBusy = true;
	}
	
	public void goIdle() {
		this.isBusy = false;
	}

	public int getServerNumber() {
		return serverNumber;
	}
	
	public int getConnections() {
		return connections;
	}

	public LinkedList<Integer> getQueuesServed() {
		return queuesServed;
	}
	
}

