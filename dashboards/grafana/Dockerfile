FROM grafana/grafana:11.4.0

ENV GF_ANALYTICS_REPORTING_ENABLED=false \
    GF_AUTH_ANONYMOUS_ENABLED=false \
    GF_AUTH_BASIC_ENABLED=false \
    GF_SECURITY_ADMIN_PASSWORD=admin \
    GF_SECURITY_ADMIN_USER=admin \
    GF_SECURITY_ALLOW_EMBEDDING=true \
    GF_SECURITY_DISABLE_GRAVATAR=true \
    GF_USERS_ALLOW_SIGN_UP=false \
    GF_USERS_DEFAULT_LANGUAGE=detect \
    GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH=/dashboards/carbon-statistics-dashboard.json \
    DATABASE_PORT=5432 \
    DATABASE_SSL_MODE=disable
    
USER grafana

COPY datasource.yaml /etc/grafana/provisioning/datasources/
COPY dashboards.yaml /etc/grafana/provisioning/dashboards/
COPY dashboards/*.json /dashboards/

EXPOSE 3000