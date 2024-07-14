package trafficFlowSimulation;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

public class Intersection {
	private Coordinate coordinate;
	private ArrayList<RoadNode> connectedRoadNodeList;
	private TrafficSignal trafficSignal;
	
	public Intersection(Coordinate coordinate, RoadNode roadNode1) {
		this.trafficSignal = null;
		this.coordinate = coordinate;
		connectedRoadNodeList = new ArrayList<RoadNode>();
		connectedRoadNodeList.add(roadNode1);
	}
	
	public Intersection(Coordinate coordinate, RoadNode roadNode1, RoadNode roadNode2) {
		this.trafficSignal = null;
		this.coordinate = coordinate;
		connectedRoadNodeList = new ArrayList<RoadNode>();
		connectedRoadNodeList.add(roadNode1);
		connectedRoadNodeList.add(roadNode2);
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
	}

	public ArrayList<RoadNode> getConnectedRoadNodeList() {
		return connectedRoadNodeList;
	}
	
	public ArrayList<RoadNode> getConnectedRoadNodeList(RoadNode currentNode) {
		ArrayList<RoadNode> connectedList = new ArrayList<RoadNode>();
		
		for (RoadNode node : connectedRoadNodeList) {
			if (!node.equals(currentNode)) {
				Road road = TrafficFlowSimulationBuilder.roadHashMap.get(node.getRoadId());
				
				if (!road.isOneWay()) {
					connectedList.add(node);
				}
				else if (!road.isLastRoadNode(node)) {
					connectedList.add(node);
				}
			}
		}
		
		return connectedList;
	}
	
	public TrafficSignal getTrafficSignal() {
		return trafficSignal;
	}

	public void addRoadNode(RoadNode roadNode) {
		connectedRoadNodeList.add(roadNode);
	}
	
	public void addTrafficSignal(TrafficSignal trafficSignal) {
		this.trafficSignal = trafficSignal;
	}
}
