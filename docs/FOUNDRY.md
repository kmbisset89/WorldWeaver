# World Weaver and Foundry VTT

World Weaver is the GM laptop. Foundry is the remote player table. Integration means taking a world or a map **out** of World Weaver and **into** Foundry as files. It is not a shared runtime.

## What we will not do

- Replace Foundry lighting, walls, vision, or video.
- Run combat in two apps at once (no live HP, fog, or token sync).
- Turn World Weaver into a Foundry module, or add a cloud account to look like World Anvil.

Same-room play stays in World Weaver’s **Player view**. Remote play stays in Foundry.

## What you can do today

1. **Export a battle map as Universal VTT** from **Maps → Export VTT**. The `.uvtt` file carries the PNG and grid size. Import it in Foundry with [Universal Battlemap Importer](https://foundryvtt.com/packages/dd-import/). Walls and lights are empty; draw those in Foundry if you need them.
2. **Import a world bible** with the companion module in [`foundry/world-weaver/`](../foundry/world-weaver/). Export a `.wwbundle` from **Worlds**, enable the module, and choose that file. Lore becomes journals (secrets stay GM-only). Locations become journal folders. Battle maps become scenes with the grid already set. Re-import updates the same documents; nothing streams live.
3. **Manual PNG** if you skip the exporter: copy the map image, set Foundry’s grid to the map’s columns × rows at the listed feet per square (usually 5 ft).

Step-by-step: [User guide — Using World Weaver with Foundry](USER_GUIDE.md#using-world-weaver-with-foundry).
