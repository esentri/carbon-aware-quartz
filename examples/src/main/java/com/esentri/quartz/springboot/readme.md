# Spring Boot 

Example to implement a TimeShifed Job in SpringBoot with enabled statistics.
The data will be stored in a in memory SQLiteDB.

To access the CarbonAware API a API-KEY is required. 
The Constant API_KEY in class `CarbonForecastClient.class` have to be changed.

also the configured [quartz.properties](../../../../../resources/com/esentri/quartz/example3/quartz.properties)

## In Memory Database

To access the stored data you can connect to the database file, named: `carbon-aware-quartz-db.mv.db` under your classpath.
Credentials are configured in the [application.properties](../../../../../resources/application-h2.properties)
Username: sa
Password: password

### start the application

Run the following command in your terminal
```bash
gradle :examples:runExampleSpringBoot-h2
```

## Postgres Database with Dashboards

To use this project with a PostgreSQL database and grafana dashboards, you have to start the local Postgres Database using docker-compose

### run the infrastructure

To run the Infrastructure execute the folowing comand on your terminal in the [dashboards](../../../../../../../../dashboards) folder:

```bash
docker-compose down && docker-compose up -d --build && docker-compose logs -f
```
With this command the database will be created and a pre-configured grafana server will be started on http://localhost:3001.

### start the application

Run the following command in your terminal

```bash
gradle :examples:runExampleSpringBoot-postgres
```

The credentials for the database are configured in the [docker-compose.yaml](../../../../../../../../dashboards/docker-compose.yaml) file.

### access your dashboard
When you open the grafana interface on http://localhost:3001 you have to sign in with the default credentials:

Username: admin
Password: admin

After sign in you have to set a new admin password for the grafana interface. After this, you can access the pre-configured carbon-statistic dashboard


