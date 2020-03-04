# SparkLoLData
## Final Informatic Degree Project
This is a Spark project which uses League of Legends (LoL) data to explore Spark framework and maybe to get some conclusions about League of Legends game.

We will take LoL data from their public [Riot Api](https://developer.riotgames.com/apis).

### Credentials configuration
You have to enable the `configuration` file, it is in `src/main/resources/credentials.properties.template`.
Open the file on a text editor.

You have to open the template and set your _Riot API Key_ to `apiKey` property.

You can add more properties and use them in the App calling the object `config.getString("YourProperty"")`, but be 
careful changing the default properties can crash the hole App!!

When you finished, save the file and change the extension to `.properties`.

### Data
If you don't want to call the Riot API to take the data you can use the default JSON data. If you want to use it
you have to unzip the `src/outputData/outputData.zip` into the `src/outputData` folder. After that, the App will load
the data from those JSON files.

The default JSON data is updated to 01/03/2020.

If you want to use the Riot API to collect your data just don't unzip that folder. It's important to remark that
it takes more than 10 hours to collect all the data if you have a _Personal Riot API Key_. So good luck! :)
