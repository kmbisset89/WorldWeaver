# WorldWeaver story list

Implementation queue for the WorldWeaver rewrite. Use story IDs in commits and PRs.

**v1 source:** `/Users/kerry.bisset/dev/git/TactWare/WorldWeaver`

**This rewrite:** Compose Desktop shell only (Home, Worlds, Settings). No persistence and no domain models yet.

## Product decisions

- A **World** is the setting: places, lore, reusable people.
- A **World** contains one or more **Campaigns**: a play-through with PCs, sessions, quests, and encounters.
- Campaigns **reference** world people and places. They do not copy them by default.

## Ownership

TactWare scoped locations, lore, and characters to `campaignId`. This rewrite persists:

| Owner | `id` field | Examples |
|---|---|---|
| World | `worldId` | Locations, lore, world NPC/monster library, factions, calendar |
| Campaign | `campaignId` | PCs, quests, sessions, encounters, party presence overlay, campaign-only notes |

Campaign overlays (party presence, campaign notes on a location) must not mutate the world record.

## Implementation constraints

- MVI: UI sends `Interaction`, ViewModel exposes one `onInteraction` and one `ViewState` stream.
- Use cases are called only from ViewModels or other use cases. Single public `invoke`.
- One top-level class-like declaration per file.
- Do not add extension functions on types we own.
- Port ideas from TactWare. Do not copy campaign-scoped ownership or giant single-file screens.

## Status key

All stories start as **Not started** unless noted.

| Status | Meaning |
|---|---|
| Not started | Not implemented in this repo |
| Partial | Shell or UI exists; behavior is stubbed |
| Done | Implemented and persisted |

## Suggested build order

1. Phase 0 + Worlds/Campaigns + Home continue card — first playable loop.
2. Locations + Lore — world building.
3. Characters (world NPC library + campaign PCs) — people.
4. Quests + Sessions — prep.
5. Encounters + dice + search — run at the table.
6. Battle maps, then VTT.
7. Identity / multiplayer / Android / extra game systems last.

Do not start Phase 10 before Phases 0–8 are usable at the table.

---

## Phase 0 — Foundations

Shell exists. These stories make feature work possible.

### WW-FND-01 Persistence

As a DM, I persist worlds and later entities locally so closing the app does not lose work.

**Acceptance**

- Local database is created on first launch.
- Schema changes use versioned migrations from day one (no destructive wipe).
- App restart restores previously saved entities.

**v1:** Room at `~/.worldweaver/worldweaver.db` with destructive alpha migrations.

**Status:** Done — Room at `~/.worldweaver/ww.db`, schema version 1, exported migrations from day one.

### WW-FND-02 Dependency wiring

As a developer, I construct repositories and use cases outside the UI so screens stay MVI-only.

**Acceptance**

- ViewModels receive use cases or providers; they do not construct Room/DAOs.
- UI composables do not call repositories or use cases.

**v1:** Koin `@ComponentScan`.

**Status:** Done — explicit Koin module; ViewModels receive use cases.

### WW-FND-03 Active context

As a DM, I have an active World and optional active Campaign that all feature screens read, so I am never editing the wrong setting.

**Acceptance**

- Active world id and optional active campaign id survive restart.
- Feature screens operate only on that context.
- Clearing the active campaign does not clear the active world.

**v1:** `AppSettings` stored the active campaign id.

**Status:** Done — `active_world_id` and optional `active_campaign_id` in Preferences. Theme, skin, profile, and nav density share the same `net.tactware.worldweaver` node via `ShellSettingsStore`.

### WW-FND-04 Feature navigation

As a DM, I open Campaigns, Locations, Lore, Characters, Sessions, Encounters, and Settings from the shell once those screens exist.

**Acceptance**

- Sidebar (or equivalent) lists destinations as their screens ship.
- Current destination is highlighted.
- Destinations that are not built yet are not shown as working.

**Today:** Home / Worlds / Campaigns / Locations / Lore / Characters / Quests / Sessions / Encounters / Maps / Dice / Settings (`Screen.kt`). Global search is shell chrome, not a destination. Later destinations stay hidden.

**Status:** Partial — Dice is listed; remaining destinations stay hidden until they ship.

### WW-FND-05 Empty and error states

As a DM, I see a clear empty state when there is no world or no campaign, and I can retry failed loads.

**Acceptance**

- ViewState includes empty, loading, and error variants where a screen loads data.
- Error states offer retry through an Interaction.
- Empty states explain the next action (create world / create campaign).

### WW-FND-06 Confirm destructive actions

As a DM, delete flows ask for confirmation and can be cancelled.

**Acceptance**

- Delete of a world, campaign, or child entity requires confirm.
- Cancel leaves data unchanged.

---

## Phase 1 — Worlds and Campaigns

Home already promises this: “Create a world to start building places, people, and stories.”

### WW-WLD-01 Create world

As a DM, I create a world with a name, description, and default game system (start with 5E) so I have a setting to build in.

**Acceptance**

- Required name; description optional.
- Default mechanics is 5E.
- New world becomes listable and can be set active.

**Status:** Done — create dialog on Worlds; new world is persisted and set active.

### WW-WLD-02 List, open, and set active world

As a DM, I browse my worlds and open one as the active setting.

**Acceptance**

- Worlds screen lists saved worlds.
- Selecting a world sets it active and is visible in the shell.
- Home “recent worlds” can open the same worlds.

### WW-WLD-03 Edit world details

As a DM, I change a world’s name, description, and default game system.

**Acceptance**

- Edits persist and show on the next open.
- Invalid empty name is rejected.

