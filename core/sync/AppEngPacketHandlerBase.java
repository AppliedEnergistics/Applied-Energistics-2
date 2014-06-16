package appeng.core.sync;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import appeng.core.sync.packets.PacketClick;
import appeng.core.sync.packets.PacketCompassRequest;
import appeng.core.sync.packets.PacketCompassResponse;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketCraftRequest;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketLightning;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketMatterCannon;
import appeng.core.sync.packets.PacketMockExplosion;
import appeng.core.sync.packets.PacketMultiPart;
import appeng.core.sync.packets.PacketNEIRecipe;
import appeng.core.sync.packets.PacketNewStorageDimension;
import appeng.core.sync.packets.PacketPartPlacement;
import appeng.core.sync.packets.PacketPartialItem;
import appeng.core.sync.packets.PacketPatternSlot;
import appeng.core.sync.packets.PacketProgressBar;
import appeng.core.sync.packets.PacketSwapSlots;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.core.sync.packets.PacketValueConfig;

public class AppEngPacketHandlerBase
{

	public static Map<Class, PacketTypes> reverseLookup = new HashMap<Class, AppEngPacketHandlerBase.PacketTypes>();

	public enum PacketTypes
	{
		PACKET_COMPASS_REQUEST(PacketCompassRequest.class),

		PACKET_COMPASS_RESPONSE(PacketCompassResponse.class),

		PACKET_INVENTORY_ACTION(PacketInventoryAction.class),

		PACKET_ME_INVENTORY_UPDATE(PacketMEInventoryUpdate.class),

		PACKET_CONFIG_BUTTON(PacketConfigButton.class),

		PACKET_MULTIPART(PacketMultiPart.class),

		PACKET_PARTPLACEMENT(PacketPartPlacement.class),

		PACKET_LIGHTNING(PacketLightning.class),

		PACKET_MATTERCANNON(PacketMatterCannon.class),

		PACKET_MOCKEXPLOSION(PacketMockExplosion.class),

		PACKET_VALUE_CONFIG(PacketValueConfig.class),

		PACKET_TRANSITION_EFFECT(PacketTransitionEffect.class),

		PACKET_PROGRESS_VALUE(PacketProgressBar.class),

		PACKET_CLICK(PacketClick.class),

		PACKET_NEW_STORAGE_DIMENSION(PacketNewStorageDimension.class),

		PACKET_SWITCH_GUIS(PacketSwitchGuis.class),

		PACKET_SWAP_SLOTS(PacketSwapSlots.class),

		PACKET_PATTERN_SLOT(PacketPatternSlot.class),

		PACKET_RECIPE_NEI(PacketNEIRecipe.class),

		PACKET_PARTIAL_ITEM(PacketPartialItem.class),

		PAKCET_CRAFTING_REQUEST(PacketCraftRequest.class);

		final public Class pc;
		final public Constructor con;

		private PacketTypes(Class c) {
			pc = c;

			Constructor x = null;
			try
			{
				x = pc.getConstructor( ByteBuf.class );
			}
			catch (NoSuchMethodException e)
			{
			}
			catch (SecurityException e)
			{
			}

			con = x;
			AppEngPacketHandlerBase.reverseLookup.put( pc, this );

			if ( con == null )
				throw new RuntimeException( "Invalid Packet Class, must be constructable on DataInputStream" );
		}

		public AppEngPacket parsePacket(ByteBuf in) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
		{
			return (AppEngPacket) con.newInstance( in );
		}

		public static PacketTypes getPacket(int id)
		{
			return (values())[id];
		}

		public static PacketTypes getID(Class<? extends AppEngPacket> c)
		{
			return AppEngPacketHandlerBase.reverseLookup.get( c );
		}

	};

}
