---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Annihilation Plane
  icon: annihilation_plane
  position: 210
categories:
- devices
item_ids:
- ae2:annihilation_plane
---

# The Annihilation Plane

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/blocks/annihilation_plane.snbt" />
</GameScene>

The Annihilation Plane breaks blocks and picks up items. It works similarly to an <ItemLink id="import_bus" />, pushing things
into [network storage](../ae2-mechanics/import-export-storage.md). For items to be picked up, they must collide with the
face of the plane, it does not pick up in an area.

Annihilation planes can be enchanted with any pickaxe enchantment, so yes, you can put crazy levels of fortune on a few and
[automate ore processing](../example-setups/ore-fortuner.md) if your modpack allows it. In addition, silk touch does what
you'd expect it to, efficiency reduces the energy cost of breaking a block, and unbreaking gives a chance of not using any energy.

They are [cable subparts](../ae2-mechanics/cable-subparts.md).

**REMEMBER TO ENABLE FAKE PLAYERS IN YOUR CHUNK CLAIM**

## Filtering

The annihilation plane will only break a block or pick up an item if it can store the resulting drops/items
in its network. this means to filter one, *you must restrict what can be stored on its network*, most likely by putting
it on a [subnetwork](../ae2-mechanics/subnetworks.md). A <ItemLink id="storage_bus" /> or [cell](../items-blocks-machines/storage_cells.md)
can be [partitioned](cell_workbench.md) to achieve this.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/annihilation_filtering.snbt" />

  <DiamondAnnotation pos="1 0.5 0.5" color="#00ff00">
        Filtered to whatever drops from the thing you want to break.
  </DiamondAnnotation>

  <DiamondAnnotation pos=".5 0.5 2.5" color="#00ff00">
        Partitioned to whatever drops from the thing you want to break.
  </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Again, it filters *by the item drops* so, for example, if you want to filter breaking of <ItemLink id="minecraft:amethyst_cluster" />s,
you need a plane enchanted with silk touch, otherwise every previous growth stage drops nothing and so the plane will break them no matter
what, as the network can always store "nothing".

## Recipe

<RecipeFor id="annihilation_plane" />
