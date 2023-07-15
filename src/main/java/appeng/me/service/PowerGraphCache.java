package appeng.me.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import appeng.core.AELog;

class PowerGraphCache {
    private static final Comparator<EnergyService> SERVICE_COMPARATOR = Comparator
            .comparingDouble(service -> -service.getMaxStoredPower());

    static void buildCache(EnergyService startingService) {
        Set<EnergyService> connectedServices = new ReferenceOpenHashSet<>();

        // Walk graph
        ObjectArrayList<EnergyService> services = new ObjectArrayList<>();
        services.add(startingService);

        while (!services.isEmpty()) {
            EnergyService service = services.pop();

            if (connectedServices.add(service)) {
                for (var provider : service.providers()) {
                    services.addAll(provider.providers());
                }
            }
        }

        // Sort services by capacity
        List<EnergyService> sortedServices = new ArrayList<>(connectedServices);
        sortedServices.sort(SERVICE_COMPARATOR);
        var cache = new PowerGraphCache(sortedServices);

        // Assign to grids
        for (var service : sortedServices) {
            if (service.powerGraph != null) {
                AELog.error("Grid %s energy service already has a power graph assigned to it!", service.grid);
            }

            service.powerGraph = cache;
        }
    }

    final List<EnergyService> energyServices;

    private PowerGraphCache(List<EnergyService> energyServices) {
        this.energyServices = energyServices;
    }

    void invalidate() {
        for (var service : energyServices) {
            service.powerGraph = null;
        }
    }
}
