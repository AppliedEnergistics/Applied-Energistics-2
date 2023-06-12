---
navigation:
  parent: example-setups/example-setups-index.md
  title: Amethyst Farm
  icon: minecraft:amethyst_shard
---

# Farming of Amethyst

While the <ItemLink id="growth_accelerator" /> works on amethyst, the usual methods of filtering [certus buds](../items-blocks-machines/budding_certus.md)
with an <ItemLink id="annihilation_plane" /> do not work on amethyst buds. Unlike non-mature certus buds which drop
<ItemLink id="certus_quartz_dust" />, non-mature amethyst buds drop nothing, so an annihilation plane will always break them
because a network can always store "nothing".

The way around this is to enchant the annihilation plane with silk touch. Then the non-mature amethyst buds *do* drop something
(the various stages of the physical bud blocks), and thus can be filtered.

The <ItemLink id="minecraft:amethyst_cluster" /> must then be placed again by a <ItemLink id="formation_plane" />, to then be
re-broken by an <ItemLink id="annihilation_plane" /> without silk touch, in order to get <ItemLink id="minecraft:amethyst_shard" />s.

Note that due to the directionality of the cluster, there must be a solid block face directly opposite of the formation plane.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/amethyst_farm.snbt" />

  <BoxAnnotation color="#dddddd" min="2.7 1 1" max="3 2 2">
        (1) Annihilation Plane #1: No GUI to configure, but enchanted with Silk Touch.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 1 1" max="2.3 2 2">
        (2) Formation Plane: Filtered to Amethyst Cluster.
        <ItemImage id="minecraft:amethyst_cluster" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1.3 0.7 1" max="2 1 2">
        (3) Annihilation Plane #2: No GUI to configure, but can be enchanted with Fortune.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 0 1" max="1.3 1 2">
        (4) Storage Bus #1: Filtered to Amethyst Shard.
        <ItemImage id="minecraft:amethyst_shard" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 .7" max="1 1 1">
        (5) Storage Bus #2: Filtered to Amethyst Shard. Has priority set higher than your main storage.
        <ItemImage id="minecraft:amethyst_shard" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

* The first <ItemLink id="annihilation_plane" /> (1) has no GUI and cannot be configured, but must be enchanted with silk touch.
* The <ItemLink id="formation_plane" /> (2) is filtered to <ItemLink id="minecraft:amethyst_cluster" />.
* The second <ItemLink id="annihilation_plane" /> (3) has no GUI and cannot be configured, but can be enchanted with fortune.
* The first <ItemLink id="storage_bus" /> (4) is filtered to <ItemLink id="minecraft:amethyst_shard" />.
* The second <ItemLink id="storage_bus" /> (5) is filtered to <ItemLink id="minecraft:amethyst_shard" />, and has its
  [priority](../ae2-mechanics/import-export-storage.md#storage-priority) set higher than your main storage.

## How It Works

1. The first <ItemLink id="annihilation_plane" /> attempts to break what is in front of it, but can only break <ItemLink id="minecraft:amethyst_cluster" />
    because the only storage on the subnet is the <ItemLink id="formation_plane" />, filtered to amethyst cluster. This only works because
the plane is enchanted with silk touch, otherwise it would be able to break the non-mature buds because they drop nothing.
2. The <ItemLink id="formation_plane" /> places the cluster on the block opposing it.
3. The second <ItemLink id="annihilation_plane" /> breaks the cluster, producing <ItemLink id="minecraft:amethyst_shard" />.
4. The first <ItemLink id="storage_bus" /> stores the shards in the barrel. This technically doesn't need to be filtered because the only
thing the second annihilation plane should be encountering is fully-grown clusters.
5. The second <ItemLink id="storage_bus" /> gives the main network access to all of the amethyst shards in the barrel. It is set to
high [priority](../ae2-mechanics/import-export-storage.md#storage-priority) so that amethyst shards are preferentially
put back in the barrel instead of in your main storage.
