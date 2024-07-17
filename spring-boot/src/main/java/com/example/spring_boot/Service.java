package com.example.spring_boot;

import org.springframework.web.client.RestTemplate;

public class Service {
	
	private String url = "http://localhost:4567";
	
	public String getSimulationData(String endpoint) {
		String endPoint = url + endpoint;
		RestTemplate restTemplate = new RestTemplate();
		String simulationData = restTemplate.getForObject(endPoint, String.class);
		
		return simulationData;
	}
	
	public String setTrafficSignalConfig(String requestBody) {
		String endPoint = url + "/traffic-signals-config";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, requestBody, String.class);
	}
	
	public String setRoadConfig(String requestBody) {
		String endPoint = url + "/roads-config";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, requestBody, String.class);
	}
	
	public String setSimulationSpeed(String requestBody) {
		String endPoint = url + "/simulation-speed";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, requestBody, String.class);
	}
	
	public String setVehicleInFlow(String requestBody) {
		String endPoint = url + "/vehicle-in-flow";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, requestBody, String.class);
	}
	
	public String setSimulationStart() {
		String endPoint = url + "/simulation-start";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, null, String.class);
	}
	
	public String setSimulationResume() {
		String endPoint = url + "/simulation-resume";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, null, String.class);
	}
	
	public String setSimulationPause() {
		String endPoint = url + "/simulation-pause";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, null, String.class);
	}
	
	public String setSimulationStop() {
		String endPoint = url + "/simulation-stop";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, null, String.class);
	}
}
