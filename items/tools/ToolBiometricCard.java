package appeng.items.tools;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import appeng.client.render.items.ToolBiometricCardRender;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ToolBiometricCard extends AEBaseItem
{

	public ToolBiometricCard() {
		super( ToolBiometricCard.class );
		setfeature( EnumSet.of( AEFeature.Security ) );
		setMaxStackSize( 1 );
		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( itemID, new ToolBiometricCardRender() );
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		String username = tag.getString( "username" );
		return username.length() > 0 ? super.getItemDisplayName( is ) + " - " + GuiText.Encoded.getLocal() : super.getItemDisplayName( is );
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack is, EntityPlayer par2EntityPlayer, EntityLivingBase target)
	{
		if ( target instanceof EntityPlayer && !par2EntityPlayer.isSneaking() )
		{
			if ( par2EntityPlayer.capabilities.isCreativeMode )
				is = par2EntityPlayer.getCurrentEquippedItem();
			encode( is, (EntityPlayer) target );
			par2EntityPlayer.swingItem();
			return true;
		}
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack is, World w, EntityPlayer p)
	{
		if ( p.isSneaking() )
		{
			encode( is, p );
			p.swingItem();
			return is;
		}

		return is;
	}

	private void encode(ItemStack is, EntityPlayer p)
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		String username = tag.getString( "username" );
		if ( p.username.equals( username ) )
			is.setTagCompound( null );
		else
			tag.setString( "username", p.username );
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer p, List l, boolean b)
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		String username = tag.getString( "username" );
		if ( username.length() > 0 )
			l.add( username );
	}

}
