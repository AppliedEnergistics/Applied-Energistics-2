package appeng.container.me;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains statistics about an ME network and its machines.
 * @see appeng.client.gui.me.NetworkStatusScreen
 */
public class NetworkStatus {

    private double averagePowerInjection;
    private double averagePowerUsage;
    private double storedPower;
    private double maxStoredPower;

    private List<MachineEntry> machines = Collections.emptyList();

    public static NetworkStatus fromGrid(IGrid grid) {
        IEnergyGrid eg = grid.getCache(IEnergyGrid.class);

        NetworkStatus status = new NetworkStatus();

        status.averagePowerInjection = eg.getAvgPowerInjection();
        status.averagePowerUsage = eg.getAvgPowerUsage();
        status.storedPower = eg.getStoredPower();
        status.maxStoredPower = eg.getMaxStoredPower();

        Map<IAEItemStack, MachineEntry> machines = new HashMap<>();
        for (final Class<? extends IGridHost> machineClass : grid.getMachinesClasses()) {
            for (IGridNode machine : grid.getMachines(machineClass)) {
                IGridBlock blk = machine.getGridBlock();
                ItemStack is = blk.getMachineRepresentation();
                IAEItemStack ais = AEItemStack.fromItemStack(is);
                if (ais != null) {
                    ais.setStackSize(1);

                    MachineEntry machineEntry = machines.get(ais);
                    if (machineEntry == null) {
                        machines.put(ais, machineEntry = new MachineEntry(is));
                    }

                    machineEntry.count++;
                    machineEntry.idlePowerUsage += machineEntry.getIdlePowerUsage();
                }
            }
        }
        status.machines = ImmutableList.copyOf(machines.values());

        return status;
    }

    public double getAveragePowerInjection() {
        return averagePowerInjection;
    }

    public double getAveragePowerUsage() {
        return averagePowerUsage;
    }

    public double getStoredPower() {
        return storedPower;
    }

    public double getMaxStoredPower() {
        return maxStoredPower;
    }

    public List<MachineEntry> getMachines() {
        return machines;
    }

    public void read(PacketBuffer data) {
        averagePowerInjection = data.readDouble();
        averagePowerUsage = data.readDouble();
        storedPower = data.readDouble();
        maxStoredPower = data.readDouble();

        int count = data.readVarInt();
        ImmutableList.Builder<MachineEntry> machines = ImmutableList.builder();
        for (int i = 0; i < count; i++) {
            machines.add(MachineEntry.read(data));
        }
        this.machines = machines.build();
    }

    public void write(PacketBuffer data) {
        data.writeDouble(averagePowerInjection);
        data.writeDouble(averagePowerUsage);
        data.writeDouble(storedPower);
        data.writeDouble(maxStoredPower);
        data.writeVarInt(machines.size());
        for (MachineEntry machine : machines) {
            machine.write(data);
        }
    }

    /**
     * Reports statistics about a machine type in the network status.
     */
    public static class MachineEntry {
        private final ItemStack display;

        private double idlePowerUsage;

        private int count;

        public MachineEntry(ItemStack display) {
            this.display = display;
        }

        public static MachineEntry read(PacketBuffer data) {
            ItemStack stack = data.readItemStack();
            MachineEntry entry = new MachineEntry(stack);
            entry.idlePowerUsage = data.readDouble();
            entry.count = data.readVarInt();
            return entry;
        }

        public void write(PacketBuffer data) {
            data.writeItemStack(display, true);
            data.writeDouble(idlePowerUsage);
            data.writeVarInt(count);
        }

        public ItemStack getDisplay() {
            return display;
        }

        public double getIdlePowerUsage() {
            return idlePowerUsage;
        }

        public int getCount() {
            return count;
        }
    }

}