### WW-WLD-04 Delete world

As a DM, I delete a world I no longer need, with a single consistent rule for campaigns inside it.

**Acceptance**

- Pick one rule and keep it: block delete while campaigns exist, or cascade delete campaigns and their play data after confirm.
- Confirm is required (WW-FND-06).

**Status:** Done — delete is blocked while any campaigns exist. Confirm is required.

### WW-WLD-05 Export and import world bundle

As a DM, I export a world (setting, campaigns, children, and images) to a backup file and import it as a new copy.

**Acceptance**

- Export writes a single `.wwbundle` archive for one world and all of its campaigns.
- Import always creates a new world with remapped IDs; existing data is never overwritten.
- Imported world is named `{original} (imported)` and becomes active.
- Avatars and battle-map files are included and remapped.

**Status:** Done — Worlds screen Export/Import; format version 1 zip with JSON + files.

### WW-CMP-01 Create campaign

As a DM, I create a campaign inside the active world (name, description, notes, mechanics inherited or overridden).

**Acceptance**

- Campaign is stored with `worldId`.
- Mechanics default to the world’s default and can be overridden.
- Campaign can be set active after create.

### WW-CMP-02 List campaigns and set active campaign

As a DM, I list campaigns for the active world and choose which one I am running.

**Acceptance**

- List is filtered to the active world.
- Setting a campaign active also implies its parent world is active.

### WW-CMP-03 Edit, archive, complete, or delete campaign

As a DM, I update campaign details or retire a campaign without losing the world.

**Acceptance**

- Edit name, description, notes, mechanics.
- Archive or complete hides the campaign from the default “active” list but keeps history.
- Delete requires confirm and does not delete the world.

### WW-CMP-04 Campaign overview

As a DM, I see party PCs, active quests, last session, and a next-session hint on one campaign screen.

**Acceptance**

- Overview uses real relations, not string lists.
- Empty sections say there are no PCs / quests / sessions yet.

**v1:** `playerCharacters`, `activeQuests`, and `completedQuests` were string lists on `Campaign`.

**Status:** Done — Party lists real campaign PCs. Active quests and last session use persisted records.

### WW-HOME-01 Home continue card

As a DM, Home shows recent worlds and a continue-active-campaign card instead of the hardcoded empty state.

**Acceptance**

- With no worlds: keep the create-world empty state.
- With worlds: show recent worlds and continue into the active campaign when one is set.

**v1:** Dashboard counts were hardcoded.

---

## Phase 2 — Locations (world-owned)

v1 hierarchy: Continent → Area → City → Place, plus climate, terrain, government, landmarks, history, and a party-presence flag.

### WW-LOC-01 Create location

As a DM, I create a location with a type and optional parent so the world has a geography.

**Acceptance**

- Types: Continent, Area, City, Place.
- Invalid parents are rejected (for example City under Place).
- Location is stored with `worldId`.

**Status:** Done — Continent → Area → City → Place; parent type is required and validated.

### WW-LOC-02 Browse as a tree

As a DM, I browse locations as a tree with breadcrumbs.

**Acceptance**

- Tree reflects parent/child links.
- Breadcrumbs show the path from the root type down to the selected place.

**Status:** Done — Locations screen tree plus breadcrumb path.

### WW-LOC-03 Edit location metadata

As a DM, I edit climate, terrain, government, landmarks, history, and world notes.

**Acceptance**

- Fields persist on the world location record.
- Landmarks are a list, not a single blob, if more than one is entered.

**Status:** Done — climate, terrain, government, landmarks (one per line), history, and world notes persist.

### WW-LOC-04 Delete location

As a DM, I delete a location with a single consistent rule for children.

**Acceptance**

- Pick one rule: children move to the parent, or children delete with the location after confirm.
- Confirm is required.

**Status:** Done — delete is blocked while child locations exist. Confirm is required. Overlay rows for the location are removed.

### WW-LOC-05 Campaign overlay on a location

As a DM, I mark party presence and campaign-only notes on a world location without changing the world record.

**Acceptance**

- Overlay is keyed by `campaignId` + `locationId`.
- Another campaign on the same world does not see this party flag or those notes.

**Status:** Done — `location_overlays` keyed by `campaignId` + `locationId`; world notes stay on the location.

### WW-LOC-06 Search and filter locations

As a DM, I search and filter locations in the active world.

**Acceptance**

- Filter by type and by name/text.
- Results stay inside the active world.

**Status:** Done — type chips and name/text search on the Locations screen.

---

## Phase 3 — Lore (world-owned)

v1: title, content, category, tags, related entries, secrets with hints.

### WW-LOR-01 Create, edit, and delete lore

As a DM, I write lore entries for the active world.

**Acceptance**

- Title and content required for save.
- Entries are stored with `worldId`.
- Delete requires confirm.

**Status:** Done — create/edit dialog on Lore; delete asks for confirm.

### WW-LOR-02 Categorize, tag, and browse

As a DM, I categorize and tag lore and browse by category.

**Acceptance**

- Category and tags persist.
- Browse groups or filters by category.

**Status:** Done — category chips group the list; tags persist as a list.

### WW-LOR-03 Link related lore

As a DM, I link related lore entries so I can jump between them.

**Acceptance**

- Related ids are stored on the entry.
- Broken links to deleted entries are cleaned up or shown as missing.

**Status:** Done — related ids persist; delete strips inbound links; missing ids show as missing.

### WW-LOR-04 DM secrets and hints

As a DM, I attach secrets with revealable hints that are never player-facing by default.

