package trafficFlowSimulation;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.scenario.ScenarioLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.locationtech.jts.geom.Coordinate;

public class TrafficFlowSimulation {

    public static Queue<String> trafficSignalListQueue;
    public static Queue<String> vehicleListQueue;
    public static int tickToSecondRepresentation = 60; // 1 tick equals to 1 seconds
    private int currentTick = 0;
    public static boolean isFirstTick = true;

    public TrafficFlowSimulation() {
    	trafficSignalListQueue = new LinkedList<>();
    	vehicleListQueue = new LinkedList<>();
    }

    @ScheduledMethod(start = 1, interval = 1)
    public void step() {
        currentTick++;
        
        if (currentTick > tickToSecondRepresentation && !isFirstTick) {
        	currentTick = 0;
        	return;
        }
        
        if (isFirstTick) {
        	isFirstTick = false;
        }
        
    	// Store JSON string of traffic signals
        String trafficSignalJson = "{\"data\":[";
    	ArrayList<TrafficSignal> nextTrafficSignalList = new ArrayList<>(TrafficFlowSimulationBuilder.trafficSignalList);
    	
    	for (int i = 0; i < nextTrafficSignalList.size(); i++) {
    		TrafficSignal trafficSignal = nextTrafficSignalList.get(i);
    		Coordinate coordinate = trafficSignal.getCoordinate();
    		
    		String trafficSignalString = "{\"name\": " + "\"" + trafficSignal.getName() + "\"" + 
    									 ", \"junctionId\": " + trafficSignal.getJunction().getJunctionId() +
    									 ", \"duration\": " + trafficSignal.getDuration() +
    									 ", \"sequence\": " + trafficSignal.getSequence() +
    									 ", \"isActive\": " + trafficSignal.getIsActive() + 
    									 ", \"xCoordinate\": " + coordinate.getX() + 
    									 ", \"yCoordinate\": " + coordinate.getY() + "}";
    		trafficSignalJson += trafficSignalString;
    		
    		if (i != nextTrafficSignalList.size() - 1) {
    			trafficSignalJson += ", ";
    		}	
    	}
    	
    	trafficSignalJson += "]}";
        
        trafficSignalListQueue.add(trafficSignalJson);
        
        // Store JSON string of vehicles
        String vehicleJson = "{\"data\":[";
    	ArrayList<Vehicle> nextVehicleList = new ArrayList<>(TrafficFlowSimulationBuilder.vehicleList);
    	boolean isFirst = true;
    	
    	for (int i = 0; i < nextVehicleList.size(); i++) {
    		Vehicle vehicle = nextVehicleList.get(i);
    		
    		if (!vehicle.isRouteCompleted()) {
    			if (isFirst) {
    				isFirst = false;
    			}
    			else {
    				vehicleJson += ", ";
    			}
    			Coordinate vehicleCoordinate = vehicle.getCurrentCoordinate();
        		String vehicleString = "{\"name\": " + vehicle.getName() + ", \"xCoordinate\": " + vehicleCoordinate.getX() + ", \"yCoordinate\": " + vehicleCoordinate.getY() + "}";
        		vehicleJson += vehicleString;
    		}
    	}
    	
    	vehicleJson += "]}";
        vehicleListQueue.add(vehicleJson);
    }
    
    public static String getFirstTrafficSignalList() {
    	if (trafficSignalListQueue.size() == 1) {
    		return trafficSignalListQueue.peek();
    	}
    	String firstTrafficSignalList = trafficSignalListQueue.remove();
    	return firstTrafficSignalList;
    }
    
    public static String getFirstVehicleList() {
    	if (vehicleListQueue.size() == 1) {
    		return vehicleListQueue.peek();
    	}
    	String firstVehicleList = vehicleListQueue.remove();
    	return firstVehicleList;
    }
    
    public static void clearTrafficSignalListQueue() {
    	trafficSignalListQueue = new LinkedList<>();
    }
    
    public static void clearVehicleListQueue() {
    	vehicleListQueue = new LinkedList<>();
    }
}
