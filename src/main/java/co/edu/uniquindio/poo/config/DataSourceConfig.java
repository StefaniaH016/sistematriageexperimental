package co.edu.uniquindio.poo.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        // Railway proporciona DATABASE_URL en formato: postgresql://user:password@host:port/database
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // Parsear DATABASE_URL
            URI dbUri = new URI(databaseUrl);
            
            String host = dbUri.getHost();
            int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
            String database = dbUri.getPath().substring(1); // Remove leading /
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            
            String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(url)
                    .username(username)
                    .password(password)
                    .build();
        }
        
        // Fallback para desarrollo local (si DATABASE_URL no está disponible)
        String pgHost = System.getenv("PGHOST");
        String pgPort = System.getenv("PGPORT");
        String pgDatabase = System.getenv("PGDATABASE");
        String pgUser = System.getenv("PGUSER");
        String pgPassword = System.getenv("PGPASSWORD");

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
