package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import appeng.api.config.FuzzyMode;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPriority;
import appeng.container.implementations.ContainerQuartzKnife;
import appeng.container.implementations.ContainerSecurity;
import appeng.container.implementations.ContainerStorageBus;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IMouseWheelItem;

public class PacketValueConfig extends AppEngPacket
{

	final public String Name;
	final public String Value;

	// automatic.
	public PacketValueConfig(ByteBuf stream) throws IOException {
		DataInputStream dis = new DataInputStream( new ByteArrayInputStream( stream.array(), stream.readerIndex(), stream.readableBytes() ) );
		Name = dis.readUTF();
		Value = dis.readUTF();
		// dis.close();
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;

		if ( Name.equals( "Item" ) && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IMouseWheelItem )
		{
			ItemStack is = player.getHeldItem();
			IMouseWheelItem si = (IMouseWheelItem) is.getItem();
			si.onWheel( is, Value.equals( "WheelUp" ) );
			return;
		}
		else if ( Name.equals( "Terminal.Cpu" ) && c instanceof ContainerCraftConfirm )
		{
			ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
			qk.cycleCpu( Value.equals( "Next" ) );
			return;
		}
		else if ( Name.equals( "Terminal.Start" ) && c instanceof ContainerCraftConfirm )
		{
			ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
			qk.startJob();
			return;
		}
		else if ( Name.equals( "TileCrafting.Cancel" ) && c instanceof ContainerCraftingCPU )
		{
			ContainerCraftingCPU qk = (ContainerCraftingCPU) c;
			qk.cancelCrafting();
			return;
		}
		else if ( Name.equals( "QuartzKnife.Name" ) && c instanceof ContainerQuartzKnife )
		{
			ContainerQuartzKnife qk = (ContainerQuartzKnife) c;
			qk.setName( Value );
			return;
		}
		else if ( Name.equals( "TileSecurity.ToggleOption" ) && c instanceof ContainerSecurity )
		{
			ContainerSecurity sc = (ContainerSecurity) c;
			sc.toggleSetting( Value, player );
			return;
		}
		else if ( Name.equals( "PriorityHost.Priority" ) && c instanceof ContainerPriority )
		{
			ContainerPriority pc = (ContainerPriority) c;
			pc.setPriority( Integer.parseInt( Value ), player );
			return;
		}
		else if ( Name.equals( "LevelEmitter.Value" ) && c instanceof ContainerLevelEmitter )
		{
			ContainerLevelEmitter lvc = (ContainerLevelEmitter) c;
			lvc.setLevel( Long.parseLong( Value ), player );
			return;
		}
		else if ( Name.startsWith( "PatternTerminal." ) && c instanceof ContainerPatternTerm )
		{
			ContainerPatternTerm cpt = (ContainerPatternTerm) c;
			if ( Name.equals( "PatternTerminal.CraftMode" ) )
			{
				cpt.ct.setCraftingRecipe( Value.equals( "1" ) );
			}
			else if ( Name.equals( "PatternTerminal.Encode" ) )
			{
				cpt.encode();
			}
			else if ( Name.equals( "PatternTerminal.Clear" ) )
			{
				cpt.clear();
			}
		}
		else if ( Name.startsWith( "StorageBus." ) && c instanceof ContainerStorageBus )
		{
			ContainerStorageBus ccw = (ContainerStorageBus) c;
			if ( Name.equals( "StorageBus.Action" ) )
			{
				if ( Value.equals( "Partition" ) )
				{
					ccw.partition();
				}
				else if ( Value.equals( "Clear" ) )
				{
					ccw.clear();
				}
			}
		}
		else if ( Name.startsWith( "CellWorkbench." ) && c instanceof ContainerCellWorkbench )
		{
			ContainerCellWorkbench ccw = (ContainerCellWorkbench) c;
			if ( Name.equals( "CellWorkbench.Action" ) )
			{
				if ( Value.equals( "CopyMode" ) )
				{
					ccw.nextCopyMode();
				}
				else if ( Value.equals( "Partition" ) )
				{
					ccw.partition();
				}
				else if ( Value.equals( "Clear" ) )
				{
					ccw.clear();
				}
			}
			else if ( Name.equals( "CellWorkbench.Fuzzy" ) )
			{
				ccw.setFuzzy( FuzzyMode.valueOf( Value ) );
			}
		}
		else if ( c instanceof ContainerNetworkTool )
		{
			if ( Name.equals( "NetworkTool" ) && Value.equals( "Toggle" ) )
			{
				((ContainerNetworkTool) c).toggleFacadeMode();
			}
		}
		else if ( c instanceof IConfigureableObject )
		{
			IConfigManager cm = ((IConfigureableObject) c).getConfigManager();

			for (Enum e : cm.getSettings())
			{
				if ( e.name().equals( Name ) )
				{
					Enum def = cm.getSetting( e );

					try
					{
						cm.putSetting( e, Enum.valueOf( def.getClass(), Value ) );
					}
					catch (IllegalArgumentException err)
					{
						// :P
					}

					break;
				}
			}
		}

	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;

		if ( Name.equals( "CustomName" ) && c instanceof AEBaseContainer )
		{
			((AEBaseContainer) c).customName = Value;
		}
		else if ( Name.startsWith( "SyncDat." ) )
		{
			((AEBaseContainer) c).stringSync( Integer.parseInt( Name.substring( 8 ) ), Value );
		}
		else if ( c instanceof IConfigureableObject )
		{
			IConfigManager cm = ((IConfigureableObject) c).getConfigManager();

			for (Enum e : cm.getSettings())
			{
				if ( e.name().equals( Name ) )
				{
					Enum def = cm.getSetting( e );

					try
					{
						cm.putSetting( e, Enum.valueOf( def.getClass(), Value ) );
					}
					catch (IllegalArgumentException err)
					{
						// :P
					}

					break;
				}
			}
		}

	}

	// api
	public PacketValueConfig(String Name, String Value) throws IOException {
		this.Name = Name;
		this.Value = Value;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( bos );
		dos.writeUTF( Name );
		dos.writeUTF( Value );
		// dos.close();

		data.writeBytes( bos.toByteArray() );

		configureWrite( data );
	}
}
