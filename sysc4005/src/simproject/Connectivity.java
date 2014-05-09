package simproject;

public class Connectivity {
	public static final int CONNECTED = 1;
	public static final int NOT_CONNECTED = 0;
	
	public int queue;
	public int server;
	public int status;
	
	public Connectivity(int queueNo, int serverNo, int status) {
		this.queue = queueNo;
		this.server = serverNo;
		this.status = status;
	}
}
