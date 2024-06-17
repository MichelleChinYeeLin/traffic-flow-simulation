package trafficFlowSimulation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
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
import repast.simphony.gis.util.GeometryUtil;
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
	private RoadNode test1 = null;
	private RoadNode test2 = null;
	
	// Test
	public static Network net2 = null;
	
	@Override
	public Context build(Context context) {
		GeographyParameters<Object> params = new GeographyParameters<Object>();
		Geography geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("TrafficFlowMap", context, params);
		GeometryFactory factory = new GeometryFactory();
		NetworkBuilder<?> netBuilder = new NetworkBuilder<Object>("Network", context, true);
		Network net = netBuilder.buildNetwork();
		NetworkBuilder<?> net2Builder = new NetworkBuilder<Object>("Test Network", context, true);
		net2 = net2Builder.buildNetwork();
		
		// Load features from shapefiles
		loadRoadFeatures("./data/roads-line.shp", context, geography, factory, net);
		loadTrafficSignalFeatures("./data/amenity_points-point.shp", context, geography, factory, net);
		
		Vehicle testVehicle = new Vehicle();
		context.add(testVehicle);
		geography.move(testVehicle, factory.createPoint(new Coordinate(101.7010, 3.0551)));
		LinkedList<RoadNode> route = testVehicle.calculateRoute(test1, test2);
		if (route != null) {
			for (int i = 0; i < route.size(); i++) {
				if (i == route.size() - 1) {
					break;
				}
				
				net2.addEdge(route.get(i), route.get(i + 1));
			}
		}
		if (route == null) {
			System.out.println("Route not found");
		}
		
		return context;
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
	
	private void loadRoadFeatures(String fileName, Context context, Geography geography, GeometryFactory factory, Network net) {
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
							if (((String)feature.getAttribute("name")).equals("Jalan SR 8/13") && j == 0) {
								test1 = node1;
							}
							
							if (((String)feature.getAttribute("name")).equals("Jalan Teknologi 6") && j == 1) {
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
		
		createRoadNetwork(context, net, geography, factory);
	}
	
	private void createRoadNetwork(Context context, Network net, Geography geography, GeometryFactory factory) {
		
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

	private void loadTrafficSignalFeatures(String fileName, Context context, Geography geography, GeometryFactory factory, Network net) {
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
		}
	}

}
