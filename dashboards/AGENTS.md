# Monitoring Dashboards

This directory contains the Grafana and Docker Compose configurations used to visualize carbon emission statistics recorded by the carbon-aware scheduler.

## Key Components

1.  **PostgreSQL (17)**: Used as the storage backend for carbon statistics.
    *   **Port**: `5444` (exposed from container's `5432`).
    *   **Database**: `carbon`.
    *   **User/Password**: `carbon/carbon`.
2.  **Grafana (11.4.0)**: Used for data visualization.
    *   **Port**: `3001` (exposed from container's `3000`).
    *   **Default Credentials**: `admin/admin`.
    *   **Features**: Includes a pre-configured PostgreSQL datasource and a carbon statistics dashboard.

## Configuration Details

The Grafana setup is automatically provisioned with:
*   **Datasource**: A PostgreSQL connection named `carbon-statistics` pointing to the `postgres` service.
*   **Dashboard**: `carbon-statistics-dashboard.json`, which is set as the default home dashboard.

## How to Start the Dashboards

To launch the monitoring stack, ensure Docker is running and execute:

```bash
cd dashboards
docker-compose up -d
```

Once the containers are running, you can access Grafana at `http://localhost:3001`.

## Integration with Examples

The **Statistics Recording (Example 3)** and **Spring Boot Integration** (when using the `postgres` profile) are configured to persist emission data into the database provided by this setup.
