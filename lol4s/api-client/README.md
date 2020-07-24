# API Client

## Purpose
This module purpose is to obtain the data from [Riot Api](https://developer.riotgames.com/apis). If you want to use
this module you need to configure some settings (there is a configuration file example in **Utils** module).

## Usage
After setting up the configuration file, simply run the main class of the module `ClientAPIMain`. This module needs Internet
connexion to work.

### Configuration file
The file ``utils/src/main/resources/application.template`` is a template about how the configuration file should be.

First, change the extension from ``.template`` to ``.conf``. 

All properties are set with default values, the only 
property that **must** be set by your own is the ``riot.api.keys`` property.

#### application.conf example
```
# Property to avoid warning messages in data headers responses
akka.http.client.parsing.ignore-illegal-header-for = [date]
# Property with the API Riot token
riot.api.token = X-Riot-Token
# Property with the API keys to access to the API (The more API Keys you put, the faster data will be obtained)
riot.api.keys = [apiKey1, apiKey2, apiKey3, apiKey4, apiKey5, ...]

# The path where data will be locally storage
riot.data.output.path = api_data

# Set if you want to print the API statistics performance at the end of the download
riot.data.print.statistics = true

# Uris to obtain the data from the API
riot.api.uri.challenger.player = /lol/league/v4/challengerleagues/by-queue/RANKED_SOLO_5x5
riot.api.uri.challenger.summoner = /lol/summoner/v4/summoners/
riot.api.uri.challenger.matchlist = /lol/match/v4/matchlists/by-account/
riot.api.uri.challenger.match = /lol/match/v4/matches/
```
## Data
The program collect 4 kind of data of each [League of Legends Region](https://developer.riotgames.com/docs/lol):
    
    Players
    Summoners
    Match References
    Matches
     
## Logs
The application will log the following events:

* The API response was lost or the API responded with an error status

If any petition is responded with an error or get lost, the program retry that petition to the API one more time.

**The response of the retry request will be logged too**.