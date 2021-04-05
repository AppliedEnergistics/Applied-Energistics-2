package appeng.container.me;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.networking.IGridBlock;

/**
 * Represents the status of machines grouped by their {@link IGridBlock#getMachineRepresentation() item representation}.
 */
public class MachineGroup {
    /**
     * The item stack used for grouping machines together, which is also used for showing the group in the UI.
     *
     * @see IGridBlock#getMachineRepresentation()
     */
    private final ItemStack display;

    /**
     * Summed up idle power usage of this machine group in AE/t.
     */
    private double idlePowerUsage;

    /**
     * The number of machines in this group.
     */
    private int count;

    MachineGroup(ItemStack display) {
        this.display = display;
    }

    /**
     * Reads back a machine group previously {@link #write(PacketBuffer) written}.
     */
    static MachineGroup read(PacketBuffer data) {
        ItemStack stack = data.readItemStack();
        MachineGroup entry = new MachineGroup(stack);
        entry.idlePowerUsage = data.readDouble();
        entry.count = data.readVarInt();
        return entry;
    }

    void write(PacketBuffer data) {
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

    void setIdlePowerUsage(double idlePowerUsage) {
        this.idlePowerUsage = idlePowerUsage;
    }

    public int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }
}
