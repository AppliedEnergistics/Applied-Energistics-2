package appeng.core.localization;

import net.minecraft.util.ChatMessageComponent;
import appeng.core.AELog;

public enum PlayerMessages
{
	ChestCannotReadStorageCell, InvalidMachine, LoadedSettings, SavedSettings, MachineNotPowered, isNowLocked, isNowUnlocked, AmmoDepleted;

	private PlayerMessages() {
		AELog.localization( "chat", getName() );
	}

	String getName()
	{
		return "chat.appliedenergistics2." + toString();
	}

	public ChatMessageComponent get()
	{
		return ChatMessageComponent.createFromTranslationKey( getName() );
	}

}
