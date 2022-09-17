/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.core.stats;


import appeng.core.AppEng;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.*;


public class AppEngAdvancementTrigger implements ICriterionTrigger<AppEngAdvancementTrigger.Instance>, IAdvancementTrigger {
    private final ResourceLocation ID;
    private final Map<PlayerAdvancements, AppEngAdvancementTrigger.Listeners> listeners = new HashMap<>();

    public AppEngAdvancementTrigger(String parString) {
        super();
        this.ID = new ResourceLocation(AppEng.MOD_ID, parString);
    }

    @Override
    public ResourceLocation getId() {
        return this.ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(playerAdvancementsIn);

        if (l == null) {
            l = new AppEngAdvancementTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, l);
        }

        l.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(playerAdvancementsIn);

        if (l != null) {
            l.remove(listener);

            if (l.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public AppEngAdvancementTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new AppEngAdvancementTrigger.Instance(this.getId());
    }

    @Override
    public void trigger(EntityPlayerMP parPlayer) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(parPlayer.getAdvancements());

        if (l != null) {
            l.trigger(parPlayer);
        }
    }

    public static class Instance extends AbstractCriterionInstance {
        public Instance(ResourceLocation parID) {
            super(parID);
        }

        public boolean test() {
            return true;
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance>> listeners = new HashSet<>();

        Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player) {
            List<ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener : this.listeners) {
                if (listener.getCriterionInstance().test()) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> l : list) {
                    l.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }
}
