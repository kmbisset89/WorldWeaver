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

**Status:** Done — `active_world_id` and optional `active_campaign_id` in Preferences.

### WW-FND-04 Feature navigation

As a DM, I open Campaigns, Locations, Lore, Characters, Sessions, Encounters, and Settings from the shell once those screens exist.

**Acceptance**

- Sidebar (or equivalent) lists destinations as their screens ship.
- Current destination is highlighted.
- Destinations that are not built yet are not shown as working.

**Today:** Home / Worlds / Campaigns / Settings (`Screen.kt`). Later destinations stay hidden.

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

### WW-LOC-02 Browse as a tree

As a DM, I browse locations as a tree with breadcrumbs.

**Acceptance**

- Tree reflects parent/child links.
- Breadcrumbs show the path from the root type down to the selected place.

### WW-LOC-03 Edit location metadata

As a DM, I edit climate, terrain, government, landmarks, history, and world notes.

**Acceptance**

- Fields persist on the world location record.
- Landmarks are a list, not a single blob, if more than one is entered.

### WW-LOC-04 Delete location

As a DM, I delete a location with a single consistent rule for children.

**Acceptance**

- Pick one rule: children move to the parent, or children delete with the location after confirm.
- Confirm is required.

### WW-LOC-05 Campaign overlay on a location

As a DM, I mark party presence and campaign-only notes on a world location without changing the world record.

**Acceptance**

- Overlay is keyed by `campaignId` + `locationId`.
- Another campaign on the same world does not see this party flag or those notes.

### WW-LOC-06 Search and filter locations

As a DM, I search and filter locations in the active world.

**Acceptance**

- Filter by type and by name/text.
- Results stay inside the active world.

---

## Phase 3 — Lore (world-owned)

v1: title, content, category, tags, related entries, secrets with hints.

### WW-LOR-01 Create, edit, and delete lore

As a DM, I write lore entries for the active world.

**Acceptance**

- Title and content required for save.
- Entries are stored with `worldId`.
- Delete requires confirm.

### WW-LOR-02 Categorize, tag, and browse

As a DM, I categorize and tag lore and browse by category.

**Acceptance**

- Category and tags persist.
- Browse groups or filters by category.

### WW-LOR-03 Link related lore

As a DM, I link related lore entries so I can jump between them.

**Acceptance**

- Related ids are stored on the entry.
- Broken links to deleted entries are cleaned up or shown as missing.

### WW-LOR-04 DM secrets and hints

As a DM, I attach secrets with revealable hints that are never player-facing by default.

**Acceptance**

- Secrets and hints are stored on the lore entry.
- UI labels them as DM-only.
- A later player view (WW-MP-01) must not include them.

### WW-LOR-05 Attach lore to a location or character

As a DM, I attach lore to a location or character.

**Acceptance**

- Optional links to `locationId` and/or character id.
- Opening the location or character can list attached lore.

**New vs v1.**

---

## Phase 4 — Characters and people

v1: PC / NPC / Monster, 5E (and partial PF2E), full sheet, random NPC generator. Characters were campaign-scoped.

### WW-CHR-01 World NPC and monster library

As a DM, I create, edit, and delete reusable people of the setting on the world.

**Acceptance**

- Type is NPC or Monster.
- Records use `worldId`, not `campaignId`.
- Delete requires confirm.

### WW-CHR-02 5E PC sheet

As a DM, I maintain a 5E player character on a campaign: abilities, HP/AC, classes, race, level, inventory, spells, features, death saves, and notes.

**Acceptance**

- PC is stored with `campaignId`.
- Multi-class levels sum to character level.
- Sheet fields persist across restart.

### WW-CHR-03 Add a world NPC to a campaign

As a DM, I add a world NPC into a campaign as a reference, or create a campaign-only NPC that is not in the world library.

**Acceptance**

- Reference keeps the world person as source; campaign can add play-specific notes/HP.
- Campaign-only NPC is not listed in the world library.
- Deleting the world person is blocked or the campaign reference is marked missing — pick one rule.

### WW-CHR-04 Character relationships

As a DM, I record who knows whom and faction lean.

**Acceptance**

- Relationships can point at world people or campaign PCs.
- Faction lean can be a string until WW-FAC-01.

**v1:** `CharacterRelationship`.

### WW-CHR-05 Random NPC generator

As a DM, I generate a random NPC (3d6 or 4d6 drop lowest) and save it into the world library.

**Acceptance**

