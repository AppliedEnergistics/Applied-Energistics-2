package appeng.container.me;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

/**
 * Contains statistics about an ME network and the machines that form it.
 *
 * @see appeng.client.gui.me.NetworkStatusScreen
 */
public class NetworkStatus {

    private double averagePowerInjection;
    private double averagePowerUsage;
    private double storedPower;
    private double maxStoredPower;

    private List<MachineGroup> groupedMachines = Collections.emptyList();

    public static NetworkStatus fromGrid(IGrid grid) {
        IEnergyGrid eg = grid.getCache(IEnergyGrid.class);

        NetworkStatus status = new NetworkStatus();

        status.averagePowerInjection = eg.getAvgPowerInjection();
        status.averagePowerUsage = eg.getAvgPowerUsage();
        status.storedPower = eg.getStoredPower();
        status.maxStoredPower = eg.getMaxStoredPower();

        // This is essentially a groupBy machineRepresentation + count, sum(idlePowerUsage)
        Map<IAEItemStack, MachineGroup> groupedMachines = new HashMap<>();
        for (final Class<? extends IGridHost> machineClass : grid.getMachinesClasses()) {
            for (IGridNode machine : grid.getMachines(machineClass)) {
                IGridBlock blk = machine.getGridBlock();
                ItemStack is = blk.getMachineRepresentation();
                IAEItemStack ais = AEItemStack.fromItemStack(is);
                if (ais != null) {
                    ais.setStackSize(1);

                    MachineGroup group = groupedMachines.get(ais);
                    if (group == null) {
                        groupedMachines.put(ais, group = new MachineGroup(is));
                    }

                    group.setCount(group.getCount() + 1);
                    group.setIdlePowerUsage(group.getIdlePowerUsage() + blk.getIdlePowerUsage());
                }
            }
        }
        status.groupedMachines = ImmutableList.copyOf(groupedMachines.values());

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

    /**
     * @return Machines grouped by their UI representation.
     */
    public List<MachineGroup> getGroupedMachines() {
        return groupedMachines;
    }

    /**
     * Reads a network status previously written using {@link #write(PacketBuffer)}.
     */
    public static NetworkStatus read(PacketBuffer data) {
        NetworkStatus status = new NetworkStatus();
        status.averagePowerInjection = data.readDouble();
        status.averagePowerUsage = data.readDouble();
        status.storedPower = data.readDouble();
        status.maxStoredPower = data.readDouble();

        int count = data.readVarInt();
        ImmutableList.Builder<MachineGroup> machines = ImmutableList.builder();
        for (int i = 0; i < count; i++) {
            machines.add(MachineGroup.read(data));
        }
        status.groupedMachines = machines.build();

        return status;
    }

    /**
     * Writes the contents of this object to a packet buffer. Use {@link #read(PacketBuffer)} to restore.
     */
    public void write(PacketBuffer data) {
        data.writeDouble(averagePowerInjection);
        data.writeDouble(averagePowerUsage);
        data.writeDouble(storedPower);
        data.writeDouble(maxStoredPower);
        data.writeVarInt(groupedMachines.size());
        for (MachineGroup machine : groupedMachines) {
            machine.write(data);
        }
    }

}
