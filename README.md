# SparkLoLData
## Final Informatic Degree Project
This is a Spark project which uses League of Legends (LoL) data to explore Spark framework and maybe to get some conclusions about League of Legends game.

We will take LoL data from their public [Riot Api](https://developer.riotgames.com/apis).

### Modules
The project has 3 main modules.
    
    1. Client API
        Which make the calls to the API and process the data to storage it locally. 
        Use it to download the data for first time or update it when you want.
    2. Scala Queries
        Which use the data storaged locally to make some queries using different classes
        from Scala Collections.
    3. Spark Queries
        Which use the data storaged locally to make some queries using Spark framework 
        (Dataset and dataframe).
        
Each module has its own README.md. Consult it if you need to use it.

### Usage
1. Download the data using the **Client API** module.
    * A configuration file must be supplied, check ``api-client/README.md`` for more information.
2. Use the modules **Scala Queries** and **Spark Queries** to run the queries.