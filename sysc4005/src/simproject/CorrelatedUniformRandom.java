package simproject;

public class CorrelatedUniformRandom {
	
	private double currentNumber;
	private double correlatedA;
	private double correlatedB;
	
	public CorrelatedUniformRandom(double a, double b) {
		this.correlatedA = a;
		this.correlatedB = b;
		this.currentNumber = Math.random();
		//System.out.println("Constant V is " + this.constantV);
	}
	
	public double nextUniformRandom() {
		double nextNumber = (this.currentNumber + Math.abs(StdRandom.uniform(0 - this.correlatedA, this.correlatedB))) % 1;
		//double nextNumber = Math.random();
		this.currentNumber = nextNumber;
		//System.out.println("Next random is " + this.currentNumber);
		return this.currentNumber;
	}
	
	public double nextUniformCorrelatedRandom() {
		double nextNumber = Math.abs(this.currentNumber + StdRandom.uniform(0 - this.correlatedA, this.correlatedB)) % 1;
		this.currentNumber = nextNumber;
		return this.currentNumber;
	}
	
	public int nextBernouliiRandom(double prob) {
		double nextUniform = this.nextUniformRandom();
		if (nextUniform <= prob)
			return 1;
		else
			return 0;
	}
	
	public int nextBernouliiCorrelatedRandom(double prob) {
		double nextUniform = this.nextUniformCorrelatedRandom() % .65;
		if (nextUniform <= prob)
			return 1;
		else
			return 0;
	}
	
	public static void main(String[] args) {
		CorrelatedUniformRandom randGen = new CorrelatedUniformRandom(0.4, 0.4);
		for (int i = 0; i < 20; i++) {
			System.out.println(randGen.nextUniformRandom());
		}
	}
}
