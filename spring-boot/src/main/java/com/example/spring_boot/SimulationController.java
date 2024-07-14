package com.example.spring_boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimulationController {
	
	private Service simulationService;
	
    @Autowired
    public SimulationController(Service simulationService) {
        this.simulationService = simulationService;
    }
	
	@GetMapping("/vehicles")
	public String getVehicleData() {
		return simulationService.getSimulationData("/vehicles");
	}
	
	@GetMapping("/roads")
	public String getRoadData() {
		return simulationService.getSimulationData("/roads");
	}
	
	@GetMapping("/traffic-signals")
	public String getTrafficSignalsData() {
		return simulationService.getSimulationData("/traffic-signals");
	}
	
	@PostMapping("/traffic-signals-config")
	public ResponseEntity<String> setTrafficSignalConfig(@RequestBody String requestBody) {
		try {
			String response = simulationService.setTrafficSignalConfig(requestBody);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}
	
	@PostMapping("/simulation-start")
	public ResponseEntity<String> setSimulationStart() {
		String response = simulationService.setSimulationStart();
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/simulation-stop")
	public ResponseEntity<String> setSimulationStop() {
		String response = simulationService.setSimulationStop();
		return ResponseEntity.ok(response);
	}
}
