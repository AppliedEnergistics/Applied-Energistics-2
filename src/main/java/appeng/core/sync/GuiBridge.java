package appeng.core.sync;

import static appeng.core.sync.GuiHostType.ITEM;
import static appeng.core.sync.GuiHostType.ITEM_OR_WORLD;
import static appeng.core.sync.GuiHostType.WORLD;

import java.lang.reflect.Constructor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.definitions.Materials;
import appeng.api.exceptions.AppEngException;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.GuiNull;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.implementations.ContainerChest;
import appeng.container.implementations.ContainerCondenser;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerDrive;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.container.implementations.ContainerGrinder;
import appeng.container.implementations.ContainerIOPort;
import appeng.container.implementations.ContainerInscriber;
import appeng.container.implementations.ContainerInterface;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.container.implementations.ContainerMAC;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.implementations.ContainerMEPortableCell;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPriority;
import appeng.container.implementations.ContainerQNB;
import appeng.container.implementations.ContainerQuartzKnife;
import appeng.container.implementations.ContainerSecurity;
import appeng.container.implementations.ContainerSkyChest;
import appeng.container.implementations.ContainerSpatialIOPort;
import appeng.container.implementations.ContainerStorageBus;
import appeng.container.implementations.ContainerTeleporter;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.implementations.ContainerVibrationChamber;
import appeng.container.implementations.ContainerWireless;
import appeng.container.implementations.ContainerWirelessTerm;
import appeng.core.stats.Achievements;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.contents.QuartzKnifeObj;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.automation.PartLevelEmitter;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartMonitor;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.crafting.TileCraftingTile;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.misc.TileCellWorkbench;
import appeng.tile.misc.TileCondenser;
import appeng.tile.misc.TileInscriber;
import appeng.tile.misc.TileSecurity;
import appeng.tile.misc.TileTeleporter;
import appeng.tile.misc.TileVibrationChamber;
import appeng.tile.networking.TileWireless;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;
import appeng.tile.storage.TileIOPort;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;

public enum GuiBridge implements IGuiHandler
{
	GUI_Handler(),

	GUI_GRINDER(ContainerGrinder.class, TileGrinder.class, WORLD, null),

	GUI_QNB(ContainerQNB.class, TileQuantumBridge.class, WORLD, SecurityPermissions.BUILD),

	GUI_SKYCHEST(ContainerSkyChest.class, TileSkyChest.class, WORLD, null),

	GUI_CHEST(ContainerChest.class, TileChest.class, WORLD, SecurityPermissions.BUILD),

	GUI_WIRELESS(ContainerWireless.class, TileWireless.class, WORLD, SecurityPermissions.BUILD),

	GUI_ME(ContainerMEMonitorable.class, ITerminalHost.class, WORLD, null),

	GUI_PORTABLE_CELL(ContainerMEPortableCell.class, IPortableCell.class, ITEM, null),

	GUI_WIRELESS_TERM(ContainerWirelessTerm.class, WirelessTerminalGuiObject.class, ITEM, null),

	GUI_NETWORK_STATUS(ContainerNetworkStatus.class, INetworkTool.class, ITEM, null),

	GUI_CRAFTING_CPU(ContainerCraftingCPU.class, TileCraftingTile.class, WORLD, SecurityPermissions.CRAFT),

	GUI_NETWORK_TOOL(ContainerNetworkTool.class, INetworkTool.class, ITEM, null),

	GUI_QUARTZ_KNIFE(ContainerQuartzKnife.class, QuartzKnifeObj.class, ITEM, null),

	GUI_DRIVE(ContainerDrive.class, TileDrive.class, WORLD, SecurityPermissions.BUILD),

	GUI_VIBRATION_CHAMBER(ContainerVibrationChamber.class, TileVibrationChamber.class, WORLD, null),

	GUI_CONDENSER(ContainerCondenser.class, TileCondenser.class, WORLD, null),

	GUI_INTERFACE(ContainerInterface.class, IInterfaceHost.class, WORLD, SecurityPermissions.BUILD),

