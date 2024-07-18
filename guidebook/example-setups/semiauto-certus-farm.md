---
navigation:
  parent: example-setups/example-setups-index.md
  title: Semi-Auto Certus Farm
  icon: certus_quartz_crystal
  position: 115
---

# Semi-Auto Certus Farm

Unfortunately, the [simple certus farm](simple-certus-farm.md) requires a <ItemLink id="flawless_budding_quartz" /> to work fully
automatically. This requires either [Spatial IO](../ae2-mechanics/spatial-io.md) or building the farm at the [meteorite](../ae2-mechanics/meteorites.md).

However, AE2 can place and break blocks, so it might just
be possible to make your farm *replace the budding certus for you*. (You will have to periodically insert some
<ItemLink id="flawed_budding_quartz" /> into the input barrel and extract <ItemLink id="quartz_block" /> from the spent
buddng certus barrel)

To do this fully automatically, see [Advanced Certus Farm](advanced-certus-farm.md).

This farm is a bit more complex than the [simple certus farm](simple-certus-farm.md), because it is actually
3 separate setups crammmed together.

**THIS IS A COMPLEX BUILD WITH STUFF HIDDEN BEHIND OTHER STUFF, PAN AROUND TO VIEW IT FROM ALL ANGLES**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/semiauto_certus_farm.snbt" />

  <BoxAnnotation color="#ddaaaa" min="3.7 2 1" max="4 3 2">
        (1) Annihilation Plane #1: No GUI to configure, but can be enchanted with Fortune.
  </BoxAnnotation>

  <BoxAnnotation color="#ddaaaa" min="2 2 1" max="2.3 3 2">
        (2) Storage Bus #1: Filtered to Certus Quartz Crystal.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 2.5 1.5" color="#ff0000">
    Cluster Breaker Subnet
  </DiamondAnnotation>

  <BoxAnnotation color="#aaddaa" min="3.7 1 1" max="4 2 2">
        (3) Annihilation Plane #2: No GUI to configure, but enchanted with Silk Touch.
  </BoxAnnotation>

  <BoxAnnotation color="#aaddaa" min="2 1 1" max="2.3 2 2">
        (4) Storage Bus #2: Filtered to Certus Quartz Block.
        <BlockImage id="quartz_block" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 1.5 1.5" color="#00ff00">
    Certus Block Breaker Subnet
  </DiamondAnnotation>

  <BoxAnnotation color="#ffddaa" min="4 0.7 1" max="5 1 2">
        (5) Formation Plane: In its default configuration.
  </BoxAnnotation>

  <BoxAnnotation color="#ffddaa" min="2 0 1" max="2.3 1 2">
        (6) Import Bus: In its default configuration.
  </BoxAnnotation>

  <DiamondAnnotation pos="3 0.5 1.5" color="#ddcc00">
    Budding Block Placer Subnet
  </DiamondAnnotation>

  <BoxAnnotation color="#aaaadd" min="0.7 2 1" max="1 3 2">
        (7) Storage Bus #3: Filtered to Certus Quartz Crystal. Has priority set higher than your main storage.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

    <DiamondAnnotation pos="1.5 0.5 1.5" color="#00ff00">
        Manually insert Flawed Budding Certus Quartz.
        <BlockImage id="flawed_budding_quartz" scale="2" />
    </DiamondAnnotation>

    <DiamondAnnotation pos="1.5 1.5 1.5" color="#00ff00">
        Manually extract Certus Quartz Block.
        <BlockImage id="quartz_block" scale="2" />
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="165" pitch="5" />
</GameScene>

## Configurations

### Cluster Breaker:

* The first <ItemLink id="annihilation_plane" /> (1) has no GUI and cannot be configured, but can be enchanted with Fortune.
* The first <ItemLink id="storage_bus" /> (2) is filtered to <ItemLink id="certus_quartz_crystal" />.

### Certus Block Breaker:

* The second <ItemLink id="annihilation_plane" /> (3) has no GUI and cannot be configured, but must be enchanted with Silk Touch.
* The second <ItemLink id="storage_bus" /> (4) is filtered to <ItemLink id="quartz_block" />.

### Budding Block Placer:

* The <ItemLink id="formation_plane" /> (5) is in its default configuration.
* The <ItemLink id="import_bus" /> (6) is in its default configuration.

### On Main Network:

* The third <ItemLink id="storage_bus" /> (7) is filtered to <ItemLink id="certus_quartz_crystal" />, and has its
  [priority](../ae2-mechanics/import-export-storage.md#storage-priority) set higher than your main storage.

## How It Works

### Cluster Breaker:

The cluster breaker subnet works very similarly to the subnet in the [simple certus farm](simple-certus-farm.md).

1. The <ItemLink id="annihilation_plane" /> attempts to break what is in front of it, but can only break <ItemLink id="quartz_cluster" />
   because the only storage on the subnet is the <ItemLink id="storage_bus" />, filtered to <ItemLink id="certus_quartz_crystal" />.
2. The <ItemLink id="storage_bus" /> stores the certus quartz crystals in the barrel.

### Certus Block Breaker

The certus block breaker subnet serves to break the depleted budding block once it turns into a plain <ItemLink id="quartz_block" />.
It works similarly to the cluster breaker.

1. The <ItemLink id="annihilation_plane" /> attempts to break what is in front of it, but can only break <ItemLink id="quartz_block" />
   because the only storage on the subnet is the <ItemLink id="storage_bus" />, filtered to <ItemLink id="quartz_block" />.
   The plane needs to have silk touch, so the budding block won't degrade upon being broken, and thus the plane won't break it prematurely.
2. The <ItemLink id="storage_bus" /> stores the certus quartz block in the spent
   buddng certus barrel, you will have to manually throw it in water with <ItemLink id="charged_certus_quartz_crystal" /> to refresh it.

### Budding Block Placer

The budding block placer subnet serves to place a new <ItemLink id="flawed_budding_quartz" /> when the breaker subnet breaks the old depleted one.

1. The <ItemLink id="import_bus" /> Imports a budding block from the input barrel.
2. The only storage on the subnet is the <ItemLink id="formation_plane" />, which places the budding block.

### On the Main Network

* The <ItemLink id="storage_bus" /> gives the main network (and also the [Charger Automation](charger-automation.md)) access to all of the certus quartz crystals in the barrel. It is set to
  high [priority](../ae2-mechanics/import-export-storage.md#storage-priority) so that certus quartz crystals are preferentially
  put back in the barrel instead of in your main storage.