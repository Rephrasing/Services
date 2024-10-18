package io.github.rephrasing.services.api;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ServiceManager {

    private static final LinkedHashMap<Class<? extends Service>, Service> registeredServicesMap = new LinkedHashMap<>();
    private static final LinkedHashMap<Class<? extends Service>, Service> runningServicesMap = new LinkedHashMap<>();
    private static final List<Service> waitingServices = new ArrayList<>();
    private static final ExecutorService asyncService = Executors.newCachedThreadPool();

    public static void startServices() {
        if (!waitingServices.isEmpty()) {
            throw new IllegalStateException("Cannot initialize services, missing dependencies " + waitingServices + "\n" + waitingServices.stream().map(c -> c.getClass().getName() + ": " + Arrays.toString(c.getDependencies()) + "\n"));
        }
        for (Service service : registeredServicesMap.values()) {
            if (service.isAsynchronous()) {
                asyncService.execute(service::start);
                continue;
            }
            service.start();
            runningServicesMap.put(service.getClass(), service);
        }
    }

    public static <T extends Service> void startService(Class<T> clazz) {
        T service = getService(clazz);
        if (service == null) {
            throw new IllegalArgumentException("Service of type [" + clazz.getName() + "] does not exist or has not been registered");
        }
        service.start();
        runningServicesMap.put(clazz, service);
    }

    public static void stopServices() {
        for (Service service : registeredServicesMap.values()) {
            if (service.isAsynchronous()) {
                asyncService.execute(service::stop);
                continue;
            }
            service.stop();
            runningServicesMap.remove(service.getClass());
        }
    }


    public static <T extends Service> void stopService(Class<T> clazz) {
        T service = getService(clazz);
        if (service == null) {
            throw new IllegalArgumentException("Service of type [" + clazz.getName() + "] does not exist or has not been registered");
        }
        if (service.isAsynchronous()) {
            asyncService.execute(service::stop);
            return;
        }
        service.stop();
        runningServicesMap.remove(service.getClass());
    }

    public static void registerService(Service... services) {
        for (Service service : services) {
            if (service.getDependencies().length > 0) {
                if(registeredServicesMap.keySet().containsAll(List.of(service.getDependencies()))) {
                    registeredServicesMap.put(service.getClass(), service);
                    continue;
                }
                for (Class<? extends Service> dependency : service.getDependencies()) {
                    if (!registeredServicesMap.containsKey(dependency)) {
                        waitingServices.add(service);
                        break;
                    }
                }
            } else {
                registeredServicesMap.put(service.getClass(), service);
            }
            if (!waitingServices.isEmpty()) {
                waitingServices.removeIf(srv -> {
                    boolean satisfied = registeredServicesMap.keySet().containsAll(List.of(srv.getDependencies()));
                    if (satisfied) registeredServicesMap.put(srv.getClass(), srv);
                    return satisfied;
                });
            }
        }
    }

    public static <T extends Service> T getService(Class<T> serviceClass) {
        return serviceClass.cast(registeredServicesMap.get(serviceClass));
    }

    public static <T extends Service> Optional<T> locate(Class<T> serviceClass) {
        return Optional.ofNullable(getService(serviceClass));
    }
}