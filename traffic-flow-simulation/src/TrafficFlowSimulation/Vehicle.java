package TrafficFlowSimulation;

import repast.simphony.space.grid.Grid;

public class Vehicle {
	private Grid<Object> position;
	
	public Vehicle(Grid<Object> position) {
		this.position = position;
	}
}
