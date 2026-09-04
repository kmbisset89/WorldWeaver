const MODULE_ID = "world-weaver";
const BUNDLE_FORMAT_VERSION = 1;

Hooks.once("init", () => {
  console.log("World Weaver | Ready to import .wwbundle files");
});

Hooks.on("renderJournalDirectory", (app, html) => {
  const root = html instanceof HTMLElement ? html : html[0];
  if (!root || root.querySelector(".world-weaver-import")) {
    return;
  }
  const footer = root.querySelector(".directory-footer") ?? root;
  const button = document.createElement("button");
  button.type = "button";
  button.classList.add("world-weaver-import");
  button.innerHTML = "<i class='fas fa-scroll'></i> Import World Weaver bundle";
  button.addEventListener("click", () => WorldWeaverBundleImporter.pickAndImport());
  footer.appendChild(button);
});

class WorldWeaverBundleImporter {
  static pickAndImport() {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = ".wwbundle,application/zip";
    input.addEventListener("change", async () => {
      const file = input.files?.[0];
      if (!file) {
        return;
      }
      try {
        ui.notifications.info(`Importing ${file.name}…`);
        const summary = await this.importFile(file);
        ui.notifications.info(
          `Imported ${summary.journals} journals and ${summary.scenes} scenes from “${summary.worldName}”.`,
        );
      } catch (error) {
        console.error(`${MODULE_ID} import failed`, error);
        ui.notifications.error(`World Weaver import failed: ${error.message}`);
      }
    });
    input.click();
  }

  static async importFile(file) {
    const files = await unzipArrayBuffer(await file.arrayBuffer());
    const manifestBytes = files["manifest.json"];
    const payloadBytes = files["bundle.json"];
    if (!manifestBytes || !payloadBytes) {
      throw new Error("That file is not a valid World Weaver bundle");
    }
    const manifest = JSON.parse(bytesToText(manifestBytes));
    if (manifest.formatVersion !== BUNDLE_FORMAT_VERSION) {
      throw new Error("This bundle was made with a newer World Weaver version");
    }
    const payload = JSON.parse(bytesToText(payloadBytes));
    const worldName = payload.world?.name || manifest.originalWorldName || "World Weaver";
    const worldId = payload.world?.id || "world";
    const journalRoot = await ensureFolder(`World Weaver — ${worldName}`, "JournalEntry");
    const sceneRoot = await ensureFolder(`World Weaver — ${worldName}`, "Scene");
    const loreCount = await this.importLore(payload.loreEntries ?? [], journalRoot);
    const locationCount = await this.importLocations(payload.locations ?? [], journalRoot);
    const sceneCount = await this.importScenes(
      payload.battleMaps ?? [],
      files,
      sceneRoot,
      worldId,
    );
    return {
      worldName,
      journals: loreCount + locationCount,
      scenes: sceneCount,
    };
  }

  static async importLore(entries, journalRoot) {
    const loreRoot = await ensureFolder("Lore", "JournalEntry", journalRoot.id);
    let count = 0;
    for (const lore of entries) {
      const categoryName = loreCategoryLabel(lore.category);
      const categoryFolder = await ensureFolder(categoryName, "JournalEntry", loreRoot.id);
      const pages = [
        journalPage("Lore", lore.content ?? "", {
          default: CONST.DOCUMENT_OWNERSHIP_LEVELS.OBSERVER,
        }),
      ];
      for (const secret of lore.secrets ?? []) {
        pages.push(
          journalPage(secret.title || "Secret", secretPageBody(secret), {
            default: CONST.DOCUMENT_OWNERSHIP_LEVELS.NONE,
          }),
        );
      }
      await upsertJournal({
        sourceId: lore.id,
        name: lore.title || "Lore",
        folderId: categoryFolder.id,
        pages,
        ownership: { default: CONST.DOCUMENT_OWNERSHIP_LEVELS.OBSERVER },
        kind: "lore",
      });
      count += 1;
    }
    return count;
  }

