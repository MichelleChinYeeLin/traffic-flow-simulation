package trafficFlowSimulation;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;

public class Road {
	private String name;
	private Coordinate[] coordinateList;
	private ArrayList<Intersection> intersectionList;
	private boolean isOneWay;
	private int laneNum;
	
	public Road(String name, Coordinate[] coordinateList, boolean isOneWay, int laneNum) {
		this.name = name;
		this.coordinateList = coordinateList;
		this.isOneWay = isOneWay;
		this.laneNum = laneNum;
		intersectionList = new ArrayList<Intersection>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Coordinate[] getCoordinateList() {
		return coordinateList;
	}

	public void setCoordinateList(Coordinate[] coordinateList) {
		this.coordinateList = coordinateList;
	}
	
	public boolean isOneWay() {
		return isOneWay;
	}

	public void setOneWay(boolean isOneWay) {
		this.isOneWay = isOneWay;
	}

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}

	public void addIntersection(Intersection intersection) {
		intersectionList.add(intersection);
	}
}
