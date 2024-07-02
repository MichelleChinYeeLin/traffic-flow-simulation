package trafficFlowSimulation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.dataLoader.ui.wizard.builder.NetworkLayer;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.DefaultSchedulableActionFactory;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.util.ContextUtils;

public class TrafficFlowSimulationBuilder implements ContextBuilder {

	public static Map<Integer, Road> roadHashMap = new HashMap<>();
	
	private List<Road> roadList = new ArrayList<Road>();
	private List<Intersection> intersectionList = new ArrayList<Intersection>();
	private List<TrafficSignal> trafficSignalList = new ArrayList<TrafficSignal>();
	private List<Junction> junctionList = new ArrayList<Junction>();
	private List<RoadNode> spawnPointList = new ArrayList<>();
	private RoadNode test1 = null;
	private RoadNode test1a = null;
	private RoadNode test2 = null;
	private int vehicleNumGenerator = 500;
	
	public static Context context;
	public static Geography geography;
	public static GeometryFactory factory;
	
	// Test
	public static Network net2 = null;
	
	@Override
	public Context build(Context context) {
		this.context = context;
		GeographyParameters<Object> params = new GeographyParameters<Object>();
		this.geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("TrafficFlowMap", context, params);
		this.factory = new GeometryFactory();
		
		NetworkBuilder<?> netBuilder = new NetworkBuilder<Object>("Network", context, true);
		Network net = netBuilder.buildNetwork();
		NetworkBuilder<?> net2Builder = new NetworkBuilder<Object>("Test Network", context, true);
		net2 = net2Builder.buildNetwork();
		
		// Load features from shapefiles
		loadRoadFeatures("./data/roads-line.shp", net);
		loadTrafficSignalFeatures("./data/amenity_points-point.shp", net);
		
		generateVehicleSpawnPointList();
		
		Schedule rneSchedule = (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters rneScheduleParameters = ScheduleParameters.createRepeating(1, 500);
		rneSchedule.schedule(rneScheduleParameters, this, "generateVehicleAgent");
		
//		Vehicle testVehicle1 = new Vehicle(test1, test2, "last");
//		Vehicle testVehicle2 = new Vehicle(test1a, test2, "front");
		
		return context;
	}
	
	public void generateVehicleAgent() {
		RandomHelper random = new RandomHelper();
		
		for (int i = 0; i < vehicleNumGenerator; i++) {
			RoadNode start = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
			RoadNode end = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
			
			while (start.equals(end)) {
				end = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
			}
			
			Vehicle newVehicle = new Vehicle(start, end);
		}
		
//		for (int i = 0; i < vehicleNumGenerator; i++) {
//			int startRoadId = -1;
//			int endRoadId = -1;
//			ArrayList<RoadNode> startRoadNodeList = new ArrayList<>();
//			ArrayList<RoadNode> endRoadNodeList = new ArrayList<>();
//			
//			boolean repeat = false;
//			do {
//				repeat = false;
//				startRoadId = random.nextIntFromTo(0, roadList.size() - 1);
//				endRoadId = random.nextIntFromTo(0, roadList.size() - 1);
//				
//				if (endRoadId == startRoadId) {
//					endRoadId = random.nextIntFromTo(0, roadList.size() - 1);
//					repeat = true;
//				}
//				
//				else if (!roadHashMap.containsKey(startRoadId)) {
//					repeat = true;
//				}
//				
//				else if (!roadHashMap.containsKey(endRoadId)) {
//					repeat = true;
//				}
//				
//				else {
////					System.out.println("Start road id: " + startRoadId);
////					System.out.println("End road id: " + endRoadId);
//					startRoadNodeList = roadHashMap.get(startRoadId).getRoadNodeList();
//					endRoadNodeList = roadHashMap.get(endRoadId).getRoadNodeList();
//				}
//				
//			} while (repeat);
//			
//			
//			int startRoadNodeNum = random.nextIntFromTo(0, startRoadNodeList.size() - 1);
//			int endRoadNodeNum = random.nextIntFromTo(0, endRoadNodeList.size() - 1);
//			
//			RoadNode startRoadNode = startRoadNodeList.get(startRoadNodeNum);
//			RoadNode endRoadNode = endRoadNodeList.get(endRoadNodeNum);
//
//			Vehicle newVehicle = new Vehicle(startRoadNode, endRoadNode);
//			context.add(newVehicle);
//			geography.move(newVehicle, factory.createPoint(newVehicle.getCurrentCoordinate()));
//		}
	}
	
	private List<SimpleFeature> loadFeaturesFromShapeFile(String fileName) {
		URL url = null;
		try {
			url = new File(fileName).toURL();
		}
		catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		
		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		
		// Try to load the shapefile
		SimpleFeatureIterator featureIter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);
		
		try {
			featureIter = store.getFeatureSource().getFeatures().features();
			
			while(featureIter.hasNext()) {
				features.add(featureIter.next());
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			featureIter.close();
			store.dispose();
		}
		
		return features;
	}
	
	private void loadRoadFeatures(String fileName, Network net) {
		List<SimpleFeature> features = loadFeaturesFromShapeFile(fileName);
		
		int roadId = 0;
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			
			if (!geom.isValid()) {
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			else if (geom instanceof MultiLineString) {
				if (!(((String)feature.getAttribute("highway")).equals("footway") || ((String)feature.getAttribute("highway")).equals("steps"))) {
					MultiLineString multiLine = (MultiLineString)feature.getDefaultGeometry();
					
					for (int i = 0; i < multiLine.getNumGeometries(); i++) {
						LineString line = (LineString)multiLine.getGeometryN(i);
						Coordinate[] coordinates = line.getCoordinates();
						ArrayList<RoadNode> roadNodeList = new ArrayList<RoadNode>();
						
						for (int j = 0; j < coordinates.length; j++) {
							RoadNode node1 = new RoadNode(roadId, coordinates[j]);
							Point point = factory.createPoint(coordinates[j]);
							
							// Add the RoadNode to the geometry map
							context.add(node1);
							geography.move(node1, point);
							
							// Connect the RoadNode with the previous RoadNode
							if (j != 0) {
								RoadNode node2 = roadNodeList.get(j - 1);
								net.addEdge(node1, node2);
							}
							
							roadNodeList.add(node1);
							
							/* Testing A star search algo */
							if (((String)feature.getAttribute("name")).equals("Jalan Inovasi 2") && j == 0) {
								test1 = node1;
							}
							
//							if (((String)feature.getAttribute("service")).equals("parking_aisle") && j == 4) {
//								test1 = node1;
//							}
							
							if (((String)feature.getAttribute("name")).equals("Jalan Merah Caga") && j == 2) {
								test1a = node1;
							}
							
							if (((String)feature.getAttribute("name")).equals("Jalan SR 8/13") && j == 1) {
								test2 = node1;
							}
						}
						
						// Read the feature attributes and assign to Road
						String name = (String)feature.getAttribute("name");
						if(name.equals("")) {
							name = "road_" + roadList.size();
						}
						boolean isOneWay = feature.getAttribute("oneway").equals("yes") ? true : false;
						isOneWay = feature.getAttribute("junction").equals("roundabout") ? true : isOneWay;
						int laneNum = feature.getAttribute("lanes") != "" ? Integer.valueOf((String)feature.getAttribute("lanes")) : 0;

						Road road = new Road(roadId, name, roadNodeList, isOneWay, laneNum);
						roadList.add(road);
						roadHashMap.put(roadId, road);
					}
				}
			}
			roadId++;
		}
		
		createRoadNetwork(net);
	}
	
	private void createRoadNetwork(Network net) {
		
		// Loop all roads in the list
		for (int i = 0; i < roadList.size(); i++) {
			ArrayList<RoadNode> roadNodeList = roadList.get(i).getRoadNodeList();

			// Loop all road nodes in the list
			for (RoadNode roadNode : roadNodeList) {
				Coordinate coordinate = roadNode.getCoordinate();
				
				// Loop all other roads in the list
				for (int j = i + 1; j < roadList.size(); j++) {
					ArrayList<RoadNode> otherRoadNodeList = roadList.get(j).getRoadNodeList();
					
					// Loop all other road nodes in the list
					for (RoadNode otherRoadNode : otherRoadNodeList) {
						Coordinate otherRoadCoordinate = otherRoadNode.getCoordinate();
						
						// Check if the roads connect and are different roads
						if (coordinate.equals(otherRoadCoordinate) && roadNode.getRoadId() != otherRoadNode.getRoadId()) {	
							boolean isExistingIntersection = false;
							
							// Check if existing intersection exists
							for (Intersection intersection : intersectionList) {
								
								// Existing intersection exists
								if (intersection.getCoordinate().equals(coordinate)) {
									intersection.addRoadNode(otherRoadNode);
									otherRoadNode.setIntersection(intersection);
									isExistingIntersection = true;
									break;
								}
							}
							
							if (!isExistingIntersection) {
								Intersection intersection = new Intersection(coordinate, roadNode, otherRoadNode);
								intersectionList.add(intersection);
								roadNode.setIntersection(intersection);
								otherRoadNode.setIntersection(intersection);
								
								context.add(intersection);
								geography.move(intersection, factory.createPoint(coordinate));
							}
							net.addEdge(roadNode, otherRoadNode);
						}
					}
				}
			}
		}
	}

	private void generateVehicleSpawnPointList() {
		for (Road road : roadList) {
			RoadNode startRoadNode = road.getFirstRoadNode();
			RoadNode endRoadNode = road.getLastRoadNode();
			 
			if (startRoadNode.getIntersection() == null) {
				spawnPointList.add(startRoadNode);
			}
			
			if (endRoadNode.getIntersection() == null) {
				spawnPointList.add(endRoadNode);
			}
		}
	}
	
	private void loadTrafficSignalFeatures(String fileName, Network net) {
		List<SimpleFeature> features = loadFeaturesFromShapeFile(fileName);
		
		// For each feature in the file
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			
			if (!geom.isValid()) {
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			// For Points, create Traffic Light agents
			if (geom instanceof Point) {
				if (((String)feature.getAttribute("highway")).equals("traffic_signals") && !((String)feature.getAttribute("crossing")).equals("traffic_signals")) {
					geom = (Point)feature.getDefaultGeometry();
					
					// Read the feature attributes and assign to Traffic Signal
					String name = (String)feature.getAttribute("Name");
					TrafficSignal trafficSignal = new TrafficSignal(name, geom.getCoordinate());
					context.add(trafficSignal);
					geography.move(trafficSignal, geom);
					trafficSignalList.add(trafficSignal);
				}
			}
		}
		
		linkTrafficSignalsWithIntersection();
	}
	
	private void linkTrafficSignalsWithIntersection() {
		for (TrafficSignal trafficSignal : trafficSignalList) {
			
			for (Intersection intersection : intersectionList) {
				
				// If traffic signal is on intersection
				if (trafficSignal.getCoordinate().equals(intersection.getCoordinate())) {
					intersection.addTrafficSignal(trafficSignal);
					break;
				}
			}
			
			if (trafficSignal.getJunction() != null) {
				continue;
			}
			
			// Check for nearby traffic signals in 200m 
			Coordinate coordinate1 = new Coordinate(trafficSignal.getCoordinate().getX() - 0.0002, trafficSignal.getCoordinate().getY() - 0.0002);
			Coordinate coordinate2 = new Coordinate(trafficSignal.getCoordinate().getX() + 0.0002, trafficSignal.getCoordinate().getY() + 0.0002);
			Envelope envelope = new Envelope(coordinate1, coordinate2);
			Iterator<TrafficSignal> iterator = geography.getObjectsWithin(envelope, TrafficSignal.class).iterator();
			
			int count = 0;
			LinkedList<TrafficSignal> tempTrafficSignalList = new LinkedList<>();
			
			while (iterator.hasNext()) {
				count++;
				tempTrafficSignalList.add(iterator.next());
			}
			
			boolean hasExistingJunction = false;
			for (Junction junction : junctionList) {
				
				for (TrafficSignal iteratorTrafficSignal : tempTrafficSignalList) {
					if (junction.contains(iteratorTrafficSignal)) {
						hasExistingJunction = true;
						break;
					}
				}
				
				if (hasExistingJunction) {
					for (TrafficSignal iteratorTrafficSignal : tempTrafficSignalList) {
						iteratorTrafficSignal.setJunction(junction);
						
						if (!junction.contains(iteratorTrafficSignal)) {
							junction.addTrafficSignal(iteratorTrafficSignal);
						}
					}
					break;
				}
			}
			
			if (!hasExistingJunction) {
				Junction newJunction = new Junction();
				
				for (TrafficSignal iteratorTrafficSignal : tempTrafficSignalList) {
					iteratorTrafficSignal.setJunction(newJunction);
					newJunction.addTrafficSignal(iteratorTrafficSignal);
				}
				
				junctionList.add(newJunction);
			}
		}
	}

}
