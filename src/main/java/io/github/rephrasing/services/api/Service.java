package io.github.rephrasing.services.api;


import java.util.Optional;
import java.util.logging.Logger;

public abstract class Service {

    private final Logger logger;
    protected boolean running = false;

    public Service() {
        ServiceInfo info = this.getClass().getAnnotation(ServiceInfo.class);
        if (info == null) throw new IllegalArgumentException("ServiceInfo annotation not found on " + this.getClass().getName());
        this.logger = Logger.getLogger(info.value());
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

        if (info.dependsOn().length < 1) {
            return false;
        }
        for (Class<? extends Service> dependency : info.dependsOn()) {
            if (dependency == clazz) return true;
        }
        return false;
    }

    public boolean isRunning() {
        return running;
    }

    public Logger getLogger() {
        return logger;
    }
}