- Generator writes a complete enough NPC to edit and save.
- Saved NPC appears in the world library.

**v1:** `RandomNpcGenerator`.

### WW-CHR-06 List and filter people

As a DM, I list and filter by type, campaign membership, and name.

**Acceptance**

- Filters: PC / NPC / Monster, in this campaign / world library, name search.
- Results respect active world/campaign context.

### WW-CHR-07 5E SRD pickers (static)

As a DM, I pick race, class, subclass, and spells from static 5E reference data.

**Acceptance**

- Pickers use bundled reference data, not a live import.
- Live SRD import is WW-SRD-01.

---

## Phase 5 — Quests

v1 stored quest titles as strings on the campaign. There was no quest UI.

### WW-QST-01 Create quest

As a DM, I create a quest on a campaign with title, summary, status, and an optional linked location.

**Acceptance**

- Quest uses `campaignId`.
- Linked location is a world location id, if present.

### WW-QST-02 Objectives

As a DM, I add objectives or steps and mark them complete or failed.

**Acceptance**

- Steps persist in order.
- Complete and fail are distinct states.

### WW-QST-03 Active and completed quests

As a DM, I move a quest between Active and Completed and see that on the campaign overview.

**Acceptance**

- Status change updates WW-CMP-04.
- Completed quests remain readable.

### WW-QST-04 Link quests

As a DM, I link quests to lore, NPCs, and sessions.

**Acceptance**

- Optional links in both directions or from the quest detail at minimum.

---

## Phase 6 — Sessions

v1: session records, plot threads, reference docs, scene plans, march order, NPC drafts. Voice recorder was “coming soon.”

### WW-SES-01 Session CRUD

As a DM, I create, select, edit, and delete a session under the active campaign.

**Acceptance**

- Session uses `campaignId`.
- Delete requires confirm.

### WW-SES-02 Notes and scene plans

As a DM, I keep session notes and an ordered scene plan list.

**Acceptance**

- Notes and scenes persist.
- Scenes can be reordered.

### WW-SES-03 Plot threads

As a DM, I track plot threads with status and priority across sessions.

**Acceptance**

- Threads belong to the campaign and may point at a session.
- Status and priority are visible on the session screen.

### WW-SES-04 Reference docs

As a DM, I attach a local path or URL to a session or campaign.

**Acceptance**

- Path/URL is stored; the app does not need to preview the file in this story.
- Broken paths are still listed.

### WW-SES-05 Party march order

As a DM, I store a party march order / snapshot for the session.

**Acceptance**

- Snapshot lists campaign PCs (and optional NPCs) in order.
- Snapshot does not overwrite the live PC sheets.

### WW-SES-06 Save NPC drafts from session

As a DM, I save NPC drafts from the session into the world library or as a campaign-only NPC.

**Acceptance**

- User chooses world library vs campaign-only (WW-CHR-03).

### WW-SES-07 Start-of-session checklist

As a DM, I see active quests, last-session recap, and party location when I start a session.

**Acceptance**

- Checklist is derived from quest, previous session notes, and location overlay.
- Missing pieces show as empty, not errors.

---

## Phase 7 — Encounters

v1: planning, manual d20 initiative, sort, AC/HP, round/turn. Persistence and conditions started as v2 in TactWare `docs/encounters.md`; the old code later persisted encounters and tracked turns.

### WW-ENC-01 Encounter CRUD

As a DM, I create, edit, and delete encounters on a campaign (name, location, difficulty, notes).

**Acceptance**

- Encounter uses `campaignId`.
- Location is a world location reference when set.

### WW-ENC-02 Add participants

As a DM, I add participants from campaign PCs, world NPCs, or a quick nameless combatant.

**Acceptance**

- Linked participants keep a reference id.
- Nameless combatants do not require a character record.

### WW-ENC-03 Initiative

As a DM, I roll or enter initiative, apply the bonus, and sort the order.

**Acceptance**

- Manual entry works even before WW-DICE-01.
- Sort is by total initiative; ties have a defined rule.

### WW-ENC-04 Run encounter

As a DM, I track the current turn, move next/prev, and increment the round.

**Acceptance**

- Current turn index and round persist while the encounter is active.
- Only one encounter per campaign is active at a time, or the UI makes the active one obvious.

### WW-ENC-05 Combat bookkeeping

As a DM, I apply damage, heal, and temp HP; set conditions; and optionally group “Goblin x4.”

**Acceptance**

