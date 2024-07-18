---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Molecular Assembler
  icon: molecular_assembler
  position: 310
categories:
- machines
item_ids:
- ae2:molecular_assembler
---

# The Molecular Assembler

<BlockImage id="molecular_assembler" scale="8" />

The molecular assembler takes items input into it and carries out the operation defined by an adjacent <ItemLink id="pattern_provider" />,
or the inserted <ItemLink id="crafting_pattern" />, <ItemLink id="smithing_table_pattern" />, or <ItemLink id="stonecutting_pattern" />,
then pushes the result to adjacent inventories.

This assembler has a crafting pattern that specifies the 1 oak log = 4 oak planks recipe. When oak logs are fed into the upper hopper,
the assembler crafts and spits oak planks into the lower hopper.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/standalone_assembler.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## The Main Use Of The Molecular Assembler

However, their main use is next to a <ItemLink id="pattern_provider" />. Pattern providers have special behavior in this case,
and will send information about the relevant pattern along with the ingredients to adjacent assemblers. Since assemblers auto-eject the results of
crafts to adjacent inventories (and thus into the return slots of the pattern provider), an assembler on a pattern provider
is all that is needed to automate crafting patterns.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/assembler_tower.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Upgrades

The Molecular Assembler supports the following [upgrades](upgrade_cards.md):

*   <ItemLink id="speed_card" />

## Recipe

<RecipeFor id="molecular_assembler" />

## Note

Optifine breaks the "push to adjacent inventories" function so most crafting setups with assemblers won't work.