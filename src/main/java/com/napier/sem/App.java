package com.napier.sem;

import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.sql.Connection;
import java.sql.Statement;

public class App {

    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    public static void main(String[] args) {
        // Create new Application
        App a = new App();

        // Connect to database
        if (args.length < 1) {
            a.connect("localhost:33060", 1000);
        }
        else {
            a.connect("db:3306", 30000);
        }

        /**
         * Capital City Reports
         */
        // Get all capital cities
        System.out.println("Calling GetCapitals_World (Ordered By Population)...");
        // Extract city information
        a.GetCapitals_World();

        // Get capital cities by continent
        System.out.println("Calling GetCapitals_Continent (Ordered By population)...");
        // Extract city information
        a.GetCapitals_Continent("Europe");

        // Get capital cities by region
        System.out.println("Calling GetCapitals_Region (Ordered By population)...");
        // Extract city information
        a.GetCapitals_Region("Northern Europe");

        // Get n capital cities in the world
        System.out.println("Calling GetCapitals_World_N (Ordered By population)...");
        // Extract city information
        a.GetCapitals_World_N(5);

        // Get n capital cities by continent
        System.out.println("Calling GetCapitals_Continent_N (Ordered By population)...");
        // Extract city information
        a.GetCapitals_Continent_N("Europe", 5);

        // Get n capital cities by region
        System.out.println("Calling GetCapitals_Region_N (Ordered By population)...");
        // Extract city information
        a.GetCapitals_Region_N("Southern Europe", 5);

        /**
         * Country Reports
         */
        // 1) Provide all countries in a world from largest population to smallest.
        //"select * from world.country order by Population desc;";
        a.country_world();

        // 2) Provide counties from largest population to smallest in a continent
        //"select * from world.country Where Continent = 'Eastern Asia' order by Population desc;";
        a.country_continent("Eastern Asia");

        // 3) Provide countries in a region from largest population to smallest
        //"select * from world.country Where Region = 'Eastern Asia' order by Population desc;";
        a.country_region("Eastern Asia");

        // 4)The top N populated countries in the world where N is provided by the user.
        //"select * from world.country order by Population desc limit 3;";
        a.country_world_N(5);

        // 5)The top N populated countries in a continent where N is provided by the user.
        //"select  * from world.country where Continent ='Asia' order by Population desc limit 3;";
        a.country_continent_N("Asia", 5);

        // 6) The top N populated countries in a region where N is provided by the user.
        //"select * from world.country where Region ='Eastern Asia' order by Population desc limit 3;";
        a.country_region_N("Eastern Asia", 5);

        /**
         * City reports
         */
        // 1) All the cities in the world organised by largest population to smallest.
        a.getAllCities();
        // 2) All the cities in a continent organised by largest population to smallest.
        a.getCitiesFromContinent("Europe");
        // 3) All the cities in a region organised by largest population to smallest.
        a.getCitiesFromRegion("Southern Europe");
        // 4) All the cities in a country organised by largest population to smallest.
        a.getCitiesFromCountry("United Kingdom");
        // 5) All the cities in a district organised by largest population to smallest.
        a.getCitiesFromDistrict("Scotland");
        // 6) The top N populated cities in the world where N is provided by the user.
        a.getNCitiesFromWorld(5);
        // 7) The top N populated cities in a continent where N is provided by the user.
        a.getNCitiesFromContinent("Europe",5);
        // 8) The top N populated cities in a region where N is provided by the user.
        a.getNCitiesFromRegion("Southern Europe", 5);
        // 9) The top N populated cities in a country where N is provided by the user.
        a.getNCitiesFromCountry("United Kingdom", 5);
        // 10) The top N populated cities in a district where N is provided by the user.
        a.getNCitiesFromDistrict("Scotland", 5);

        // Disconnect from database
        a.disconnect();
    }

    /**
     * Connect to the MySQL database.
     *
     * @param conString db:3306 for docker and localhost:33060 for local or Integration Tests
     * @param //i
     */
    // Database Username, Password
    static final String USER = "root";
    static final String PASS = "example";

