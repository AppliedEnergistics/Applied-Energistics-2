package appeng.core.localization;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public enum PlayerMessages
{
	ChestCannotReadStorageCell, InvalidMachine, LoadedSettings, SavedSettings, MachineNotPowered,

	isNowLocked, isNowUnlocked, AmmoDepleted, CommunicationError, OutOfRange, DeviceNotPowered;

	String getName()
	{
		return "chat.appliedenergistics2." + toString();
	}

	public IChatComponent get()
	{
		return new ChatComponentTranslation( getName() );
	}

}
