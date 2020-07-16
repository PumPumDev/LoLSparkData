# Spark Queries

## Purpose
This module is use to create some queries to the data using Spark framework and record the results in `.tab` or `.graph` 
files.

## Usage
Simply run the main class: `SparkQueriesMain`. This Spark program can be launched on the cloud using services like 
[AWS EMR](https://aws.amazon.com/es/emr/).

**The EMR version must be 6.0.0 or higher**.

The `.tab` or `.graph` files will be written in the path set in the `.conf` file (in **Utils** module).

## Approx amount of data
* Players number: 2.050
* Matches number: 118.698
* Total stats number: 1.186.926
* Challenger stats number: 246.752