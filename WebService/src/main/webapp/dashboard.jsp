<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Covid Tracker Dashboard</title>
</head>
<body>
<br/>
<form method="GET" action="/dashboard">
    <h1>Dashboard for Covid Tracker App</h1>
    <h2>Analytics based on log data: </h2>
    <ul>
        <li>Number of searches today:  <%%><%= request.getAttribute("visits") %></li>
        <% if (!request.getAttribute("populatedState").equals("No Logs")) { %>
        <li>Fastest API response:  <%%><%= request.getAttribute("fastestResponse") %> ms</li>
        <li>Most searched state code:  <%%><%= request.getAttribute("mostSearched") %></li>
        <li>Most populated state: <%%><%= request.getAttribute("populatedState") %>
            <ul>
                <li>Population: <%%><%= request.getAttribute("population") %></li>
            </ul>
        </li>
        <li>State Code with Maximum Covid Test Positivity Ratio:  <%%><%= request.getAttribute("positivityState") %>(<%%><%= request.getAttribute("positivity") %>)</li>
        <li>State Code with Maximum Vaccination Completed Ratio:  <%%><%= request.getAttribute("vacState") %>(<%%><%= request.getAttribute("vacComp") %>)</li>
        <% } else { %>
        <li>No logs available for analytics</li>
        <% } %>
    </ul>
    <h2>The logs are as follows:</h2>
<table style="border: 1px solid black;background-color: burlywood">
    <tr>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">Device</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">Timestamp</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">API Response Status Code</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">API Response Time</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">State Code</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">Population</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">Test Positivity Rate</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">Infection Rate</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">ICU Capacity Ratio</th>
        <th style="border-right: 1px solid black;border-bottom: 1px solid black;padding:10px">Vaccinations Initiated Ratio</th>
        <th style="border-bottom: 1px solid black;padding:10px">Vaccinations Completed Ratio</th>
    </tr>
    <%%><%= request.getAttribute("data") %>
</table>
</form>
</body>
</html>