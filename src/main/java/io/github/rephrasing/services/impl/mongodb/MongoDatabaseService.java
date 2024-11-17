package io.github.rephrasing.services.impl.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.rephrasing.services.api.Service;
import io.github.rephrasing.services.api.ServiceInfo;
import io.github.rephrasing.services.api.ServiceManager;
import org.bson.codecs.configuration.CodecRegistry;

@ServiceInfo(value = "MongoDatabase-Service")
public final class MongoDatabaseService extends Service {

    private MongoClient client;
    private CodecRegistry[] registries;
    private final String connectionString;

    public MongoDatabaseService(String connectionString) {
        this.connectionString = connectionString;
    }
    public MongoDatabaseService(String connectionString, CodecRegistry... registries) {
        this.connectionString = connectionString;
        this.registries = registries;
    }

    public MongoDatabase getDatabase(String databaseName) {
        return this.client.getDatabase(databaseName);
    }

    public <T> MongoCollection<T> getCollection(String databaseName, String collectionName, Class<T> clazz) {
        return this.client.getDatabase(databaseName).getCollection(collectionName, clazz);
    }

    @Override
    protected void start() {
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        builder.applyConnectionString(new ConnectionString(this.connectionString));
        if (registries != null) {
            for (CodecRegistry registry : registries) {
                builder.codecRegistry(registry);
            }
        }
        this.client = MongoClients.create();
    }

    @Override
    protected void stop() {
        this.client.close();
    }
}