	GUI_BUS(ContainerUpgradeable.class, IUpgradeableHost.class, WORLD, SecurityPermissions.BUILD),

	GUI_IOPORT(ContainerIOPort.class, TileIOPort.class, WORLD, SecurityPermissions.BUILD),

	GUI_STORAGEBUS(ContainerStorageBus.class, PartStorageBus.class, WORLD, SecurityPermissions.BUILD),

	GUI_FORMATION_PLANE(ContainerFormationPlane.class, PartFormationPlane.class, WORLD, SecurityPermissions.BUILD),

	GUI_PRIORITY(ContainerPriority.class, IPriorityHost.class, WORLD, SecurityPermissions.BUILD),

	GUI_SECURITY(ContainerSecurity.class, TileSecurity.class, WORLD, SecurityPermissions.SECURITY),

	GUI_CRAFTING_TERMINAL(ContainerCraftingTerm.class, PartCraftingTerminal.class, WORLD, SecurityPermissions.CRAFT),

	GUI_PATTERN_TERMINAL(ContainerPatternTerm.class, PartPatternTerminal.class, WORLD, SecurityPermissions.CRAFT),

	// extends (Container/Gui) + Bus
	GUI_LEVEL_EMITTER(ContainerLevelEmitter.class, PartLevelEmitter.class, WORLD, SecurityPermissions.BUILD),

	GUI_SPATIAL_IO_PORT(ContainerSpatialIOPort.class, TileSpatialIOPort.class, WORLD, SecurityPermissions.BUILD),

	GUI_INSCRIBER(ContainerInscriber.class, TileInscriber.class, WORLD, null),

	GUI_CELL_WORKBENCH(ContainerCellWorkbench.class, TileCellWorkbench.class, WORLD, null),

	GUI_MAC(ContainerMAC.class, TileMolecularAssembler.class, WORLD, null),

	GUI_CRAFTING_AMOUNT(ContainerCraftAmount.class, ITerminalHost.class, ITEM_OR_WORLD, SecurityPermissions.CRAFT),

	GUI_CRAFTING_CONFIRM(ContainerCraftConfirm.class, ITerminalHost.class, ITEM_OR_WORLD, SecurityPermissions.CRAFT),

	GUI_INTERFACE_TERMINAL(ContainerInterfaceTerminal.class, PartMonitor.class, WORLD, SecurityPermissions.BUILD),

	GUI_CRAFTING_STATUS(ContainerCraftingStatus.class, ITerminalHost.class, ITEM_OR_WORLD, SecurityPermissions.CRAFT), 

	GUI_TELEPORTER(ContainerTeleporter.class, TileTeleporter.class, WORLD, SecurityPermissions.BUILD);

	private final Class Tile;
	private Class Gui;
	private final Class Container;
	private GuiHostType type;
	private SecurityPermissions requiredPermission;

	private GuiBridge() {
		Tile = null;
		Gui = null;
		Container = null;
	}

	/**
	 * I honestly wish I could just use the GuiClass Names myself, but I can't access them without MC's Server
	 * Exploding.
	 */
	private void getGui()
	{
		if ( Platform.isClient() )
		{
			String start = Container.getName();
			String GuiClass = start.replaceFirst( "container.", "client.gui." ).replace( ".Container", ".Gui" );
			if ( start.equals( GuiClass ) )
				throw new RuntimeException( "Unable to find gui class" );
			Gui = ReflectionHelper.getClass( this.getClass().getClassLoader(), GuiClass );
			if ( Gui == null )
				throw new RuntimeException( "Cannot Load class: " + GuiClass );
		}
	}

	private GuiBridge(Class _Container, SecurityPermissions requiredPermission) {
		this.requiredPermission = requiredPermission;
		Container = _Container;
		Tile = null;
		getGui();
	}

	private GuiBridge(Class _Container, Class _Tile, GuiHostType type, SecurityPermissions requiredPermission) {
		this.requiredPermission = requiredPermission;
		Container = _Container;
		this.type = type;
		Tile = _Tile;
		getGui();
	}

