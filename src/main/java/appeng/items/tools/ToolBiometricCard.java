/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.items.tools;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

import com.mojang.authlib.GameProfile;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.security.ISecurityRegistry;
import appeng.client.render.items.ToolBiometricCardRender;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public class ToolBiometricCard extends AEBaseItem implements IBiometricCard
{

	public ToolBiometricCard()
	{
		super( ToolBiometricCard.class );
		this.setFeature( EnumSet.of( AEFeature.Security ) );
		this.setMaxStackSize( 1 );
		if( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this, new ToolBiometricCardRender() );
	}

	@Override
	public ItemStack onItemRightClick( ItemStack is, World w, EntityPlayer p )
	{
		if( p.isSneaking() )
		{
			this.encode( is, p );
			p.swingItem();
			return is;
		}

		return is;
	}

	@Override
	public boolean itemInteractionForEntity( ItemStack is, EntityPlayer par2EntityPlayer, EntityLivingBase target )
	{
		if( target instanceof EntityPlayer && !par2EntityPlayer.isSneaking() )
		{
			if( par2EntityPlayer.capabilities.isCreativeMode )
				is = par2EntityPlayer.getCurrentEquippedItem();
			this.encode( is, (EntityPlayer) target );
			par2EntityPlayer.swingItem();
			return true;
		}
		return false;
	}

	@Override
	public String getItemStackDisplayName( ItemStack is )
	{
		GameProfile username = this.getProfile( is );
		return username != null ? super.getItemStackDisplayName( is ) + " - " + username.getName() : super.getItemStackDisplayName( is );
	}

	private void encode( ItemStack is, EntityPlayer p )
	{
		GameProfile username = this.getProfile( is );

		if( username != null && username.equals( p.getGameProfile() ) )
			this.setProfile( is, null );
		else
			this.setProfile( is, p.getGameProfile() );
	}

	@Override
	public void setProfile( ItemStack itemStack, GameProfile profile )
	{
		NBTTagCompound tag = Platform.openNbtData( itemStack );

		if( profile != null )
		{
			NBTTagCompound pNBT = new NBTTagCompound();
			NBTUtil.func_152460_a( pNBT, profile );
			tag.setTag( "profile", pNBT );
		}
		else
			tag.removeTag( "profile" );
	}

	@Override
	public GameProfile getProfile( ItemStack is )
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		if( tag.hasKey( "profile" ) )
			return NBTUtil.func_152459_a( tag.getCompoundTag( "profile" ) );
		return null;
	}

	@Override
	public EnumSet<SecurityPermissions> getPermissions( ItemStack is )
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		EnumSet<SecurityPermissions> result = EnumSet.noneOf( SecurityPermissions.class );

		for( SecurityPermissions sp : SecurityPermissions.values() )
		{
			if( tag.getBoolean( sp.name() ) )
				result.add( sp );
		}

		return result;
	}

	@Override
	public boolean hasPermission( ItemStack is, SecurityPermissions permission )
	{
		NBTTagCompound tag = Platform.openNbtData( is );
		return tag.getBoolean( permission.name() );
	}

	@Override
	public void removePermission( ItemStack itemStack, SecurityPermissions permission )
	{
		NBTTagCompound tag = Platform.openNbtData( itemStack );
		if( tag.hasKey( permission.name() ) )
			tag.removeTag( permission.name() );
	}

	@Override
	public void addPermission( ItemStack itemStack, SecurityPermissions permission )
	{
		NBTTagCompound tag = Platform.openNbtData( itemStack );
		tag.setBoolean( permission.name(), true );
	}

	@Override
	public void registerPermissions( ISecurityRegistry register, IPlayerRegistry pr, ItemStack is )
	{
		register.addPlayer( pr.getID( this.getProfile( is ) ), this.getPermissions( is ) );
	}

	@Override
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayAdditionalInformation )
	{
		EnumSet<SecurityPermissions> perms = this.getPermissions( stack );
		if( perms.isEmpty() )
			lines.add( GuiText.NoPermissions.getLocal() );
		else
		{
			String msg = null;

			for( SecurityPermissions sp : perms )
			{
				if( msg == null )
					msg = Platform.gui_localize( sp.getUnlocalizedName() );
				else
					msg = msg + ", " + Platform.gui_localize( sp.getUnlocalizedName() );
			}
			lines.add( msg );
		}
	}
}
