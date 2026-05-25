package co.edu.uniquindio.poo.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String pgHost = System.getenv("PGHOST");
        String pgPort = System.getenv("PGPORT");
        String pgDatabase = System.getenv("PGDATABASE");
        String pgUser = System.getenv("PGUSER");
        String pgPassword = System.getenv("PGPASSWORD");

        // Defaults para desarrollo local
        if (pgHost == null) pgHost = "localhost";
        if (pgPort == null) pgPort = "5432";
        if (pgDatabase == null) pgDatabase = "solicitudesdb";
        if (pgUser == null) pgUser = "postgres";
        if (pgPassword == null) pgPassword = "postgres";

        String url = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDatabase);

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(url)
                .username(pgUser)
                .password(pgPassword)
                .build();
    }
}
