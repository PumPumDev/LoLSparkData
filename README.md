# SparkLoLData
## Final Informatic Degree Project
This is a Spark project which uses League of Legends (LoL) data to explore Spark framework and 
to get some conclusions about League of Legends game.

We will take LoL data from their public [Riot Api](https://developer.riotgames.com/apis).

### Modules
The project has 3 main modules and 2 secondary modules.

The main modules are:

    1. Client API
        Which makes the calls to the API and process the data to storage it locally. 
        Use it to download the data for first time or update it when you want.
    2. Spark Queries
        Which uses the data storaged locally to make some queries using Spark framework 
        (Dataset and dataframe).
    3. Visualization Data
        Which creates differents tables and graphs to visualize the results of the queries.

The secondary modules are:

    4. Models
        Which modelizes the structure of the data.
    5. Utils
        Which contains multiple useful functions used by the whole project. This module also contains the 
        .conf file (configuration file).
        
Each module has its own README.md. Consult it if you need to use it.

### Usage
1. Download the data using the **Client API** module.
2. Use the module **Spark Queries** to run the queries. The results of queries will be storage locally in `.tab` and 
`.graph` files.
3. The module **Visualization Data** have an HTML file with a generated `javascript` file. Open the HTML file in your favourite
browser and select the `.tab` or `.graph` file you want to see.
    * To generate the `javascript` file you need to run the sbt task: `fastOptJS`. For more information, check 
    [Scala js](https://www.scala-js.org/doc/project/building.html).
    
#### Important
Before starting, you need to provide a configuration file to the project. Check de `README` in the module **Utils**.
