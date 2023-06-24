---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Cables
  icon: fluix_glass_cable
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:white_glass_cable
- ae2:orange_glass_cable
- ae2:magenta_glass_cable
- ae2:light_blue_glass_cable
- ae2:yellow_glass_cable
- ae2:lime_glass_cable
- ae2:pink_glass_cable
- ae2:gray_glass_cable
- ae2:light_gray_glass_cable
- ae2:cyan_glass_cable
- ae2:purple_glass_cable
- ae2:blue_glass_cable
- ae2:brown_glass_cable
- ae2:green_glass_cable
- ae2:red_glass_cable
- ae2:black_glass_cable
- ae2:fluix_glass_cable
- ae2:white_covered_cable
- ae2:orange_covered_cable
- ae2:magenta_covered_cable
- ae2:light_blue_covered_cable
- ae2:yellow_covered_cable
- ae2:lime_covered_cable
- ae2:pink_covered_cable
- ae2:gray_covered_cable
- ae2:light_gray_covered_cable
- ae2:cyan_covered_cable
- ae2:purple_covered_cable
- ae2:blue_covered_cable
- ae2:brown_covered_cable
- ae2:green_covered_cable
- ae2:red_covered_cable
- ae2:black_covered_cable
- ae2:fluix_covered_cable
- ae2:white_covered_dense_cable
- ae2:orange_covered_dense_cable
- ae2:magenta_covered_dense_cable
- ae2:light_blue_covered_dense_cable
- ae2:yellow_covered_dense_cable
- ae2:lime_covered_dense_cable
- ae2:pink_covered_dense_cable
- ae2:gray_covered_dense_cable
- ae2:light_gray_covered_dense_cable
- ae2:cyan_covered_dense_cable
- ae2:purple_covered_dense_cable
- ae2:blue_covered_dense_cable
- ae2:brown_covered_dense_cable
- ae2:green_covered_dense_cable
- ae2:red_covered_dense_cable
- ae2:black_covered_dense_cable
- ae2:fluix_covered_dense_cable
- ae2:white_smart_cable
- ae2:orange_smart_cable
- ae2:magenta_smart_cable
- ae2:light_blue_smart_cable
- ae2:yellow_smart_cable
- ae2:lime_smart_cable
- ae2:pink_smart_cable
- ae2:gray_smart_cable
- ae2:light_gray_smart_cable
- ae2:cyan_smart_cable
- ae2:purple_smart_cable
- ae2:blue_smart_cable
- ae2:brown_smart_cable
- ae2:green_smart_cable
- ae2:red_smart_cable
- ae2:black_smart_cable
- ae2:fluix_smart_cable
- ae2:white_smart_dense_cable
- ae2:orange_smart_dense_cable
- ae2:magenta_smart_dense_cable
- ae2:light_blue_smart_dense_cable
- ae2:yellow_smart_dense_cable
- ae2:lime_smart_dense_cable
- ae2:pink_smart_dense_cable
- ae2:gray_smart_dense_cable
- ae2:light_gray_smart_dense_cable
- ae2:cyan_smart_dense_cable
- ae2:purple_smart_dense_cable
- ae2:blue_smart_dense_cable
- ae2:brown_smart_dense_cable
- ae2:green_smart_dense_cable
- ae2:red_smart_dense_cable
- ae2:black_smart_dense_cable
- ae2:fluix_smart_dense_cable
---

# Cables

<GameScene zoom="3" background="transparent">
  <ImportStructure src="../assets/assemblies/cables.snbt" />
  <IsometricCamera yaw="180" pitch="30" />
</GameScene>

While ME networks are also created by adjacent ME-capable machines, cables are the primary way of
extending an ME network over larger areas.

Differently colored cables can be used to ensure adjacent cables do not connected to each other,
allowing [channels](../ae2-mechanics/channels.md) to be distributed more efficiently. They also affect the color of terminals connected to them,
so you don't have to have all your terminals be purple. Fluix cables connect to every other color.

Of note, **CHANNELS HAVE NOTHING TO DO WITH CABLE COLOR**

## An Important Note

**If you are new to AE2 and unfamiliar with channels, use smart cable and dense smart cable wherever you can.
It will show how channels are routed through your network, making their behavior more understandable.**

## Another Note

**These are not item or fluid or energy or etc. pipes.** They have no internal inventory, pattern providers and machines do not "push"
into them, all they do is connect AE2 [devices](../ae2-mechanics/devices.md) together into a network.

## Glass Cable

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/fluix_glass_cable.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="fluix_glass_cable" /> is the simplest cable to make, transfers power
and up to 8 [channels](../ae2-mechanics/channels.md). It comes in 17 diffrent colors, the default
being Fluix, and can be dyed any color using any of the 16 dyes.

To craft colored cables surround a dye of any type with 8 cables of the same
type ( color of the cables dosn't matter, but they must be the same type,
glass, smart, etc ). You can also paint cables with any forge compatible paint
brush in world.

You can craft any colored cable with a water bucket to remove the dye.

You can cover the cable with wool to create <ItemLink id="fluix_covered_cable" />, and craft <ItemLink id="fluix_smart_cable" /> to get a better idea of what is going on with
your [channels](../ae2-mechanics/channels.md).

<RecipeFor id="fluix_glass_cable" />

<RecipeFor id="blue_glass_cable" />

## Covered Cable

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_covered_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

The covered cable variant offers no gameplay benefits over its <ItemLink id="fluix_glass_cable" /> counterpart. It can however be used
as an alternate aesthetic choice if you prefer the covered look.

Can be colored in the same manner as <ItemLink id="fluix_glass_cable" />. Four <ItemLink id="fluix_covered_cable" /> can be crafted with
redstone and glowstone to make <ItemLink id="fluix_covered_dense_cable" />.

<Recipe id="network/cables/covered_fluix" />

<RecipeFor id="blue_covered_cable" />

## Dense Cable

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_covered_dense_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Higher Capacity cable, can carry 32 channels unlike standard cable which can only carry 8,
however it doesn't support buses so you must first step down from dense to a
smaller cable (such as <ItemLink id="fluix_glass_cable" /> or <ItemLink id="fluix_smart_cable" />) before using buses or
panels.

Dense cables slightly override the "shortest path" behavior of channels, channels will take the shortest path to a
dense cable, and then the shortest path through that dense cable to a controller.

<Recipe id="network/cables/dense_covered_fluix" />

<RecipeFor id="blue_covered_dense_cable" />

## Smart Cable

<Row>
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_smart_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_smart_dense_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
</Row>

While bearing some similarity to <ItemLink id="fluix_covered_cable" /> in appearance, they
provide diagnostic function by visualizing the channel usage on the cables,
the channels appear as lit colored lines that run along the black stripe on
the cables giving you an understanding of how your channels are being used on
your network. For regular smart cables, the first four channels show as lines matching the color of the
cable, the next four show as white lines. For dense smart cable, each stripe represents 4 channels.

On networks with a <ItemLink id="controller" />, the lines on the cables show the exact path the channels take.

Smart cables on ad-hoc networks will instead show the number of channels in use network-wide instead of the number of channels flowing through that specific cable.

These can also be colored in the same manner as <ItemLink id="fluix_glass_cable" />.

<Recipe id="network/cables/smart_fluix" />

<Recipe id="network/cables/dense_smart_fluix" />

<RecipeFor id="blue_smart_cable" />
