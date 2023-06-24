---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Energy Cells
  icon: energy_cell
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:energy_cell
- ae2:dense_energy_cell
- ae2:creative_energy_cell
---

# Energy Cells

<Row gap="20">
  <BlockImage id="energy_cell" scale="8" p:fullness="4" />

  <BlockImage id="dense_energy_cell" scale="8" p:fullness="4" />

  <BlockImage id="creative_energy_cell" scale="8" />
</Row>

Energy cells give a network more [energy](../ae2-mechanics/energy.md) storage. Some amount of energy buffer helps to smooth
out spikes in energy draw when large amounts of items are inserted or extracted, and larger amounts of energy storage
allow the network to run while energy isn't being generated (like at night with solar panels) or handle the massive instantaneous
energy draw of [spatial storage](../ae2-mechanics/spatial-io.md).

## Fill Bars

<Row>
<BlockImage id="energy_cell" scale="4" p:fullness="0" />
<BlockImage id="energy_cell" scale="4" p:fullness="1" />
<BlockImage id="energy_cell" scale="4" p:fullness="2" />
<BlockImage id="energy_cell" scale="4" p:fullness="3" />
<BlockImage id="energy_cell" scale="4" p:fullness="4" />
</Row>

The bars on the side of a cell correspond to how much energy it has.

*   0 when below 25% charge
*   1 when between 25% and 50% charge
*   2 when between 50% and 75% charge
*   3 when between 75% and 99% charge
*   4 when above 99% charge

## Types Of Cell

*   The <ItemLink id="energy_cell" /> can store 200k AE, and just one should be sufficient for most use cases, handling the power surges
    of normal network use with ease.
*   The <ItemLink id="dense_energy_cell" /> can store 1.6M AE and is for when you want to run a network off of stored power, or
    handle the massive instantaneous energy draw of large [spatial storage](../ae2-mechanics/spatial-io.md) setups.
*   The <ItemLink id="creative_energy_cell" /> is a creative item for testing, providing UNLIMITED POWAHHHH or whatever.

## Recipes

<Row>
  <RecipeFor id="energy_cell" />

  <RecipeFor id="dense_energy_cell" />
</Row>