**Acceptance**

- Secrets and hints are stored on the lore entry.
- UI labels them as DM-only.
- A later player view (WW-MP-01) must not include them.

**Status:** Done — secrets and hints live in `lore_secrets` / `lore_hints` and are labeled DM-only.

### WW-LOR-05 Attach lore to a location or character

As a DM, I attach lore to a location or character.

**Acceptance**

- Optional links to `locationId` and/or character id.
- Opening the location or character can list attached lore.

**New vs v1.**

**Status:** Done — optional `locationId` and `characterId`; location and character detail list attached lore. The lore editor has a character picker.

---

## Phase 4 — Characters and people

v1: PC / NPC / Monster, 5E (and partial PF2E), full sheet, random NPC generator. Characters were campaign-scoped.

### WW-CHR-01 World NPC and monster library

As a DM, I create, edit, and delete reusable people of the setting on the world.

**Acceptance**

- Type is NPC or Monster.
- Records use `worldId`, not `campaignId`.
- Delete requires confirm.

**Status:** Done — `world_people` is world-owned; NPC and Monster only.

### WW-CHR-02 5E PC sheet

As a DM, I maintain a 5E player character on a campaign: abilities, HP/AC, classes, race, level, inventory, spells, features, death saves, and notes.

**Acceptance**

- PC is stored with `campaignId`.
- Multi-class levels sum to character level.
- Sheet fields persist across restart.

**Status:** Done — campaign PCs store a 5E sheet; character level is the sum of class levels.

### WW-CHR-03 Add a world NPC to a campaign

As a DM, I add a world NPC into a campaign as a reference, or create a campaign-only NPC that is not in the world library.

**Acceptance**

- Reference keeps the world person as source; campaign can add play-specific notes/HP.
- Campaign-only NPC is not listed in the world library.
- Deleting the world person is blocked or the campaign reference is marked missing — pick one rule.

**Status:** Done — campaign references keep overlay HP/notes; campaign-only NPCs stay off the world library. Deleting a world person is blocked while any campaign still references it.

### WW-CHR-04 Character relationships

As a DM, I record who knows whom and faction lean.

**Acceptance**

- Relationships can point at world people or campaign PCs.
- Faction lean can be a string until WW-FAC-01.

**v1:** `CharacterRelationship`.

**Status:** Done — `person_relationships` can point at a world person or a campaign person; faction lean is a string.

### WW-CHR-05 Random NPC generator

As a DM, I generate a random NPC (3d6 or 4d6 drop lowest) and save it into the world library.

**Acceptance**

- Generator writes a complete enough NPC to edit and save.
- Saved NPC appears in the world library.

**v1:** `RandomNpcGenerator`.

**Status:** Done — 3d6 or 4d6-drop-lowest preview; save writes a world-library NPC.

### WW-CHR-06 List and filter people

As a DM, I list and filter by type, campaign membership, and name.

**Acceptance**

- Filters: PC / NPC / Monster, in this campaign / world library, name search.
- Results respect active world/campaign context.

**Status:** Done — type, membership, and name filters on the Characters screen.

### WW-CHR-07 5E SRD pickers (static)

As a DM, I pick race, class, subclass, and spells from static 5E reference data.

**Acceptance**

- Pickers use bundled reference data, not a live import.
- Live SRD import is WW-SRD-01.

**Status:** Done — static race, class, subclass, and spell-name pickers; free text still allowed.

### WW-CHR-08 Character creation wizard

As a DM, I create a person through a stepped wizard (identity, race/class, abilities, companions, review) instead of one long form.

**Acceptance**

- New person opens the wizard. Edit still uses the existing editor for inventory, spells, and features.
- Required name; campaign membership requires an active campaign.
- Create persists the owner, then any companion people and links.

**Status:** Done — five-step create wizard; edit remains the existing dialog.

### WW-CHR-09 Familiars and animal companions

As a DM, I attach familiars and animal companions as linked NPC/Monster people so they keep their own sheet and can join encounters.

**Acceptance**

- Companion link is a dedicated association, not a social relationship.
- Companion kind is Familiar or Animal Companion. The companion person is NPC or Monster, not a PC.
- Campaign-only monsters can be created for companions.
- Character detail lists companions; encounter editor can add a participant’s companions.

**Status:** Done — `person_companions` links owner to companion people; encounter editor offers “Add companions”.

---

## Phase 5 — Quests

v1 stored quest titles as strings on the campaign. There was no quest UI.

### WW-QST-01 Create quest

As a DM, I create a quest on a campaign with title, summary, status, and an optional linked location.

**Acceptance**

- Quest uses `campaignId`.
- Linked location is a world location id, if present.

**Status:** Done — campaign-owned quests with title, summary, status, and optional world location.

### WW-QST-02 Objectives

As a DM, I add objectives or steps and mark them complete or failed.

**Acceptance**

- Steps persist in order.
- Complete and fail are distinct states.

**Status:** Done — ordered `quest_objectives` with Open / Complete / Failed.

### WW-QST-03 Active and completed quests

As a DM, I move a quest between Active and Completed and see that on the campaign overview.

**Acceptance**

- Status change updates WW-CMP-04.
- Completed quests remain readable.

**Status:** Done — Active/Completed toggle; campaign overview lists active quest titles.

### WW-QST-04 Link quests

As a DM, I link quests to lore, NPCs, and sessions.

**Acceptance**

- Optional links in both directions or from the quest detail at minimum.

**Status:** Done — quest links to lore, people, and sessions; inbound lists on location, character, and session detail.

---

