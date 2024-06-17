package trafficFlowSimulation;

public class RoadNodeWrapper {
	private RoadNode roadNode;
	private double priority;
	
	public RoadNodeWrapper(RoadNode roadNode, double priority) {
		this.roadNode = roadNode;
		this.priority = priority;
	}

	public RoadNode getRoadNode() {
		return roadNode;
	}

	public double getPriority() {
		return priority;
	}
}
