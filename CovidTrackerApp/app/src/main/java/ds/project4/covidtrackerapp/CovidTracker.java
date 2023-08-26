package ds.project4.covidtrackerapp;
/*
 * @author Shivani Poovaiah Ajjikutira
 * Last Modified 14 November 2021
 *
 * This class creates an android application capable of getting covid metrics for a state code by fetching data from a web service
 * given a state code. The user input is checked to ensure correct state code is entered. Else error message is displayed
 * to the user. The state code is used to fetch data from a webservice. The response received is then
 * displayed to the user.
 * */
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONException;
import org.json.JSONObject;

// Source: Code taken from lab 8 Android - Interesting Picture
public class CovidTracker extends AppCompatActivity {

    // method to start the app
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.hello);
        /*
         * The click listener will need a reference to this object, so that upon successfully fetching data from web service, it
         * can callback to this object with the resulting response data.  The "this" of the OnClick will be the OnClickListener, not
         * this CovidTracker.
         */
        final CovidTracker ct = this;

        /*
         * Find the "submit" button, and add a listener to it
         */
        Button submitButton = findViewById(R.id.submit);


        // Add a listener to the send button
        // if search term is correct, fetch data from webservice and set all metric TextViews
        // by parsing the json response string and make all metric TextViews visible.
        // else display error message to user and hide the all metric TextViews
        submitButton.setOnClickListener(viewParam -> {
            String searchTerm = ((EditText)findViewById(R.id.searchTerm)).getText().toString();
            if(searchTerm.length()!= 0 && searchTerm.matches("[A-Z]{2}") && validStateCode(searchTerm)) {
                System.out.println("searchTerm = " + searchTerm);
                GetAPIData gp = new GetAPIData();
                gp.search(searchTerm, ct); // Done asynchronously in another thread.  It calls ct.apiReadyReady() in this thread when complete.
            } else {
                TextView stateView = findViewById(R.id.state);
                if(searchTerm.length() == 0) {
                    stateView.setText(getString(R.string.nullError));
                } else if(!searchTerm.matches("[A-Z]{2}")) {
                    stateView.setText(getString(R.string.formatError));
                } else if(!validStateCode(searchTerm)){
                    stateView.setText(getString(R.string.noStateError));
                } else {
                    stateView.setText(getString(R.string.error));
                }
                TextView populationView = findViewById(R.id.population);
                populationView.setText("");
                TextView metricView = findViewById(R.id.metrics);
                metricView.setText("");
                TextView infectionRatioView = findViewById(R.id.infectionRate);
                infectionRatioView.setText("");
                TextView testPositivityRateView = findViewById(R.id.testPositivityRatio);
                testPositivityRateView.setText("");
                TextView vaccinationInitiatedView = findViewById(R.id.vaccinationInitiated);
                vaccinationInitiatedView.setText("");
                TextView vaccinationCompletedView = findViewById(R.id.vaccinationCompleted);
                vaccinationCompletedView.setText("");
                TextView icuCapacityRatioView = findViewById(R.id.icuCapacityRatio);
                icuCapacityRatioView.setText("");
                stateView.setVisibility(View.VISIBLE);
                populationView.setVisibility(View.VISIBLE);
                infectionRatioView.setVisibility(View.VISIBLE);
                testPositivityRateView.setVisibility(View.VISIBLE);
                findViewById(R.id.metrics).setVisibility(View.VISIBLE);
                icuCapacityRatioView.setVisibility(View.VISIBLE);
                vaccinationInitiatedView.setVisibility(View.VISIBLE);
                vaccinationCompletedView.setVisibility(View.VISIBLE);
            }
        });
    }

    /*
     * This is called by the GetAPIData object when the picture is ready.
     * This allows for passing back the response data for updating the view with all the metrics
     */
    public void apiDataReady(String data) {
        TextView stateView = findViewById(R.id.state);
        TextView populationView = findViewById(R.id.population);
        TextView infectionRatioView = findViewById(R.id.infectionRate);
        TextView testPositivityRateView = findViewById(R.id.testPositivityRatio);
        TextView vaccinationInitiatedView = findViewById(R.id.vaccinationInitiated);
        TextView vaccinationCompletedView = findViewById(R.id.vaccinationCompleted);
        TextView icuCapacityRatioView = findViewById(R.id.icuCapacityRatio);
        if (data != null) {
            try {
                JSONObject response = new JSONObject(data);
                stateView.setText(R.string.state);
                stateView.append(response.getString("state"));
                populationView.setText(R.string.population);
                populationView.append(String.valueOf(response.getInt("population")));
                infectionRatioView.setText(R.string.infectionRatio);
                infectionRatioView.append(String.valueOf(response.getDouble("infectionRate")));
                testPositivityRateView.setText(R.string.testPositivityRate);
                testPositivityRateView.append(String.valueOf(response.getDouble("testPositivityRatio")));
                icuCapacityRatioView.setText(R.string.icuCapacityRatio);
                icuCapacityRatioView.append(String.valueOf(response.getDouble("icuCapacityRatio")));
                vaccinationInitiatedView.setText(R.string.vacInit);
                vaccinationInitiatedView.append(String.valueOf(response.getDouble("vaccinationsInitiatedRatio")));
                vaccinationCompletedView.setText(R.string.vacComp);
                vaccinationCompletedView.append(String.valueOf(response.getDouble("vaccinationsCompletedRatio")));
                System.out.println(response.getString("state"));
                stateView.setVisibility(View.VISIBLE);
                populationView.setVisibility(View.VISIBLE);
                infectionRatioView.setVisibility(View.VISIBLE);
                testPositivityRateView.setVisibility(View.VISIBLE);
                findViewById(R.id.metrics).setVisibility(View.VISIBLE);
                icuCapacityRatioView.setVisibility(View.VISIBLE);
                vaccinationInitiatedView.setVisibility(View.VISIBLE);
                vaccinationCompletedView.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                System.out.println("JSONException encountered");
            }
        } else {
            stateView.setText(getString(R.string.error));
            stateView.setVisibility(View.VISIBLE);
        }
    }

    // method to check if state code entered is a valid state code or not
    private boolean validStateCode(String state) {
        boolean valid = false;
        String [] states = {"AK", "AL","AR","AZ","CA","CO","CT","DC","DE","FL","GA","HI", "IA", "ID","IL", "IN", "KS", "KY", "LA",
                "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV","NY","OH","OK", "OR", "PA",
                "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY"};
        for (String s : states) {
            if (s.equals(state)) {
                valid = true;
                break;
            }
        }
        return valid;
    }
}
