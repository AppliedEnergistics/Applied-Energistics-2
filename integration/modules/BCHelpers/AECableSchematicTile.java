package appeng.integration.modules.BCHelpers;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.parts.CableBusContainer;
import buildcraft.api.blueprints.IBuilderContext;

public class AECableSchematicTile extends AEGenericSchematicTile implements IPartHost
{

	@Override
	public void rotateLeft(IBuilderContext context)
	{
		CableBusContainer cbc = new CableBusContainer( this );
		cbc.readFromNBT( tileNBT );

		cbc.rotateLeft();

		tileNBT = new NBTTagCompound();
		cbc.writeToNBT( tileNBT );
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		return null;
	}

	@Override
	public boolean canAddPart(ItemStack part, ForgeDirection side)
	{
		return false;
	}

	@Override
	public ForgeDirection addPart(ItemStack is, ForgeDirection side, EntityPlayer owner)
	{
		return null;
	}

	@Override
	public IPart getPart(ForgeDirection side)
	{
		return null;
	}

	@Override
	public void removePart(ForgeDirection side, boolean suppressUpdate)
	{

	}

	@Override
	public void markForUpdate()
	{

	}

	@Override
	public DimensionalCoord getLocation()
	{
		return null;
	}

	@Override
	public TileEntity getTile()
	{
		return null;
	}

	@Override
	public AEColor getColor()
	{
		return null;
	}

	@Override
	public void clearContainer()
	{

	}

	@Override
	public boolean isBlocked(ForgeDirection side)
	{
		return false;
	}

	@Override
	public SelectedPart selectPart(Vec3 pos)
	{
		return null;
	}

	@Override
	public void markForSave()
	{

	}

	@Override
	public void partChanged()
	{

	}

	@Override
	public boolean hasRedstone(ForgeDirection side)
	{
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public Set<LayerFlags> getLayerFlags()
	{
		return null;
	}

	@Override
	public void cleanup()
	{

	}

	@Override
	public void notifyNeighbors()
	{

	}

	@Override
	public boolean isInWorld()
	{
		return false;
	}
}
