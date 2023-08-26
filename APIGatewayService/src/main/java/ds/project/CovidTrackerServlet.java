package ds.project;
/*
 * @author Shivani Poovaiah Ajjikutira
 * Last Modified 14 November 2021
 *
 * This class is the controller of the webservice. The model class is CovidTrackerModel.java.
 * The controller receives requests from the Android application to fetch data corresponding to a
 * state code as given in the query parameter. It then passed the query parameter to the model class
 * which handles the business logic of fetching data from the API and returns the json response
 * string sent by the model class back to the requesting Android application.
 * */
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "CovidTrackerServlet", urlPatterns = {"/getCovidData"})
public class CovidTrackerServlet extends HttpServlet {
    private CovidTrackerModel covidTrackerModel;
    // tp store state query parameter value
    private String state;

    // on startup creates an object of model class
    public void init() {
        covidTrackerModel = new CovidTrackerModel();
    }

    // handles HTTP get requests
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // to write and send the response
        PrintWriter out = response.getWriter();
        if(request.getParameter("state") != null) {
            state = request.getParameter("state");
        }
        // gets api response from model class and returns to calling method
        String responseStr = covidTrackerModel.getAPIData(state);
        out.write(responseStr);
        out.flush();
    }
}