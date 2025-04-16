package trafficFlowSimulation;

import java.util.ArrayList;

import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Junction {
	private int junctionId;
	private ArrayList<TrafficSignal> trafficSignalList;
	private int currentActiveTrafficSignalIndex;
	private boolean isActive;
	private int currentTickCount;
	private int currentTrafficSignalTickDuration;
	
	public Junction(int junctionId) {
		this.junctionId = junctionId;
		trafficSignalList = new ArrayList<>();
		isActive = false;
		currentTickCount = 0;
		currentActiveTrafficSignalIndex = 0;
		TrafficFlowSimulationBuilder.context.add(this);
	}
	
	public int getJunctionId() {
		return junctionId;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void run() {
		if (trafficSignalList == null || trafficSignalList.size() == 0) {
			return;
		}
		
		// If no traffic signals are active
		if (!isActive) {
			// Activate the next traffic signal in the sequence
			TrafficSignal trafficSignal = trafficSignalList.get(currentActiveTrafficSignalIndex);
			currentTrafficSignalTickDuration = trafficSignal.getDuration();
			currentTickCount = 0;
			trafficSignal.setIsActive(true);
			isActive = true;
		}
		else {
			// Track the duration the current traffic signal has been active
			currentTickCount++;
			
			// If the active traffic signal's duration is reached, deactivate it
			if (currentTickCount >= currentTrafficSignalTickDuration) {
				isActive = false;
				trafficSignalList.get(currentActiveTrafficSignalIndex).setIsActive(false);
				currentActiveTrafficSignalIndex++;
				
				if (currentActiveTrafficSignalIndex > trafficSignalList.size() - 1) {
					currentActiveTrafficSignalIndex = 0;
				}
			}
		}
	}
	
	public void addTrafficSignal(TrafficSignal trafficSignal) {
		trafficSignalList.add(trafficSignal);
	}
	
	public TrafficSignal getCurrentActiveTrafficSignal() {
		if (currentActiveTrafficSignalIndex < 0) {
			return null;
		}
		
		return trafficSignalList.get(currentActiveTrafficSignalIndex);
	}
	
	public ArrayList<TrafficSignal> getTrafficSignalList() {
		return trafficSignalList;
	}
	
	public boolean contains (TrafficSignal checkTrafficSignal) {
		for (TrafficSignal trafficSignal : trafficSignalList) {
			if (trafficSignal.getCoordinate().equals(checkTrafficSignal.getCoordinate())) {
				return true;
			}
		}
		
		return false;
	}
}
