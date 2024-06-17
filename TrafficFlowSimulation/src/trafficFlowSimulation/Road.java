package trafficFlowSimulation;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;

public class Road {
	private int roadId;
	private String name;
	private ArrayList<RoadNode> roadNodeList;
	private boolean isOneWay;
	private int laneNum;
	
	public Road(int roadId, String name, ArrayList<RoadNode> roadNodeList, boolean isOneWay, int laneNum) {
		this.roadId = roadId;
		this.name = name;
		this.roadNodeList = roadNodeList;
		this.isOneWay = isOneWay;
		this.laneNum = laneNum;
	}
	
	public int getRoadId() {
		return roadId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public ArrayList<RoadNode> getRoadNodeList() {
		return roadNodeList;
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
	
	public ArrayList<RoadNode> getAdjacentRoadNodes(RoadNode roadNode) {
		ArrayList<RoadNode> adjacentRoadNodesList = new ArrayList<>();
		
		for (int i = 0; i < roadNodeList.size(); i++) {
			if (roadNodeList.get(i).equals(roadNode)) {
				
				if (i + 1 < roadNodeList.size()) {
					adjacentRoadNodesList.add(roadNodeList.get(i + 1));
				}
				
				if (!isOneWay && ((i - 1) >= 0)) {
					adjacentRoadNodesList.add(roadNodeList.get(i - 1));
				}
				
				break;
			}
		}
		return adjacentRoadNodesList;
	}
	
	public boolean isLastRoadNode(RoadNode roadNode) {
		System.out.println("Tested: " + TrafficFlowSimulationBuilder.roadHashMap.get(roadId).getName());
		if (roadNodeList.get(roadNodeList.size() - 1).equals(roadNode)) {
			return true;
		}
		
		return false;
	}
}
