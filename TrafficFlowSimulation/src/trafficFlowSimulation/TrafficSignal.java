package trafficFlowSimulation;

import org.locationtech.jts.geom.Coordinate;

public class TrafficSignal {
	private String name;
	private Coordinate coordinate;
	
	public TrafficSignal(String name, Coordinate coordinate) {
		this.name = name;
		this.coordinate = coordinate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
}
