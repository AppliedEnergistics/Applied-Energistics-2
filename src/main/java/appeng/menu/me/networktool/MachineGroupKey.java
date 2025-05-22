package appeng.menu.me.networktool;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.stacks.AEItemKey;

/**
 * The information by which machines are grouped together.
 */
record MachineGroupKey(AEItemKey display, boolean missingChannel) {

    public static MachineGroupKey fromPacket(FriendlyByteBuf data) {
        var display = AEItemKey.fromPacket(data);
        var missingChannel = data.readBoolean();
        return new MachineGroupKey(display, missingChannel);

    }

    public void write(FriendlyByteBuf data) {
        display.writeToPacket(data);
        data.writeBoolean(missingChannel);
    }

}
