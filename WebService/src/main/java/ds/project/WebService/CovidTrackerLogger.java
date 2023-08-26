package ds.project.WebService;
/*
 * @author Shivani Poovaiah Ajjikutira
 * Last Modified 14 November 2021
 *
 * Helper class to log data to the database and store the number of
 * searches for each state code
 * */
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class CovidTrackerLogger {
        // to store number of times a state code was logged
        HashMap<String,Integer> stateCount = new HashMap<>();
        // method to log data to database by calling private helper method sendLogData
        public void logToMongoDb(MongoCollection<Document> collection, String apiResponse, String model,int status, long responseTime, long requestTime) {
            sendLogData(collection, apiResponse, model,status,responseTime,requestTime);
        }

        // method to find most searched state code and return the state code
        public String mostSearchedState() {
            int count = 0;
            String mostSearched = "";
            if(stateCount!=null && stateCount.size()>0) {
            for (Map.Entry<String, Integer> entry : stateCount.entrySet()) {
                if(count<entry.getValue()) {
                    count = entry.getValue();
                    mostSearched = entry.getKey();
                }
            }}
            return mostSearched;
        }

        // private helper method to create a document with all necessary data to log
        // and insert the document to the database
        private void sendLogData(MongoCollection<Document> collection, String apiResponse, String model, int status,long responseTime,long requestTime){
            try {
                Document document = new Document();
                document.append("Device", model);
                document.append("Timestamp", new Timestamp(requestTime).toString());
                document.append("API Response Status Code", status);
                document.append("API Response Time", responseTime);
                if(apiResponse!=null) {
                    JSONObject json = new JSONObject(apiResponse);
                    document.append("State Code",json.get("state"));
                    String state = json.get("state").toString();
                    if(stateCount.get(state)!=null) {
                        int count = stateCount.get(state);
                        stateCount.put(state,count+1);
                    } else {
                        stateCount.put(state,1);
                    }
                    document.append("Population",json.get("population"));
                    document.append("Test Positivity Ratio",json.get("testPositivityRatio"));
                    document.append("Infection Rate",json.get("infectionRate"));
                    document.append("Icu Capacity Ratio",json.get("icuCapacityRatio"));
                    document.append("Vaccinations Initiated Ratio",json.get("vaccinationsInitiatedRatio"));
                    document.append("Vaccinations Completed Ratio",json.get("vaccinationsCompletedRatio"));
                }
                collection.insertOne(document);
                System.out.println("Successfully added to the database!");
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }
        }
}