## Phase 6 — Sessions

v1: session records, plot threads, reference docs, scene plans, march order, NPC drafts. Voice recorder was “coming soon.”

### WW-SES-01 Session CRUD

As a DM, I create, select, edit, and delete a session under the active campaign.

**Acceptance**

- Session uses `campaignId`.
- Delete requires confirm.

**Status:** Done — Sessions screen CRUD under the active campaign; delete asks for confirm.

### WW-SES-02 Notes and scene plans

As a DM, I keep session notes and an ordered scene plan list.

**Acceptance**

- Notes and scenes persist.
- Scenes can be reordered.

**Status:** Done — notes persist on the session; scenes persist in order with up/down.

### WW-SES-03 Plot threads

As a DM, I track plot threads with status and priority across sessions.

**Acceptance**

- Threads belong to the campaign and may point at a session.
- Status and priority are visible on the session screen.

**Status:** Done — campaign-owned threads with optional session attach; status and priority show on the session screen.

### WW-SES-04 Reference docs

As a DM, I attach a local path or URL to a session or campaign.

**Acceptance**

- Path/URL is stored; the app does not need to preview the file in this story.
- Broken paths are still listed.

**Status:** Done — title + path/URL on the campaign, optional session attach; no preview.

### WW-SES-05 Party march order

As a DM, I store a party march order / snapshot for the session.

**Acceptance**

- Snapshot lists campaign PCs (and optional NPCs) in order.
- Snapshot does not overwrite the live PC sheets.

**Status:** Done — ordered snapshot with display names; live PC sheets are not written.

### WW-SES-06 Save NPC drafts from session

As a DM, I save NPC drafts from the session into the world library or as a campaign-only NPC.

**Acceptance**

- User chooses world library vs campaign-only (WW-CHR-03).

**Status:** Done — generate a draft in the session UI, then save to the world library or as a campaign-only NPC.

### WW-SES-07 Start-of-session checklist

As a DM, I see active quests, last-session recap, and party location when I start a session.

**Acceptance**

- Checklist is derived from quest, previous session notes, and location overlay.
- Missing pieces show as empty, not errors.

**Status:** Done — derived checklist on session detail: active quests, previous notes, party location.

---

## Phase 7 — Encounters

v1: planning, manual d20 initiative, sort, AC/HP, round/turn. Persistence and conditions started as v2 in TactWare `docs/encounters.md`; the old code later persisted encounters and tracked turns.

### WW-ENC-01 Encounter CRUD

As a DM, I create, edit, and delete encounters on a campaign (name, location, difficulty, notes).

**Acceptance**

- Encounter uses `campaignId`.
- Location is a world location reference when set.

**Status:** Done

### WW-ENC-02 Add participants

As a DM, I add participants from campaign PCs, world NPCs, or a quick nameless combatant.

**Acceptance**

- Linked participants keep a reference id.
- Nameless combatants do not require a character record.

**Status:** Done

### WW-ENC-03 Initiative

As a DM, I roll or enter initiative, apply the bonus, and sort the order.

**Acceptance**

- Manual entry works even before WW-DICE-01.
- Sort is by total initiative; ties have a defined rule.

**Tie rule:** higher total first, then name (case-insensitive), then participant id.

**Status:** Done

### WW-ENC-04 Run encounter

As a DM, I track the current turn, move next/prev, and increment the round.

**Acceptance**

- Current turn index and round persist while the encounter is active.
- Only one encounter per campaign is active at a time, or the UI makes the active one obvious.

**Active rule:** starting an encounter sets it Active and returns any other Active encounter in the same campaign to Planned. The list highlights Active.

**Status:** Done

### WW-ENC-05 Combat bookkeeping

As a DM, I apply damage, heal, and temp HP; set conditions; and optionally group “Goblin x4.”

**Acceptance**

- HP cannot silently go below 0 without a visible downed/dead state.
- Conditions are a list on the participant.
- Grouped combatants can share a name and be tracked as a count or as individuals — pick one and document it.

**Grouping:** nameless count > 1 creates individual rows (Goblin 1…N) by default. “Swarm (shared HP)” keeps the old `groupCount` shared pool.

**Status:** Done

### WW-ENC-06 End encounter

As a DM, I end an encounter and write a short outcome note back to the current session.

**Acceptance**

- Encounter is no longer active.
- Outcome note appears on the session if a session is active; otherwise it stays on the encounter.

**Current session:** the campaign session with the latest `updatedAt` (then `createdAt`). There is no separate active-session preference yet. Outcome is always stored on the encounter; it is appended to that session only when one exists.

**Status:** Done

### WW-ENC-07 Plan and run layouts

As a DM, I set up a fight in a library pane and run it in a separate combat console.

**Acceptance**

- Planned and ended encounters open a setup pane (identity + roster), not a dialog.
- Starting an Active encounter (or selecting it) opens the Run console: compact tracker + Next turn.
- Library returns to the list without ending the fight.

**Status:** Done

### WW-ENC-08 Roll all initiative

As a DM, I roll initiative for every combatant missing a roll with one action.

**Acceptance**

- Setup and Run both offer Roll all.
- Existing totals are left alone unless overwrite is requested.
- A warning lists how many combatants still have no initiative before Start.

**Status:** Done

### WW-ENC-09 Inline combat HP

As a DM, I apply damage, heal, and temp HP on the selected combatant only.

**Acceptance**

- There is no shared amount field for the whole screen.
- Selecting a row (or token) shows that row’s amount pad.
- Initiative fields stay on setup, not on live cards.

**Status:** Done

