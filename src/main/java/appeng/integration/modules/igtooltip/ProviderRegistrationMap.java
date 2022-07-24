package appeng.integration.modules.igtooltip;

import appeng.api.integrations.igtooltip.InGameTooltipProvider;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ProviderRegistrationMap<T extends ProviderRegistrationMap.Registration> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final List<T> registrations = new ArrayList<>();
    private final Map<Class<?>, CachedProviders<?>> cache = new IdentityHashMap<>();
    private final Comparator<T> comparator;

    public ProviderRegistrationMap() {
        this.comparator = Comparator.comparingInt(Registration::getPosition);
    }

    public void register(T registration) {
        writeLock.lock();
        try {
            registrations.add(registration);
            registrations.sort(comparator);
            cache.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <U> List<InGameTooltipProvider<? super U>> getProviders(Class<U> objectClass) {
        var providers = getCachedProviders(objectClass);

        if (providers == null) {
            writeLock.lock();
            try {
                providers = ((CachedProviders<U>) cache.computeIfAbsent(objectClass, this::createProviderList)).providers();
            } finally {
                writeLock.unlock();
            }
        }

        return providers;
    }

    public List<T> getRegistrations() {
        readLock.lock();
        try {
            return new ArrayList<>(registrations);
        } finally {
            readLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private <U> List<InGameTooltipProvider<? super U>> getCachedProviders(Class<U> clazz) {
        readLock.lock();
        try {
            var result = ((CachedProviders<U>) cache.get(clazz));
            return result != null ? result.providers() : null;
        } finally {
            readLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @GuardedBy("writeLock")
    private <U> CachedProviders<U> createProviderList(Class<U> clazz) {
        var result = new ArrayList<InGameTooltipProvider<? super U>>();
        for (var registration : registrations) {
            if (registration.supports(clazz)) {
                result.add((InGameTooltipProvider<? super U>) registration.getProvider());
            }
        }

        return new CachedProviders<U>(List.copyOf(result));
    }

    interface Registration {
        boolean supports(Class<?> objectClass);

        InGameTooltipProvider<?> getProvider();

        int getPosition();
    }

    record CachedProviders<U>(List<InGameTooltipProvider<? super U>> providers) {
    }
}
