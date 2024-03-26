package appeng.menu.me.networktool;

import net.minecraft.network.RegistryFriendlyByteBuf;

import appeng.api.stacks.AEItemKey;

/**
 * The information by which machines are grouped together.
 */
record MachineGroupKey(AEItemKey display, boolean missingChannel) {

    public static MachineGroupKey fromPacket(RegistryFriendlyByteBuf data) {
        var display = AEItemKey.fromPacket(data);
        var missingChannel = data.readBoolean();
        return new MachineGroupKey(display, missingChannel);

    }

    public void write(RegistryFriendlyByteBuf data) {
        display.writeToPacket(data);
        data.writeBoolean(missingChannel);
    }

}
