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
	
	public String setSimulationStart() {
		String endPoint = url + "/simulation-start";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, null, String.class);
	}
	
	public String setSimulationStop() {
		String endPoint = url + "/simulation-stop";
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(endPoint, null, String.class);
	}
}
