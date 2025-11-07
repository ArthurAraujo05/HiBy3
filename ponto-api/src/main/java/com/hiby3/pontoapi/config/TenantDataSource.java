package com.hiby3.pontoapi.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
public class TenantDataSource {

    // Nosso "pool" de conexões.
    // A chave (String) será o nome do banco
    // O valor (DataSource) será a conexão real com aquele banco.
    private Map<String, DataSource> dataSources = new HashMap<>();

    // Este é o método que vamos chamar para obter a conexão de um cliente
    public DataSource getDataSource(String tenantId) {
        // 1. O 'tenantId' que vamos receber é o nome do banco

        // 2. Primeiro, checamos se já criamos essa conexão antes
        if (dataSources.containsKey(tenantId)) {
            System.out.println(">>> Conexão para " + tenantId + " já existe. Retornando...");
            return dataSources.get(tenantId);
        }

        // 3. Se não existe, vamos CRIAR UMA NOVA conexão
        System.out.println(">>> Criando nova conexão para " + tenantId);
        DataSource dataSource = createDataSource(tenantId);

        // 4. Guardamos no nosso "pool" para a próxima vez
        dataSources.put(tenantId, dataSource);

        return dataSource;
    }

    // Este é o método privado que constrói a conexão
    private DataSource createDataSource(String tenantId) {
        String url = "jdbc:mysql://localhost:3306/" + tenantId + "?useSSL=false&allowPublicKeyRetrieval=true";
        String username = "root"; // Estamos usando o root por enquanto
        String password = "admin123"; // Nossa senha do container

        DataSourceBuilder<?> factory = DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver") // Driver do MySQL
                .url(url)
                .username(username)
                .password(password);

        return factory.build();
    }
}