### WW-ENC-10 Embedded battle map

As a DM, I place tokens and see the board beside the tracker while the fight is running.

**Acceptance**

- Run embeds the attached map; Open map from setup still goes to Maps authoring.
- Token click selects the combatant; cell click places or moves them.
- Player view opens from the combat console.

**Status:** Done

### WW-ENC-11 Condition picker

As a DM, I toggle standard 5E conditions from a known list.

**Acceptance**

- The picker lists PHB conditions.
- Applied conditions persist as today’s string list (no schema change).

**Status:** Done

### WW-ENC-12 Combat death saves

As a DM, I track death saves on a downed campaign person from the combat console.

**Acceptance**

- Downed campaign-person participants show 0–3 success and failure boxes.
- Values write through to the linked character sheet.
- Three failures mark the participant Dead.

**Status:** Done

### WW-ENC-13 Roster shortcuts

As a DM, I add the party in one click and expand nameless groups into individuals.

**Acceptance**

- Add party adds every campaign PC not already on the roster.
- Nameless count > 1 creates Goblin 1…N unless Swarm (shared HP) is selected.

**Status:** Done

### WW-ENC-14 Action economy

As a DM, I track attacks used, bonus action, and reaction on each combatant during a fight.

**Acceptance**

- Selected combatant can mark attacks used, set attacks allowed (default 1), and toggle bonus/reaction.
- Roster rows show current spend (`Atk used/allowed`, BA, R).
- Next turn clears the arriving combatant’s used attacks, bonus, and reaction; allowed stays.
- Start encounter clears spend for all participants.
- Values persist across restart and bundle export. Older bundles default to 1 / 0 / unused.

**Status:** Done

---

## Phase 8 — Table tools v1 never finished

### WW-DICE-01 Standalone dice roller

As a DM, I roll common polyhedrals with advantage/disadvantage and see a history.

**Acceptance**

- d4, d6, d8, d10, d12, d20, d100, and a modifier.
- Advantage/disadvantage on d20.
- History is at least in-session; persist if cheap.

**History:** in-session on the singleton Dice screen ViewModel. Not persisted across app restart.

**Status:** Done

### WW-DICE-02 Shared roller

As a DM, I use the same roller from initiative, ability checks, and NPC generation.

**Acceptance**

- Encounter initiative and NPC generator call the same roll API.
- Results can be accepted or overwritten manually.

**Status:** Done — `DiceRoller` is the shared API. Encounter initiative and the NPC generator delegate to it. Manual initiative overwrite is unchanged.

### WW-DICE-03 Physical feel and table entry

As a DM, I roll dice that look like the ones on the table, pick a color, type a notation like `2d6+3`, or log the faces I already rolled.

**Acceptance**

- Standalone Dice screen shows 2D die faces that tumble on a digital roll.
- Color/style swatches persist across restart.
- Notation field stays in sync with die, count, modifier, and advantage.
- Table mode logs comma-separated faces into the same history, marked as table rolls.

**Status:** Done

### WW-SRCH-01 Global search

As a DM, I search across worlds, campaigns, locations, lore, characters, quests, and sessions.

**Acceptance**

- Results navigate to the matching record and set context if needed.
- DM secrets are not shown in result snippets that a later player view could reuse.

**v1:** Top bar always said “No results yet.”

**Search fields:** world name/description; campaign name/description/notes; location name/metadata/landmarks/world notes; lore title/content/tags (never secrets or hints); person name/description; quest title/summary; session name/notes.

**Status:** Done — shell search bar; opening a hit sets world/campaign context and selects the record.

### WW-MAP-01 Import battle map

As a DM, I import a battle-map image, tile it, zoom, and open a viewer.

**Acceptance**

- PNG (or equivalent) import stores the image and tile grid.
- Viewer supports zoom.

**v1:** MapCompose MP, PNG + tile entities.

**Status:** Done — PNG import slices a 256px pyramid onto disk; Maps screen viewer zooms with MapComposeMP.

### WW-MAP-02 Attach map to encounter

As a DM, I attach a battle map to an encounter and open it while the encounter is active.

**Acceptance**

- Encounter detail has an open-map action when a map is attached.

**Status:** Done — `encounter.battleMapId` persists; Open map navigates to the Maps viewer.

### WW-MAP-03 Battle map maker

As a DM, I preview an imported image with a gameplay grid and render-tile overlay, set scale/rows/cols/units, then save the tiled map.

**Acceptance**

- Maker opens from Maps (replaces the small Import dialog).
- Required name; PNG (or ImageIO-readable) image.
- Gameplay grid (columns, rows, unit name, units per tile) is separate from 256px MapCompose tiles.
- Preview shows the image plus optional gameplay grid and 256px render-tile lines.
- Import scale (10–400%) is applied before tiling.
- Save writes the disk pyramid and persists grid metadata.
- Invalid grid (blank name, missing image, columns/rows < 1, columns > image width, rows > image height) is rejected.

**Status:** Done — Maps maker pane with preview overlays; grid fields persist on `battle_maps`.

### WW-MAP-04 Situation layers

As a DM, I attach named PNG layers to a battle map (flood, cave-in, revealed room) and toggle them without re-importing the map.

**Acceptance**

- Add a situation from the Maps viewer (name + ImageIO-readable PNG).
- Overlay is fitted to the base map size and stored as a tile pyramid under `~/.worldweaver/maps/{mapId}/situations/{id}`.
- Toggle shows or hides the layer on the existing MapCompose viewer.
- Visible flag persists. Delete removes the row and files.
- Invalid name, missing map, or unreadable image is rejected.
- Deleting a map removes its situations.

