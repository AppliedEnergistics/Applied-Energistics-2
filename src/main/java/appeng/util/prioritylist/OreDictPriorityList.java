package appeng.util.prioritylist;

import appeng.api.storage.data.IAEStack;
import appeng.util.item.AEItemStack;
import appeng.util.item.OreReference;

import java.util.ArrayList;
import java.util.Set;


public class OreDictPriorityList<T extends IAEStack<T>> implements IPartitionList<T>
{
    private final Set<Integer> oreIDs;
    private final String oreMatch;

    public OreDictPriorityList( Set<Integer> oreIDs, String oreMatch )
    {
        this.oreIDs = oreIDs;
        this.oreMatch = oreMatch;
    }

    @Override
    public boolean isListed( final T input )
    {
        OreReference or = ( (AEItemStack) input ).getOre().orElse( null );
        if( or != null )
        {
            for( Integer oreID : or.getOres() )
            {
                if( this.oreIDs.contains( oreID ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return oreMatch.equals( "" );
    }

    @Override
    public Iterable<T> getItems()
    {
        return new ArrayList<>();
    }

}
