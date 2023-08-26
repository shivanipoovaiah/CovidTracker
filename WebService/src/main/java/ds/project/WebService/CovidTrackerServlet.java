package ds.project.WebService;
/*
 * @author Shivani Poovaiah Ajjikutira
 * Last Modified 14 November 2021
 *
 * This class is the controller of the webservice. The model class is CovidTrackerModel.java.
 * The model class uses two helper classes CovidTrackerLogger.java and CovidTrackerDashboard.java
 * to log the required information to the MongoDB Atlas database and to retrieve the logged data
 * from the database and display the data in the form of a dashboard along with some operational
 * analytics respectively. The controller receives requests from the Android application to fetch
 * data corresponding to a state code as given in the query parameter. It then passed the query
 * parameter to the model class which handles the business logic of fetching data from the API
 * and returns the json response string sent by the model class back to the requesting Android
 * application. The controller also receives requests from a web browser to open the dashboard.
 * Based on the url pattern the controller decides whether to fetch data from the API or to
 * redirect and display the dashboard.
 * */
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

@WebServlet(name = "CovidTrackerServlet", urlPatterns = {"/getCovidData","/dashboard"})
public class CovidTrackerServlet extends HttpServlet {
    private CovidTrackerModel covidTrackerModel;
    // to store state query parameter value
    private String state;
    // to store device query parameter value
    private String model;
    // to store the time at which the request is received
    private long requestTime;
    // initialize model object
    public void init() {
        covidTrackerModel = new CovidTrackerModel();
    }

    // handle GET requests
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // pass state value to model class and return the formatted json response
        // back to the Android application
        if(request.getServletPath().equals("/getCovidData")) {
            requestTime = System.currentTimeMillis();
            PrintWriter out = response.getWriter();
            if (request.getParameter("state") != null) {
                state = request.getParameter("state");
            }
            if (request.getParameter("device") != null) {
                model = request.getParameter("device");
            }
            String responseStr = covidTrackerModel.getAPIData(state);
            covidTrackerModel.logToDatabase(responseStr, model, requestTime);
            out.write(responseStr);
            out.flush();
        } else if(request.getServletPath().equals("/dashboard")){ // setup table and redirect to dashboard page
            StringBuilder output = new StringBuilder();
            String outputString = covidTrackerModel.setUpTable(output,request);
            request.setAttribute("data",outputString);
            request.getRequestDispatcher("dashboard.jsp").forward(request,response);
        }
    }
}