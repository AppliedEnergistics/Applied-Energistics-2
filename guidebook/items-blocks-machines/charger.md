---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Charger
  icon: charger
  position: 310
categories:
- machines
item_ids:
- ae2:charger
---

# The Charger

<BlockImage id="charger" scale="8" />

The Charger provides a way to charge
supported tools, and <ItemLink id="certus_quartz_crystal" />.

Power can be provided via the top or bottom, via either AE2's [cables](cables.md), or other mod power cables. It can
accept either AE2's power (AE) or Forge Energy (FE). Items can be inserted or removed from any side. Only the results can
be removed, so no need for filters to prevent removing certus crystals instead of charged certus. Can be rotated with a
<ItemLink id="certus_quartz_wrench" /> in order to facilitate automation.

Can be used to create <ItemLink id="charged_certus_quartz_crystal" />
from <ItemLink id="certus_quartz_crystal" />, and <ItemLink id="meteorite_compass" /> from <ItemLink id="minecraft:compass" />.

To power it manually, place a <ItemLink id="crank" /> on the top or bottom and right-click it until the item is charged.

It also acts as the workstation for the AE2 villager.

## Simple Automation

As an example, the rotateability lets you semi-automate chargers like so:

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/charger_hopper.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Recipe

<RecipeFor id="charger" />
