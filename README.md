GeoSharerMod
=========
A Minecraft ForgeModLoader mod for sharing minecraft multiplayer map data. Tailored for use with Civcraft (http://www.civcraft.vg/).


How To Use
===========
Use Minecraft Forge and add the mod zip to your mods directory. The mod will automatically download overworld data from any multiplayer server you play on, and store it in a stripped down and compressed *.geosharer format in the mods/GeoSharer/[servername]/ directory.

These *.geosharer files can be shared with map-making collaborators to ensure that everyone has the most up to date world data. Each chunk is given a timestamp, which is used to synchronise data from multiple *.geosharer payloads.

To take the *.geosharer files and turn them into a world you need to use the GeoSharerMerge program.


Compiling
=========
The mod is compiled using Eclipse and Forge MCP for the relevant version of Minecraft. Version naming is kept in step with Minecraft, with appended letters for multiple releases withing a single version.


Source Directory Structure
==========================
All mod source files are in the base directory.
