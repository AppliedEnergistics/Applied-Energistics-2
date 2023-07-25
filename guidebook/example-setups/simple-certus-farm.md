---
navigation:
  parent: example-setups/example-setups-index.md
  title: Simple Certus Farm
  icon: certus_quartz_crystal
  position: 110
---

# Simple Certus Farm

As mentioned in [Certus Growth](../ae2-mechanics/certus-growth.md), automation of <ItemLink id="certus_quartz_crystal" />
harvesting involves <ItemLink id="annihilation_plane" />s and <ItemLink id="storage_bus" />ses. 
<ItemLink id="growth_accelerator" />s are used to massively speed up the growth of certus quartz buds, and then the planes
break the fully grown <ItemLink id="quartz_cluster" />. They are filtered by taking advantage of the suspiciously fortunate trait that non-mature
certus buds drop <ItemLink id="certus_quartz_dust" /> instead of dropping nothing.

This farm works fully automatically with <ItemLink id="flawless_budding_quartz" />, but with flawed, chipped, and damaged
budding certus quartz you will have to replace the budding block manually. Or, as described in [Semi-Auto Certus Farm](semiauto-certus-farm.md)
and [Advanced Certus Farm](advanced-certus-farm.md), automatically.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/simple_certus_farm.snbt" />

  <BoxAnnotation color="#dddddd" min="3.7 1 1" max="4 2 2">
        (1) Annihilation Plane: No GUI to configure, but can be enchanted with Fortune.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 1" max="3.3 2 2">
        (2) Storage Bus #1: Filtered to Certus Quartz Crystal.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 .7" max="2 2 1">
        (3) Storage Bus #2: Filtered to Certus Quartz Crystal. Has priority set higher than the main storage.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="1 0.5 0.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

* The first <ItemLink id="annihilation_plane" /> (1) has no GUI and cannot be configured, but can be enchanted with fortune.
* The first <ItemLink id="storage_bus" /> (2) is filtered to <ItemLink id="certus_quartz_crystal" />.
* The second <ItemLink id="storage_bus" /> (3) is filtered to <ItemLink id="certus_quartz_crystal" />, and has its
  [priority](../ae2-mechanics/import-export-storage.md#storage-priority) set higher than the main storage.

## How It Works

1. The <ItemLink id="annihilation_plane" /> attempts to break what is in front of it, but can only break <ItemLink id="quartz_cluster" />
   because the only storage on the subnet is the <ItemLink id="storage_bus" />, filtered to <ItemLink id="certus_quartz_crystal" />.
4. The first <ItemLink id="storage_bus" /> stores the certus quartz crystals in the barrel.
5. The second <ItemLink id="storage_bus" /> gives the main network access to all of the certus quartz crystals in the barrel. It is set to
   high [priority](../ae2-mechanics/import-export-storage.md#storage-priority) so that certus quartz crystals are preferentially
   put back in the barrel instead of in your main storage.
