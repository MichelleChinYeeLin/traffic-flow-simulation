package trafficFlowSimulation;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Vehicle {
	private String name;
	private Grid<Object> currentPosition;
	private Grid<Object> destination;
	private double speed;
	private double acceleration;
	
	public Vehicle() {
//		this.currentPosition = position;
	}
	
	public void move() {
		Context context = ContextUtils.getContext(this);
		Geography<Vehicle> geography = (Geography)context.getProjection("TrafficFlowMap");
	}
	
	public void calculateRoute() {
		
	}
}
