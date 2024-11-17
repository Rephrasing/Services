package io.github.rephrasing.services.impl.cache;

import com.mongodb.client.MongoCollection;
import io.github.rephrasing.services.api.Service;
import io.github.rephrasing.services.api.ServiceInfo;
import io.github.rephrasing.services.api.ServiceManager;
import io.github.rephrasing.services.impl.mongodb.MongoDatabaseService;
import org.bson.Document;

import java.util.*;
import java.util.function.Predicate;

@ServiceInfo(value = "Caching-Service", dependsOn = MongoDatabaseService.class)
public class CachingService extends Service {

    private final List<CacherAdapter<?>> adapters = new ArrayList<>();
    private final Map<Class<?>, List<?>> cache = new HashMap<>();
    private MongoDatabaseService databaseService;

    @Override
    protected void start() {
        this.databaseService = ServiceManager.getService(MongoDatabaseService.class);
        if (!databaseService.isRunning()) {
            throw new IllegalArgumentException("Could not start because MongoDatabaseService was not active");
        }
        getLogger().info("Initiated!");
    }

    @Override
    protected void stop() {
        getLogger().info("Stopped");
    }

    public <T> void cache(T object, Class<T> clazz) {
        getCacheList(clazz).add(object);
    }

    public <T> boolean remove(Class<T> clazz, Predicate<T> filter) {
        return getCacheList(clazz).removeIf(filter);
    }

    public <T> Optional<T> find(Class<T> clazz, Predicate<T> filter) {
        return getCacheList(clazz).stream().filter(filter).findAny();
    }

    public <T> List<T> getCacheList(Class<T> clazz) {
        return (List<T>) this.cache.get(clazz);
    }

    public <T> void pullFromDatabase(Class<T> type, boolean replaceCurrentCache) {
        CacherAdapter<T> adapter = getAdapterByType(type);
        if (adapter == null) {
            throw new IllegalArgumentException("Cannot pull from database for type \"" + type.getName() + "\" because there was no adapter registered for it");
        }
        MongoCollection<Document> coll = this.databaseService.getCollection(adapter.getDatabaseName(), adapter.getCollectionName(), Document.class);

        if (replaceCurrentCache) {
            this.cache.put(type, new ArrayList<>());
        }
        for (Document doc : coll.find()) {
            T deserializedObject = adapter.deserialize(doc);
            getCacheList(type).add(deserializedObject);
        }
        getLogger().info(String.format("Successfully pulled from database (%s, %s)", adapter.getDatabaseName(), adapter.getCollectionName()));
    }

    public <T> void pushToDatabase(Class<T> type, boolean replaceOldCollectionObjects) {
        CacherAdapter<T> adapter = getAdapterByType(type);
        if (adapter == null) {
            throw new IllegalArgumentException("Cannot pull from database for type \"" + type.getName() + "\" because there was no adapter registered for it");
        }
        MongoCollection<Document> coll = this.databaseService.getCollection(adapter.getDatabaseName(), adapter.getCollectionName(), Document.class);

        if (replaceOldCollectionObjects) {
            coll.dropIndexes();
        }
        List<Document> serializedObjects = getCacheList(type).stream().map(adapter::serialize).toList();
        coll.insertMany(serializedObjects);
        getLogger().info(String.format("Successfully pushed to database (%s, %s)", adapter.getDatabaseName(), adapter.getCollectionName()));
    }

    public <T> void registerAdapter(CacherAdapter<T> adapter) {
        if (findAdapterByType(adapter.getType()).isPresent()) {
            getLogger().warning(String.format("CachingAdapter of type \"%s\" is already registered", adapter.getType().getName()));
            return;
        }
        this.adapters.add(adapter);
        this.cache.put(adapter.getType(), new ArrayList<>());
        getLogger().info(String.format("Registered CachingAdapter for type %s", adapter.getType().getName()));
    }

    public <T> Optional<CacherAdapter<T>> findAdapterByType(Class<T> clazz) {
        return adapters.stream().filter(adapter -> adapter.getType() == clazz).map(adapter -> (CacherAdapter<T>) adapter).findAny();
    }

    public <T> CacherAdapter<T> getAdapterByType(Class<T> clazz) {
        return adapters.stream().filter(adapter -> adapter.getType() == clazz).map(adapter -> (CacherAdapter<T>) adapter).findAny().orElse(null);
    }
}