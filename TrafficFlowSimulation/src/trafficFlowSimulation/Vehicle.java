package trafficFlowSimulation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Vehicle {
	private String name;
	private double speed;
	private double acceleration;
	private boolean isRouteCompleted;
	private LinkedList<RoadNode> route;
	private Coordinate currentCoordinate;
	private double currentTrajectoryAngle;
	private Junction currentJunction = null;
	private GeometryFactory factory = new GeometryFactory();
	private Context context;
	private Geography<Vehicle> geography;
	
	private final double maxSpeed = 16.6667;
	private final double minAcceleration = -4.5;
	private final double maxAcceleration = 3.25;
	private final double minDistance = 0.000001;
	
	public Vehicle(RoadNode startingRoadNode, RoadNode destinationNode) {
		isRouteCompleted = false;
		currentCoordinate = startingRoadNode.getCoordinate();
		calculateRoute(startingRoadNode, destinationNode);
		
		if (route != null) {
			route.removeFirst();
			
			if (route.size() == 0) {
				isRouteCompleted = true;
			}
			
			else {
				TrafficFlowSimulationBuilder.context.add(this);
				TrafficFlowSimulationBuilder.geography.move(this, factory.createPoint(currentCoordinate));
				
				// Get angle of movement from current position to next RoadNode
				currentTrajectoryAngle = calculateAngleBetweenPoints(currentCoordinate, route.getFirst().getCoordinate());
			}	
		}
		
		else {
			isRouteCompleted = true;
		}
	}
	
	public Vehicle(RoadNode startingRoadNode, RoadNode destinationNode, String name) {
		this.name = name;
		isRouteCompleted = false;
		currentCoordinate = startingRoadNode.getCoordinate();
		calculateRoute(startingRoadNode, destinationNode);

		if (route != null) {
			route.removeFirst();
			
			if (route.size() == 0) {
				isRouteCompleted = true;
			}
			
			else {
				TrafficFlowSimulationBuilder.context.add(this);
				TrafficFlowSimulationBuilder.geography.move(this, factory.createPoint(currentCoordinate));
				
				// Get angle of movement from current position to next RoadNode
				currentTrajectoryAngle = calculateAngleBetweenPoints(currentCoordinate, route.getFirst().getCoordinate());
			}	
		}
		
		else {
			isRouteCompleted = true;
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void move() {
		context = ContextUtils.getContext(this);
		geography = (Geography)context.getProjection("TrafficFlowMap");
		
		if (route == null || route.size() == 0 || isRouteCompleted) {
			context.remove(this);
			return;
		}
		
		// Check if the vehicle can move
		// Create Envelope to check for vehicles
		Vehicle frontVehicle = null;
		frontVehicle = getClosestVehicle();
		
		if (frontVehicle == null) {
			checkIntersection();
		}
		else {
			double distanceFromVehicle = frontVehicle.getCurrentCoordinate().distance(currentCoordinate);
			
			boolean checkFrontVehicleDistance = true;
			
			// If vehicle is heading to an intersection
			if (route.getFirst().getIntersection() != null) {
				double distanceFromIntersection = route.getFirst().getIntersection().
						getCoordinate().distance(currentCoordinate);
				
				// Check if the intersection is closer than the next vehicle
				if (distanceFromIntersection < distanceFromVehicle) {
					checkIntersection();
					checkFrontVehicleDistance = false;
				}
			}
			
			// Check the distance of the front vehicle
			if (checkFrontVehicleDistance) {
				// Emergency brake if the distance is too close
				if (distanceFromVehicle <= minDistance) {
					speed = 0;
					acceleration = 0;
					return;
				}
				
				else {
					brake(distanceFromVehicle - minDistance, frontVehicle.getSpeed());
				}	
			}
		}
		
		// Calculate new coordinates based on current speed
		double coordinateDistance = speed * 0.00001; // 0.00001 = 1 meter
		double firstNodeDistance = currentCoordinate.distance(route.getFirst().getCoordinate());
		Coordinate newCoordinate = new Coordinate();
		currentTrajectoryAngle = calculateAngleBetweenPoints(currentCoordinate, route.getFirst().getCoordinate());
		
		// If the new coordinate exceeds the next RoadNode coordinate
		if (coordinateDistance > firstNodeDistance || firstNodeDistance < 0.00002) {
			newCoordinate = route.getFirst().getCoordinate();
			route.removeFirst();
			
			if (route.size() == 0) {
				isRouteCompleted = true;
				context.remove(this);
				return;
			}
			
			// Update angle
			currentTrajectoryAngle = calculateAngleBetweenPoints(currentCoordinate, route.getFirst().getCoordinate());
		}
		
		// Calculate the updated coordinate
		else {
			double newCoordinateYDifference = Math.round(coordinateDistance * Math.sin(currentTrajectoryAngle) 
											  * 1000000.0) / 1000000.0;
			double newCoordinateXDifference = Math.round(coordinateDistance * Math.cos(currentTrajectoryAngle) 
											  * 1000000.0) / 1000000.0;
			double newCoordinateX;
			double newCoordinateY;
			
			if (route.getFirst().getCoordinate().getX() == currentCoordinate.getX()) {
				newCoordinateX = route.getFirst().getCoordinate().getX();
			}
			
			else if (route.getFirst().getCoordinate().getX() > currentCoordinate.getX()) {
				newCoordinateX = currentCoordinate.getX() + newCoordinateXDifference;
			}
			
			else {
				newCoordinateX = currentCoordinate.getX() - newCoordinateXDifference;
			}
			
			if (route.getFirst().getCoordinate().getY() == currentCoordinate.getY()) {
				newCoordinateY = route.getFirst().getCoordinate().getY();
			}
			
			else if (route.getFirst().getCoordinate().getY() > currentCoordinate.getY()) {
				newCoordinateY = currentCoordinate.getY() + newCoordinateYDifference;
			}
			
			else {
				newCoordinateY = currentCoordinate.getY() - newCoordinateYDifference;
			}
			
			newCoordinate = new Coordinate(Math.round(newCoordinateX * 1000000.0) / 1000000.0, 
										   Math.round(newCoordinateY * 1000000.0) / 1000000.0);
		}
		
		// Update the vehicle's position
		currentCoordinate = new Coordinate(newCoordinate);
		geography.move(this, factory.createPoint(currentCoordinate));
	}
	
	private void accelerate() {
		
		if (acceleration < maxAcceleration) {
			acceleration += 1;
		}

		speed += acceleration;
		if (speed > maxSpeed) {
			speed = maxSpeed;
			acceleration = 0;
		}
	}
	
	private void brake(double distance, double finalVelocity) {
		double requiredDeceleration = (Math.pow(finalVelocity, 2) - Math.pow(speed, 2)) / (2 * distance);
		
		acceleration = Math.max(requiredDeceleration, minAcceleration);
		
		speed += acceleration;
		
		if (speed <= 0) {
			speed = 0;
			acceleration = 0;
		}
	}
	
	private double calculateAngleBetweenPoints(Coordinate startingCoordinate, Coordinate destinationCoordinate) {
		// Calculate the x-coordinate difference between current position to next RoadNode
		double xDifference = Math.abs(startingCoordinate.getX() - destinationCoordinate.getX());
		
		// Calculate the y-coordinate difference between current position to next RoadNode
		double yDifference = Math.abs(startingCoordinate.getY() - destinationCoordinate.getY());
		
		// Calculate the angle of movement
		double angle = Math.round(Math.atan(yDifference / xDifference) * 1000.0) / 1000.0;
		
		return angle;
	}
	
	private Coordinate calculateEnvelopeCoordinate(Coordinate startingCoordinate, Coordinate nextCoordinate, double distance) {
		double angle = calculateAngleBetweenPoints(startingCoordinate, nextCoordinate);
		
		Coordinate checkCoordinate = new Coordinate();
		double checkCoordinateYDifference = 0.001 * Math.sin(angle);
		double checkCoordinateXDifference = 0.001 * Math.cos(angle);
		double checkCoordinateX;
		double checkCoordinateY;
		if (route.getFirst().getCoordinate().getX() > currentCoordinate.getX()) {
			checkCoordinateX = currentCoordinate.getX() + checkCoordinateXDifference;
		}
		
		else {
			checkCoordinateX = currentCoordinate.getX() - checkCoordinateXDifference;
		}
		
		if (route.getFirst().getCoordinate().getY() > currentCoordinate.getY()) {
			checkCoordinateY = currentCoordinate.getY() + checkCoordinateYDifference;
		}
		
		else {
			checkCoordinateY = currentCoordinate.getY() - checkCoordinateYDifference;
		}
		
		checkCoordinate = new Coordinate(checkCoordinateX, checkCoordinateY);
		return checkCoordinate;
	}
	
	private Vehicle getClosestVehicle() {
		double totalDistance = 0.001;
		
		// If next road node distance is less than 100m
		if (totalDistance > currentCoordinate.distance(route.getFirst().getCoordinate())) {
			totalDistance -= currentCoordinate.distance(route.getFirst().getCoordinate());
			Coordinate checkCoordinate = route.getFirst().getCoordinate();
			Envelope envelope = new Envelope(currentCoordinate, checkCoordinate);
			Vehicle nextVehicle = getClosestVehicleWithinEnvelope(envelope);
			
			int count = 1;
			
			// If there is a vehicle within 100m
			if (nextVehicle == null) {
				int index = 0;
				
				// Check for vehicles along the next road nodes if it is within 100m
				while (totalDistance >= 0 && nextVehicle == null && index > route.size() - 1) {
					Coordinate checkStartCoordinate = route.get(index).getCoordinate();
					Coordinate checkEndCoordinate = route.get(index + 1).getCoordinate();
					
					if (checkStartCoordinate.distance(checkEndCoordinate) < 0) {
						checkCoordinate = calculateEnvelopeCoordinate(checkStartCoordinate, checkEndCoordinate, totalDistance);
						envelope = new Envelope(checkStartCoordinate, checkCoordinate);
						nextVehicle = getClosestVehicleWithinEnvelope(envelope);
					}
					else {
						envelope = new Envelope(checkStartCoordinate, checkEndCoordinate);
						nextVehicle = getClosestVehicleWithinEnvelope(envelope);
					}
					
					totalDistance -= checkStartCoordinate.distance(checkEndCoordinate);
					index++;
				}
			}
			
			return nextVehicle;
		}
		
		// If the next road node distance is more or equal than 100m
		else {
			Coordinate checkCoordinate = calculateEnvelopeCoordinate(currentCoordinate, route.getFirst().getCoordinate(), totalDistance);
			Envelope envelope = new Envelope(currentCoordinate, checkCoordinate);
			Vehicle nextVehicle = getClosestVehicleWithinEnvelope(envelope);
			return nextVehicle;
		}
	}
	
	private Vehicle getClosestVehicleWithinEnvelope(Envelope envelope) {
		boolean isFirstFound = true;
		Vehicle closestVehicle = null;
		
		try {
			// Get vehicle objects within the envelope
			Iterator<Vehicle> iterator = geography.getObjectsWithin(envelope, Vehicle.class).iterator();
			while (iterator.hasNext()) {
				Vehicle nextVehicle = iterator.next();
				Road currentRoad = TrafficFlowSimulationBuilder.roadHashMap.get(route.getFirst().getRoadId());
				
				// Check if the vehicles are on the same road and going the same direction
				boolean directionCheckValid = true;
				if (nextVehicle.getCurrentRoadNode() != null) {
					Road nextVehicleCurrentRoad = TrafficFlowSimulationBuilder.roadHashMap.get(nextVehicle.getCurrentRoadNode().getRoadId());
					
					// If the vehicles are on the same road, and the road is not a one-way
					if (currentRoad.getRoadId() == nextVehicleCurrentRoad.getRoadId() && !currentRoad.isOneWay()) {
						// Check the direction the vehicles are moving in
						boolean nextVehicleIsReverseDirection = currentRoad.checkRouteDirection(nextVehicle.getCurrentRoadNode(), nextVehicle.getNextRoadNode());
						boolean vehicleIsReverseDirection = currentRoad.checkRouteDirection(route.getFirst(), route.get(1));
						
						// If the vehicles are not moving in the same direction
						if (nextVehicleIsReverseDirection != vehicleIsReverseDirection) {
							directionCheckValid = false;
						}
					}
					
					else {
						directionCheckValid = false;
					}
				}
				
				// If the front vehicle is found
				if (nextVehicle.getCurrentRoadNode() != null && nextVehicle != this && directionCheckValid) {
					if (isFirstFound) {
						closestVehicle = nextVehicle;
						isFirstFound = false;
					}
					else {
						double closestVehicleDistance = closestVehicle.getCurrentCoordinate().distance(currentCoordinate);
						double nextVehicleDistance = nextVehicle.getCurrentCoordinate().distance(currentCoordinate);
						
						// Compare the vehicle distance to determine the closer vehicle
						if (nextVehicleDistance < closestVehicleDistance) {
							closestVehicle = nextVehicle;
						}
					}
				}
			}
		}
		catch (Exception e) {
			return null;
		}
		
		return closestVehicle;
	}
	
	private void checkIntersection() {
		// If RoadNode is an intersection, check traffic lights
		if (route.getFirst().getIntersection() != null) {
			Intersection intersection = route.getFirst().getIntersection();

			// If the intersection has a traffic signal and it is a green light
			if (intersection.getTrafficSignal() != null && intersection.getTrafficSignal().getIsActive()) {
				currentJunction = intersection.getTrafficSignal().getJunction();
				accelerate();
			}
						
			// If the intersection has a traffic signal and it is a red light
			else if (intersection.getTrafficSignal() != null && !intersection.getTrafficSignal().getIsActive()) {
				Junction junction = intersection.getTrafficSignal().getJunction();
				
				// Check if the traffic signal belongs to the same junction that the vehicle just passed
				if (currentJunction != null && junction.getJunctionId() == currentJunction.getJunctionId()) {
					accelerate();
				}
				else {
					brake(intersection.getCoordinate().distance(currentCoordinate), 0);
				}
			}
						
			else {
				accelerate();
			}
		}
		
		// If the road node is not an intersection
		else {
			currentJunction = null;
			accelerate();
		}
	}
	
	private void calculateRoute(RoadNode startingRoadNode, RoadNode destinationNode) {

		// If the starting and destination is the same
		if (startingRoadNode.equals(destinationNode)) {
			isRouteCompleted = true;
			return;
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
			
			if (currentRoadNode.equals(destinationNode)) {
				this.route = new LinkedList<>();
				
				// Trace back route
				RoadNode node = destinationNode;
				route.addLast(destinationNode);
				while (!node.equals(startingRoadNode)) {
					RoadNode prevNode = routeLink.get(node);
					
					if (prevNode == null) {
						return;
					}
					
					route.addFirst(prevNode);
					node = prevNode;
				}
				
				return;
			}
			
			// Get the neighbouring nodes of the current node
			Road currentRoad = TrafficFlowSimulationBuilder.roadHashMap.get((Integer)currentRoadNode.getRoadId());
			ArrayList<RoadNode> neighbours = currentRoad.getAdjacentRoadNodes(currentRoadNode);
			
			// Get the road nodes connected at intersections (if any)
			if (currentRoadNode.getIntersection() != null) {
				Intersection intersection = currentRoadNode.getIntersection();
				neighbours.addAll(intersection.getConnectedRoadNodeList(currentRoadNode));
			}
			
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
		
		return;
	}

	public String getName() {
		return name;
	}

	public Coordinate getCurrentCoordinate() {
		return currentCoordinate;
	}
	
	public double getSpeed() {
		return speed;
	}

	public boolean isRouteCompleted() {
		return isRouteCompleted;
	}
	
	public void setIsRouteCompleted(boolean flag) {
		isRouteCompleted = flag;
	}

	public RoadNode getCurrentRoadNode() {
		if (route == null || route.size() == 0) {
			return null;
		}
		
		return route.getFirst();
	}
	
	public RoadNode getNextRoadNode() {
		if (route == null || route.size() == 0 || route.size() == 1) {
			return null;
		}
		
		return route.get(1);
	}
}
