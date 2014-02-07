package appeng.core.localization;

import net.minecraft.util.ChatMessageComponent;

public enum PlayerMessages
{
	ChestCannotReadStorageCell, InvalidMachine, LoadedSettings, SavedSettings, MachineNotPowered,

	isNowLocked, isNowUnlocked, AmmoDepleted, CommunicationError, OutOfRange, DeviceNotPowered;

	String getName()
	{
		return "chat.appliedenergistics2." + toString();
	}

	public ChatMessageComponent get()
	{
		return ChatMessageComponent.createFromTranslationKey( getName() );
	}

}