**Status:** Done — `battle_map_situations` plus stacked MapCompose layers; not fog of war (WW-VTT-02).

### WW-MAP-05 Player table window

As a DM, I open a map-only player window I can drag to a TV or second monitor.

**Acceptance**

- Maps and Encounter detail offer Player view when a map is available.
- The second window shows the map, visible situation layers, movement dots, and placed tokens only.
- Closing the player window does not quit the app.
- Player pan/zoom is independent of the DM viewer. Situation toggles and range dots stay in sync.

**Status:** Done — Compose `Window` from App; dual `MapState` via `BattleMapMapStateFactory`.

### WW-HOME-02 Dashboard counts

As a DM, I see dashboard counts and continue cards from real data.

**Acceptance**

- Counts match persisted worlds, campaigns, and people.
- No hardcoded totals.

**Status:** Done — Home count cards use persisted world, campaign, and people totals.

---

## Phase 9 — Settings and shell polish

### WW-SET-01 Persist theme

As a DM, my light / dark / system theme survives restart.

**Acceptance**

- Theme is stored and applied on launch.

**Status:** Done — `ShellSettingsStore` owns `theme_mode` under `net.tactware.worldweaver`. A leftover root `theme_mode` key is migrated once.

### WW-SET-02 Theme skins

As a DM, I pick a skin beyond light/dark (Fantasy, Gothic, Cozy Tavern, and the rest of the v1 set).

**Acceptance**

- Skin applies to the shell and feature screens.
- Skin persists.

**v1:** 10 skins.

**Status:** Done — Fantasy, Sci-Fi, Modern, Dark Academia, High Fantasy, Gothic, Steampunk, Cyberpunk, Cozy Tavern, Minimal Monochrome. Light/dark/system still applies on top.

### WW-SET-03 Local profile

As a DM, I edit the display name (and local email if kept) shown in Settings and the sidebar.

**Acceptance**

- Edits persist.
- Settings is no longer read-only for the profile.

**Status:** Done — Settings edits name and email; sidebar and Home greet with the saved name.

### WW-SET-04 Nav density

As a DM, I collapse the navigation rail to save space.

**Acceptance**

- Collapsed and expanded states persist.

**v1:** Nav rail preference.

**Status:** Done — chevron on the rail and a Settings switch; `nav_expanded` persists.

### WW-SET-05 Backup and restore

As a DM, I export the local database and restore it on a new machine.

**Acceptance**

- Export produces a single backup artifact.
- Restore replaces or merges with a stated rule and a confirm.

**Replace rule:** restore replaces `~/.worldweaver/` (`ww.db`, avatars, maps) and the saved Preferences snapshot (active world/campaign, profile, appearance, dice color). No merge. WorldWeaver quits after restore so Room can reopen the replaced files.

**Status:** Done — Settings Export/Restore writes a `.wwbackup` zip (manifest, prefs, `data/ww.db`, avatars, maps). Confirm is required.

### WW-SET-06 Notifications

As a DM, notifications are real (session reminders, unfinished encounter) or they are removed until they are.

**Acceptance**

- Either: notifications are created from real events and can be dismissed.
- Or: the hardcoded welcome notification and panel are removed.

**Status:** Done — hardcoded welcome notification and panel removed until real events exist.

---

## Phase 10 — Later epics

Do not start these before Phases 0–8 are usable.

### Voice and table aids

#### WW-VOC-01 Voice clips

As a DM, I record or attach a voice clip to an NPC or location (accent, pronunciation).

**Acceptance**

- A location or person can have one WAV clip, recorded in the app or attached from disk.
- Play and remove work from the detail pane.
- The clip is stored as a sidecar file and survives restart, world-bundle export, and app backup.
- Adding a world person to a campaign copies their clip. Deleting the person or location removes it.

**Status:** Done — Voice clip card on character and location detail; sidecar files under `~/.worldweaver/voices/`.

**v1:** Session screen button labeled “Coming soon.” The clip now lives on the person or location, not the session screen.

#### WW-CAL-01 World calendar

As a DM, I keep a world calendar and stamp an in-world date on sessions.

**Acceptance**

- Each world has exactly one calendar: ordered months (name + days), ordered weekdays (name), optional era suffix, optional current world date.
- Creating a world seeds a Gregorian-like default (12 months, Feb 28, Sun–Sat, empty era, no current date). Existing worlds get the same default on migrate.
- A session can stamp an optional in-world date (year + month + day). Invalid dates are rejected. New sessions default to the calendar’s current date, else the previous session’s stamp.
- Formatted stamp looks like `Moonday, 12 Flamerule, 1492 DR` (weekday from year 1 / first month / day 1 = first weekday; omit weekday or era when empty).
- Calendar and stamps survive restart, world-bundle export/import (old bundles without a calendar get the default), and app backup.
- Delete a month that is referenced by a session stamp or the current date is blocked.

**Status:** Done — Calendar screen on the world; session editor stamps year/month/day; schema 16.

#### WW-FAC-01 Factions

As a DM, I manage factions as first-class world entities, not only a field on a character.

### Rules and content

#### WW-SRD-01 Import 5E SRD

As a DM, I import 5E SRD reference data (races, classes, spells, monsters) instead of relying only on bundled pickers.

#### WW-SYS-01 Pathfinder 2E sheets

As a DM, I use PF2E character sheets that are not a 5E skin.

**v1:** Declared PF2E; UI was incomplete.

#### WW-SYS-02 Other game systems

