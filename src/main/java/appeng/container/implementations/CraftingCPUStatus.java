package appeng.container.implementations;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.ItemSorters;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.io.*;

public class CraftingCPUStatus implements Comparable<CraftingCPUStatus> {

    @Nullable
    private final ICraftingCPU serverCluster;
    private final String name;
    private final int serial;
    private final long storage;
    private final long coprocessors;
    private final long totalItems;
    private final long remainingItems;
    private final IAEItemStack crafting;

    public CraftingCPUStatus() {
        this.serverCluster = null;
        this.name = "ERROR";
        this.serial = 0;
        this.storage = 0;
        this.coprocessors = 0;
        this.totalItems = 0;
        this.remainingItems = 0;
        this.crafting = null;
    }

    public CraftingCPUStatus(ICraftingCPU cluster, int serial) {
        this.serverCluster = cluster;
        this.name = cluster.getName();
        this.serial = serial;
        if (cluster.isBusy()) {
            crafting = cluster.getFinalOutput();
            totalItems = cluster.getStartItemCount();
            remainingItems = cluster.getRemainingItemCount();
        } else {
            crafting = null;
            totalItems = 0;
            remainingItems = 0;
        }
        this.storage = cluster.getAvailableStorage();
        this.coprocessors = cluster.getCoProcessors();
    }

    public CraftingCPUStatus(NBTTagCompound i) {
        this.serverCluster = null;
        this.name = i.getString("name");
        this.serial = i.getInteger("serial");
        this.storage = i.getLong("storage");
        this.coprocessors = i.getLong("coprocessors");
        this.totalItems = i.getLong("totalItems");
        this.remainingItems = i.getLong("remainingItems");
        this.crafting = i.hasKey("crafting") ? AEItemStack.fromNBT(i.getCompoundTag("crafting")) : null;
    }

    public CraftingCPUStatus(ByteBuf packet) throws IOException {
        this(readNBTFromPacket(packet));
    }

    private static NBTTagCompound readNBTFromPacket(ByteBuf packet) throws IOException {
        final int size = packet.readInt();
        final byte[] tagBytes = new byte[size];
        packet.readBytes(tagBytes);
        final ByteArrayInputStream di = new ByteArrayInputStream(tagBytes);
        return CompressedStreamTools.read(new DataInputStream(di));
    }

    public void writeToNBT(NBTTagCompound i) {
        if (name != null && !name.isEmpty()) {
            i.setString("name", name);
        }
        i.setInteger("serial", serial);
        i.setLong("storage", storage);
        i.setLong("coprocessors", coprocessors);
        i.setLong("totalItems", totalItems);
        i.setLong("remainingItems", remainingItems);
        if (crafting != null) {
            NBTTagCompound stack = new NBTTagCompound();
            crafting.writeToNBT(stack);
            i.setTag("crafting", stack);
        }
    }

    public void writeToPacket(ByteBuf i) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final DataOutputStream data = new DataOutputStream(bytes);

        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        CompressedStreamTools.write(tag, data);

        final byte[] tagBytes = bytes.toByteArray();
        final int size = tagBytes.length;

        i.writeInt(size);
        i.writeBytes(tagBytes);
    }

    @Nullable
    public ICraftingCPU getServerCluster() {
        return serverCluster;
    }

    public String getName() {
        return name;
    }

    public int getSerial() {
        return serial;
    }

    public long getStorage() {
        return storage;
    }

    public long getCoprocessors() {
        return coprocessors;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getRemainingItems() {
        return remainingItems;
    }

    public IAEItemStack getCrafting() {
        return crafting;
    }

    @Override
    public int compareTo(CraftingCPUStatus o) {
        final int a = ItemSorters.compareLong(o.getCoprocessors(), this.getCoprocessors());
        if (a != 0) {
            return a;
        }
        return ItemSorters.compareLong(o.getStorage(), this.getStorage());
    }

    public String formatStorage() {
        long val = getStorage();
        if (val > 4_000_000_000_000L) {
            return String.format("%dT", val / 1024 / 1024 / 1024 / 1024);
        } else if (val > 4_000_000_000L) {
            return String.format("%dG", val / 1024 / 1024 / 1024);
        } else if (val > 4_000_000L) {
            return String.format("%dM", val / 1024 / 1024);
        } else if (val > 4_000L) {
            return String.format("%dk", val / 1024);
        } else {
            return Long.toString(val);
        }
    }
}
