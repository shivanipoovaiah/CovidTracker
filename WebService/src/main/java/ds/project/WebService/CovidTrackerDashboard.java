package ds.project.WebService;
/*
 * @author Shivani Poovaiah Ajjikutira
 * Last Modified 14 November 2021
 *
 * Helper class to set up the dashboard table containing logged items along with performing operation
 * analytics logic on logged data.
 * */
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CovidTrackerDashboard {
    // operation analytics parameter variables
    int visits;
    long fastestResponse;
    double positivity;
    String positiveState;
    int population;
    String populatedState;
    String vacState;
    double vacComp;

    // initializes dashboard values to default value
    CovidTrackerDashboard() {
        setToValuesDefault();
    }

    // set default values
    void setToValuesDefault() {
        positiveState = "No Logs";
        vacState="No Logs";
        populatedState="No Logs";
    }
    // helper method to get logs from the database and set up table along with performing operation analytics
    private String getCollection( MongoCollection<Document> collection, StringBuilder output) {
        setToValuesDefault();
        // to compare with logged dates to check number of searches today
        Date today= Calendar.getInstance().getTime();
        // documents in database
        List<Document> myDocs = collection.find().into(new ArrayList<>());
        int i=0;
        // create table with all the log data
        for (Document doc : myDocs) {
            output.append("<tr>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("Device")).append("</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("Timestamp")).append("</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("API Response Status Code")).append("</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("API Response Time")).append(" ms</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("State Code")).append("</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("Population")).append("</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("Test Positivity Ratio")).append("</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("Infection Rate")).append("</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("Icu Capacity Ratio")).append("</td>");
            output.append("<td style=\"border-right: 1px solid black;padding:10px\">").append(doc.get("Vaccinations Initiated Ratio")).append("</td>");
            output.append("<td style=\"padding:10px\">").append(doc.get("Vaccinations Completed Ratio")).append("</td>");
            output.append("</tr>");

            // Compare logged date and today's date to see how many searches were made today
            // Code from:
            // https://stackoverflow.com/questions/2517709/comparing-two-java-util-dates-to-see-if-they-are-in-the-same-day
            // https://stackoverflow.com/questions/18915075/java-convert-string-to-timestamp
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                Date parsedDate = dateFormat.parse((String) doc.get("Timestamp"));
                Timestamp visitedTime = new java.sql.Timestamp(parsedDate.getTime());
                Date dateVisited = new Date(visitedTime.getTime());
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(today);
                cal2.setTime(dateVisited);
                boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
                if(sameDay) visits++;
            } catch(Exception e) { //this generic but you can control another types of exception
                // look the origin of exception
                System.out.println("Exception: "+ e);
            }
            // initially set operation analytics parameters to the first log's data
            if(i==0) {
                fastestResponse=Long.parseLong(String.valueOf(doc.get("API Response Time")));
                population=Integer.parseInt(String.valueOf(doc.get("Population")));
                positivity=Double.parseDouble(String.valueOf(doc.get("Test Positivity Ratio")));
                positiveState= (String) doc.get("State Code");
                vacState=positiveState;
                populatedState=positiveState;
                vacComp=Double.parseDouble(String.valueOf(doc.get("Vaccinations Completed Ratio")));
            } // compare each logged data to set the operation analytics parameters
            else {
                int nextLogPopulation=Integer.parseInt(String.valueOf(doc.get("Population")));
                String newState= String.valueOf(doc.get("State Code"));
                long nextFastestResponse=Long.parseLong(String.valueOf(doc.get("API Response Time")));
                double nextLogPositivity = Double.parseDouble(String.valueOf(doc.get("Test Positivity Ratio")));
                double nextLogVac = Double.parseDouble(String.valueOf(doc.get("Vaccinations Completed Ratio")));
                // store the minimum API response time
                fastestResponse= Math.min(fastestResponse, nextFastestResponse);
                // store the state code with maximum test positivity ratio
                positiveState=positivity<nextLogPositivity?newState:positiveState;
                // maximum test positivity ratio
                positivity= Math.max(positivity, nextLogPositivity);
                // store the state code with maximum vaccination completed ratio
                vacState=vacComp<nextLogVac?newState:vacState;
                // maximum vaccination completed ratio
                vacComp= Math.max(vacComp, nextLogVac);
                // store the state code with maximum population
                populatedState=population<nextLogPopulation?newState:populatedState;
                // maximum population
                population= Math.max(population, nextLogPopulation);
            }
            i++;
        }
        return output.toString();
    }

    // method to get data from database and setup table by calling private helper method getCollection
    public String getDatabaseData( MongoCollection<Document> collection, StringBuilder output) {
        return getCollection(collection, output);
    }
}
