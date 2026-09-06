# World Weaver user guide

World Weaver is a desktop app for preparing and running tabletop campaigns. Everything stays on your computer. This guide follows the screens in the left navigation and the extra windows you open at the table.

## Contents

1. [First launch](#first-launch)
2. [How the app is organized](#how-the-app-is-organized)
3. [Home](#home)
4. [Worlds](#worlds)
5. [One-shot wizard](#one-shot-wizard)
6. [Campaigns](#campaigns)
7. [Locations and world maps](#locations-and-world-maps)
8. [Lore](#lore)
9. [Calendar](#calendar)
10. [Factions](#factions)
11. [Links](#links)
12. [Characters](#characters)
13. [Quests](#quests)
14. [Sessions](#sessions)
15. [Tonight (session runner)](#tonight-session-runner)
16. [Maps](#maps)
17. [Encounters](#encounters)
18. [Dice](#dice)
19. [Atmosphere](#atmosphere)
20. [Search](#search)
21. [Settings](#settings)
22. [Files, bundles, and backups](#files-bundles-and-backups)
23. [Using World Weaver with Foundry](#using-world-weaver-with-foundry)
24. [Sample worlds](#sample-worlds)

## First launch

On first open you land on **Home**. World Weaver uses a **local profile** (name and email shown in the sidebar). There is no password and no online account. Change the name and email under **Settings → Local profile**.

Pick a theme from the sidebar sun/moon button (Light, Dark, or System) or from **Settings → Appearance**. Skins such as Fantasy, Cozy Tavern, and Cyberpunk change the color palette without changing how the app works.

Most screens need an **active world**. Campaign-scoped screens (quests, sessions, maps, encounters, player characters) also need an **active campaign**. Click a world on **Worlds**, then a campaign on **Campaigns**. The sidebar subtitle shows `World · Campaign` when both are set.

## How the app is organized

| Scope | What lives there |
|-------|------------------|
| **World** | Locations, world maps, lore, calendar, holidays, celestial bodies, factions, the people library |
| **Campaign** | Status, party PCs, quests, sessions, battle maps, encounters, campaign notes on locations |

Create a world first, then a campaign inside it. You can keep several campaigns in one world (an ongoing game, an archived run, a completed chronicle).

**World Map** and the **one-shot wizard** are not listed in the sidebar. Open them from Locations and Worlds/Home. **Tonight** is in the sidebar; Home **Continue tonight** also opens it.

On a wide window, library screens keep a roster beside the selected item. On a narrower window the selected item fills the page; use the named control in the header to search, filter, and switch. Maps, Links, and the world map keep the canvas visible and move the list or inspector into that same control.

The main window also opens extra windows:

- **Character sheet** — full sheet for a person
- **Player view** — fog-filtered battle map for the players
- **Dice** — floating tray you can keep above other apps
- **Atmosphere** — floating tray that fires Home Assistant, Philips Hue, and Govee lighting together

## Home

Home greets you and shows counts for worlds, campaigns, and people. Click a count card to jump to that screen.

**Continue tonight** appears when an active campaign has a session to resume. **Continue** opens the run screen for that session.

**Recent worlds** lists worlds you have been using. Click one to make it active.

If you have no worlds yet, use **New world** or **Create a one-shot**.

## Worlds

**Worlds** is your setting library.

1. Click **New world**.
2. Enter a name and optional description.
3. Choose a default game system: **5E** or **PF2E**. Campaigns can override this later.
4. Click the world row to make it **Active**.

The pencil and trash icons on each row are **Edit** and **Delete** (hover to see the label). You cannot delete a world that still has campaigns; archive or delete those campaigns first.

**Import world** loads a `.wwbundle` file (a single world and its contents). **Export** on a world writes a `.wwbundle` you can share or archive. Importing creates a copy; it does not replace your other worlds.

**One-shot** starts the wizard described next.

## One-shot wizard

Use this when you want a playable evening without building a full setting by hand.

Steps:

1. **Identity** — name and game system
2. **Hook** — why the party is here
3. **Places** — starter locations
4. **People** — who they meet
5. **Conflict** — what is at stake
6. **Table plan** — encounter difficulty and table notes
7. **Review** — confirm, then **Create**

The wizard creates a world and campaign you can keep editing afterward. **Cancel** returns without saving.

## Campaigns

Campaigns belong to the **active world**. If you see “create or select a world first,” go to **Worlds** and click a world.

1. Click **New campaign**.
2. Set name, description, notes, and optional game-system override.
3. Choose **Milestone** (default) or **XP** leveling. Milestone prompts you to award a party level after you close a session or complete a quest. XP mode stores a running total on each PC; you enter the amount yourself. World Weaver does not calculate encounter XP.
4. Click the campaign to make it the **active campaign**.

Statuses:

- **Active** — current play-through (the default list)
- **Archived** — paused
- **Completed** — finished chronicle

Use **Archive**, **Complete**, or **Reopen** from the campaign detail. The trash icon is **Delete**. The overview shows party, active quests, last session, and shortcuts to **Quests**, **Sessions**, and **Characters**.

If the list looks empty, you may only be seeing active campaigns. Show archived and completed when you need history.

## Locations and world maps

Locations are a strict tree:

**Continent → Area → City → Place**

A continent has no parent. Each other type must sit under the type above it.

On **Locations** you can:

- Search the tree and filter by type
- Create and edit name, description, climate, terrain, government, landmarks, history, and world notes
- Attach lore and quests
- Attach **voice clips** (WAV files, record, play, remove)
- With an active campaign: mark **Party is here**, write **Campaign notes**, and save them

### World maps

World maps are nested PNG maps, not battle grids.

From a location, use **Open world map** or **Add world map** (or **Open map** / **Add map** on the detail pane). That screen is titled **World map**.

- **Import PNG** to start a map at that location
- Place child locations as pins: pick an **Unplaced** child, then tap the map
- Click a pin to select that location; **Clear pin** removes the anchor without deleting the location
- Drill into a child that has its own map
- **Replace PNG** swaps the image; the trash icon (**Delete map**) removes the map file but keeps location pins on the records

Use **Locations** in the header to go back to the tree.

## Lore

Lore is world-scoped. Categories include History, Myth, Religion, Culture, Geography, Magic, Politics, and Other.

Each entry can have body text, tags, related lore, and links to locations or people. Mark **DM-only secrets** and optional **hints** so you can reveal them at the table without putting the secret in the player-facing text.

## Calendar

The calendar belongs to the world: era suffix, custom month names, weekday names, and the current in-world date. Sessions can stamp an in-world date so the calendar reflects what you have already played.

Add **holidays and important days** on the same screen. A holiday repeats every year on a month and day. An important day can also carry a year so it marks one date in history. Link either kind to lore; those days show up on the lore entry, and matching days appear on **Run** when the session date or calendar today lands on them.

Add **celestial bodies** (suns, moons, and planets) with a cycle length in world-days and an optional offset. The offset is how far into the cycle the body is on year 1, day 1 of the first month. When a current world date is set, **Today’s sky** shows each body’s place in that cycle. Moons also show a named phase (new through waning crescent). Search can open a body from the **Sky** group.

You need an active world before the calendar has a setting to edit.

## Factions

Create factions with name, description, goals, and notes. Attach people as members. Memberships show up on **Links**.

## Links

**Links** is a relationship web of people and faction memberships.

Filter by search and relationship type (Parent, Child, Sibling, Spouse, Ancestor, Descendant, Mentor, Student, Ally, Rival, Enemy, Other). You can show or hide isolates and memberships.

Add relationships on **Characters** and memberships on **Factions**; this screen visualizes them.

## Characters

**Characters** lists people in the active world. Subtitle shows the world name.

Kinds:

| Kind | How to create | Notes |
|------|----------------|-------|
| **PC** | **New PC** | Requires an active campaign |
| **NPC** | **New person** | World library or campaign |
| **Monster** | **Add SRD monster** (5E) | Needs the SRD catalog (bundled lists or an import) |

**Random NPC** (5E) generates a person from the current lists.

The creation wizard and editors follow the world’s (or campaign’s) game system: **5E** or **PF2E**. You can set avatars, companions, relationships, and voice clips on the detail pane. When the campaign uses **XP** leveling, the editor and character sheet also show current XP.

Open the **character sheet** window from a person (also from Tonight’s party cards). The sheet is a separate window with HP, armor class, ability scores, skills, spells and slots, features, items, death saves, and concentration. The pencil icon (**Edit sheet**) returns you to the in-app editor.

Importing the 5E SRD under Settings adds official SRD races, classes, spells, and monsters to the pickers. Clearing the import returns pickers to the bundled 5E lists; people you already created are unchanged.

## Quests

Quests belong to the **active campaign**.

- Title, summary, status (**Active** / **Completed**)
- Optional location
- Objectives: **Open**, **Complete**, or **Failed**
- Links to lore, people, and sessions

Use quests as the spine of a campaign; Tonight surfaces active objectives.

## Sessions

Sessions also belong to the active campaign. Clicking a session makes it the **active session** (the one Home and Tonight continue).

Typical fields and sections:

- Name, notes, in-world date
- Recap (“what changed”)
- Scenes
- March order
- Plot threads (Open, In progress, Resolved, Dropped; priority Low through Critical)
- Reference docs
- Start-of-session checklist (including active quests)
- Linked quests

Create with **New session**. Close-session tools help you write a recap before the next game. After you close a session, World Weaver offers **Award a party level** (milestone campaigns) or **Award party XP** (XP campaigns). Completing a quest offers the same prompt. Level-ups bump class or character level only; they do not fill in new features or spell slots.

## Tonight (session runner)

**Tonight** is the table dashboard the GM keeps up during a session. Open it from the sidebar or from Home with **Continue tonight → Continue** after a session is active.

You need an active world, campaign, and session. Empty states send you to the matching screen if something is missing.

On a wide window Tonight is three columns. On a narrower window the same sections stack in one scroll.

The run screen shows:

- Session name, in-world date, calendar “today”, and any holiday or important day that falls on that date
- Planned scenes
- Progress clocks (2–12 segments) for story pressure; click a segment to fill or unfill
- One countdown timer with 5 / 10 / 15 minute presets
- **Record** — Pick a **microphone**, then **Mic** for table audio or **Camera** for video plus that mic with a live preview. The first Camera attempt asks the OS for access. If that is denied, **Allow camera** opens Camera privacy settings. **Open** plays the file in the OS player. Recordings stay on this computer with the session (`~/.worldweaver/recordings`); they are not included in world bundles or machine backups
- Lookup for people, locations, and lore, with a read-only peek and **Open** to the full screen
- Editable session notes and a separate scratch pad (scratch does not overwrite prep notes)
- Recap (“what changed”) after you close a session
- Party cards (HP and AC; click to open the sheet)
- Atmosphere scenes, one-shot effects, and live loops in place, plus **Pop out** for the floating tray
- Combat summary and **Open tracker** when an encounter is running
- Objectives, party locations
- Close-session notes

Shortcuts: **Dice tray**, **Encounters**, **Maps**, and **Player view** when the current encounter has a map.

## Maps

**Maps** holds **battle maps** for the active campaign (tactical grids, not the Locations world maps).

### Import a map

Open the maker, preview a PNG, set **columns**, **rows**, unit name (default **ft**), and units per tile (default **5**), then save. World Weaver tiles the image for pan and zoom.

### Starter maps

**Starter maps** offers bundled maps. Small maps are 20×20; medium maps are 30×30. Some maps include extra **situation** layers (stages of a fight or changing terrain).

### At the table

- **Measure** distances on the grid
- **Fog** — Hide or Reveal brush, Reveal all, Hide all. Click cells to reveal them on Player view
- **Situations** — toggle extra PNG layers (**Add layer**)
- Tokens and items on cells
- Terrain overlays (blocked or difficult cells)
- **Player view** — a second window (~1024×768) that shows only what you have revealed. Title uses the battle map name
- **Export VTT** — writes a `.uvtt` file (PNG plus grid) for Foundry, Owlbear, and other virtual tabletops. Walls and lights are not included.

Close Player view from that window or with **Player view open** on the GM side.

## Encounters

Encounters go **Planned → Active → Ended**. Difficulty chips: Easy, Medium, Hard, Deadly, Other.

### Setup

Name the encounter, pick a location and optional battle map, add notes, and add participants (named people or nameless/swarm combatants). Set initiative, then **Start**.

### Running combat

The tracker includes:

- Initiative order, **Roll all**, per-combatant rolls and bonuses
- HP damage and healing
- Conditions (5E condition chips)
- Combat states: **Conscious**, **Downed**, **Dead**, plus death saves
- **Next turn** and round tracking
- Token placement: click a combatant, then a map cell
- The same fog and **Player view** tools as Maps
- **End encounter**

Player characters stay visible on Player view. You can hide NPC and monster tokens until you want them seen.

## Dice

**Dice** rolls digital dice or logs faces from physical dice.

- Click a die, the stage, or **Roll** to tumble the current notation
- Sides: d4, d6, d8, d10, d12, d20, d100
- Notation such as `2d6+3`
- **Normal**, **Advantage**, and **Disadvantage** (advantage/disadvantage apply to a single d20)
- **Digital** vs **Table** — in Table mode, enter comma-separated faces and **Log**
- Die colors: Bone, Onyx, Crimson, Forest, Azure
- History, with Clear

**Pop out** opens a floating **Dice** window. Turn on **Always on top** to keep the tray above other windows (the choice is remembered). Tonight’s **Dice tray** opens the same tray.

## Atmosphere

**Atmosphere** is a named tray of table-side looks. One button can fire **Home Assistant**, **Philips Hue**, and **Govee** together. World Weaver does not play music itself and does not change Foundry or battle-map lighting.

Connect whichever hubs you actually have. You do not need all three.

### Set up Home Assistant

1. In Home Assistant, create the scenes you want at the table (Tavern, Combat, Boss, and so on). A scene can set lights and media players together.
2. Create a **long-lived access token** (Home Assistant profile → Long-Lived Access Tokens).
3. In World Weaver, open **Atmosphere**.
4. Enter the local URL, such as `http://homeassistant.local:8123` or `http://192.168.1.50:8123`, and the token.
5. The save icon (**Save connection**), then **Test connection**. **Load HA scenes** lists `scene.*` entities from that hub.

### Set up Philips Hue

Hue talks to a bridge on your LAN. World Weaver uses the local Hue API, not the Hue cloud.

1. Put the bridge on the same network as this computer.
2. Open **Atmosphere** and enter the bridge IP, or click **Find bridge**.
3. Press the round **link button** on the bridge, then **Pair**.
4. **Test**, then **Load Hue lights** and pick the lamps Atmosphere may change.
5. Choose a **Look** for those lamps. Built-in moods (Warm, Campfire, Dawn, Forest, Dungeon, Night, Storm, Combat, Off), custom moods, the color picker, brightness slider, and power switch apply to the selected lights. Set **Transition** to Instant, Soft, Slow, or Dramatic — Hue fades in the bulbs, and Govee is stepped in software so the change stays smooth. One-shot effects (Lightning, Critical strike, Fireball, Heal, Holy light, Poison, Darkness, Arcane burst) flash over the current look and restore it. Live effects (Fire, Water, Candle, Storm, Ice, Arcane, Forest, Lava) keep oscillating between colors until you tap them again, fire a one-shot, or change the look. Name the current look and save it as a custom mood. You can also paste a hex color. **Or** **Load Hue scenes** and pick a Hue scene. Click a Hue scene again to clear it. Leave lights unselected only when you want a Hue scene to change every lamp in that scene.
6. Click a mapped scene in the tray to apply it. Clicking a different mapped scene switches those lights to that look.

If pairing already happened, you can paste an existing application key and use the save icon (**Save**).

### Set up Govee lighting

Govee uses LAN Control (UDP on the local network), not the Govee cloud.

1. In the Govee app, enable **LAN Control** for each light you want at the table.
2. On **Atmosphere**, click **Scan LAN**.
3. Select the lights that should follow the next mapping. Color, brightness, and power come from **Look** (moods or the picker) and follow the same transition as Hue.

Govee has no local scene catalog. World Weaver stores the last scan so mappings can still find those lights later. If a light’s IP changes, scan again.

### Map scenes

Give the table a short name, then attach any combination of:

- a Home Assistant scene (`scene.tavern`, or pick from the loaded catalog)
- Hue lights with a Look, or a Hue scene from the loaded Hue catalog (optionally limited to selected Hue lights)
- Govee lights selected above

**Add scene** requires at least one of those targets.

### At the table

**Pop out** opens a floating **Atmosphere** window. Named scenes, selected Hue and Govee lights, moods, the color picker, transition, one-shot effects, and live looping effects live in that tray. Turn on **Always on top** to keep it above other apps (the choice is remembered). Tonight embeds the same scene and effect controls; **Pop out** on Tonight opens the floating tray. Click a named scene to activate it, use Look to set color and brightness on the selected lights, fire a one-shot effect when something happens at the table, or start a live loop such as Fire or Water.

If the tray says you are not connected, connect Home Assistant, pair Hue, or scan Govee lights on the Atmosphere screen first.

Home Assistant URL and token, Hue bridge host and application key, loaded Hue lights, scanned Govee devices, selected lights for Look, custom moods, and scene mappings stay on this computer. They are included in a **Settings** `.wwbackup` so a restore can reconnect. They are not included in a world `.wwbundle`.

## Search

The field at the top of the main window searches:

- Worlds
- Campaigns
- Locations
- Lore
- Holidays
- Sky (celestial bodies)
- Factions
- People
- Quests
- Sessions

Choosing a result activates the needed world or campaign and opens that screen. Empty query results show **No matching records.**

Search does not include encounters, battle maps, dice history, or atmosphere scenes.

## Settings

**Settings** covers appearance, navigation, profile, backups, and the 5E SRD catalog.

### Appearance

- **Mode:** Light, Dark, System
- **Skin:** Fantasy, Sci-Fi, Modern, Dark Academia, High Fantasy, Gothic, Steampunk, Cyberpunk, Cozy Tavern, Minimal Monochrome

The sidebar theme button cycles Light → Dark → System.

### Navigation

**Expanded sidebar** keeps labels visible. Turn it off for an icon rail. The choice is remembered. You can also collapse the rail from the chevron on the sidebar.

### Local profile

Edit **Name** and **Email**, then the save icon (**Save profile**). This is display-only on this machine.

### Backup and restore

**Export backup** writes a `.wwbackup` (default name like `worldweaver-YYYYMMDD.wwbackup`). The archive includes worlds, campaigns, maps, avatars, voice clips, imported SRD, appearance/profile, and this computer’s Home Assistant connection plus atmosphere scene mappings. A backup contains the Home Assistant access token. Session recordings on Tonight are not included.

**Restore backup** **replaces all World Weaver data on this computer**, then quits so you can reopen with the restored files. Confirm with **Restore and quit**. Export first if you might need the current machine’s data.

### 5E SRD catalog

Character pickers start from bundled 5E lists. **Import bundled SRD** or **Import from file** (JSON) adds races, classes, spells, and monsters. Status text shows counts after import. **Clear import** returns pickers to the bundled lists and does not delete people you already made.

## Files, bundles, and backups

| Extension | What it is |
|-----------|------------|
| `.wwbundle` | One world (export/import from **Worlds**; Foundry companion module) |
| `.wwbackup` | Entire app data for this machine (**Settings**) |
| `.uvtt` | One battle map for a virtual tabletop (**Maps → Export VTT**) |
| `.png` | World maps and battle maps |
| `.wav` | Voice clips on locations and people; Tonight mic recordings |
| `.mp4` | Tonight camera recordings |
| `.json` | SRD catalog import |

On-disk folder: **`~/.worldweaver/`**

| Path | Contents |
|------|----------|
| `ww.db` | Database |
| `avatars/` | Person portraits |
| `maps/` | Battle map tiles |
| `world_maps/` | World map tiles |
| `voices/` | Voice clips |
| `recordings/` | Tonight session recordings (not in bundles or backups) |
| `srd/` | Imported SRD |

Appearance, profile, and Atmosphere (Home Assistant URL and token, Hue application key, scanned Govee devices, and scene mappings) also use this machine’s Java preferences. Copying only `~/.worldweaver` moves worlds and media; a `.wwbackup` is the supported way to migrate everything Settings and Atmosphere know about.

World Weaver is local-first. There is no cloud sync. To play on another computer, export a backup (or a world bundle) and import or restore there.

## Using World Weaver with Foundry

World Weaver stays the local GM laptop. Foundry stays the remote player table. There is no live sync of HP, fog, or tokens. A short product statement lives in [docs/FOUNDRY.md](FOUNDRY.md).

### Same-room vs remote

Use **Player view** when the table can see a second window on this computer. Use Foundry when players connect over the network. Do not run the same encounter in both.

### Drop a map into Foundry

1. Open **Maps**, select the battle map, and click **Export VTT**.
2. Save the `.uvtt` file. It embeds the PNG and the grid (columns × rows, pixels per square, and feet per square).
3. In Foundry, install [Universal Battlemap Importer](https://foundryvtt.com/packages/dd-import/), enable it in the world, and import the file.

The exporter does not write walls, doors, or lights. Draw those in Foundry if you need line of sight.

Without the exporter, import the PNG yourself and set the scene grid to the size shown under the map name (for example **20×20 · 5 ft**). Bundled small maps are 20×20; medium maps are 30×30.

### Bring lore and maps from a world bundle

1. On **Worlds**, **Export** the world to a `.wwbundle`.
2. Copy [`foundry/world-weaver/`](../foundry/world-weaver/) into Foundry’s `Data/modules/world-weaver` folder and enable **World Weaver** in the world.
3. Open the Journal directory, click **Import World Weaver bundle**, and choose the `.wwbundle`.

Lore entries become journal entries. **Secrets stay GM-only pages**; player-visible hints that you have already revealed are included on those secret pages. Locations become journals in nested folders. Battle maps become scenes with the background image and grid set. Re-importing the same bundle updates those documents; it does not stream changes while you play.

Character sheets and the combat tracker stay in World Weaver. The module does not create Foundry system actors.

## Sample worlds

This repository includes bundles you can import from **Worlds → Import world**:

- `fixtures/demo-campaign.wwbundle`
- `fixtures/shattered-accord.wwbundle`
- `fixtures/large-campaign.wwbundle`

Use these to click through locations, people, maps, and a campaign without starting from a blank world.
