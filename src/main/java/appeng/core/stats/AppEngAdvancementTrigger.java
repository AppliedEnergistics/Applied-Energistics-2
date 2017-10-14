
package appeng.core.stats;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;


public class AppEngAdvancementTrigger implements ICriterionTrigger<AppEngAdvancementTrigger.Instance>, IAdvancementTrigger
{
	private final ResourceLocation ID;
	private final Map<PlayerAdvancements, AppEngAdvancementTrigger.Listeners> listeners = new HashMap<>();

	public AppEngAdvancementTrigger( String parString )
	{
		super();
		ID = new ResourceLocation( parString );
	}

	public AppEngAdvancementTrigger( ResourceLocation parRL )
	{
		super();
		ID = parRL;
	}

	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	public void addListener( PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener )
	{
		AppEngAdvancementTrigger.Listeners l = this.listeners.get( playerAdvancementsIn );

		if( l == null )
		{
			l = new AppEngAdvancementTrigger.Listeners( playerAdvancementsIn );
			this.listeners.put( playerAdvancementsIn, l );
		}

		l.add( listener );
	}

	@Override
	public void removeListener( PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener )
	{
		AppEngAdvancementTrigger.Listeners l = this.listeners.get( playerAdvancementsIn );

		if( l != null )
		{
			l.remove( listener );

			if( l.isEmpty() )
			{
				this.listeners.remove( playerAdvancementsIn );
			}
		}
	}

	@Override
	public void removeAllListeners( PlayerAdvancements playerAdvancementsIn )
	{
		this.listeners.remove( playerAdvancementsIn );
	}

	@Override
	public AppEngAdvancementTrigger.Instance deserializeInstance( JsonObject json, JsonDeserializationContext context )
	{
		return new AppEngAdvancementTrigger.Instance( this.getId() );
	}

	public void trigger( EntityPlayerMP parPlayer )
	{
		AppEngAdvancementTrigger.Listeners l = this.listeners.get( parPlayer.getAdvancements() );

		if( l != null )
		{
			l.trigger( parPlayer );
		}
	}

	public static class Instance extends AbstractCriterionInstance
	{
		public Instance( ResourceLocation parID )
		{
			super( parID );
		}

		public boolean test()
		{
			return true;
		}
	}

	static class Listeners
	{
		private final PlayerAdvancements playerAdvancements;
		private final Set<ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance>> listeners = new HashSet<>();

		public Listeners( PlayerAdvancements playerAdvancementsIn )
		{
			this.playerAdvancements = playerAdvancementsIn;
		}

		public boolean isEmpty()
		{
			return this.listeners.isEmpty();
		}

		public void add( ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener )
		{
			this.listeners.add( listener );
		}

		public void remove( ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener )
		{
			this.listeners.remove( listener );
		}

		public void trigger( EntityPlayerMP player )
		{
			List<ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance>> list = null;

			for( ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> listener : this.listeners )
			{
				if( listener.getCriterionInstance().test() )
				{
					if( list == null )
					{
						list = new ArrayList<>();
					}

					list.add( listener );
				}
			}

			if( list != null )
			{
				for( ICriterionTrigger.Listener<AppEngAdvancementTrigger.Instance> l : list )
				{
					l.grantCriterion( this.playerAdvancements );
				}
			}
		}
	}
}
