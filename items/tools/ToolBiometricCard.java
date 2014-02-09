package appeng.items.tools;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IBiometricCard;
import appeng.client.render.items.ToolBiometricCardRender;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ToolBiometricCard extends AEBaseItem implements IBiometricCard
{

	public ToolBiometricCard() {
		super( ToolBiometricCard.class );
		setfeature( EnumSet.of( AEFeature.Security ) );
		setMaxStackSize( 1 );
		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this, new ToolBiometricCardRender() );
	}

	@Override
	public String getItemStackDisplayName(ItemStack is)
	{
		String username = getUsername( is );
		return username.length() > 0 ? super.getItemStackDisplayName( is ) + " - " + username : super.getItemStackDisplayName( is );
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
		String username = getUsername( is );
		if ( p.getCommandSenderName().equals( username ) )
			setUsername( is, "" );
		else
			setUsername( is, p.getCommandSenderName() );
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer p, List l, boolean b)
	{
		// String username = getUsername( is );
		// if ( username.length() > 0 )
		// l.add( username );

		EnumSet<SecurityPermissions> perms = getPermissions( is );
		if ( perms.isEmpty() )
			l.add( GuiText.NoPermissions.getLocal() );
		else
		{
			String msg = null;

			for (SecurityPermissions sp : perms)
			{
				if ( msg == null )
					msg = Platform.gui_localize( sp.getUnlocalizedName() );
				else
					msg = msg + ", " + Platform.gui_localize( sp.getUnlocalizedName() );
			}
			l.add( msg );
		}

	}

	@Override
	public String getUsername(ItemStack is)
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		return tag.getString( "username" );
	}

	@Override
	public EnumSet<SecurityPermissions> getPermissions(ItemStack is)
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		EnumSet<SecurityPermissions> result = EnumSet.noneOf( SecurityPermissions.class );

		for (SecurityPermissions sp : SecurityPermissions.values())
		{
			if ( tag.getBoolean( sp.name() ) )
				result.add( sp );
		}

		return result;
	}

	@Override
	public boolean hasPermission(ItemStack is, SecurityPermissions permission)
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		return tag.getBoolean( permission.name() );
	}

	@Override
	public void setUsername(ItemStack itemStack, String username)
	{
		NBTTagCompound tag = Platform.openNbtData( itemStack );
		tag.setString( "username", username );
	}

	@Override
	public void removePermission(ItemStack itemStack, SecurityPermissions permission)
	{
		NBTTagCompound tag = Platform.openNbtData( itemStack );
		if ( tag.hasKey( permission.name() ) )
			tag.removeTag( permission.name() );
	}

	@Override
	public void addPermission(ItemStack itemStack, SecurityPermissions permission)
	{
		NBTTagCompound tag = Platform.openNbtData( itemStack );
		tag.setBoolean( permission.name(), true );
	}

}