	public boolean CorrectTileOrPart(Object tE)
	{
		if ( Tile == null )
			throw new RuntimeException( "This Gui Cannot use the standard Handler." );

		return Tile.isInstance( tE );
	}

	public Object ConstructContainer(InventoryPlayer inventory, ForgeDirection side, Object tE)
	{
		try
		{
			Constructor[] c = Container.getConstructors();
			if ( c.length == 0 )
				throw new AppEngException( "Invalid Gui Class" );

			Constructor target = findConstructor( c, inventory, tE );

			if ( target == null )
			{
				throw new RuntimeException( "Cannot find " + Container.getName() + "( " + typeName( inventory ) + ", " + typeName( tE ) + " )" );
			}

			Object o = target.newInstance( inventory, tE );

			/**
			 * triggers achievement when the player sees presses.
			 */
			if ( o instanceof AEBaseContainer )
			{
				AEBaseContainer bc = (AEBaseContainer) o;
				for (Object so : bc.inventorySlots)
				{
					if ( so instanceof Slot )
					{
						ItemStack is = ((Slot) so).getStack();

						Materials m = AEApi.instance().materials();
						if ( m.materialLogicProcessorPress.sameAsStack( is ) || m.materialEngProcessorPress.sameAsStack( is )
								|| m.materialCalcProcessorPress.sameAsStack( is ) || m.materialSiliconPress.sameAsStack( is ) )
						{
							Achievements.Presses.addToPlayer( inventory.player );
						}
					}
				}
			}

			return o;
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	public Object ConstructGui(InventoryPlayer inventory, ForgeDirection side, Object tE)
	{
		try
		{
			Constructor[] c = Gui.getConstructors();
			if ( c.length == 0 )
				throw new AppEngException( "Invalid Gui Class" );

			Constructor target = findConstructor( c, inventory, tE );

			if ( target == null )
			{
				throw new RuntimeException( "Cannot find " + Container.getName() + "( " + typeName( inventory ) + ", " + typeName( tE ) + " )" );
			}

			return target.newInstance( inventory, tE );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	private String typeName(Object inventory)
	{
		if ( inventory == null )
			return "NULL";

		return inventory.getClass().getName();
	}

	private Constructor findConstructor(Constructor[] c, InventoryPlayer inventory, Object tE)
	{
		for (Constructor con : c)
		{
			Class[] types = con.getParameterTypes();
			if ( types.length == 2 )
			{
				if ( types[0].isAssignableFrom( inventory.getClass() ) && types[1].isAssignableFrom( tE.getClass() ) )
					return con;
			}
		}
		return null;
	}

	private Object updateGui(Object newContainer, World w, int x, int y, int z, ForgeDirection side, Object myItem)
	{
		if ( newContainer instanceof AEBaseContainer )
		{
			AEBaseContainer bc = (AEBaseContainer) newContainer;
			bc.openContext = new ContainerOpenContext( myItem );
			bc.openContext.w = w;
			bc.openContext.x = x;
			bc.openContext.y = y;
			bc.openContext.z = z;
			bc.openContext.side = side;
		}

		return newContainer;
	}

	@Override
	public Object getServerGuiElement(int ID_ORDINAL, EntityPlayer player, World w, int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.getOrientation( ID_ORDINAL & 0x07 );
		GuiBridge ID = values()[ID_ORDINAL >> 4];
		boolean stem = ((ID_ORDINAL >> 3) & 1) == 1;

		if ( ID.type.isItem() && stem )
		{
			ItemStack it = player.inventory.getCurrentItem();
			Object myItem = getGuiObject( it, player, w, x, y, z );
			if ( myItem != null && ID.CorrectTileOrPart( myItem ) )
				return updateGui( ID.ConstructContainer( player.inventory, side, myItem ), w, x, y, z, side, myItem );
		}

		if ( ID.type.isTile() )
		{
			TileEntity TE = w.getTileEntity( x, y, z );
			if ( TE instanceof IPartHost )
			{
				((IPartHost) TE).getPart( side );
				IPart part = ((IPartHost) TE).getPart( side );
				if ( ID.CorrectTileOrPart( part ) )
					return updateGui( ID.ConstructContainer( player.inventory, side, part ), w, x, y, z, side, part );
			}
			else
			{
				if ( ID.CorrectTileOrPart( TE ) )
					return updateGui( ID.ConstructContainer( player.inventory, side, TE ), w, x, y, z, side, TE );
			}
		}

		return new ContainerNull();
	}

	private Object getGuiObject(ItemStack it, EntityPlayer player, World w, int x, int y, int z)
	{
		if ( it != null )
		{
			if ( it.getItem() instanceof IGuiItem )
			{
				return ((IGuiItem) it.getItem()).getGuiObject( it, w, x, y, z );
			}

			IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler( it );
			if ( wh != null )
				return new WirelessTerminalGuiObject( wh, it, player, w, x, y, z );
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID_ORDINAL, EntityPlayer player, World w, int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.getOrientation( ID_ORDINAL & 0x07 );
		GuiBridge ID = values()[ID_ORDINAL >> 4];
		boolean stem = ((ID_ORDINAL >> 3) & 1) == 1;

		if ( ID.type.isItem() && stem )
		{
			ItemStack it = player.inventory.getCurrentItem();
			Object myItem = getGuiObject( it, player, w, x, y, z );
			if ( ID.CorrectTileOrPart( myItem ) )
				return ID.ConstructGui( player.inventory, side, myItem );
		}

		if ( ID.type.isTile() )
		{
			TileEntity TE = w.getTileEntity( x, y, z );

			if ( TE instanceof IPartHost )
			{
				((IPartHost) TE).getPart( side );
				IPart part = ((IPartHost) TE).getPart( side );
				if ( ID.CorrectTileOrPart( part ) )
					return ID.ConstructGui( player.inventory, side, part );
			}
			else
			{
				if ( ID.CorrectTileOrPart( TE ) )
					return ID.ConstructGui( player.inventory, side, TE );
			}
		}

		return new GuiNull( new ContainerNull() );
	}

	public boolean hasPermissions(TileEntity te, int x, int y, int z, ForgeDirection side, EntityPlayer player)
	{
		World w = player.getEntityWorld();

		if ( Platform.hasPermissions( te != null ? new DimensionalCoord( te ) : new DimensionalCoord( player.worldObj, x, y, z ), player ) )
		{
			if ( type.isItem() )
			{
				ItemStack it = player.inventory.getCurrentItem();
				if ( it != null && it.getItem() instanceof IGuiItem )
				{
					Object myItem = ((IGuiItem) it.getItem()).getGuiObject( it, w, x, y, z );
					if ( CorrectTileOrPart( myItem ) )
					{
						return true;
					}
				}
			}

			if ( type.isTile() )
			{
				TileEntity TE = w.getTileEntity( x, y, z );
				if ( TE instanceof IPartHost )
				{
					((IPartHost) TE).getPart( side );
					IPart part = ((IPartHost) TE).getPart( side );
					if ( CorrectTileOrPart( part ) )
						return securityCheck( part, player );
				}
				else
				{
					if ( CorrectTileOrPart( TE ) )
						return securityCheck( TE, player );
				}
			}
		}
		return false;
	}

	private boolean securityCheck(Object te, EntityPlayer player)
	{
		if ( te instanceof IActionHost && requiredPermission != null )
		{
			boolean requirePower = false;

			IGridNode gn = ((IActionHost) te).getActionableNode();
			if ( gn != null )
			{
				IGrid g = gn.getGrid();
				if ( g != null )
				{
					if ( requirePower )
					{
						IEnergyGrid eg = g.getCache( IEnergyGrid.class );
						if ( !eg.isNetworkPowered() )
						{
							return false;
						}
					}

					ISecurityGrid sg = g.getCache( ISecurityGrid.class );
					if ( sg.hasPermission( player, requiredPermission ) )
						return true;
				}
			}

			return false;
		}
		return true;
	}

	public GuiHostType getType()
	{
		return type;
	}

}
