package ds.project;
/*
 * @author Shivani Poovaiah Ajjikutira
 * Last Modified 14 November 2021
 *
 * This class is the model of the webservice. The controller sends the state code request received
 * from the Android application to the model. The model uses the state code to fetch data from the
 * COVIDActNow API using HTTP GET request and returns the json response string sent by API back to
 * the controller after formatting the JSON to include only data needed in the Android app.
 * */

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CovidTrackerModel {
    // key needed to access the API
    private static final String apiKey = "bf29200dacdd47978b570eae4c06847b";

    // private helper method to send request to API
    // Code taken from lab 7 - REST-Programming Lab
    private Result sendAPIRequest(String state) {
        HttpURLConnection conn;
        int status;
        Result result = new Result();
        String apiEndpoint = "https://api.covidactnow.org/v2/state/"+state+".json?apiKey="+apiKey;
        try {
            // set URL of endpoint
            URL url = new URL(apiEndpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // send plain text request
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            // accept json response
            conn.setRequestProperty("Accept", "application/json");

            // get response code for request
            status = conn.getResponseCode();

            // set response code
            result.setResponseCode(status);
            // set response status message
            result.setResponseText(conn.getResponseMessage());
            // if status code is 200 get response body text
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
        return result;
    }

    // return formatted json response to the controller
    public String getAPIData(String state) {
        return parseResponseJSON(sendAPIRequest(state).getResponseText());
    }

    // format json response to hold only necessary data
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

    // A simple class to wrap HTTP result. Code from Lab 7 - Rest Programming
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
