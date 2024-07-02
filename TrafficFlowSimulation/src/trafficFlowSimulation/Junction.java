package trafficFlowSimulation;

import java.util.ArrayList;

import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Junction {
	private ArrayList<TrafficSignal> trafficSignalList;
	private int currentActiveTrafficSignalIndex;
	private boolean isActive;
	private int currentTickCount;
	private int currentTrafficSignalTickDuration;
	
	public Junction() {
		trafficSignalList = new ArrayList<>();
		isActive = false;
		currentTickCount = 0;
		currentActiveTrafficSignalIndex = 0;
		TrafficFlowSimulationBuilder.context.add(this);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void run() {
		if (trafficSignalList == null || trafficSignalList.size() == 0) {
			return;
		}
		
		if (!isActive) {
			TrafficSignal trafficSignal = trafficSignalList.get(currentActiveTrafficSignalIndex);
			currentTrafficSignalTickDuration = trafficSignal.getDuration();
			currentTickCount = 0;
			trafficSignal.setIsActive(true);
			isActive = true;
		}
		else {
			currentTickCount++;
			
			if (currentTickCount >= currentTrafficSignalTickDuration) {
				isActive = false;
				trafficSignalList.get(currentActiveTrafficSignalIndex).setIsActive(false);
				currentActiveTrafficSignalIndex++;
				
				if (currentActiveTrafficSignalIndex > trafficSignalList.size() - 1) {
					currentActiveTrafficSignalIndex = 0;
				}
			}
		}
//		trafficSignal.startTrafficSignalTimer();
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
	
//	public void changeActiveToNextTrafficSignal() {
//		if (currentActiveTrafficSignalIndex >= trafficSignalList.size() - 1) {
//			currentActiveTrafficSignalIndex = 0;
//			return;
//		}
//		
//		else {
//			currentActiveTrafficSignalIndex++;
//		}
//		
//		TrafficSignal trafficSignal = trafficSignalList.get(currentActiveTrafficSignalIndex);
//		trafficSignal.startTrafficSignalTimer();
//		System.out.println("changing light");
//	}
	
	public boolean contains (TrafficSignal checkTrafficSignal) {
		for (TrafficSignal trafficSignal : trafficSignalList) {
			if (trafficSignal.getCoordinate().equals(checkTrafficSignal.getCoordinate())) {
				return true;
			}
		}
		
		return false;
	}
}