    // Connect to the MySQL database.
    public void connect(String conString, int delay) {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start
                Thread.sleep(delay);
                // Connect to database
                //Added allowPublicKeyRetrieval=true to get Integration Tests to work. Possibly due to accessing from another class?
                con = DriverManager.getConnection("jdbc:mysql://" + conString + "/world?allowPublicKeyRetrieval=true&useSSL=false", USER, PASS);
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + Integer.toString(i));
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    // Country reports
    /**
     * 1) Provide all countries in a world from largest population to smallest.
     */
    public ArrayList<Results> country_world() {
        System.out.println("Creating the report (Countries in world by population)...");
        StringBuilder sb  = new StringBuilder();
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement - REMOVED
            String sql = "select * from world.country order by Population desc";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(sql);
            // Extract country information
            ArrayList<Results> ress = new ArrayList<Results>();
            // Return  if valid.
            // Check one is returned
            while (rset.next()) {
                Results res = new Results();
                res.countryCode = rset.getString("code");
                res.countryName = rset.getString("name");
                res.continent = rset.getString("continent");
                res.region = rset.getString("region");
                res.pop = rset.getInt("population");
                res.capital = rset.getInt("Capital");
                ress.add(res);
            }
            // Print to text file
            new File("./reports/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./reports/task06.txt")));
            writer.write(sb.toString());
            writer.close();
            System.out.println("Report created look in a reports folder!");
            // Print reports out
            PrintCountryResults(ress);
            return ress;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get country details");
            return null;
        }
    }

    /**
     * 2) Provide countries in a continent from largest population to smallest
     */
    public ArrayList<Results> country_continent(String continent) {
        System.out.println("Creating the report (Countries in continent by population)...");
        StringBuilder sb  = new StringBuilder();
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement - REMOVED
            String sql = "select * from world.country Where Continent = '" + continent + "' order by Population desc;";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(sql);
            // Extract country information
            ArrayList<Results> ress = new ArrayList<Results>();
            // Return  if valid.
            // Check one is returned
            while (rset.next()) {
                Results res = new Results();
                res.countryCode = rset.getString("code");
                res.countryName = rset.getString("name");
                res.continent = rset.getString("continent");
                res.region = rset.getString("region");
                res.pop = rset.getInt("population");
                res.capital = rset.getInt("Capital");
                ress.add(res);
            }
            // Print to text file
            new File("./reports/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./reports/task06.txt")));
            writer.write(sb.toString());
            writer.close();
            System.out.println("Report created look in a reports folder!");
            // Print reports out
            PrintCountryResults(ress);
            return ress;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get country details");
            return null;
        }
    }

    /**
     * 3) Provide countries in a region from largest population to smallest
     */
    public ArrayList<Results> country_region(String region) {
        System.out.println("Creating the report (Countries in region by population)...");
        StringBuilder sb  = new StringBuilder();
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement - REMOVED
            String sql = "select * from world.country Where Region = '" + region + "' order by Population desc;";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(sql);
            // Extract country information
            ArrayList<Results> ress = new ArrayList<Results>();
            // Return  if valid.
            // Check one is returned
            while (rset.next()) {
                Results res = new Results();
                res.countryCode = rset.getString("code");
                res.countryName = rset.getString("name");
                res.continent = rset.getString("continent");
                res.region = rset.getString("region");
                res.pop = rset.getInt("population");
                res.capital = rset.getInt("Capital");
                ress.add(res);
            }
            // Print to text file
            new File("./reports/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./reports/task06.txt")));
            writer.write(sb.toString());
            writer.close();
            System.out.println("Report created look in a reports folder!");
            // Print reports out
            PrintCountryResults(ress);
            return ress;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get country details");
            return null;
        }
    }

    /**
     * 4)The top N populated countries in the world where N is provided by the user.
     */
    public ArrayList<Results> country_world_N(int n) {
        System.out.println("Creating the report (N Countries in world by population)...");
        StringBuilder sb  = new StringBuilder();
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement - REMOVED
            String sql = "select * from world.country order by Population desc limit " + n +";";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(sql);
            // Extract country information
            ArrayList<Results> ress = new ArrayList<Results>();
            // Return  if valid.
            // Check one is returned
            while (rset.next()) {
                Results res = new Results();
                res.countryCode = rset.getString("code");
                res.countryName = rset.getString("name");
                res.continent = rset.getString("continent");
                res.region = rset.getString("region");
                res.pop = rset.getInt("population");
                res.capital = rset.getInt("Capital");
                ress.add(res);
            }
            // Print to text file
            new File("./reports/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./reports/task06.txt")));
            writer.write(sb.toString());
            writer.close();
            System.out.println("Report created look in a reports folder!");
            // Print reports out
            PrintCountryResults(ress);
            return ress;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get country details");
            return null;
        }
    }

    /**
     * 5)The top N populated countries in a continent where N is provided by the user.
     */
    public ArrayList<Results> country_continent_N(String continent, int n) {
        System.out.println("Creating the report (N Countries in continent by population)...");
        StringBuilder sb  = new StringBuilder();
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement - REMOVED
            String sql = "select  * from world.country where Continent = '" + continent + "' order by Population desc limit " + n +";";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(sql);
            // Extract country information
            ArrayList<Results> ress = new ArrayList<Results>();
            // Return  if valid.
            // Check one is returned
            while (rset.next()) {
                Results res = new Results();
                res.countryCode = rset.getString("code");
                res.countryName = rset.getString("name");
                res.continent = rset.getString("continent");
                res.region = rset.getString("region");
                res.pop = rset.getInt("population");
                res.capital = rset.getInt("Capital");
                ress.add(res);
            }
            // Print to text file
            new File("./reports/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./reports/task06.txt")));
            writer.write(sb.toString());
            writer.close();
            System.out.println("Report created look in a reports folder!");
            // Print reports out
            PrintCountryResults(ress);
            return ress;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get country details");
            return null;
        }
    }

    /**
     * 6) The top N populated countries in a region where N is provided by the user.
     */
    public ArrayList<Results> country_region_N(String region, int n) {
        System.out.println("Creating the report (N Countries in region by population)...");
        StringBuilder sb  = new StringBuilder();
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement - REMOVED
            String sql = "select * from world.country where Region = '" + region + "' order by Population desc limit " + n +";";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(sql);
            // Extract country information
            ArrayList<Results> ress = new ArrayList<Results>();
            // Return  if valid.
            // Check one is returned
            while (rset.next()) {
                Results res = new Results();
                res.countryCode = rset.getString("code");
                res.countryName = rset.getString("name");
                res.continent = rset.getString("continent");
                res.region = rset.getString("region");
                res.pop = rset.getInt("population");
                res.capital = rset.getInt("Capital");
                ress.add(res);
            }
            // Print to text file
            new File("./reports/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./reports/task06.txt")));
            writer.write(sb.toString());
            writer.close();
            System.out.println("Report created look in a reports folder!");
            // Print reports out
            PrintCountryResults(ress);
            return ress;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get country details");
            return null;
        }
    }


    // Capital reports
    /**
     * 1) Get list of all capital cities by population
     */
    public ArrayList<Results> GetCapitals_World(){
        System.out.println("Getting cities (All)...");
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District FROM city INNER JOIN country ON city.ID = country.Capital ORDER BY city.Population DESC";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract employee information
            ArrayList<Results> ress = new ArrayList<Results>();
            while (rset.next())
            {
                Results res = new Results();
                res.cityName = rset.getString("city.Name");
                res.population = rset.getInt("city.Population");
                res.countryCode = rset.getString("city.CountryCode");
                ress.add(res);
            }
            // Print results
            PrintCapitalResults(ress);
            return ress;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get city details");
            return null;
        }
    }

    /**
     * 2) Get list of capital cities by continent
     */
    public ArrayList<Results> GetCapitals_Continent(String continent){
        System.out.println("Getting cities (Continent)...");
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District FROM city INNER JOIN country ON city.ID = country.Capital WHERE country.Continent LIKE '" + continent + "' ORDER BY city.Population DESC";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract city information
            ArrayList<Results> ress = new ArrayList<Results>();
            while (rset.next())
            {
                Results res = new Results();
                res.cityName = rset.getString("city.Name");
                res.population = rset.getInt("city.Population");
                res.countryCode = rset.getString("city.CountryCode");
                ress.add(res);
            }
            // Print results
            PrintCapitalResults(ress);
            return ress;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get city details");
            return null;
        }
    }

    /**
     * 3) Get list of capital cities by region
     */
    public ArrayList<Results> GetCapitals_Region(String region){
        System.out.println("Getting cities (Region)...");
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District FROM city INNER JOIN country ON city.ID = country.Capital WHERE country.Region LIKE '" + region + "' ORDER BY city.Population DESC";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract city information
            ArrayList<Results> ress = new ArrayList<Results>();
            while (rset.next())
            {
                Results res = new Results();
                res.cityName = rset.getString("city.Name");
                res.population = rset.getInt("city.Population");
                res.countryCode = rset.getString("city.CountryCode");
                ress.add(res);
            }
            // Print results
            PrintCapitalResults(ress);
            return ress;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get city details");
            return null;
        }
    }

    /**
     * 4) Get list of N capital cities in the world
     */
    public ArrayList<Results> GetCapitals_World_N(int n){
        System.out.println("Getting " + n + " cities (All)...");
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District FROM city INNER JOIN country ON city.ID = country.Capital ORDER BY city.Population DESC LIMIT " + n;
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract city information
            ArrayList<Results> ress = new ArrayList<Results>();
            while (rset.next())
            {
                Results res = new Results();
                res.id = rset.getInt("city.ID");
                res.cityName = rset.getString("city.Name");
                res.population = rset.getInt("city.Population");
                res.countryCode = rset.getString("city.CountryCode");
                res.district = rset.getString("city.District");
                ress.add(res);
            }
            // Print results
            PrintCapitalResults(ress);
            return ress;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get city details");
            return null;
        }
    }

    /**
     * 5) Get list of N capital cities by continent
     */
    public ArrayList<Results> GetCapitals_Continent_N(String continent, int n){
        System.out.println("Getting " + n + " cities (Continent)...");
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District FROM city INNER JOIN country ON city.ID = country.Capital WHERE country.Continent LIKE '" + continent + "' ORDER BY city.Population DESC LIMIT " + n;
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract city information
            ArrayList<Results> ress = new ArrayList<Results>();
            while (rset.next())
            {
                Results res = new Results();
                res.id = rset.getInt("city.ID");
                res.cityName = rset.getString("city.Name");
                res.population = rset.getInt("city.Population");
                res.countryCode = rset.getString("city.CountryCode");
                res.district = rset.getString("city.District");
                ress.add(res);
            }
            // Print results
            PrintCapitalResults(ress);
            return ress;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get city details");
            return null;
        }
    }

    /**
     * 6) Get list of N capital cities by continent
     */
    public ArrayList<Results> GetCapitals_Region_N(String region, int n){
        System.out.println("Getting " + n + " cities (Region)...");
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District FROM city INNER JOIN country ON city.ID = country.Capital WHERE country.Region LIKE '" + region + "' ORDER BY city.Population DESC LIMIT " + n;
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract city information
            ArrayList<Results> ress = new ArrayList<Results>();
            while (rset.next())
            {
                Results res = new Results();
                res.cityName = rset.getString("city.Name");
                res.population = rset.getInt("city.Population");
                res.countryCode = rset.getString("city.CountryCode");
                ress.add(res);
            }
            // Print results
            PrintCapitalResults(ress);
            return ress;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get city details");
            return null;
        }
    }


    // City reports
    /**
     *  1) All the cities in the world organised by largest population to smallest from query string allCities
     * @return getCities(allCities)
     */
    public ArrayList<Results> getAllCities() {
        String allCities = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District FROM city ORDER BY city.Population DESC";
        System.out.println("Cities in the world:");
        return getCities(allCities);
    }

    /**
     *  2) All the cities in a continent organised by largest population to smallest.
     * @param continent is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getCitiesFromContinent(String continent) {
        String citiesFromContinent = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District" +
                "    FROM city" +
                "    INNER JOIN country ON city.CountryCode = country.Code " +
                "    WHERE country.Continent LIKE '" +
                continent + "' ORDER BY city.Population DESC";
        System.out.println("Cities in " + continent + ":");
        return getCities(citiesFromContinent);
    }

    /**
     * 3) All the cities in a region organised by largest population to smallest.
     * @param region is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getCitiesFromRegion(String region) {
        String citiesFromRegion = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District " +
                "FROM city" +
                " INNER JOIN country ON city.ID = country.Capital " +
                "WHERE country.Region LIKE '" +
                region + "' ORDER BY city.Population DESC";
        System.out.println("Cities in " + region + ":");
        return getCities(citiesFromRegion);
    }

    /**
     * 4) All the cities in a country organised by largest population to smallest.
     * @param country is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getCitiesFromCountry(String country) {
        String citiesFromCountry = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District " +
                "FROM city" +
                "    INNER JOIN country ON city.CountryCode = country.Code " +
                "WHERE country.Name LIKE '" +
                country + "' ORDER BY city.Population DESC";
        System.out.println("Cities in " + country + ":");
        return getCities(citiesFromCountry);

    }

    /**
     *  5) All the cities in a district organised by largest population to smallest.
     * @param district is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getCitiesFromDistrict(String district) {
        String citiesFromDistrict = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District " +
                "FROM city" +
                "    INNER JOIN country ON city.CountryCode = country.Code " +
                "WHERE district LIKE '" +
                district + "' ORDER BY city.Population DESC";
        System.out.println("Cities in " + district + ":");
        return getCities(citiesFromDistrict);
    }

    /**
     * 6) The top N populated cities in the world where N is provided by the user.
     * @param N is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getNCitiesFromWorld(int N) {
        String cities = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District" +
                " FROM city" +
                " ORDER BY city.Population DESC LIMIT " + N;
        System.out.println("Top " + N + " cities in the world (by population):");
        return getCities(cities);
    }

    /**
     *  7) The top N populated cities in a continent where N is provided by the user.
     * @param N  is used in SQL query of world db.
     * @param continent is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getNCitiesFromContinent(String continent, int N) {
        String citiesFromContinent = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District" +
                "    FROM city" +
                "    INNER JOIN country ON city.CountryCode = country.Code " +
                "    WHERE country.Continent LIKE '" +
                continent + "' ORDER BY city.Population DESC LIMIT " + N;
        System.out.println("Top " + N + " cities in " + continent + " (by population):");
        return getCities(citiesFromContinent);
    }

    /**
     *  8) The top N populated cities in a region where N is provided by the user.
     * @param N  is used in SQL query of world db.
     * @param region is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getNCitiesFromRegion(String region, int N) {
        String citiesFromRegion = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District " +
                "FROM city" +
                " INNER JOIN country ON city.ID = country.Capital " +
                "WHERE country.Region LIKE '" +
                region + "' ORDER BY city.Population  DESC LIMIT " + N;
        System.out.println("Top " + N + " cities in " + region + " (by population):");
        return getCities(citiesFromRegion);
    }

    /**
     * 9) The top N populated cities in a country where N is provided by the user.
     * @param N is used in SQL query of world db.
     * @param country is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getNCitiesFromCountry(String country, int N) {
        String citiesFromCountry = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District " +
                "FROM city" +
                "    INNER JOIN country ON city.CountryCode = country.Code " +
                "WHERE country.Name LIKE '" +
                country + "' ORDER BY city.Population DESC LIMIT " + N;
        System.out.println("Top " + N + " cities in " + country + " (by population):");
        return getCities(citiesFromCountry);
    }

    /**
     * 10) The top N populated cities in a district where N is provided by the user.
     * @param N is used in SQL query of world db.
     * @param district is used in SQL query of world db.
     * @return getCities(query)
     */
    public ArrayList<Results> getNCitiesFromDistrict(String district, int N) {
        String citiesFromDistrict = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District " +
                "FROM city" +
                "    INNER JOIN country ON city.CountryCode = country.Code " +
                "WHERE district LIKE '" +
                district + "' ORDER BY city.Population DESC LIMIT " + N;
        System.out.println("Top " + N + " cities in " + district + " (by population):");
        return getCities(citiesFromDistrict);

    }

    /**
     *  Return and print list of cities from a query in world db.
     *  It calls getCityList(rset) formating query results
     * @param query
     * @return ArrayList<Results> of cities data or null
     * @throws Exception Failed to get city details"
     */
    public ArrayList<Results> getCities(String query) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect = query;

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract employee information
            ArrayList<Results> ress = getCityList(rset);
            if (ress != null) {
                PrintCityResults(ress);
                System.out.println("Number of results: " + ress.size());
                System.out.println(""); // Leave one line empty for clear view
            }
            return ress;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get city details");
            return null;
        }
    }

    /**
     *  Get ArrayList of city names, population, country code and location district
     * @param resultSet from World DB SQL query
     * @return ArrayList<Results> cityLists
     * @throws SQLException
     */
    public ArrayList<Results> getCityList(ResultSet resultSet) throws SQLException {
        ArrayList<Results> cityList = new ArrayList<Results>();
        while (resultSet.next()) {
            Results result = new Results();
            result.cityName = resultSet.getString("city.Name");
            result.population = resultSet.getInt("city.Population");
            result.countryCode = resultSet.getString("city.CountryCode");
            result.district = resultSet.getString("city.District");
            cityList.add(result);
        }
        return cityList;
    }

    /**
     *  Created for integration test.It make query in world db with @param name,
     *  the same quary as in ArrayList<Results> getCityArray(String name), but I couldn't make it to work with the test
     * @param name of the city
     * @return result
     * @throws Exception Failed to get city details
     */
    public Results getCity(String name) {
        String query = "SELECT city.ID , city.Name, city.Population, city.CountryCode, city.District " +
                "FROM city " +
                "WHERE city.Name = '" + name + "'";
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Execute SQL statement
            ResultSet resultSet = stmt.executeQuery(query);
            // Extract city information
            if (resultSet.next())  {
                Results result = new Results();
                result.population = resultSet.getInt("city.Population");
                result.countryCode = resultSet.getString("city.CountryCode");
                result.district = resultSet.getString("city.District");
                return result;
            } else return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get city details");
            return null;
        }
    }


    // Printing methods

    /**
     * Print country data: country code, name, continent, region, population, capital
     *  or "No results"
     * @param results - world db query
     */
    public void PrintCountryResults(ArrayList<Results> results){
        // Check results is not null
        if (results == null)
        {
            System.out.println("No results");
            return;
        }
        // Print header
        System.out.println(String.format("%-10s %-40s %-10s %-8s %-40s %-8s",
                "Country Code", "Country Name", "Continent", "Region", "Population", "Capital"));
        // Loop over all results in the list
        for (Results res : results)
        {
            if (results == null)
                continue;
            String emp_string =
                    String.format("%-10s %-40s %-10s %-8s %-40s %-8s",
                            res.countryCode, res.countryName, res.continent, res.region, res.pop, res.capital);
            System.out.println(emp_string);
        }
    }

    /**
     * Print capital city data: name, population, country code)
     *  or "No results"
     * @param results - world db query
     */
    public void PrintCapitalResults(ArrayList<Results> results) {
        // Check results is not null
        if (results == null)
        {
            System.out.println("No results");
            return;
        }
        // Print header
        System.out.println(String.format("%-40s %-10s %-8s", "City Name", "City Population", "City Country Code"));
        // Loop over all results in the list
        for (Results res : results)
        {
            if (results == null)
                continue;
            String emp_string = String.format("%-40s %-15s %-18s",res.cityName, res.population, res.countryCode);
            System.out.println(emp_string);
        }
    }

    /**
     * Print city data: name, country code, district, population (000s)
     *  or "No results"
     * @param results - world db query
     */
    public void PrintCityResults(ArrayList<Results> results) {
        // Check results is not null
        if (results == null) {
            System.out.println("No results");
            return;
        }
        // Print header
        System.out.println(String.format("%-30s %-15s %-30s %-5s",
                "Name", "Country Code", "District", "Population (000s)"));
        // Loop over all results in the list
        for (Results res : results) {
            if (results == null)
                continue;
            String emp_string =
                    String.format("%-30s %-15s %-30s %-15s",
                            res.cityName, res.countryCode, res.district, res.population/1000);
            System.out.println(emp_string);
        }
    }

    public void PrintLanguageResults(ArrayList<Results> results) {
        // Check results is not null
        if (results == null)
        {
            System.out.println("No results");
            return;
        }
        // Print header
        System.out.println(String.format("%-10s %-40s %-10s %-8s",
                "Country Code", "Language", "Is Official", "Percentage"));
        // Loop over all results in the list
        for (Results res : results)
        {
            if (results == null)
                continue;
            String emp_string =
                    String.format("%-10s %-40s %-10s %-8s",
                            res.country_code, res.language, res.isOfficial, res.percentage);
            System.out.println(emp_string);
        }
    }
}