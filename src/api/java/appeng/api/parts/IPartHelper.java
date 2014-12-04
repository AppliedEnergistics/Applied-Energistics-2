package appeng.api.parts;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IPartHelper
{

	/**
	 * Register a new layer with the part layer system, this allows you to write
	 * an in between between tile entities and parts.
	 * 
	 * AE By Default includes,
	 * 
	 * 1. ISidedInventory ( and by extension IInventory. )
	 * 
	 * 2. IFluidHandler Forge Fluids
	 * 
	 * 3. IPowerEmitter BC Power output.
	 * 
	 * 4. IPowerReceptor BC Power input.
	 * 
	 * 5. IEnergySink IC2 Power input.
	 * 
	 * 6. IEnergySource IC2 Power output.
	 * 
	 * 7. IPipeConnection BC Pipe Connections
	 * 
	 * As long as a valid layer is registered for a interface you can simply
	 * implement that interface on a part get implement it.
	 * 
	 * @return true on success, false on failure, usually a error will be logged
	 *         as well.
	 */
	boolean registerNewLayer(String string, String layerInterface);

	/**
	 * Register IBusItem with renderer
	 */
	void setItemBusRenderer(IPartItem i);

	/**
	 * use in use item, to try and place a IBusItem
	 * 
	 * @param is ItemStack of an item which implements {@link IPartItem}
	 * @param x x pos of part
	 * @param y y pos of part
	 * @param z z pos of part
	 * @param side side which the part should be on
	 * @param player player placing part
	 * @param world part in world
	 * @return true if placing was successful
	 */
	boolean placeBus(ItemStack is, int x, int y, int z, int side, EntityPlayer player, World world);

	/**
	 * @return the render mode
	 */
	CableRenderMode getCableRenderMode();
}
