package com.hiby3.pontoapi.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
public class TenantDataSource {

    @Value("${admin.db.username}")
    private String adminUsername;

    @Value("${admin.db.password}")
    private String adminPassword;

    private Map<String, DataSource> dataSources = new HashMap<>();

    public DataSource getDataSource(String tenantId) {

        if (dataSources.containsKey(tenantId)) {
            System.out.println(">>> Conexão para " + tenantId + " já existe. Retornando...");
            return dataSources.get(tenantId);
        }

        System.out.println(">>> Criando nova conexão para " + tenantId);
        DataSource dataSource = createDataSource(tenantId);

        dataSources.put(tenantId, dataSource);

        return dataSource;
    }

    private DataSource createDataSource(String tenantId) {
        
        String url = "jdbc:mysql://localhost:3306/" + tenantId + "?useSSL=false&allowPublicKeyRetrieval=true";

        String username = adminUsername; 
        String password = adminPassword; 

        DataSourceBuilder<?> factory = DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver") 
                .url(url)
                .username(username)
                .password(password);

        return factory.build();
    }
}