package trafficFlowSimulation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
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

public class TrafficFlowSimulationBuilder implements ContextBuilder {

	private List<Road> roadList = new ArrayList<Road>();
	private List<Intersection> intersectionList = new ArrayList<Intersection>();
	
	@Override
	public Context build(Context context) {
		GeographyParameters<Object> params = new GeographyParameters<Object>();
		Geography geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("TrafficFlowMap", context, params);
		GeometryFactory factory = new GeometryFactory();
		NetworkBuilder<?> netBuilder = new NetworkBuilder<Object>("Network", context, true);
		Network net = netBuilder.buildNetwork();
		
		// Create an area in which to create agents.
//		List<SimpleFeature> buildingFeatures = loadFeaturesFromShapeFile("./data/buildings-polygon.shp");
//		Geometry buildingBoundary = (MultiPolygon)buildingFeatures.iterator().next().getDefaultGeometry();
//		List<SimpleFeature> roadFeatures = loadFeaturesFromShapeFile("./data/roads-line.shp");
//		Geometry roadBoundary = (MultiLineString)roadFeatures.iterator().next().getDefaultGeometry();
//		
//		// Generate random points in the area (buildings) to create agents
//		List<Coordinate> agentCoordinates1 = GeometryUtil.generateRandomPointsInPolygon(buildingBoundary, 100);
		
		// Generate random points in the area (roads) to create agents
//		List<Coordinate> agentCoordinates2 = GeometryUtil.generateRandomPointsInLine(roadBoundary, 50);
		
		// Create the agents from the collection of random coords.
//		int cnt=0;
//		for (Coordinate coord : agentCoordinates1) {
//			Vehicle agent = new Vehicle();
//			context.add(agent);
//
//			Point geom = factory.createPoint(coord);
//			geography.move(agent, geom);
//			
//			Object o = context.getRandomObject();
//		
//			if (o != null && o instanceof Vehicle) {
//				net.addEdge(agent, o, 1.0);
//			}
//			cnt++;
//		}
		
		// Load features from shapefiles
		loadFeatures("./data/amenity_points-point.shp", context, geography);
		loadFeatures("./data/roads-line.shp", context, geography);
		createRoadNetwork();
		
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

	private void loadFeatures(String fileName, Context context, Geography geography) {
		List<SimpleFeature> features = loadFeaturesFromShapeFile(fileName);
		
		// For each feature in the file
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			Object agent = null;
			
			if (!geom.isValid()) {
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			// For Points, create Traffic Light agents
			if (geom instanceof Point) {
				if (((String)feature.getAttribute("highway")).equals("traffic_signals")) {
					geom = (Point)feature.getDefaultGeometry();
					
					// Read the feature attributes and assign to Traffic Signal
					String name = (String)feature.getAttribute("Name");
					agent = new TrafficSignal(name);
				}
			}
			else if (geom instanceof MultiLineString) {
				if (!((String)feature.getAttribute("highway")).equals("footway")) {
					MultiLineString line = (MultiLineString)feature.getDefaultGeometry();
					geom = line.getGeometryN(0);
					
					// Read the feature attributes and assign to Road
					String name = (String)feature.getAttribute("name");
					if(name.equals("")) {
						name = "road_" + roadList.size();
					}
					boolean isOneWay = feature.getAttribute("oneway").equals("yes") ? true : false;
					int laneNum = feature.getAttribute("lanes") != "" ? Integer.valueOf((String)feature.getAttribute("lanes")) : 0;
					agent = new Road(name, line.getCoordinates(), isOneWay, laneNum);
					roadList.add((Road) agent);
				}
			}
			
			// Add the agent to this context and associate it with its geometry
			if (agent != null) {
				context.add(agent);
				geography.move(agent, geom);
			}
		}
	}
	
	private void createRoadNetwork() {
		for (int i = 0; i < roadList.size(); i++) {
			Coordinate[] coordinates = roadList.get(i).getCoordinateList();
			
			for (Coordinate coordinate : coordinates) {
				double xCoordinate = coordinate.getX();
				double yCoordinate = coordinate.getY();
				
				for (int j = i + 1; j < roadList.size(); j++) {
					Coordinate[] otherRoadCoordinates = roadList.get(j).getCoordinateList();
					
					for (Coordinate otherRoadCoordinate : otherRoadCoordinates) {
						// Check if the roads connect
						if (otherRoadCoordinate.getX() == xCoordinate && otherRoadCoordinate.getY() == yCoordinate) {
							
							boolean isExistingIntersection = false;
							
							// Check if existing intersection exists
							for (Intersection intersection : intersectionList) {
								// Existing intersection exists
								if (intersection.getCoordinate().getX() == xCoordinate && intersection.getCoordinate().getY() == yCoordinate) {
									intersection.addRoad(roadList.get(j));
									roadList.get(j).addIntersection(intersection);
									isExistingIntersection = true;
									break;
								}
							}
							
							if (!isExistingIntersection) {
								Intersection intersection = new Intersection(coordinate, roadList.get(i), roadList.get(j));
								intersectionList.add(intersection);
								roadList.get(i).addIntersection(intersection);
								roadList.get(j).addIntersection(intersection);
								
								System.out.println(roadList.get(i).getName() + " is connected to " + roadList.get(j).getName());
							}
							
						}
					}
				}
			}
		}
	}
}