- HP cannot silently go below 0 without a visible downed/dead state.
- Conditions are a list on the participant.
- Grouped combatants can share a name and be tracked as a count or as individuals — pick one and document it.

### WW-ENC-06 End encounter

As a DM, I end an encounter and write a short outcome note back to the current session.

**Acceptance**

- Encounter is no longer active.
- Outcome note appears on the session if a session is active; otherwise it stays on the encounter.

---

## Phase 8 — Table tools v1 never finished

### WW-DICE-01 Standalone dice roller

As a DM, I roll common polyhedrals with advantage/disadvantage and see a history.

**Acceptance**

- d4, d6, d8, d10, d12, d20, d100, and a modifier.
- Advantage/disadvantage on d20.
- History is at least in-session; persist if cheap.

### WW-DICE-02 Shared roller

As a DM, I use the same roller from initiative, ability checks, and NPC generation.

**Acceptance**

- Encounter initiative and NPC generator call the same roll API.
- Results can be accepted or overwritten manually.

### WW-SRCH-01 Global search

As a DM, I search across worlds, campaigns, locations, lore, characters, quests, and sessions.

**Acceptance**

- Results navigate to the matching record and set context if needed.
- DM secrets are not shown in result snippets that a later player view could reuse.

**v1:** Top bar always said “No results yet.”

### WW-MAP-01 Import battle map

As a DM, I import a battle-map image, tile it, zoom, and open a viewer.

**Acceptance**

- PNG (or equivalent) import stores the image and tile grid.
- Viewer supports zoom.

**v1:** MapCompose MP, PNG + tile entities.

### WW-MAP-02 Attach map to encounter

As a DM, I attach a battle map to an encounter and open it while the encounter is active.

**Acceptance**

- Encounter detail has an open-map action when a map is attached.

### WW-HOME-02 Dashboard counts

As a DM, I see dashboard counts and continue cards from real data.

**Acceptance**

- Counts match persisted worlds, campaigns, and people.
- No hardcoded totals.

---

## Phase 9 — Settings and shell polish

### WW-SET-01 Persist theme

As a DM, my light / dark / system theme survives restart.

**Acceptance**

- Theme is stored and applied on launch.

**Status:** Partial — `ThemeMode` already uses `Preferences`. Confirm it remains the source of truth once a broader settings store exists.

### WW-SET-02 Theme skins

As a DM, I pick a skin beyond light/dark (Fantasy, Gothic, Cozy Tavern, and the rest of the v1 set).

**Acceptance**

- Skin applies to the shell and feature screens.
- Skin persists.

**v1:** 10 skins.

### WW-SET-03 Local profile

As a DM, I edit the display name (and local email if kept) shown in Settings and the sidebar.

**Acceptance**

- Edits persist.
- Settings is no longer read-only for the profile.

**Today:** Hardcoded `LocalUser` (“Local Author”).

### WW-SET-04 Nav density

As a DM, I collapse the navigation rail to save space.

**Acceptance**

- Collapsed and expanded states persist.

**v1:** Nav rail preference.

### WW-SET-05 Backup and restore

As a DM, I export the local database and restore it on a new machine.

**Acceptance**

- Export produces a single backup artifact.
- Restore replaces or merges with a stated rule and a confirm.

### WW-SET-06 Notifications

As a DM, notifications are real (session reminders, unfinished encounter) or they are removed until they are.

**Acceptance**

- Either: notifications are created from real events and can be dismissed.
- Or: the hardcoded welcome notification and panel are removed.

**Today:** In-memory welcome notification only.

---

## Phase 10 — Later epics

Do not start these before Phases 0–8 are usable.

### Voice and table aids

#### WW-VOC-01 Voice clips

As a DM, I record or attach a voice clip to an NPC or location (accent, pronunciation).

**v1:** Session screen button labeled “Coming soon.”

#### WW-CAL-01 World calendar

As a DM, I keep a world calendar and stamp an in-world date on sessions.

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

As a DM, I place encounter participants as tokens on the battle map.

#### WW-VTT-02 Fog of war

As a DM, I hide and reveal map areas; a player view does not see unrevealed areas.

#### WW-VTT-03 Measure and range

