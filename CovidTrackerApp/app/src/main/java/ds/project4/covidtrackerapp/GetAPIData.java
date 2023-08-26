package ds.project4.covidtrackerapp;
/*
 * @author Shivani Poovaiah Ajjikutira
 * Last Modified 14 November 2021
 *
 * This class provides capabilities to get covid metrics for a state code by fetching data from a web service given a state code.
 * The method "search" is the entry to the class. Network operations cannot be done from the UI thread,
 * therefore this class makes use of an AsyncTask inner class that will do the network
 * operations in a separate worker thread.  However, any UI updates should be done in the UI thread
 * so avoid any synchronization problems.onPostExecution runs in the UI thread, and it calls the
 * apiDataReady method to do the update.
 * */
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

// Source: Code taken from lab 8 Android - Interesting Picture
public class GetAPIData {
    CovidTracker ct = null;

    /*
     * search is the public GetAPIData method.  Its arguments are the search term, and the CovidTracker object that called it.  This provides a callback
     * path such that the apiDataReady method in that object is called when the apiData is available from the search.
     */
    public void search(String searchTerm, CovidTracker ct) {
        this.ct = ct;
        new AsyncAPIFetch().execute(searchTerm);
    }

    /*
     * AsyncTask provides a simple way to use a thread separate from the UI thread in which to do network operations.
     * doInBackground is run in the helper thread.
     * onPostExecute is run in the UI thread, allowing for safe UI updates.
     */
    private class AsyncAPIFetch extends AsyncTask<String, Void, String> {
            protected String doInBackground(String... urls) {
            return search(urls[0]);
        }

        protected void onPostExecute(String apiDataResponse) {
            ct.apiDataReady(apiDataResponse);
        }

        /*
         * Send state query parameter to webservice endpoint and get response Data
         */
        private String search(String searchTerm) {
            String webServiceURL;
            if (searchTerm.length() == 0) {
                return null; // no data entered
            } else {
                String model = Build.MODEL;
                System.out.println(model);
                webServiceURL = "https://quiet-earth-22508.herokuapp.com/getCovidData?state="+searchTerm+"&device="+model;
            }
            try {
                URL u = new URL(webServiceURL);
                return getRemoteResponse(u);
            } catch (Exception e) {
                e.printStackTrace();
                return null; // so compiler does not complain
            }

        }
        /*
         * Given a webservice URL, return a json data of that state code
         */
        @RequiresApi(api = Build.VERSION_CODES.P)
        private String getRemoteResponse(final URL url) {
            try {
                final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int status = conn.getResponseCode();
                if(status==200) {
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    byte[] contents = new byte[1024];

                    int bytesRead;
                    StringBuilder apiResponse=new StringBuilder();
                    while((bytesRead = bis.read(contents)) != -1) {
                        apiResponse.append(new String(contents, 0, bytesRead));
                    }
                    return apiResponse.toString();
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}