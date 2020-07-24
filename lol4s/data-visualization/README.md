# Visualization Data
## Purpose
This module purpose is to provide a local HTML file (with a generate `javascript` file) to visualize the result of the
**Spark Queries** module.

## Usage
Simply run the sbt task: `fastOptJS` on this module.
    
    sbt visualization/fastOptJS

This action will generate a `js` file in the *target* directory of the module. The HTML will use this generated `js` file
to plot the selected `.tab` or `.graph` file.

The `.tab` or `.graph` files will be written by the **Spark Queries** module.