As a DM, I measure distance and range on the map.

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
| WW-CMP-01 | 1 | Create campaign | Campaign | Done |
| WW-CMP-02 | 1 | List campaigns / set active | Campaign | Done |
| WW-CMP-03 | 1 | Edit / archive / complete / delete campaign | Campaign | Done |
| WW-CMP-04 | 1 | Campaign overview | Campaign | Done |
| WW-HOME-01 | 1 | Home continue card | App | Done |
| WW-LOC-01 | 2 | Create location | World | Not started |
| WW-LOC-02 | 2 | Browse as a tree | World | Not started |
| WW-LOC-03 | 2 | Edit location metadata | World | Not started |
| WW-LOC-04 | 2 | Delete location | World | Not started |
| WW-LOC-05 | 2 | Campaign overlay on a location | Campaign | Not started |
| WW-LOC-06 | 2 | Search / filter locations | World | Not started |
| WW-LOR-01 | 3 | Create / edit / delete lore | World | Not started |
| WW-LOR-02 | 3 | Categorize, tag, browse | World | Not started |
| WW-LOR-03 | 3 | Link related lore | World | Not started |
| WW-LOR-04 | 3 | DM secrets and hints | World | Not started |
| WW-LOR-05 | 3 | Attach lore to location or character | World | Not started |
| WW-CHR-01 | 4 | World NPC / monster library | World | Not started |
| WW-CHR-02 | 4 | 5E PC sheet | Campaign | Not started |
| WW-CHR-03 | 4 | Add world NPC to campaign | Campaign | Not started |
| WW-CHR-04 | 4 | Character relationships | Both | Not started |
| WW-CHR-05 | 4 | Random NPC generator | World | Not started |
| WW-CHR-06 | 4 | List / filter people | Both | Not started |
| WW-CHR-07 | 4 | 5E SRD pickers (static) | World | Not started |
| WW-QST-01 | 5 | Create quest | Campaign | Not started |
| WW-QST-02 | 5 | Objectives | Campaign | Not started |
| WW-QST-03 | 5 | Active and completed quests | Campaign | Not started |
| WW-QST-04 | 5 | Link quests | Campaign | Not started |
| WW-SES-01 | 6 | Session CRUD | Campaign | Not started |
| WW-SES-02 | 6 | Notes and scene plans | Campaign | Not started |
| WW-SES-03 | 6 | Plot threads | Campaign | Not started |
| WW-SES-04 | 6 | Reference docs | Campaign | Not started |
| WW-SES-05 | 6 | Party march order | Campaign | Not started |
| WW-SES-06 | 6 | Save NPC drafts from session | Both | Not started |
| WW-SES-07 | 6 | Start-of-session checklist | Campaign | Not started |
| WW-ENC-01 | 7 | Encounter CRUD | Campaign | Not started |
| WW-ENC-02 | 7 | Add participants | Campaign | Not started |
| WW-ENC-03 | 7 | Initiative | Campaign | Not started |
| WW-ENC-04 | 7 | Run encounter | Campaign | Not started |
| WW-ENC-05 | 7 | Combat bookkeeping | Campaign | Not started |
| WW-ENC-06 | 7 | End encounter | Campaign | Not started |
| WW-DICE-01 | 8 | Standalone dice roller | App | Not started |
| WW-DICE-02 | 8 | Shared roller | App | Not started |
| WW-SRCH-01 | 8 | Global search | App | Not started |
| WW-MAP-01 | 8 | Import battle map | Campaign | Not started |
| WW-MAP-02 | 8 | Attach map to encounter | Campaign | Not started |
| WW-HOME-02 | 8 | Dashboard counts | App | Not started |
| WW-SET-01 | 9 | Persist theme | App | Partial |
| WW-SET-02 | 9 | Theme skins | App | Not started |
| WW-SET-03 | 9 | Local profile | App | Partial |
| WW-SET-04 | 9 | Nav density | App | Not started |
| WW-SET-05 | 9 | Backup and restore | App | Not started |
| WW-SET-06 | 9 | Notifications | App | Partial |
| WW-VOC-01 | 10 | Voice clips | World | Not started |
| WW-CAL-01 | 10 | World calendar | World | Not started |
| WW-FAC-01 | 10 | Factions | World | Not started |
| WW-SRD-01 | 10 | Import 5E SRD | App | Not started |
| WW-SYS-01 | 10 | Pathfinder 2E sheets | Campaign | Not started |
| WW-SYS-02 | 10 | Other game systems | App | Not started |
| WW-VTT-01 | 10 | Tokens | Campaign | Not started |
| WW-VTT-02 | 10 | Fog of war | Campaign | Not started |
| WW-VTT-03 | 10 | Measure and range | Campaign | Not started |
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