As a DM, I can pick Call of Cthulhu, Starfinder, FATE, Savage Worlds, or Custom only when that system has its own slice.

**Acceptance**

- Do not show a working dropdown for unsupported systems.
- Each system is a later slice, not a fake enum value in the UI.

**v1:** `GameMechanics` enum listed these; character UI fell back to 5E.

### Virtual table

#### WW-VTT-01 Tokens

As a DM, I place encounter participants as tokens on the battle map and they stay there.

**Status:** Done — click a cell to place or move the selected participant (current turn by default); click a token to select it and show range. Positions persist on the encounter. People have PNG avatars (list, detail, and map tokens; initials if none).

**Acceptance**

- Tokens appear on both the DM map and the player window.
- Restarting the app keeps token cells.
- A person avatar shows on the character list, detail, and their map token.

#### WW-VTT-02 Fog of war

As a DM, I hide and reveal map areas; a player view does not see unrevealed areas.

**Acceptance**

- Fog is stored on the battle map and survives restart and world-bundle export.
- Existing maps stay fully visible until the DM hides cells or uses Hide all.
- Player View paints unrevealed cells black and omits tokens and walk dots on those cells.
- The DM map still shows the full image, with a translucent overlay on hidden cells.

**Status:** Done — Fog paint on Maps and the combat console; `fogEnabled` + revealed cells on `battle_maps` (schema 15).

#### WW-VTT-03 Measure and range

As a DM, I measure distance and range on the map.

**Acceptance**

- Walking-range dots still come from 5E `walkSpeed` (Chebyshev).
- Measure mode is a DM-only tape: click two cells for squares and units.
- Measure clicks do not place or move tokens.
- Player View does not show the tape.

**Status:** Done — Measure chip on Maps and the combat console; Chebyshev tape overlay with a distance label.

#### WW-VTT-04 Token visibility and status

As a DM, I hide or reveal people on the player window and show combat status on the board pieces.

**Status:** Done — `visibleToPlayers` persists on the participant; Hidden stays on the DM map and encounter list. Player View omits hidden tokens and their walk dots. Tokens are square pieces with name, combat state, and conditions.

**Acceptance**

- Hidden people stay on the DM map and encounter list; they disappear from Player View immediately.
- Changing combat state or conditions updates both windows without moving the piece.
- Restarting the app keeps hide/show.
- Closing Player View does not quit the app.

### Identity and multiplayer

#### WW-ID-01 Real account

As a DM, I sign in with a real account.

**Today:** Sidebar logout shows “Sign out is not configured.” Settings shows a local email.

#### WW-MP-01 Player invite

As a DM, I invite players to a campaign as read-limited viewers who cannot see DM secrets.

#### WW-MP-02 Live table

As a DM and players, we see live encounter and map updates when connected.

#### WW-MP-03 Player sheet

As a player, I edit my HP and inventory; the DM can override.

### Platform

#### WW-AND-01 Android target

As a DM, I run the same domain and use cases on Android.

**v1:** README mentioned Android; no `androidMain` source set existed.

#### WW-AND-02 Mobile navigation

As a DM on a phone, I navigate without a desktop sidebar.

### Hardening

#### WW-QA-01 Migrations

As a DM, schema upgrades keep my data.

#### WW-QA-02 Ownership tests

As a developer, use-case tests cover world/campaign ownership and cascade rules.

#### WW-QA-03 No orphan caches

As a developer, there is no leftover in-memory service beside the database of record.

**v1:** `LocationService` still had hardcoded sample locations after Room was the real path.

### Optional import from v1

#### WW-MIG-01 Import TactWare database

As a DM, I import an old `~/.worldweaver/worldweaver.db` into the new World/Campaign ownership model.

**Acceptance**

- Campaign-scoped locations and lore become world-owned under a chosen or created world.
- Import is explicit and confirmable; it is not the default first-run path.

---

## Story index

