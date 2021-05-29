Changes so far :

Design changes/breakages:
 - Interfaces no longer spill items of one pattern on other faces
 - Interfaces now push patterns in round-robin (Interfaces, then sides)




Fixes:

 - Fix gregtech machines appearing with 'unnamed' or 'draconium lens' on the interface terminal on specific situations.
 - Add memorycard support for fluid interfaces, import/export/storage busses and level emitters ( this fix was upstreamed to AE2 for MC 1.16)
 - Storage busses now hide/show inaccessible items as configured.
 - Exclusive Blocking mode for GTCE ( shapes, molds and configured circuits do not block GTCE machines )
 - Blocking modes default to block on any item in the iventory the entity exposes
 - Fuzzy includes items that report that they're damageable, but report a maxDamage of 0 ( auto-crafting of basic capacitors -> resonant capacitors is now possible ) <- Broken item implementation.

Performance:

 - Added @talchas fixes for insane channelless AE networks.
 - Implemented StorageDrawers slotless itemrepository.
 - Removed CraftedEvent calls (really bad lag with craftweaker versions before CraftTweaker2-1.12-4.1.20.626)
 - Count items set in interfaces before queuing crafting for them needlessly
 - Backported b7ca98d ( Avoid copying items on simulated item extraction )
 - Cache some level emitters functions

QOL:

 - added highlight interface button to interface terminal ("?" button on the left of the interface slots)
 - Added bar on the interface terminal that search by inputs ( The one on the LEFT, also searchs by interface name )
 - Shortcut to molecular assemblers with free slots on the terminal interface by @Theisyat
 - Toggle button on interface terminal to hide full interfaces
 - JEI "U""R" and "A" (Usages/Recipes/Bookmark) now work on the Crafting Status GUI. ( the one that shows the total items to craft, and whats missing)
 - Patterns can now be made with items currently showing on JEI. ( This is overriden by Just Enough Energistics. )
 - Added multiplier buttons to processing pattern gui
 - Switched crafting terminal JEI search to fuzzy mode. If the recipe uses a damageable item, AE will try to grab it ( damaged tools )
 - Encoded patterns stack up to 64 ( holding shift and clicking the encode arrow will transfer the encoded pattern to the player inventory)
 - Encoded patterns can be draggred on the interface terminal.
 - Storage Monitor and Conversion Monitor now also ccepts fluids
 - Draggable JEI ghost items (also works on bookmarked items. SHIFT + Click will move the hovered item into the first free target slot)
  -JEI auto switches between crafting and processing patterns

HOTKEYS:

 - Implemented mousetweaks API. AE2 custom keybinds now work. (Try right clicking and use the scroll-wheel on the terminals)
 - Holding SHIFT and scrolling UP and DOWN will increase the items set on the configured slots of Interfaces and Pattern Terminal (Processing mode)
