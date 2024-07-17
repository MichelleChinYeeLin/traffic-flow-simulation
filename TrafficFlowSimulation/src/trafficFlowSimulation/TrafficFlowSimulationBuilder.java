package trafficFlowSimulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.port;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
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
import repast.simphony.context.DefaultContext;
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
import repast.simphony.engine.environment.RunEnvironmentBuilder;
import repast.simphony.engine.schedule.DefaultSchedulableActionFactory;
import repast.simphony.engine.schedule.ISchedule;
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
	public static List<TrafficSignal> trafficSignalList = new ArrayList<TrafficSignal>();
	private List<Junction> junctionList = new ArrayList<Junction>();
	private List<RoadNode> spawnPointList = new ArrayList<>();
	public static List<Vehicle> vehicleList = new ArrayList<>();
	private RoadNode test1 = null;
	private RoadNode test1a = null;
	private RoadNode test2 = null;
	private int vehicleNumGenerator = 1000; // vehicle in-flow per hour (veh/h)
	private int vehicleNumFirstInitGenerator = 200;
	private int vehicleCount = 0;
	private boolean isFirstInitialization = true;
	
	public static Context context;
	public static Geography geography;
	public static GeometryFactory factory;
	
	// Test
	public static Network net2 = null;
	public static Network roadNetwork = null;
	
	@Override
	public Context build(Context context) {
		this.context = context;
		GeographyParameters<Object> params = new GeographyParameters<Object>();
		this.geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("TrafficFlowMap", context, params);
		this.factory = new GeometryFactory();
		
		NetworkBuilder<?> netBuilder = new NetworkBuilder<Object>("Network", context, true);
		roadNetwork = netBuilder.buildNetwork();
		NetworkBuilder<?> net2Builder = new NetworkBuilder<Object>("Test Network", context, true);
		net2 = net2Builder.buildNetwork();
		
		// Load features from shapefiles
		loadRoadFeatures("./data/roads-line.shp", roadNetwork);
		loadTrafficSignalFeatures("./data/amenity_points-point.shp", roadNetwork);
		
		generateVehicleSpawnPointList();
		generateVehicleAgentFirstInitialization();
		
		Schedule vehicleGeneratorSchedule = (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters rneScheduleParameters = ScheduleParameters.createRepeating(1, 60);
		vehicleGeneratorSchedule.schedule(rneScheduleParameters, this, "generateVehicleAgent");
		
//		for (Road road : roadList) {
//			for (RoadNode roadNode : road.getRoadNodeList()) {
//				if (roadNode.getCoordinate().equals(testCoordinate)) {
//					System.out.println(road.getRoadId() + " : " + roadNode.getIntersection().getConnectedRoadNodeList().size());
//					
//					for (RoadNode testRoadNode : roadNode.getIntersection().getConnectedRoadNodeList()) {
//						System.out.println(testRoadNode.getRoadId());
//					}
//				}
//			}
//		}
		
//		for (Intersection intersection : intersectionList) {
//			System.out.println("Intersection coordinates: (" + intersection.getCoordinate().getX() + ", " + intersection.getCoordinate().getY() + ") | " + intersection.getConnectedRoadNodeList().size());
//		}
		
		if (isFirstInitialization) {
			initializeEndpoints();
			isFirstInitialization = false;
		}
		
		return context;
	}
	
//	public void initializeSimulation() {
//		Context<Object> newContext = new DefaultContext<>("MySimulation");
//		build(newContext);
//	}
	
	public void initializeEndpoints() {
		try {
            port(4567);

            TrafficFlowSimulation simulation = new TrafficFlowSimulation();
            context.add(simulation);
//            simulation.run();

            get("/vehicles", (req, res) -> {
//            	String response = "{\"data\":[";
//            	ArrayList<Vehicle> nextVehicleList = TrafficFlowSimulation.getFirstVehicleList();
//            	boolean isFirst = true;
//            	
//            	for (int i = 0; i < nextVehicleList.size(); i++) {
//            		Vehicle vehicle = nextVehicleList.get(i);
//            		
//            		if (!vehicle.isRouteCompleted()) {
//            			if (isFirst) {
//            				isFirst = false;
//            			}
//            			else {
//            				response += ", ";
//            			}
//            			Coordinate vehicleCoordinate = vehicle.getCurrentCoordinate();
//                		String vehicleString = "{\"name\": " + vehicle.getName() + ", \"xCoordinate\": " + vehicleCoordinate.getX() + ", \"yCoordinate\": " + vehicleCoordinate.getY() + "}";
//                		response += vehicleString;
//            		}
//            	}
//            	
//            	response += "]}";
            	
                return TrafficFlowSimulation.getFirstVehicleList();
            });
            
            get("/roads", (req, res) -> {
            	String response = "{\"data\":[";
            	
            	for (int i = 0; i < roadList.size(); i++) {
            		Road road = roadList.get(i);
            		
            		if (road != null) {
            			String roadString = "{\"id\": " + road.getRoadId() + ", \"name\": \"" + road.getName() + "\", \"isOneWay\": " + road.isOneWay() + ", \"node\":[";
                		for (int j = 0; j < road.getRoadNodeList().size(); j++) {
                			RoadNode node = road.getRoadNodeList().get(j);
                			Coordinate nodeCoordinate = node.getCoordinate();
                			roadString += "{\"roadNodeId\": " + j + ", \"xCoordinate\": " + nodeCoordinate.getX() + ", \"yCoordinate\": " + nodeCoordinate.getY() + "}";
                			
                			if (j < road.getRoadNodeList().size() - 1) {
                				roadString += ", ";
                			}
                    		else {
                    			roadString += "]}";
                    		}
                		}
                		
                		response += roadString;
                		if (i != roadList.size() - 1) {
                			response += ",";
                		}
            		}
            	}
            	
            	response += "]}";
                return response;
            });
            
            get("/traffic-signals", (req, res) -> {
//            	String response = "{\"data\":[";
//            	ArrayList<TrafficSignal> nextTrafficSignalList = TrafficFlowSimulation.getFirstTrafficSignalList();
//            	
//            	for (int i = 0; i < nextTrafficSignalList.size(); i++) {
//            		TrafficSignal trafficSignal = nextTrafficSignalList.get(i);
//            		Coordinate coordinate = trafficSignal.getCoordinate();
//            		
//            		String trafficSignalString = "{\"name\": " + "\"" + trafficSignal.getName() + "\"" + 
//            									 ", \"junctionId\": " + trafficSignal.getJunction().getJunctionId() +
//            									 ", \"duration\": " + trafficSignal.getDuration() +
//            									 ", \"sequence\": " + trafficSignal.getSequence() +
//            									 ", \"isActive\": " + trafficSignal.getIsActive() + 
//            									 ", \"xCoordinate\": " + coordinate.getX() + 
//            									 ", \"yCoordinate\": " + coordinate.getY() + "}";
//            		response += trafficSignalString;
//            		
//            		if (i != trafficSignalList.size() - 1) {
//            			response += ", ";
//            		}	
//            	}
//            	
//            	response += "]}";
            	
                return TrafficFlowSimulation.getFirstTrafficSignalList();
            });
            
            post("/traffic-signals-config", (req, res) -> {
            	String requestBody = req.body();
            	
            	// Remove all current traffic signals
            	for (TrafficSignal trafficSignal : trafficSignalList) {
            		context.remove(trafficSignal);
            	}
            	
            	// Remove all current junctions
            	for (Junction junction : junctionList ) {
            		context.remove(junction);
            	}

            	trafficSignalList = new ArrayList<>();
            	junctionList = new ArrayList<>();
            	
            	try {
            		List<String> data = extractJsonList(requestBody, "config");
                	for (String dataString : data) {
                		String trafficSignalName = extractJsonValue(dataString, "name");
                		int duration = Integer.parseInt(extractJsonValue(dataString, "duration"));
                		int sequence = Integer.parseInt(extractJsonValue(dataString, "sequence"));
                		Double xCoordinate = Double.parseDouble(extractJsonValue(dataString, "xCoordinate"));
                		Double yCoordinate = Double.parseDouble(extractJsonValue(dataString, "yCoordinate"));
                		int junctionId = Integer.parseInt(extractJsonValue(dataString, "junctionId"));
                		boolean isJunctionFound = false;
                		
                		TrafficSignal trafficSignal = new TrafficSignal(trafficSignalName, new Coordinate(xCoordinate, yCoordinate), duration, sequence);
                		
                		for (Junction currentJunction : junctionList) {
                			if (currentJunction.getJunctionId() == junctionId) {
                				isJunctionFound = true;
                				
                				trafficSignal.setJunction(currentJunction);
                				currentJunction.addTrafficSignal(trafficSignal);
            					context.add(trafficSignal);
            					geography.move(trafficSignal, factory.createPoint(trafficSignal.getCoordinate()));
            					trafficSignalList.add(trafficSignal);
                				
                				break;
                			}
                		}
                		
                		if (!isJunctionFound) {
                			Junction junction = new Junction(junctionId);
                			junctionList.add(junction);
                			
                    		trafficSignal.setJunction(junction);
                    		junction.addTrafficSignal(trafficSignal);
        					context.add(trafficSignal);
        					geography.move(trafficSignal, factory.createPoint(trafficSignal.getCoordinate()));
        					trafficSignalList.add(trafficSignal);
                		}
                		
                		boolean isIntersectionFound = false;
                		for (Intersection intersection : intersectionList) {
            				
            				// If traffic signal is on intersection
            				if (trafficSignal.getCoordinate().equals(intersection.getCoordinate())) {
            					intersection.addTrafficSignal(trafficSignal);
            					isIntersectionFound = true;
            					break;
            				}
            			}
                		
                		if (!isIntersectionFound) {
                			boolean isRoadNodeFound = false;
                			for (Road road : roadList) {
                				for (RoadNode roadNode : road.getRoadNodeList()) {
                					if (roadNode.getCoordinate().equals(trafficSignal.getCoordinate())) {
                						Intersection newIntersection = new Intersection(trafficSignal.getCoordinate(), roadNode);
                						intersectionList.add(newIntersection);
                						isRoadNodeFound = true;
                						break;
                					}
                				}
                				
                				if (isRoadNodeFound) {
                					break;
                				}
                			}
                		}
                	}
            	}
            	catch (Exception e) {
            		System.err.println("Error processing request: " + e.getMessage());
            		e.printStackTrace();
            		
            		res.status(500);
                    res.type("application/json");
                    return "{\"message\": \"Internal server error\"}";
            	}
            	res.status(200);
            	res.type("application/json");
            	
            	return "{\"message\": \"Success\"}";
            });
            
            post("/roads-config", (req, res) -> { 
            	String requestBody = req.body();
            	
            	ArrayList<Intersection> testIntersectionList = new ArrayList<>(intersectionList);
            	ArrayList<Road> testRoadList = new ArrayList<>(roadList);
            	
            	// Remove all current intersections
            	for (Intersection intersection : intersectionList) {
            		context.remove(intersection);
            	}
            	
            	// Remove all current roads
            	for (Road road : roadList) {
            		for (RoadNode roadNode : road.getRoadNodeList()) {
            			context.remove(roadNode);
            		}
            	}
            	
//            	roadList = new ArrayList<>();
//            	roadHashMap = new HashMap<>();
//            	intersectionList = new ArrayList<>();
            	roadList.clear();
            	roadHashMap.clear();
            	intersectionList.clear();
            	roadNetwork.removeEdges();
            	
            	try {
            		List<String> data = extractJsonList(requestBody, "config");
                	for (String dataString : data) {
                		int roadId = Integer.parseInt(extractJsonValue(dataString, "id"));
                		String roadName = extractJsonValue(dataString, "name");
                		boolean isOneWay = extractJsonValue(dataString, "isOneWay").equals("true") ? true : false;
                		List<String> roadNodeData = extractJsonList(dataString, "node");
                		ArrayList<RoadNode> roadNodeList = new ArrayList<>();
                		
                		RoadNode prevRoadNode = null;
                		for (String roadNodeString: roadNodeData) {
                			double xCoordinate = Double.parseDouble(extractJsonValue(roadNodeString, "xCoordinate"));
                			double yCoordinate = Double.parseDouble(extractJsonValue(roadNodeString, "yCoordinate"));
                			RoadNode newRoadNode = new RoadNode(roadId, new Coordinate(xCoordinate, yCoordinate));
                			
                			context.add(newRoadNode);
                			geography.move(newRoadNode, factory.createPoint(new Coordinate(xCoordinate, yCoordinate)));
                			
                			if (prevRoadNode != null) {
								roadNetwork.addEdge(newRoadNode, prevRoadNode);
							}
                			prevRoadNode = newRoadNode;
                			roadNodeList.add(newRoadNode);
                		}
                		
                		Road newRoad = new Road(roadId, roadName, roadNodeList, isOneWay);
                		roadList.add(newRoad);
                		roadHashMap.put(roadId, newRoad);
                	}
            	}
            	catch (Exception e) {
            		System.err.println("Error processing request: " + e.getMessage());
            		e.printStackTrace();
            		
            		res.status(500);
                    res.type("application/json");
                    return "{\"message\": \"Internal server error\"}";
            	}
            	
            	createRoadNetwork(roadNetwork);
            	
            	spawnPointList = new ArrayList<>();
            	generateVehicleSpawnPointList();
            	
            	
//            	System.out.println("Intersection list size: " + intersectionList.size());
            	
//        		for (Intersection intersection : intersectionList) {
//        			System.out.println("Intersection coordinates: (" + intersection.getCoordinate().getX() + ", " + intersection.getCoordinate().getY() + ") | " + intersection.getConnectedRoadNodeList().size());
//        		}
            	
            	// Log final state
//                System.out.println("Final state:");
//                System.out.println("hash map: " + roadHashMap.size());
//                System.out.println("road list: " + roadList.size());
//                System.out.println("intersection list: " + intersectionList.size());
            	
            	res.status(200);
            	res.type("application/json");
            	
            	
            	            	
//            	for (Road road : roadList) {
//        			for (RoadNode roadNode : road.getRoadNodeList()) {
//        				if (roadNode.getCoordinate().equals(testCoordinate)) {
//        					System.out.println(road.getRoadId() + " : " + roadNode.getIntersection().getConnectedRoadNodeList().size());
//        				}
//        			}
//        		}
            	
//            	for (Road road : testRoadList) {
//            		System.out.println("Road " + road.getRoadId() + ": " + road.getRoadNodeList().size());
//            	}
            	
//            	String testString = "{\"data\": [";
//            	
//            	for (Intersection intersection : intersectionList) {
//            		testString += "{\"coordinate\": (" + intersection.getCoordinate().getX() + ", " + intersection.getCoordinate().getY() + "), \"node\": [";
//            		
//            		ArrayList<RoadNode> roadNodeList = intersection.getConnectedRoadNodeList();
//            		
//            		for (RoadNode roadNode : roadNodeList) {
//            			testString += "{" + roadNode.getRoadId() + " | " + roadNode.getCoordinate().getX() + " | " + roadNode.getCoordinate().getY() + "}, ";
//            		}
//            		
//            		testString += "]}";
//            	}
//            	
//            	testString += "]}";
            	
            	// Return a response
//            	return testString;
            	return "{\"message\": \"Success\"}";
            });
            
            post("/simulation-speed", (req, res) -> {
            	String requestBody = req.body();
            	
            	int speed = Integer.parseInt(extractJsonValue(requestBody, "speed"));
            	TrafficFlowSimulation.tickToSecondRepresentation = speed;
            	System.out.println("speed: " + speed);
            	res.status(200);
            	res.type("application/json");
            	return "{\"message\": \"Success\"}";
            });
            
            post("/vehicle-in-flow", (req, res) -> {
            	String requestBody = req.body();
            	
            	int vehicleInFlow = Integer.parseInt(extractJsonValue(requestBody, "vehicleInFlow"));
            	vehicleNumGenerator = vehicleInFlow;
            	System.out.println("Vehicle in flow: " + vehicleInFlow);
            	res.status(200);
            	res.type("application/json");
            	return "{\"message\": \"Success\"}";
            });
            
            post("/simulation-start", (req, res) -> {
            	Schedule vehicleGeneratorSchedule = (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
            	
            	if (vehicleGeneratorSchedule != null) {
            		ScheduleParameters rneScheduleParameters = ScheduleParameters.createRepeating(1, 60);
            		vehicleGeneratorSchedule.schedule(rneScheduleParameters, this, "generateVehicleAgent");
            	}
//            	else {
//            		System.out.println("no schedule");
//            	}
            	generateVehicleAgentFirstInitialization();
        		TrafficFlowSimulation.isFirstTick = true;
            	RunEnvironment.getInstance().resumeRun();
            	res.status(200);
                res.type("application/json");

                // Return a response
                return "{\"message\": \"Simulation start\"}";
            });
            
            post("/simulation-resume", (req, res) -> {
            	RunEnvironment.getInstance().resumeRun();
            	res.status(200);
                res.type("application/json");

                // Return a response
                return "{\"message\": \"Simulation resumed\"}";
            });
            
            post("/simulation-pause", (req, res) -> {
            	RunEnvironment.getInstance().pauseRun();
            	res.status(200);
                res.type("application/json");

                // Return a response
                return "{\"message\": \"Simulation resumed\"}";
            });
            
            post("/simulation-stop", (req, res) -> {
            	RunEnvironment.getInstance().pauseRun();
            	
            	for (Vehicle vehicle : vehicleList) {
            		vehicle.setIsRouteCompleted(true);
            	}
            	TrafficFlowSimulation.clearVehicleListQueue();
            	TrafficFlowSimulation.clearTrafficSignalListQueue();
            	
            	res.status(200);
                res.type("application/json");

                // Return a response
                return "{\"message\": \"Simulation stopped\"}";
            });
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateVehicleAgentFirstInitialization() {
		RandomHelper random = new RandomHelper();
		
		for (int i = 0; i < vehicleNumFirstInitGenerator; i++) {
			RoadNode start = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
			RoadNode end = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
			
			while (start.equals(end)) {
				end = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
			}
			
			Vehicle newVehicle = new Vehicle(start, end, String.valueOf(++vehicleCount));
			vehicleList.add(newVehicle);
		}
	}
	
	public void generateVehicleAgent() {
		RandomHelper random = new RandomHelper();
		
		for (int i = 0; i < vehicleNumGenerator / 60; i++) {
			Vehicle newVehicle = null;
			do {
				RoadNode start = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
				RoadNode end = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
				
				while (start.equals(end)) {
					end = spawnPointList.get(random.nextIntFromTo(0, spawnPointList.size() - 1));
				}
				
				newVehicle = new Vehicle(start, end, String.valueOf(++vehicleCount));
			} while(newVehicle == null || (newVehicle != null && newVehicle.isRouteCompleted()));
			vehicleList.add(newVehicle);
		}
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
//						int laneNum = feature.getAttribute("lanes") != "" ? Integer.valueOf((String)feature.getAttribute("lanes")) : 0;

//						if (isOneWay) {
//							System.out.println("is one way");
//						}
						Road road = new Road(roadId, name, roadNodeList, isOneWay);
						roadList.add(road);
						roadHashMap.put(roadId, road);
					}
					roadId++;
				}
			}
		}
		
		createRoadNetwork(net);
	}
	
	private void createRoadNetwork(Network<RoadNode> net) {
	    // Temporary map to store intersections by coordinates
	    Map<Coordinate, Intersection> intersectionMap = new HashMap<>();

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
	                        // Check if intersection already exists in the map
	                        Intersection intersection = intersectionMap.get(coordinate);

	                        if (intersection == null) { 
	    	                    
	                            // Create new intersection if it doesn't exist
	                            intersection = new Intersection(coordinate, roadNode, otherRoadNode);
	                            intersectionMap.put(coordinate, intersection);
	                            
	                            // Set intersection for the road nodes
		                        roadNode.setIntersection(intersection);
		                        otherRoadNode.setIntersection(intersection);
		                        
		                        intersectionList.add(intersection);
	                            context.add(intersection);
	                            geography.move(intersection, factory.createPoint(coordinate));
	                        } 
	                        
	                        else {
	                        	
		                        if (!intersection.containsRoadNode(roadNode)) {
		                        	intersection.addRoadNode(roadNode);
		                        	roadNode.setIntersection(intersection);
		                        }
		                        
		                        if (!intersection.containsRoadNode(otherRoadNode)) {
		                        	intersection.addRoadNode(otherRoadNode);
		                        	otherRoadNode.setIntersection(intersection);
		                        }
	                        }
//	                        else {
//	                            // Add road nodes to the existing intersection
//	                            intersection.addRoadNode(roadNode);
//	                            intersection.addRoadNode(otherRoadNode);
//	                        }
//
//	                        // Set intersection for the road nodes
//	                        roadNode.setIntersection(intersection);
//	                        otherRoadNode.setIntersection(intersection);

	                        // Add edge to the network
//	                        net.addEdge(roadNode, otherRoadNode);
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
		int count = 0;
		
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
					TrafficSignal trafficSignal = new TrafficSignal("Traffic Signal " + count++, geom.getCoordinate());
					context.add(trafficSignal);
					geography.move(trafficSignal, geom);
					trafficSignalList.add(trafficSignal);
				}
			}
		}
		
		linkTrafficSignalsWithIntersection();
	}
	
	private void linkTrafficSignalsWithIntersection() {
		int junctionCount = 0;
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
							iteratorTrafficSignal.setSequence(junction.getTrafficSignalList().size() - 1);
						}
					}
					break;
				}
			}
			
			if (!hasExistingJunction) {
				Junction newJunction = new Junction(junctionCount++);
				
				for (TrafficSignal iteratorTrafficSignal : tempTrafficSignalList) {
					iteratorTrafficSignal.setJunction(newJunction);
					newJunction.addTrafficSignal(iteratorTrafficSignal);
					iteratorTrafficSignal.setSequence(newJunction.getTrafficSignalList().size() - 1);
				}
				
				junctionList.add(newJunction);
			}
		}
	}
	
	// Helper method to extract value from JSON by key
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
        	System.out.println("search key:" + searchKey);
        	System.out.println("not found");
            return null;
        }

        startIndex += searchKey.length();
        char firstChar = json.charAt(startIndex);
        String value;

        if (firstChar == '[') {
            // Extract array
            int endIndex = findClosingBracket(json, startIndex, '[', ']');
            value = json.substring(startIndex, endIndex + 1).trim();
        } else if (firstChar == '{') {
            // Extract object
            int endIndex = findClosingBracket(json, startIndex, '{', '}');
            value = json.substring(startIndex, endIndex + 1).trim();
        } else {
            // Extract primitive value
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = json.indexOf("}", startIndex);
            }
            value = json.substring(startIndex, endIndex).trim();
        }

        // Remove any enclosing quotes from the value
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        return value;
    }

    // Helper method to extract a list of JSON objects by key
    private static List<String> extractJsonList(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
        	System.out.println("what??");
            return new ArrayList<>();
        }

        startIndex += searchKey.length();
        int endIndex = findClosingBracket(json, startIndex, '[', ']');
        String arrayString = json.substring(startIndex, endIndex + 1).trim();
        List<String> items = new ArrayList<>();
        int arrayStart = arrayString.indexOf('[') + 1;
        int arrayEnd = arrayString.lastIndexOf(']');

        int i = arrayStart;
        while (i < arrayEnd) {
            while (i < arrayEnd && Character.isWhitespace(arrayString.charAt(i))) {
                i++;
            }

            char firstChar = arrayString.charAt(i);
            int itemEndIndex = i;

            if (firstChar == '{') {
                itemEndIndex = findClosingBracket(arrayString, i, '{', '}');
                items.add(arrayString.substring(i, itemEndIndex + 1).trim());
                i = itemEndIndex + 1;
            }
            else if (firstChar == '[') {
                itemEndIndex = findClosingBracket(arrayString, i, '[', ']');
                items.add(arrayString.substring(i, itemEndIndex + 1).trim());
                i = itemEndIndex + 1;
            }

            
            while (i < arrayEnd && arrayString.charAt(i) == ',') {
                i++;
            }
        }

        return items;
    }

    // Helper method to find the closing bracket for nested structures
    private static int findClosingBracket(String json, int startIndex, char openBracket, char closeBracket) {
        int depth = 1;
        for (int i = startIndex + 1; i < json.length(); i++) {
            if (json.charAt(i) == openBracket) {
                depth++;
            } else if (json.charAt(i) == closeBracket) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;  // If not found, which indicates malformed JSON
    }

}
