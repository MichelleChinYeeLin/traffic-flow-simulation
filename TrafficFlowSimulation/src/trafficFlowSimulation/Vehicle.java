package trafficFlowSimulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import org.locationtech.jts.geom.Coordinate;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Vehicle {
	private String name;
	private double speed;
	private double acceleration;
	private RoadNode currentNode;
	private RoadNode destinationNode;
	private boolean isRouteCompleted;
	private LinkedList route;
	
	public Vehicle() {
		isRouteCompleted = false;
	}
	
	private void move() {
		Context context = ContextUtils.getContext(this);
		Geography<Vehicle> geography = (Geography)context.getProjection("TrafficFlowMap");
	}
	
	public LinkedList<RoadNode> calculateRoute(RoadNode startingRoadNode, RoadNode destinationNode) {
		this.destinationNode = destinationNode;
		this.currentNode = startingRoadNode;
		route = aStarSearch(startingRoadNode);
		return route;
	}
	
	private LinkedList<RoadNode> aStarSearch(RoadNode startingRoadNode) {
		// If the starting and destination is the same
		if (currentNode.equals(destinationNode)) {
			isRouteCompleted = true;
			return null;
		}
		
		// List containing unexplored nodes
		// Define a comparator for the RoadNodeWrapper that compares the priority
		Comparator<RoadNodeWrapper> wrapperComparator = new Comparator<RoadNodeWrapper>() {
		    @Override
		    public int compare(RoadNodeWrapper wrapper1, RoadNodeWrapper wrapper2) {
		        return Double.compare(wrapper1.getPriority(), wrapper2.getPriority());
		    }
		};
		PriorityQueue<RoadNodeWrapper> openList = new PriorityQueue<>(wrapperComparator);
		openList.add(new RoadNodeWrapper(startingRoadNode, 0.0));
		
		// Hash map containing explored nodes
		Map<RoadNode, Double> closeList = new HashMap<>();
		closeList.put(startingRoadNode, 0.0);
		
		Map<RoadNode, RoadNode> routeLink = new HashMap<>();
		routeLink.put(startingRoadNode, null);
		
		while (!openList.isEmpty()) {
			RoadNode currentRoadNode = openList.poll().getRoadNode();
			System.out.println("Moved +1");
			
			if (currentRoadNode.equals(destinationNode)) {
				LinkedList<RoadNode> route = new LinkedList<>();
				
				// Trace back route
				RoadNode node = destinationNode;
				route.addLast(destinationNode);
				while (!node.equals(startingRoadNode)) {
					RoadNode prevNode = routeLink.get(node);
					route.addFirst(prevNode);
					node = prevNode;
				}
				
				System.out.println("Vehicle Route: ");
				for (int i = 0; i < route.size(); i++) {
					System.out.print(route.get(i));
					
					if (i != route.size() - 1) {
						System.out.print(" -> ");
					}
				}
				System.out.println();
				return route;
			}
			
			// Get the neighbouring nodes of the current node
			Road currentRoad = TrafficFlowSimulationBuilder.roadHashMap.get((Integer)currentRoadNode.getRoadId());
			ArrayList<RoadNode> neighbours = currentRoad.getAdjacentRoadNodes(currentRoadNode);
			System.out.println("+" + neighbours.size() + " neighbours (not including intersections)");
			
			// Get the road nodes connected at intersections (if any)
			if (currentRoadNode.getIntersection() != null) {
				Intersection intersection = currentRoadNode.getIntersection();
				neighbours.addAll(intersection.getConnectedRoadNodeList(currentRoadNode));
			}
			System.out.println("+" + neighbours.size() + " neighbours (intersection + non)");
			
			for (RoadNode neighbour : neighbours) {
				double newCost = closeList.get(currentRoadNode) + Math.sqrt(Math.pow((currentRoadNode.getCoordinate().getX() - neighbour.getCoordinate().getX()), 2) + Math.pow((currentRoadNode.getCoordinate().getY() - neighbour.getCoordinate().getY()), 2));
				
				if (!closeList.containsKey(neighbour)) {
					closeList.put(neighbour, newCost);
					
					// Calculate heuristic value (Manhattan distance)
					double xCoordinate = neighbour.getCoordinate().getX();
					double yCoordinate = neighbour.getCoordinate().getY();
					double heuristicValue = Math.abs(xCoordinate - destinationNode.getCoordinate().getX()) + Math.abs(yCoordinate - destinationNode.getCoordinate().getY());
					
					// Calculate priority (heuristic value + cost)
					double priority = newCost + heuristicValue;
					
					openList.add(new RoadNodeWrapper(neighbour, priority));
					routeLink.put(neighbour, currentRoadNode);
				}
			}
		}
		
		return null;
	}
}
