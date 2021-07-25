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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public class AppEngAdvancementTrigger
        implements CriterionTrigger<AppEngAdvancementTrigger.Instance>, IAdvancementTrigger {
    private final ResourceLocation id;
    private final Map<PlayerAdvancements, AppEngAdvancementTrigger.Listeners> listeners = new HashMap<>();

    public AppEngAdvancementTrigger(String id) {
        super();
        this.id = AppEng.makeId(id);
    }

    public Instance instance() {
        return new Instance(id);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements playerAdvancementsIn,
            CriterionTrigger.Listener<Instance> listener) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(playerAdvancementsIn);

        if (l == null) {
            l = new AppEngAdvancementTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, l);
        }

        l.add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements playerAdvancementsIn,
            CriterionTrigger.Listener<Instance> listener) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(playerAdvancementsIn);

        if (l != null) {
            l.remove(listener);

            if (l.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Instance createInstance(JsonObject object, DeserializationContext conditions) {
        return new AppEngAdvancementTrigger.Instance(this.getId());
    }

    @Override
    public void trigger(ServerPlayer parPlayer) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(parPlayer.getAdvancements());

        if (l != null) {
            l.trigger(parPlayer);
        }
    }

    public static class Instance implements CriterionTriggerInstance {
        private final ResourceLocation id;

        public Instance(ResourceLocation id) {
            this.id = id;
        }

        public boolean test() {
            return true;
        }

        @Override
        public ResourceLocation getCriterion() {
            return id;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext conditions) {
            return new JsonObject();
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<CriterionTrigger.Listener<Instance>> listeners = new HashSet<>();

        Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(CriterionTrigger.Listener<Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(CriterionTrigger.Listener<Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(Player player) {
            List<CriterionTrigger.Listener<Instance>> list = null;

            for (CriterionTrigger.Listener<Instance> listener : this.listeners) {
                if (listener.getTriggerInstance().test()) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (CriterionTrigger.Listener<Instance> l : list) {
                    l.run(this.playerAdvancements);
                }
            }
        }
    }
}