  static async importLocations(locations, journalRoot) {
    const locationsRoot = await ensureFolder("Locations", "JournalEntry", journalRoot.id);
    let count = 0;
    for (const location of locations) {
      const typeFolder = await ensureFolder(
        locationTypeLabel(location.type),
        "JournalEntry",
        locationsRoot.id,
      );
      await upsertJournal({
        sourceId: location.id,
        name: location.name || "Location",
        folderId: typeFolder.id,
        pages: [journalPage("Location", locationPageBody(location))],
        ownership: { default: CONST.DOCUMENT_OWNERSHIP_LEVELS.OBSERVER },
        kind: "location",
      });
      count += 1;
    }
    return count;
  }

  static async importScenes(battleMaps, files, sceneRoot, worldId) {
    let count = 0;
    for (const map of battleMaps) {
      const png = files[`maps/${map.id}/original.png`];
      if (!png) {
        continue;
      }
      const directory = `worldweaver/${sanitizePath(worldId)}/maps`;
      const fileName = `${sanitizePath(map.id)}.png`;
      const file = new File([png], fileName, { type: "image/png" });
      const uploaded = await FilePicker.upload("data", directory, file, {}, { notify: false });
      const imagePath = uploaded?.path || `${directory}/${fileName}`;
      const columns = Math.max(1, map.columns || 20);
      const pixelsPerGrid = Math.max(1, Math.floor((map.originalWidth || 1) / columns));
      await upsertScene({
        sourceId: map.id,
        name: map.name || "Battle map",
        folderId: sceneRoot.id,
        img: imagePath,
        width: map.originalWidth,
        height: map.originalHeight,
        grid: {
          type: CONST.GRID_TYPES?.SQUARE ?? 1,
          size: pixelsPerGrid,
          distance: map.unitsPerTile ?? 5,
          units: map.unitName || "ft",
        },
      });
      count += 1;
    }
    return count;
  }
}

async function upsertJournal({ sourceId, name, folderId, pages, ownership, kind }) {
  const existing = game.journal.find((doc) => doc.getFlag(MODULE_ID, "sourceId") === sourceId);
  const flags = { [MODULE_ID]: { sourceId, kind } };
  if (!existing) {
    await JournalEntry.create({
      name,
      folder: folderId,
      pages,
      ownership,
      flags,
    });
    return;
  }
  await existing.update({ name, folder: folderId, ownership, flags });
  const pageIds = existing.pages.map((page) => page.id);
  if (pageIds.length > 0) {
    await existing.deleteEmbeddedDocuments("JournalEntryPage", pageIds);
  }
  await existing.createEmbeddedDocuments("JournalEntryPage", pages);
}

async function upsertScene({ sourceId, name, folderId, img, width, height, grid }) {
  const existing = game.scenes.find((doc) => doc.getFlag(MODULE_ID, "sourceId") === sourceId);
  const data = {
    name,
    folder: folderId,
    img,
    background: { src: img },
    width,
    height,
    padding: 0.05,
    grid,
    flags: { [MODULE_ID]: { sourceId, kind: "battle-map" } },
  };
  if (existing) {
    await existing.update(data);
    return;
  }
  await Scene.create(data);
}

async function ensureFolder(name, type, parentId = null) {
  const existing = game.folders.find((folder) => {
    const parent = folder.folder?.id ?? null;
    return folder.type === type && folder.name === name && parent === parentId;
  });
  if (existing) {
    return existing;
  }
  return Folder.create({ name, type, folder: parentId });
}

function journalPage(name, text, ownership) {
  const page = {
    name,
    type: "text",
    text: {
      content: toHtml(text),
      format: CONST.JOURNAL_ENTRY_PAGE_FORMATS?.HTML ?? 1,
    },
  };
  if (ownership) {
    page.ownership = ownership;
  }
  return page;
}

