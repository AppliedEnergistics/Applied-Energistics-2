---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Processors
  icon: logic_processor
  position: 010
categories:
- misc ingredients blocks
item_ids:
- ae2:logic_processor
- ae2:calculation_processor
- ae2:engineering_processor
- ae2:printed_silicon
- ae2:printed_logic_processor
- ae2:printed_calculation_processor
- ae2:printed_engineering_processor
- ae2:silicon
---

# Processors

<Row>
  <ItemImage id="logic_processor" scale="4" />

  <ItemImage id="calculation_processor" scale="4" />

  <ItemImage id="engineering_processor" scale="4" />
</Row>

Processors are one of the primary ingredients in AE2 [devices](../ae2-mechanics/devices.md) and machines. They are also one of your first
big automation challenges. There are three types of processor, made with gold, <ItemLink id="certus_quartz_crystal" />,
and diamond respectively. They are made using [presses](presses.md) in an <ItemLink id="inscriber" />, in a multi-step
process (usually achieved via a series of inscribers and filtered piping).

## Production Steps

<Column gap="5">
  1.  Gather/make the required ingredients: silicon, redstone, gold, <ItemLink id="certus_quartz_crystal" />, diamond.

  <RecipeFor id="silicon" />

  <br />

  2.  Press the prerequisite printed circuit components

  <Row>
    <RecipeFor id="printed_silicon" />

    <RecipeFor id="printed_logic_processor" />
  </Row>

  <Row>
    <RecipeFor id="printed_calculation_processor" />

    <RecipeFor id="printed_engineering_processor" />
  </Row>

  <br />

  3.  Final assembly

  <Row>
    <RecipeFor id="logic_processor" />

    <RecipeFor id="calculation_processor" />
  </Row>

  <RecipeFor id="engineering_processor" />
</Column>
