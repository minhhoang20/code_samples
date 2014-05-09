package simproject;

public class SimPacket {
	
	public long queuedTime;
	public long finishedTime;
	public long delay;
	
	public SimPacket() {
		this.queuedTime = System.currentTimeMillis();
		this.finishedTime = 0;
		this.delay = 0;
	}
	
	public long finish() {
		this.finishedTime = System.currentTimeMillis();
		this.delay = this.finishedTime - this.queuedTime;
		return this.delay;
	}
}
