package trafficFlowSimulation;

import java.util.LinkedList;

import org.locationtech.jts.geom.Coordinate;

public class RoadNode {
	private int roadId;
	private Coordinate coordinate;
	private Intersection intersection;
	private LinkedList<Vehicle> vehicleQueue; // List of vehicles heading to this RoadNode
	
	public RoadNode(int roadId, Coordinate coordinate) {
		this.roadId = roadId;
		this.coordinate = coordinate;
		this.intersection = null;
	}
	
	public int getRoadId() {
		return roadId;
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
	}
	
	public Intersection getIntersection() {
		return intersection;
	}
	
	public void setIntersection(Intersection intersection) {
		this.intersection = intersection;
	}
	
	public boolean equals(RoadNode node) {
		if (roadId == node.getRoadId() && coordinate.equals(node.getCoordinate())) {
			return true;
		}
		
		return false;
	}
	
	public void addVehicle(Vehicle vehicle) {
		vehicleQueue.addLast(vehicle);
	}
	
	public void removeVehicle(Vehicle vehicle) {
		for (int i = 0; i < vehicleQueue.size(); i++) {
			if (vehicle.getName().equals(vehicleQueue.get(i).getName())) {
				vehicleQueue.remove(i);
				return;
			}
		}
		return;
	}
}
