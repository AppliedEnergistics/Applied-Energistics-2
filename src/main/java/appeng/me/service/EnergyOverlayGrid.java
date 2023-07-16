package appeng.me.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import appeng.core.AELog;

/**
 * This class caches all energy services that are part of the overlay energy grid. This overlay grid can span multiple
 * normal {@linkplain appeng.me.Grid grids} if they are connected by {@link appeng.parts.networking.QuartzFiberPart
 * quartz fibers}.
 */
class EnergyOverlayGrid {
    /**
     * Prefer grids with high energy storage for operations by sorting them to the front of the list.
     */
    private static final Comparator<EnergyService> SERVICE_COMPARATOR = Comparator
            .comparingDouble(EnergyService::getMaxStoredPower)
            .reversed();

    final List<EnergyService> energyServices;

    private EnergyOverlayGrid(List<EnergyService> energyServices) {
        this.energyServices = energyServices;
    }

    void invalidate() {
        for (var service : energyServices) {
            service.overlayGrid = null;
        }
    }

    /**
     * Build a new overlay energy grid by discovering all accessible {@linkplain EnergyService energy services} starting
     * with the given grid.
     */
    static void buildCache(EnergyService startingService) {
        var connectedServices = new ReferenceOpenHashSet<EnergyService>();

        // Walk graph
        var services = new ObjectArrayList<EnergyService>();
        services.add(startingService);

        while (!services.isEmpty()) {
            var service = services.pop();

            // If the service was not already processed, add it and also discover other services
            // reachable via its grid.
            if (connectedServices.add(service)) {
                for (var provider : service.getOverlayGridConnections()) {
                    services.addAll(provider.connectedEnergyServices());
                }
            }
        }

        // Sort services by capacity
        var sortedServices = new ArrayList<>(connectedServices);
        sortedServices.sort(SERVICE_COMPARATOR);
        var overlayGrid = new EnergyOverlayGrid(List.copyOf(sortedServices));

        // Associate all grids that are part of the overlay grid with this instance
        for (var service : sortedServices) {
            // A previous overlay grid should have been invalidated before building a new one
            if (service.overlayGrid != null) {
                AELog.error("Grid %s energy service already has a power graph assigned to it!", service.grid);
            }

            service.overlayGrid = overlayGrid;
        }
    }
}
