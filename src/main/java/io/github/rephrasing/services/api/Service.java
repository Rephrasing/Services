package io.github.rephrasing.services.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public abstract class Service {

    private final Logger logger;

    public Service() {
        ServiceInfo info = this.getClass().getAnnotation(ServiceInfo.class);
        if (info == null) throw new IllegalArgumentException("ServiceInfo annotation not found on " + this.getClass().getName());
        this.logger = LogManager.getLogger(info.value());
    }

    protected abstract void start();
    protected abstract void stop();

    protected <T extends Service> T getService(Class<T> serviceClass) {
        return ServiceManager.getService(serviceClass);
    }

    protected <T extends Service> Optional<T> locateService(Class<T> serviceClass) {
        return ServiceManager.locate(serviceClass);
    }

    public boolean isAsynchronous() {
        return this.getClass().getAnnotation(ServiceInfo.class).async();
    }

    public Class<? extends Service>[] getDependencies() {
        ServiceInfo info = this.getClass().getAnnotation(ServiceInfo.class);

        return info.dependsOn();
    }

    public boolean isDependency(Class<? extends Service> clazz) {
        ServiceInfo info = this.getClass().getAnnotation(ServiceInfo.class);

        return info.dependsOn().length > 0 && info.dependsOn()[0] == clazz;
    }

    public Logger getLogger() {
        return logger;
    }
}
