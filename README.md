**Project status:** This repository contains a working project structure and implementation code, but a few behaviors and launch settings need verification before the application can be described as production-ready. See [Known Limitations](#known-limitations).

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Traffic Simulation Logic](#traffic-simulation-logic)
- [Emergency Priority](#emergency-priority)
- [Pedestrian Crossing](#pedestrian-crossing)
- [Database and Event Logging](#database-and-event-logging)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [Build and Run](#build-and-run)
- [How to Use the Application](#how-to-use-the-application)
- [Troubleshooting](#troubleshooting)
- [Known Limitations](#known-limitations)
- [Possible Improvements](#possible-improvements)
- [Author](#author)

## Overview

TrafficLight is a desktop simulation of an intersection with a **main road** and a **cross road**. The application cycles through traffic-light phases and adjusts green-light duration according to simulated traffic-density values. Users can also request emergency priority for one of the two directions.

The project is intended as an educational demonstration of Java application development, JavaFX interfaces, object-oriented design, background-thread simulation, and JDBC-based database logging.

**Important:** traffic density is generated and changed randomly inside the application. The current implementation does not read data from physical cameras, road sensors, GPS devices, or a trained machine-learning model. The adaptive timing is rule-based rather than a validated AI traffic-prediction system.

## Features

### 1. Two-road traffic-light simulation

- Displays separate traffic signals for the main road and cross road.
- Represents red, yellow, and green states visually.
- Runs a repeating sequence for each direction.
- Includes an all-red safety phase between directions.
- Updates the interface from the simulation running in a background thread.

### 2. Dynamic traffic-density indicators

- Maintains a density value for each road.
- Simulates density changes over time using random fluctuations.
- Displays density as a percentage and a progress bar.
- Changes the progress-bar color based on density:
  - Green for low density.
  - Yellow for medium density.
  - Red for high density.
- Calculates green duration from the current density.

The current duration rule is:

```text
green duration = 5 seconds + (density × 10 seconds)
```

With the implemented density range of `0.1` to `1.0`, the calculated green phase ranges from approximately 6 to 15 seconds.

### 3. Emergency-priority request

- Includes a control to request tram/emergency priority for the main road.
- Provides a dashboard with sample ambulance and police vehicles.
- Lets the user select the incoming direction when starting an emergency from a vehicle card.
- Logs emergency start and end events when database logging is configured.
- Runs an emergency sequence that first sets both roads to red, grants green to the selected direction, then returns both roads to red before resuming normal operation.

### 4. Pedestrian-crossing panel

- Displays STOP and WALK indicators.
- Includes a pedestrian crossing-request button.
- Shows a waiting message while a request is pending.
- Uses JavaFX properties and bindings to update the pedestrian panel.

The pedestrian panel currently demonstrates request-state and display logic. It does not yet implement a complete request queue integrated into the traffic-cycle scheduler.

### 5. MySQL event logging

The JDBC component is designed to store system and emergency events in a MySQL database. Events include system startup, emergency requests, emergency completion, and a test connection event.

The database is used for event logging; the traffic-density simulation itself runs in application memory.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Application logic and object-oriented model |
| JavaFX Controls | Desktop UI components |
| JavaFX FXML | Layout and UI/controller connection |
| Maven | Dependency management and build lifecycle |
| MySQL | Persistent event logging |
| JDBC | Java-to-MySQL database access |
| MySQL Connector/J | MySQL JDBC driver |
| JUnit 5 | Test framework dependencies configured in Maven |

Versions declared in `pom.xml` include JavaFX `21.0.6`, MySQL Connector/J `8.3.0`, and JUnit `5.12.1`. The Maven compiler is configured for Java `25`, so a compatible JDK is required unless that setting is changed.

## Architecture

The application follows a simple separation of responsibilities:

- **Model** — represents traffic lights, density simulation, pedestrian state, and emergency vehicles.
- **Controller** — connects UI actions to the simulation and refreshes JavaFX components.
- **View** — builds the traffic dashboard, emergency vehicle cards, and pedestrian panel.
- **Database layer** — writes event records to MySQL.

### Main classes

| Class | Responsibility |
|---|---|
| `Main` | Starts the JavaFX application and loads the FXML layout |
| `TrafficController` | Handles dashboard actions and updates lights, density bars, and pedestrian state |
| `TrafficModel` | Runs the traffic phases, changes density values, calculates green durations, and handles emergency requests |
| `TrafficLight` | Represents an individual traffic light and emergency override state |
| `LightColor` | Defines the `RED`, `YELLOW`, and `GREEN` signal states |
| `EmergencyVehicle` | Represents a sample emergency vehicle and its status |
| `EmergencyManager` | Manages sample emergency vehicles and starts emergency-priority requests |
| `PietonModel` | Stores pedestrian signal and crossing-request state |
| `EmergencyDashboard` | Displays emergency vehicle cards and their controls |
| `PietonDashboard` | Displays pedestrian lights and the crossing-request button |
| `DatabaseHandler` | Inserts event logs into MySQL |

## Traffic Simulation Logic

The normal cycle implemented in `TrafficModel` is:

1. Main road green; cross road red.
2. Main road yellow; cross road red.
3. Both roads red for a safety interval.
4. Main road red; cross road green.
5. Main road red; cross road yellow.
6. Both roads red for a safety interval.
7. Repeat while the simulation is running.

During waiting periods, the application updates the simulated density approximately every 500 milliseconds. Density values are clamped between `0.1` and `1.0`.

## Emergency Priority

An emergency request specifies either `MAIN` or `CROSS` as the priority direction. The simulation is intended to:

1. Set both roads to red.
2. Wait briefly for a safety interval.
3. Set the requested direction to green.
4. Keep that priority for a fixed interval.
5. Return both roads to red.
6. Log the end of the emergency and resume the normal cycle.

The main dashboard's **Force Tram Priority** control requests priority for the main road. The vehicle dashboard also offers a direction-selection dialog when an emergency is started from a sample vehicle card.

Emergency handling and thread interruption should be tested carefully before relying on this behavior; see [Known Limitations](#known-limitations).

## Pedestrian Crossing

The pedestrian dashboard has two indicators and a **Request Crossing** button. A request is recorded when the pedestrian signal is red. The controller updates the pedestrian display according to the current traffic-light states.

At present, the request flag is a UI/model state rather than a fully scheduled request that guarantees the next safe crossing phase. A future version should connect pedestrian requests to the same phase scheduler used by vehicles.

## Database and Event Logging

`DatabaseHandler` uses JDBC to connect to a local MySQL database with these settings in the current source:

- Database: `traffic_db`
- Host: `localhost`
- Port: `3306`
- User: `root`
- Password: empty string

These are source-code defaults, not universal MySQL credentials. Change them to match your local environment. Do not commit real passwords or other secrets to GitHub.

### Expected table

The insert statement in `DatabaseHandler` expects a table named `traffic_logs` with columns named `event_type`, `timestamp`, and `details`. One compatible example schema is:

```sql
CREATE DATABASE IF NOT EXISTS traffic_db;
USE traffic_db;

CREATE TABLE IF NOT EXISTS traffic_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    details TEXT
);
```

The `id` column is an additional auto-generated identifier; the application insert statement does not need to provide it.

### Events currently logged

| Event | Meaning |
|---|---|
| `SYSTEM_START` | Normal traffic simulation started |
| `EMERGENCY_TRIGGER` | Emergency priority was requested, including the direction |
| `EMERGENCY_END` | Emergency sequence completed |
| `TEST_CONNECTION` | Event used by the `DatabaseHandler` test entry point |

Database logging is asynchronous. If the database is unavailable or the table does not exist, the application prints a connection or SQL error to the console.

## Project Structure

```text
trafficlight/
├── .mvn/
│   └── wrapper/                  # Maven Wrapper files
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java  # Java module declarations
│       │   └── com/traffic/
│       │       ├── Main.java
│       │       ├── controller/
│       │       │   ├── EmergencyManager.java
│       │       │   └── TrafficController.java
│       │       ├── db/
│       │       │   └── DatabaseHandler.java
│       │       ├── model/
│       │       │   ├── EmergencyVehicle.java
│       │       │   ├── LightColor.java
│       │       │   ├── PietonModel.java
│       │       │   ├── TrafficLight.java
│       │       │   └── TrafficModel.java
│       │       └── view/
│       │           ├── EmergencyDashboard.java
│       │           └── PietonDashboard.java
│       └── resources/
│           └── com/traffic/
│               └── view.fxml
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

Generated build output such as `target/` is excluded from this structure.

## Prerequisites

Install the following:

1. **JDK 25**, or adjust the Maven compiler settings to match a supported JDK installed on your machine.
2. **Maven** (optional if using the included Maven Wrapper).
3. **MySQL Server** if you want event logging to persist to a database.
4. An IDE such as IntelliJ IDEA or Visual Studio Code with Java support (optional).

Verify Java and Maven availability:

```bash
java -version
mvn -version
```

On Windows, the Maven Wrapper can be invoked with `mvnw.cmd`.

## Database Setup

1. Start the MySQL server.
2. Open MySQL Workbench or another MySQL client.
3. Run the SQL schema shown in [Database and Event Logging](#database-and-event-logging).
4. Check the username and password used by `DatabaseHandler`.
5. Confirm that the MySQL server is listening on port `3306`.

If you do not configure MySQL, the UI may still open, but database operations will report errors in the console.

## Configuration

Before running the application:

1. Open `src/main/java/com/traffic/db/DatabaseHandler.java`.
2. Verify the JDBC URL, database name, username, and password.
3. Do not store a production password directly in source code. Prefer environment variables or a configuration file excluded from version control.
4. Verify the Java compiler version in `pom.xml`.

### Verify the JavaFX Maven entry point

The current `pom.xml` contains a JavaFX Maven plugin `mainClass` value that does not match the actual application entry point. The source code declares the module as `com.traffic` and the JavaFX entry point as `com.traffic.Main`.

Before using the Maven JavaFX run goal, update the plugin configuration to use:

```xml
<mainClass>com.traffic/com.traffic.Main</mainClass>
```

This corrects the mismatch between the configured launcher and the actual class/module names.

## Build and Run

From the project root directory:

### Windows (PowerShell)

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd javafx:run
```

### macOS / Linux

```bash
chmod +x mvnw
./mvnw clean compile
./mvnw javafx:run
```

If Maven is installed globally, you can replace the wrapper command with `mvn`.

The `javafx:run` command depends on the `mainClass` correction described above. If the application fails to start, check the troubleshooting section.

## How to Use the Application

1. Launch the application.
2. Click **Start AI Cycle** to start the automatic traffic-light sequence.
3. Observe the two intersections' signal states and the changing density percentages.
4. Click **Force Tram Priority** to request emergency priority for the main road.
5. In the emergency vehicle dashboard, use **Siren ON/OFF** to start or stop the emergency state for a sample vehicle. Starting an emergency opens a direction-selection dialog.
6. Use the pedestrian crossing button to create a crossing request and observe the displayed state.
7. If MySQL is configured correctly, inspect the `traffic_logs` table for recorded system and emergency events.

The interface is a simulator; it is not connected to real road infrastructure and must not be used to control actual traffic signals.

## Troubleshooting

### The JavaFX Maven command cannot find the main class

Check the `mainClass` value in the JavaFX Maven plugin. It should match the project's module and entry point:

```xml
<mainClass>com.traffic/com.traffic.Main</mainClass>
```

### Java version errors during compilation

The current Maven compiler source/target is set to Java 25. Install a compatible JDK or change both compiler values to the version you intend to use, then rebuild.

### MySQL connection errors

- Confirm that MySQL is running.
- Verify the database name, port, username, and password.
- Confirm that the `traffic_logs` table exists.
- Check the console output for the underlying JDBC exception.

### No event records appear

- Ensure the application can connect to MySQL.
- Verify that the table columns match the insert statement.
- Look for `[DB SUCCESS]` or `[DB ERROR]` messages in the console.

### Emergency priority does not resume the normal cycle

Review the interruption handling in `TrafficModel`. The current implementation's outer simulation thread catches an `InterruptedException` and re-interrupts the thread, which can terminate the normal simulation loop rather than reliably returning to emergency handling. This should be corrected and tested.

## Known Limitations

- Traffic density is randomly simulated; no real sensor, camera, or external traffic feed is connected.
- The timing strategy is a simple density-based rule, not a trained or validated AI model.
- The emergency simulation's interruption and resumption logic needs correction and testing.
- `EmergencyManager.simulateVehicleMovement()` is currently empty, so the **Go to Light** button does not yet simulate vehicle movement.
- Pedestrian crossing requests are represented in the UI/model but are not fully integrated into the traffic-phase scheduler.
- MySQL settings are hard-coded in the current `DatabaseHandler`; externalized configuration is recommended.
- The repository's Maven JavaFX `mainClass` configuration needs to be aligned with `com.traffic.Main`.
- The repository includes JUnit 5 dependencies, but no test classes were present in the supplied source tree.

## Possible Improvements

- Move database credentials to environment variables or a local, ignored configuration file.
- Correct and unit-test the simulation's thread lifecycle and emergency interruption behavior.
- Integrate pedestrian requests into a safe phase scheduler.
- Implement emergency vehicle movement and a complete arrival/pass workflow.
- Add automated tests for signal transitions, density bounds, emergency priority, and event logging.
- Add a database migration or schema setup script.
- Add screenshots or a short demonstration GIF to the GitHub README.
- If the project is extended toward real traffic analytics, introduce validated sensor input and document the algorithm separately from the current simulation.

## Author

**Chaimaa Lahlou**

This project is an educational software simulation focused on Java, JavaFX, traffic-control logic, and database event logging.
