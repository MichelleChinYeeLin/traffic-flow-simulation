package trafficFlowSimulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimulationController {
	
//	@Autowired
//	private SimulationService simulationService;
//	
//	@GetMapping("/simulation")
//	public String getSimulationData() {
//		return simulationService.runSimulation();
//	}
//import java.io.IOException;
//
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//public class SimulationController extends HttpServlet {
//	
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		String parameter = request.getParameter("type");
//		
//		String jsonResponse = "{test}";
//		response.setContentType("application/json");
//		response.getWriter().write(jsonResponse);
//	}
	
//	@ScheduledMethod(start = 1, interval = 1)
//	public void sendPostRequest() {
//		try {
//			URL url = new URL("http://localhost:8080/api/simulation");
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			connection.setRequestMethod("POST");
//			connection.setDoOutput(true);
//			
//			String postData = "{testlala}";
//			
//			try (OutputStream os = connection.getOutputStream()) {
//				os.write(postData.getBytes());
//			}
//			
//			int responseCode = connection.getResponseCode();
//			
//			System.out.println("Response code: " + responseCode);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
//	public void postData() {
//		try {
//			URL url = new URL("https://api.example.com/data");
//			
//			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//			connection.setRequestMethod("POST");
//		}
//	}
	
//	@Override
//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//		
//		String requestUrl = request.getRequestURI();
//		String requestData = requestUrl.substring("/simulation/".length());
//		
//		if(requestData != null){
////			String json = "{\n";
////			json += "\"name\": " + JSONObject.quote(person.getName()) + ",\n";
////			json += "\"about\": " + JSONObject.quote(person.getAbout()) + ",\n";
////			json += "\"birthYear\": " + person.getBirthYear() + "\n";
////			json += "}";
////			response.getOutputStream().println(json);
//			
//			if (requestData.equals("vehicle-data")) {
//				//Create json
//				String json = "{\n";
//				
//				json += "}";
//				response.getOutputStream().println(json);
//			}
//		}
//		else{
//			//Return an empty JSON object
//			response.getOutputStream().println("{}");
//		}
//	}
}


