---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Patterns
  icon: crafting_pattern
  position: 410
categories:
- tools
item_ids:
- ae2:blank_pattern
- ae2:crafting_pattern
- ae2:processing_pattern
- ae2:smithing_table_pattern
- ae2:stonecutting_pattern
---

# Patterns

<ItemImage id="crafting_pattern" scale="4" />

Patterns are made in a <ItemLink id="pattern_encoding_terminal" /> out of blank patterns, and inserted in <ItemLink id="pattern_provider" />s
or <ItemLink id="molecular_assembler" />s.

There are several different types of pattern for different things:

*   <ItemLink id="crafting_pattern" />s encode recipes made by a crafting table. They can be put directly in a <ItemLink id="molecular_assembler" /> to make it
    craft the result whenever given the ingredients, but their main use is in a <ItemLink id="pattern_provider" /> next to a molecular assembler.
    Pattern providers have special behavior in this case, and will send the relevant pattern along with the ingredients to adjacent assemblers.
    Since assemblers auto-eject the results of crafts to adjacent inventories, an assembler on a pattern provider is all that is needed to automate crafting patterns.

***

*   <ItemLink id="smithing_table_pattern" />s are very similar to crafting patterns, but they encode smithing table recipes. They are also automated by a pattern
    provider and molecular assembler, and function in the exact same way. In fact, crafting, smithing, and stonecutting patterns can be
    used in the same setup.

***

*   <ItemLink id="stonecutting_pattern" />s are very similar to crafting patterns, but they encode stonecutter recipes. They are also automated by a pattern
    provider and molecular assembler, and function in the exact same way. In fact, crafting, smithing, and stonecutting patterns can be
    used in the same setup.

***

*   <ItemLink id="processing_pattern" />s are where a lot of flexibility in autocrafting comes from. They are the most generalized type, simply
    saying "if a pattern provider pushes these ingredients to adjacent inventories, the ME system will recieve these items at some point in the
    near or distant future". They are how you will autocraft with almost any modded machine, or furnaces and the like. Because they are so
    general in use and do not care what happens between pushing ingredients and receiving the result, you can do some really funky stuff, like inputting
    the ingredients into an entire complex factory production chain which will sort out stuff, take in other ingredients from infinitely-producing
    farms, print the entirety of the Bee Movie script, the ME system does not care as long as it gets the result the pattern specifies. In fact,
    it doesn't even care if the ingredients are in any way related to the result. You could tell it "1 cherry wood planks = 1 nether star" and have
    your wither farm kill a wither upon receiving a cherry wood planks and it would work.

Multiple <ItemLink id="pattern_provider" />s with identical patterns are supported and work in parallel. Additionally, you can have a pattern say,
for example, 8 cobblestone = 8 stone instead of 1 cobblestone = 1 stone, and the pattern provider will insert 8 cobblestone into
your smelting setup every operation instead of one at a time.

## Recipe

<RecipeFor id="blank_pattern" />