| ID | Phase | Title | Owner | Status |
|---|---|---|---|---|
| WW-FND-01 | 0 | Persistence | App | Done |
| WW-FND-02 | 0 | Dependency wiring | App | Done |
| WW-FND-03 | 0 | Active context | App | Done |
| WW-FND-04 | 0 | Feature navigation | App | Partial |
| WW-FND-05 | 0 | Empty and error states | App | Done |
| WW-FND-06 | 0 | Confirm destructive actions | App | Done |
| WW-WLD-01 | 1 | Create world | World | Done |
| WW-WLD-02 | 1 | List / open / set active world | World | Done |
| WW-WLD-03 | 1 | Edit world details | World | Done |
| WW-WLD-04 | 1 | Delete world | World | Done |
| WW-WLD-05 | 1 | Export and import world bundle | World | Done |
| WW-CMP-01 | 1 | Create campaign | Campaign | Done |
| WW-CMP-02 | 1 | List campaigns / set active | Campaign | Done |
| WW-CMP-03 | 1 | Edit / archive / complete / delete campaign | Campaign | Done |
| WW-CMP-04 | 1 | Campaign overview | Campaign | Done |
| WW-HOME-01 | 1 | Home continue card | App | Done |
| WW-LOC-01 | 2 | Create location | World | Done |
| WW-LOC-02 | 2 | Browse as a tree | World | Done |
| WW-LOC-03 | 2 | Edit location metadata | World | Done |
| WW-LOC-04 | 2 | Delete location | World | Done |
| WW-LOC-05 | 2 | Campaign overlay on a location | Campaign | Done |
| WW-LOC-06 | 2 | Search / filter locations | World | Done |
| WW-LOR-01 | 3 | Create / edit / delete lore | World | Done |
| WW-LOR-02 | 3 | Categorize, tag, browse | World | Done |
| WW-LOR-03 | 3 | Link related lore | World | Done |
| WW-LOR-04 | 3 | DM secrets and hints | World | Done |
| WW-LOR-05 | 3 | Attach lore to location or character | World | Done |
| WW-CHR-01 | 4 | World NPC / monster library | World | Done |
| WW-CHR-02 | 4 | 5E PC sheet | Campaign | Done |
| WW-CHR-03 | 4 | Add world NPC to campaign | Campaign | Done |
| WW-CHR-04 | 4 | Character relationships | Both | Done |
| WW-CHR-05 | 4 | Random NPC generator | World | Done |
| WW-CHR-06 | 4 | List / filter people | Both | Done |
| WW-CHR-07 | 4 | 5E SRD pickers (static) | World | Done |
| WW-CHR-08 | 4 | Character creation wizard | Both | Done |
| WW-CHR-09 | 4 | Familiars and animal companions | Both | Done |
| WW-QST-01 | 5 | Create quest | Campaign | Done |
| WW-QST-02 | 5 | Objectives | Campaign | Done |
| WW-QST-03 | 5 | Active and completed quests | Campaign | Done |
| WW-QST-04 | 5 | Link quests | Campaign | Done |
| WW-SES-01 | 6 | Session CRUD | Campaign | Done |
| WW-SES-02 | 6 | Notes and scene plans | Campaign | Done |
| WW-SES-03 | 6 | Plot threads | Campaign | Done |
| WW-SES-04 | 6 | Reference docs | Campaign | Done |
| WW-SES-05 | 6 | Party march order | Campaign | Done |
| WW-SES-06 | 6 | Save NPC drafts from session | Both | Done |
| WW-SES-07 | 6 | Start-of-session checklist | Campaign | Done |
| WW-ENC-01 | 7 | Encounter CRUD | Campaign | Done |
| WW-ENC-02 | 7 | Add participants | Campaign | Done |
| WW-ENC-03 | 7 | Initiative | Campaign | Done |
| WW-ENC-04 | 7 | Run encounter | Campaign | Done |
| WW-ENC-05 | 7 | Combat bookkeeping | Campaign | Done |
| WW-ENC-06 | 7 | End encounter | Campaign | Done |
| WW-ENC-07 | 7 | Plan and run layouts | Campaign | Done |
| WW-ENC-08 | 7 | Roll all initiative | Campaign | Done |
| WW-ENC-09 | 7 | Inline combat HP | Campaign | Done |
| WW-ENC-10 | 7 | Embedded battle map | Campaign | Done |
| WW-ENC-11 | 7 | Condition picker | Campaign | Done |
| WW-ENC-12 | 7 | Combat death saves | Campaign | Done |
| WW-ENC-13 | 7 | Roster shortcuts | Campaign | Done |
| WW-ENC-14 | 7 | Action economy | Campaign | Done |
| WW-DICE-01 | 8 | Standalone dice roller | App | Done |
| WW-DICE-02 | 8 | Shared roller | App | Done |
| WW-DICE-03 | 8 | Physical feel and table entry | App | Done |
| WW-SRCH-01 | 8 | Global search | App | Done |
| WW-MAP-01 | 8 | Import battle map | Campaign | Done |
| WW-MAP-02 | 8 | Attach map to encounter | Campaign | Done |
| WW-MAP-03 | 8 | Battle map maker | Campaign | Done |
| WW-MAP-04 | 8 | Situation layers | Campaign | Done |
| WW-MAP-05 | 8 | Player table window | Campaign | Done |
| WW-HOME-02 | 8 | Dashboard counts | App | Done |
| WW-SET-01 | 9 | Persist theme | App | Done |
| WW-SET-02 | 9 | Theme skins | App | Done |
| WW-SET-03 | 9 | Local profile | App | Done |
| WW-SET-04 | 9 | Nav density | App | Done |
| WW-SET-05 | 9 | Backup and restore | App | Done |
| WW-SET-06 | 9 | Notifications | App | Done |
| WW-VOC-01 | 10 | Voice clips | World | Done |
| WW-CAL-01 | 10 | World calendar | World | Done |
| WW-FAC-01 | 10 | Factions | World | Not started |
| WW-SRD-01 | 10 | Import 5E SRD | App | Not started |
| WW-SYS-01 | 10 | Pathfinder 2E sheets | Campaign | Not started |
| WW-SYS-02 | 10 | Other game systems | App | Not started |
| WW-VTT-01 | 10 | Tokens | Campaign | Done |
| WW-VTT-02 | 10 | Fog of war | Campaign | Done |
| WW-VTT-03 | 10 | Measure and range | Campaign | Done |
| WW-VTT-04 | 10 | Token visibility and status | Campaign | Done |
| WW-ID-01 | 10 | Real account | App | Not started |
| WW-MP-01 | 10 | Player invite | Campaign | Not started |
| WW-MP-02 | 10 | Live table | Campaign | Not started |
| WW-MP-03 | 10 | Player sheet | Campaign | Not started |
| WW-AND-01 | 10 | Android target | App | Not started |
| WW-AND-02 | 10 | Mobile navigation | App | Not started |
| WW-QA-01 | 10 | Migrations | App | Not started |
| WW-QA-02 | 10 | Ownership tests | App | Not started |
| WW-QA-03 | 10 | No orphan caches | App | Not started |
| WW-MIG-01 | 10 | Import TactWare database | App | Not started |
