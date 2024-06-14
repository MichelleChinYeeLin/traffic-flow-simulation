package trafficFlowSimulation;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;

public class Intersection {
	private Coordinate coordinate;
	private ArrayList<Road> connectedRoadList;
	
	public Intersection(Coordinate coordinate, Road road1, Road road2) {
		this.coordinate = coordinate;
		connectedRoadList = new ArrayList<Road>();
		connectedRoadList.add(road1);
		connectedRoadList.add(road2);
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
	}

	public ArrayList<Road> getConnectedRoadList() {
		return connectedRoadList;
	}

	public void addRoad(Road road) {
		connectedRoadList.add(road);
	}
}
