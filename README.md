# CovidTracker
The application takes a US state code from the user and displays the COVID-related metrics for that state fetched from the web service deployed to Heroku which in turn gets this data from Covid Act Now API (https://apidocs.covidactnow.org/). Each request made by the user is logged into the MongoDB Atlas. The logs and some operational analytics based on the records are available in the form of a dashboard at:

https://quiet-earth-22508.herokuapp.com/dashboard

1. LOGGING:

  The following information is logged:
  • Device – Android device model used to make the request
  • Timestamp – Time at which the request was received by the web service
  • API Response Status Code
  • API Response Time
  • State Code
  • Population
  • Test Positivity Rate
  • Infection Rate
  • ICU Capacity Ratio
  • Vaccinations Initiated Ratio
  • Vaccinations Completed Ratio

2. DATABASE:
     
  CovidTrackerModel.java class connects to the database. CovidTrackerModel.java sends the necessary
  information to the CovidTrackerLogger.java class where the data is logged into the database. The
  connection string is as follows:
  
    "mongodb+srv://" + username + ":" + pwd + 
        "@covidtrackercluster.8htga.mongodb.net/myFirstDatabase?retryWrites=true&w=majority"
      
  where:
  
  • username is my username as set on MongoDB Atlas
  • pwd is my password as set on MongoDB Atlas
  
  Cluster name: CovidTrackerCluster
  Database name: CovidTrackerDB Database Collection: CovidTrackerLogs
  
  An example of logged information in database is as follows:
  
 3. OPERATIONAL ANALYTICS AND DASHBOARD:
    CovidTrackerModel.java retrieves the documents from the database and sends to
    CovidTrackerDashboard.java to form the table.

    3.1. A unique URL addresses a web interface dashboard for the web service.
    The dashboard URL is:
    https://quiet-earth-22508.herokuapp.com/dashboard

    3.2. The dashboard displays the following operational analytics provided based on logged data:
        • Number of searches today
        • Fastest API response
        • The most searched state code
        • Most populated state and its population
        • State Code with Maximum Covid Test Positivity Ratio and its percentage
        • State Code with Maximum Vaccination Completed Ratio and its percentage

    3.3. The dashboard displays formatted full logs. The logs include the following:
        • Device – Android device model used to make the request
        • Timestamp – Time at which the request was received by the web service
        • API Response Status Code
        • API Response Time
        • State Code
        • Population
        • Test Positivity Rate
        • Infection Rate
        • ICU Capacity Ratio
        • Vaccinations Initiated Ratio
        • Vaccinations Completed Ratio
    
        The dashboard looks as follows:
    
 4. The URL of my web service deployed to Heroku is:
    https://quiet-earth-22508.herokuapp.com/
    The project directory name is WebService.

    4.1. Implementation of web service:
    In my project, the web service consists of:
    
        Model: CovidTrackerModel.java
    
        View: index.jsp, dashboard.jsp
    
        Controller: CovidTrackerServlet.java
    
        Helper classes: CovidTrackerLogger.java,
                        CovidTrackerDashboard.java
    
    4.2. Receives an HTTP request from the native Android application and web browser

    CovidTrackerServlet.java receives the HTTP GET request with the argument “state” and “device”. It
    passes these strings onto the CovidTrackerModel.java.

    CovidTrackerServlet.java receives the HTTP GET request with URL pattern: /dashboard.

    This redirects to the dashboard page by creating a table and setting the view from dashboard.jsp.

    4.3. Executes business logic appropriate to the application:

    This includes fetching XML or JSON information from some 3rd party API and processing the response.

    CovidTrackerModel.java makes an HTTP GET Request to API:

    API endpoint: "https://api.covidactnow.org/v2/state/"+state+".json?apiKey="+apiKey

    where state is the state code for which the Covid metric is to be fetched
    apiKey is my API key for accessing the API data.

    It then parses the JSON response and extracts parts it needs to respond to the Android application.
    CovidTrackerServlet.java calls a method in CovidTrackerModel.java to retrieve data from the database
    and create a table to display in the dashboard. The operation analytics are performed in
    CovidTrackerDashboard.java and parameters for the operation analytics are set in
    CovidTrackerModel.java

    4.4. Replies to the Android application with an XML or JSON formatted response. The schema of the
    response can be of your own design. Creates and displays dashboard The CovidTrackerServlet.java
    receives the formatted JSON response from CovidTrackerModel.java and returns the formatted JSON
    response to the Android application.

    Example of JSON response:
    {
      "testPositivityRatio":0.025,
      "infectionRate":0.98,
      "state":"CA",
      "vaccinationsCompletedRatio":0.622,
      "icuCapacityRatio":0.77,
      "vaccinationsInitiatedRatio":0.764,
      "population":39 512223
    }

    The CovidTrackerServlet.java displays the dashboard when the dashboard URL is hit.

 5. Implementation of native Android application:

    The name of my native Android application project in Android Studio is CovidTrackerApp.

    5.1. My application uses TextView, EditText, Button, and ScrollView.
    See context_main.xml for details of how they are incorporated with a LinearLayout.
    Here is a picture of the layout before fetching the metrics for a state code.

    5.2. Requires input from the user
    Here is a screenshot of the user searching for the state code of California, i.e., CA

    5.3. My application does an HTTP GET request in GetAPIData.java.
    The HTTP Request is:
    https://quiet-earth-22508.herokuapp.com/getCovidData?state="+searchTerm+"&device="+model where:

    • searchTerm is the user’s search term
    • model is the model name of the Android device making the request

    The search method makes this request to the web service deployed on Heroku, which in turn uses the
    query parameter attached to the URL to fetch the data from the API.

    5.4. An example of the JSON response is:
      {
        "testPositivityRatio":0.025,
        "infectionRate":0.98,
        "state":"CA",
        "vaccinationsCompletedRatio":0.622,
        "icuCapacityRatio":0.77,
        "vaccinationsInitiatedRatio":0.764,
        "population":39 512223
      }

    5.5. Displays new information to the user

    Here is the screenshot after the search result is obtained.

    5.6. Is repeatable (i.e., the user can repeatedly reuse the application without restarting it.)
    The user can search for the metrics of another state code and hit the Submit button.
    Here is an example for the state code of Pennsylvania, i.e., PA
  
