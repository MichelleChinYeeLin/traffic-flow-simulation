package trafficFlowSimulation;

import java.util.LinkedList;

import org.locationtech.jts.geom.Coordinate;

public class RoadNode {
	private int roadId;
	private Coordinate coordinate;
	private Intersection intersection;
	
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
}
