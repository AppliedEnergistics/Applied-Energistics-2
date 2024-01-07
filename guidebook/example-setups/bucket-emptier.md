---
navigation:
  parent: example-setups/example-setups-index.md
  title: Bucket Emptier
  icon: minecraft:bucket
---

# Bucket Emptier

Also see [Bucket Filler](bucket-filler.md).

Note that since this uses a <ItemLink id="pattern_provider" />, it is meant to integrate into your [autocrafting](../ae2-mechanics/autocrafting.md)
setup.

Sometimes, life is inconvenient and you need the fluid itself but you can only make the fluid in a bucket. Sometimes a machine might do this for you
(like the Fluid Transposer from Thermal Expansion), but you might not always have a mod that does it conveniently for you. Luckily
vanilla Minecraft has a slightly-less-convenient way, the <ItemLink id="minecraft:dispenser" />.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/bucket_emptier.snbt" />

<BoxAnnotation color="#dddddd" min="2 1 0" max="3 2 1">
        (1) Pattern Provider: Set to lock crafting "With redstone signal" and blocking mode turned on, with the relevant processing patterns.

        <Row>
        ![Fill Pattern](../assets/diagrams/water_empty_pattern_small.png)
        ![Fill Pattern](../assets/diagrams/lava_empty_pattern_small.png)
        </Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2.1 2 0.1" max="2.9 2.2 0.9">
        (2) Interface: In its default configuration.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.1 2 1.1" max="3.9 2.2 1.9">
        (3) Storage Bus #1: In its default configuration.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="4.05 1.05 0.8" max="4.95 1.95 1">
        (4) Annihilation Plane: No GUI to configure.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.2 1.2 0.8" max="3.8 1.8 1">
        (5) Import Bus: Filtered to buckets.
        <ItemImage id="minecraft:bucket" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 1.1 0.1" max="3.2 1.9 0.9">
        (6) Storage Bus #2: In its default configuration.
  </BoxAnnotation>

<DiamondAnnotation pos="0 1.5 0.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="225" pitch="45" />
</GameScene>

## Configurations

* The <ItemLink id="pattern_provider" /> (1) is Set to lock crafting "With redstone signal" and blocking mode turned on,
  with the relevant <ItemLink id="processing_pattern" />s.
  
    ![Charger Pattern](../assets/diagrams/water_empty_pattern.png)
    ![Charger Pattern](../assets/diagrams/lava_empty_pattern.png)

* The <ItemLink id="interface" /> (2) is in its default configuration.
* The first <ItemLink id="storage_bus" /> (3) is in its default configuration.
* The <ItemLink id="annihilation_plane" /> (4) has no GUI and cannot be configured.
* The <ItemLink id="import_bus" /> (5) is filtered to buckets.
  <ItemImage id="minecraft:bucket" scale="2" />
* The second <ItemLink id="storage_bus" /> (6) is in its default configuration.

## How It Works

1. The <ItemLink id="pattern_provider" /> pushes the ingredients into the <ItemLink id="interface" />.
   (Actually, as an optimization, it pushes directly through the storage bus asi fit was an extension of the provider's faces. The items never actually enter the interface.)
2. Through mechanisms described in [pipe subnets](pipe-subnet.md#providing-to-multiple-places),
   the bucket ends up in the <ItemLink id="minecraft:dispenser" />.
3. The <ItemLink id="minecraft:comparator" /> detects the bucket in the dispenser and thus simultaneously powers the dispenser and locks
   the <ItemLink id="pattern_provider" />.
4. The dispenser dumps out the fluid from the bucket, it now has an empty bucket in itself.
5. The <ItemLink id="import_bus" /> pulls the empty bucket out of the dispenser and stores it through the
   <ItemLink id="storage_bus" /> into the pattern provider, returning it to the main network.
6. The comparator sees the dispenser is empty, unlocking the provider.