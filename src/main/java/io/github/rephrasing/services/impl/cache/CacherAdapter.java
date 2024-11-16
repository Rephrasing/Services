package io.github.rephrasing.services.impl.cache;

import org.bson.Document;

public interface CacherAdapter<T> {

    Document serialize(T object);
    T deserialize(Document document);
    Class<T> getType();
    String getDatabaseName();
    String getCollectionName();
}
