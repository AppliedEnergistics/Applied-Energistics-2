---
navigation:
  parent: example-setups/example-setups-index.md
  title: Amethyst Farm
  icon: minecraft:amethyst_shard
---

# Farming of Amethyst

NEEDS ANNOTATON FUNCTIONALITY

<GameScene zoom="6">
  <ImportStructure src="../assets/assemblies/amethyst_farm.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

While the <ItemLink id="growth_accelerator" /> works on amethyst, the usual methods of filtering [certus buds](../items-blocks-machines/budding_certus.md)
with an <ItemLink id="annihilation_plane" /> do not work on amethyst buds. Unlike non-mature certus buds which drop
<ItemLink id="certus_quartz_dust" />, non-mature amethyst buds drop nothing, so an annihilation plane will always break them
because a network can always store "nothing".

The way around this is to enchant the annihilation plane with silk touch. Then the non-mature amethyst buds *do* drop something
(the various stages of the physical bud blocks), and thus can be filtered.

The <ItemLink id="minecraft:amethyst_cluster" /> must then be placed again by a <ItemLink id="formation_plane" />, to then be
re-broken by an <ItemLink id="annihilation_plane" /> without silk touch, in order to get <ItemLink id="minecraft:amethyst_shard" />s.

Note that due to the directionality of the cluster, there must be a solid block face directly opposite of the formation plane.