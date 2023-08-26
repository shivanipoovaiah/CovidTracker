package ds.project.WebService;
/*
 * @author Shivani Poovaiah Ajjikutira
 * Last Modified 14 November 2021
 *
 * This class is the model of the webservice. The controller sends the state code request received
 * from the Android application to the model. The model uses the state code to fetch data from the
 * COVIDActNow API using HTTP GET request and returns the json response string sent by API back to
 * the controller after formatting the JSON to include only data needed in the Android app.The model
 * uses two helper classes CovidTrackerLogger.java and CovidTrackerDashboard.java to log data to the
 * database and to set up the dashboard table containing logged items along with performing operation
 * analytics.
 * */

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.Document;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CovidTrackerModel {
    private final CovidTrackerLogger logger;
    private final CovidTrackerDashboard dashboard;
    // api key needed to access API data
    private static final String apiKey = "bf29200dacdd47978b570eae4c06847b";
    // username set on MongoDB Atlas
    private static final String username = "sajjikut";
    // password set on MongoDB Atlas
    private static final String pwd = "sajjikut11";
    // MongoDB Atlas connection string
    private static final String loggingUrl = "mongodb+srv://" + username + ":" + pwd + "@covidtrackercluster.8htga.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
    // store API response status code
    private int statusCode=0;
    // store API response time
    private long responseTime=0;

    // create objects of helper classes
    CovidTrackerModel() {
        logger = new CovidTrackerLogger();
        dashboard= new CovidTrackerDashboard();
    }

    // getters and setters for responseTime and statusCode respective
    long getResponseTime() {
        return responseTime;
    }
    void setResponseTime(long responseTime) {
        this.responseTime=responseTime;
    }
    int getStatusCode() {
        return this.statusCode;
    }
    void setStatusCode(int statusCode) {
        this.statusCode=statusCode;
    }

    // helper method for sending request to API
    // Code from Lab 7 - Rest-Programming
    private Result sendAPIRequest(String state) {
        long startTime = System.currentTimeMillis();
        HttpURLConnection conn;
        int status;
        Result result = new Result();
        String apiEndpoint = "https://api.covidactnow.org/v2/state/"+state+".json?apiKey="+apiKey;
        try {
            // GET wants us to pass the name on the URL line
            URL url = new URL(apiEndpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // we are sending plain text
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            // tell the server what format we want back
            conn.setRequestProperty("Accept", "application/json");

            // wait for response
            status = conn.getResponseCode();
            System.out.println("API Response Status: "+status);

            // set http response code
            result.setResponseCode(status);
            setStatusCode(status);
            // set http response message - this is just a status message
            // and not the body returned by GET
            result.setResponseText(conn.getResponseMessage());

            if (status == 200) {
                String responseBody = getResponseBody(conn);
                result.setResponseText(responseBody);
            }

            conn.disconnect();

        }
        // handle exceptions
        catch (MalformedURLException e) {
            System.out.println("URL Exception thrown" + e);
        } catch (IOException e) {
            System.out.println("IO Exception thrown" + e);
        } catch (Exception e) {
            System.out.println("Exception thrown" + e);
        }
        long endTime = System.currentTimeMillis();
        setResponseTime(endTime-startTime);
        return result;
    }

    // method passes required items to private helper method to perform
    // the connection with API. Return formatted json response to the controller
    public String getAPIData(String state) {
        return parseResponseJSON(sendAPIRequest(state).getResponseText());
    }

    // https://stackoverflow.com/questions/57324744/how-to-connect-mongodb-using-mongodb-atlas?rq=1
    // Connect to MongoDB Atlas and create database and collection.
    private MongoCollection<Document> connectToMongoDB() {
        ConnectionString connectionString = new ConnectionString(loggingUrl);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("CovidTrackerDB");
        return database.getCollection("CovidTrackerLogs");
    }

    // method to log data to database by passing necessary data to logToMongoDb method
    // of CovidTrackerLogger.java class
    public void logToDatabase(String response, String device, long requestTime) {
        MongoCollection<Document> collection = connectToMongoDB();
        logger.logToMongoDb(collection,response,device,getStatusCode(),getResponseTime(),requestTime);
    }

    // method to set up table in dashboard and set operation analytics attributes by
    // calling getDatabaseData method of CovidTrackerDashboard.java class
    public String setUpTable(StringBuilder output, HttpServletRequest request) {
        MongoCollection<Document> collection = connectToMongoDB();
        String outputString =  dashboard.getDatabaseData(collection,output);
        String mostSearched = logger.mostSearchedState();
        request.setAttribute("mostSearched",mostSearched);
        request.setAttribute("visits",dashboard.visits);
        request.setAttribute("fastestResponse",dashboard.fastestResponse);
        request.setAttribute("population",dashboard.population);
        request.setAttribute("populatedState",dashboard.populatedState);
        request.setAttribute("positivity",String.format("%.2f%%", dashboard.positivity*100));
        request.setAttribute("positivityState",dashboard.positiveState);
        request.setAttribute("vacComp",String.format("%.2f%%", dashboard.vacComp*100));
        request.setAttribute("vacState",dashboard.vacState);
        return outputString;
    }

    // format json response to hold only necessary data to be sent back to controller
    private String parseResponseJSON(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONObject outputJson = new JSONObject();
        JSONObject metrics = jsonObject.getJSONObject("metrics");
        outputJson.put("state",jsonObject.get("state"));
        outputJson.put("population",jsonObject.get("population"));
        outputJson.put("testPositivityRatio",metrics.get("testPositivityRatio"));
        outputJson.put("infectionRate",metrics.get("infectionRate"));
        outputJson.put("icuCapacityRatio",metrics.get("icuCapacityRatio"));
        outputJson.put("vaccinationsInitiatedRatio",metrics.get("vaccinationsInitiatedRatio"));
        outputJson.put("vaccinationsCompletedRatio",metrics.get("vaccinationsCompletedRatio"));
        return outputJson.toString();
    }

    // Code from Lab 7 - Rest Programming
    // private helper method to get response body which reads
    // from the connection and returns String containing response
    private String getResponseBody(HttpURLConnection conn) {
        StringBuilder responseText = new StringBuilder();
        try {
            String output;
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            while ((output = br.readLine()) != null) {
                responseText.append(output);
            }
            conn.disconnect();
        } catch (IOException e) {
            System.out.println("Exception caught " + e);
        }
        return responseText.toString();
    }

    // A simple class to wrap HTTP result. Code from lab 7 - Rest Programming
    class Result {
        private int responseCode;
        private String responseText;

        public int getResponseCode() { return responseCode; }
        public void setResponseCode(int code) { responseCode = code; }
        public String getResponseText() { return responseText; }
        public void setResponseText(String msg) { responseText = msg; }

        public String toString() { return responseCode + ":" + responseText; }
    }
}
