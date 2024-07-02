package trafficFlowSimulation;

import java.util.Timer;
import java.util.TimerTask;

import org.locationtech.jts.geom.Coordinate;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;

public class TrafficSignal {
	private String name;
	private Coordinate coordinate;
	private int duration;
	private boolean isActive;
	private Timer timer;
	private TimerTask timerTask;
	private Junction junction;
	private int trafficSignalIndex;
	
	public TrafficSignal(String name, Coordinate coordinate) {
		this.name = name;
		this.coordinate = coordinate;
		duration = 30;
		isActive = false;
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
	
	public boolean getIsActive() {
		return isActive;
	}
	
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public Junction getJunction() {
		return junction;
	}
	
	public void setJunction(Junction junction) {
		this.junction = junction;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setTrafficSignalIndex(int index) {
		this.trafficSignalIndex = index;
	}
}
