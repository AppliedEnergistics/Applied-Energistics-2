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

package appeng.container.implementations;

import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.client.gui.implementations.NumberEntryWidget;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.helpers.IPriorityHost;
import appeng.util.Platform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PriorityContainer extends AEBaseContainer {

    public static ContainerType<PriorityContainer> TYPE;

    private static final ContainerHelper<PriorityContainer, IPriorityHost> helper = new ContainerHelper<>(
            PriorityContainer::new, IPriorityHost.class, SecurityPermissions.BUILD);

    public static PriorityContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final IPriorityHost priHost;

    @OnlyIn(Dist.CLIENT)
    private NumberEntryWidget textField;

    @GuiSync(2)
    public long PriorityValue = -1;

    public PriorityContainer(int id, final PlayerInventory ip, final IPriorityHost te) {
        super(TYPE, id, ip, (TileEntity) (te instanceof TileEntity ? te : null),
                (IPart) (te instanceof IPart ? te : null));
        this.priHost = te;
    }

    @OnlyIn(Dist.CLIENT)
    public void setTextField(final NumberEntryWidget level) {
        this.textField = level;
        this.textField.setValue(this.PriorityValue, true);
    }

    public void setPriority(final int newValue, final PlayerEntity player) {
        this.priHost.setPriority(newValue);
        this.PriorityValue = newValue;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.PriorityValue = this.priHost.getPriority();
        }
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("PriorityValue")) {
            if (this.textField != null) {
                this.textField.setValue(this.PriorityValue, true);
            }
        }

        super.onUpdate(field, oldValue, newValue);
    }

    public IPriorityHost getPriorityHost() {
        return this.priHost;
    }
}