function secretPageBody(secret) {
  const hints = secret.hints ?? [];
  const revealed = hints.filter((hint) => hint.revealed).map((hint) => hint.text);
  const hidden = hints.filter((hint) => !hint.revealed).map((hint) => hint.text);
  const parts = [secret.secret || ""];
  if (revealed.length > 0) {
    parts.push("", "Hints the table has heard:", ...revealed.map((text) => `• ${text}`));
  }
  if (hidden.length > 0) {
    parts.push("", "Unrevealed hints:", ...hidden.map((text) => `• ${text}`));
  }
  return parts.join("\n");
}

function locationPageBody(location) {
  const landmarks = Array.isArray(location.landmarks) ? location.landmarks.filter(Boolean) : [];
  const lines = [
    location.description,
    location.climate ? `Climate: ${location.climate}` : "",
    location.terrain ? `Terrain: ${location.terrain}` : "",
    location.government ? `Government: ${location.government}` : "",
    landmarks.length > 0 ? `Landmarks: ${landmarks.join(", ")}` : "",
    location.history,
    location.notes,
  ];
  return lines.filter(Boolean).join("\n\n");
}

function loreCategoryLabel(category) {
  const labels = {
    History: "History",
    Myth: "Myth",
    Religion: "Religion",
    Culture: "Culture",
    Geography: "Geography",
    Magic: "Magic",
    Politics: "Politics",
    Other: "Other",
  };
  return labels[category] || "Other";
}

function locationTypeLabel(type) {
  const labels = {
    Continent: "Continents",
    Area: "Areas",
    City: "Cities",
    Place: "Places",
  };
  return labels[type] || "Places";
}

function toHtml(text) {
  const escaped = String(text ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
  return escaped.replace(/\n/g, "<br>");
}

function sanitizePath(value) {
  return String(value ?? "item").replace(/[^A-Za-z0-9._-]+/g, "_");
}

function bytesToText(bytes) {
  return new TextDecoder().decode(bytes);
}

async function unzipArrayBuffer(arrayBuffer) {
  const view = new DataView(arrayBuffer);
  const bytes = new Uint8Array(arrayBuffer);
  let eocd = -1;
  for (let index = arrayBuffer.byteLength - 22; index >= 0; index -= 1) {
    if (view.getUint32(index, true) === 0x06054b50) {
      eocd = index;
      break;
    }
  }
  if (eocd < 0) {
    throw new Error("That file is not a valid World Weaver bundle");
  }
  const entryCount = view.getUint16(eocd + 10, true);
  let offset = view.getUint32(eocd + 16, true);
  const files = {};
  for (let index = 0; index < entryCount; index += 1) {
    if (view.getUint32(offset, true) !== 0x02014b50) {
      throw new Error("That file is not a valid World Weaver bundle");
    }
    const method = view.getUint16(offset + 10, true);
    const compressedSize = view.getUint32(offset + 20, true);
    const nameLength = view.getUint16(offset + 28, true);
    const extraLength = view.getUint16(offset + 30, true);
    const commentLength = view.getUint16(offset + 32, true);
    const localOffset = view.getUint32(offset + 42, true);
    const name = new TextDecoder().decode(bytes.subarray(offset + 46, offset + 46 + nameLength));
    const localNameLength = view.getUint16(localOffset + 26, true);
    const localExtraLength = view.getUint16(localOffset + 28, true);
    const dataStart = localOffset + 30 + localNameLength + localExtraLength;
    const compressed = bytes.subarray(dataStart, dataStart + compressedSize);
    files[name] = method === 0 ? compressed : await inflateRaw(compressed);
    offset += 46 + nameLength + extraLength + commentLength;
  }
  return files;
}

async function inflateRaw(compressed) {
  const stream = new DecompressionStream("deflate-raw");
  const writer = stream.writable.getWriter();
  await writer.write(compressed);
  await writer.close();
  const reader = stream.readable.getReader();
  const chunks = [];
  let total = 0;
  while (true) {
    const { value, done } = await reader.read();
    if (done) {
      break;
    }
    chunks.push(value);
    total += value.byteLength;
  }
  const output = new Uint8Array(total);
  let position = 0;
  for (const chunk of chunks) {
    output.set(chunk, position);
    position += chunk.byteLength;
  }
  return output;
}
