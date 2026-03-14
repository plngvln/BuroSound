### BuroSound

**BuroSound** lets map‑makers and resource‑pack creators place custom music and ambience directly in the world.  
Walk into an area → a track starts. Leave it → it fades out or switches to another track.  
Everything is driven by simple configuration in `sounds.json`.

---

### Features

- **Area‑based music**
  - Play music or ambience when the player enters a 3D region (“box”) in the world.
  - Each box is linked to a sound event from a resource pack.
  - Boxes can be limited to specific dimensions.
- **Smooth transitions**
  - When you move from one music area to another, the current track can fade out before the new one starts.
  - “Exit” zones can fade everything out and clear the queue.
- **Music chains / playlists**
  - One track can automatically start another when it finishes, using a simple `next` field in `sounds.json`.
- **Note block friendly**
  - When note blocks play, BuroSound temporarily lowers the background music volume so you can clearly hear note block songs and contraptions.
- **Visual debug overlay**
  - Optional overlay shows all sound boxes around you as colored outlines with labels, so creators can see exactly where music will play.
- **Client commands**
  - `/burosound boxes` – Toggle rendering of sound boxes (debug overlay).
  - `/burosound stop` – Stop all BuroSound music and clear the next‑track queue.

---

### Who Is This For?

- **Players**
  - Install it like a normal client mod.
  - On its own, BuroSound does nothing; it needs a resource pack or modpack that defines music regions in `sounds.json`.
- **Map / resource‑pack creators**
  - Ideal for:
    - Lobby music in specific rooms
    - Boss / dungeon themes tied to arenas
    - Ambient zones in towns, biomes, interiors
    - Smooth transitions between areas (corridors, portals, etc.)

No in‑game GUI is required: everything is controlled via resource packs and versionable JSON.

---

### Installation (Player)

1. Install **Fabric Loader** and **Fabric API** for your Minecraft version.
2. Download the **BuroSound** JAR and place it into your `mods` folder.
3. Install or enable a **resource pack** that defines BuroSound boxes and music (see examples below).
4. Start the game and join a world.

---

### Quick Start for Creators

BuroSound reads `assets/minecraft/sounds.json` from resource packs.  
For each sound event, you can add extra fields that BuroSound understands:

- **box** – One or more 3D regions where this sound should be active.
- **next** – The next sound event to play when this one finishes.
- **dimension** – Which dimension this region belongs to.
- **exit** – Mark this region as an “exit zone” that only fades out music.
- **play_while_inside** – If `true`, the sound is tied to being inside the box: if the player leaves the box, it fades out immediately; if it ends while the player is still inside, it **does not** restart on its own (use `next` for continuous playback).
- **block_trigger** – Optional extra trigger that acts like “entering” the same music box, but is activated by right‑clicking a specific block.

All coordinates are in **block coordinates** in the world.

---

### Example 1: Simple Lobby Music

This example plays `my_pack:music/lobby_theme` inside a rectangular lobby in the overworld.

```json
{
  "music_disc.lobby_theme": {
    "sounds": [
      {
        "name": "my_pack:music/lobby_theme",
        "stream": true,
        "box": [0, 60, 0, 30, 80, 30],
        "dimension": "minecraft:overworld"
      }
    ]
  }
}
```

- When the player is inside `x: 0–30`, `y: 60–80`, `z: 0–30` in the overworld, the lobby theme plays.
- With the default `play_while_inside = false`, once the music has been triggered it keeps playing even if the player leaves the box, until it finishes or is stopped by an exit trigger/command/dimension change.
- Because `play_while_inside` is not set here and there is no `next`, if the track finishes while the player is still inside the box, it will **not** automatically restart.
- To loop a track, set `"next": "lobby_theme"` to chain the event to itself for a continuous loop.

---

### Example 2: Boss Arena with Exit Zone

In this example, one arena region plays a boss theme, and a small “exit corridor” fades out the music.

```json
{
  "music_disc.boss_theme": {
    "sounds": [
      {
        "name": "my_pack:music/boss_theme",
        "stream": true,
        "box": [0, 60, 0, 30, 80, 30],
        "dimension": "the_nether",
        "play_while_inside": true,
        "next": "boss_theme"
      }
    ]
  },
  "exit": {
    "sounds": [
      {
        "name": "none",
        "box": [30, 60, -5, 0, 80, -10],
        "dimension": "the_nether",
        "exit": true
      }
    ]
  }
}
```

- Inside the big arena box, the boss theme plays.
- Stepping into the separate `exit` box fades the music out and stop playing.

---

### Example 3: Chained Tracks (Playlists)

You can chain tracks together using the `next` field.  
If `next` does not contain a dot, BuroSound automatically prefixes `music_disc.`

```json
{
  "music_disc.track_one": {
    "sounds": [
      {
        "name": "my_pack:music/track_one",
        "stream": true,
        "box": [100, 64, 100, 140, 80, 140],
        "next": "track_two"
      }
    ]
  },
  "music_disc.track_two": {
    "sounds": [
      {
        "name": "my_pack:music/track_two",
        "stream": true,
        "box": [100, 64, 100, 140, 80, 140]
      }
    ]
  }
}
```

- Inside the box, `music_disc.track_one` plays first.
- When it finishes, BuroSound automatically starts `music_disc.track_two` (because of `next: "track_two"`).
- You can create longer playlists by chaining multiple events.

---

### Example 4: Multiple Boxes for the Same Track

You can assign multiple regions to the same sound using an array of arrays:

```json
{
  "music_disc.town_theme": {
    "sounds": [
      {
        "name": "my_pack:music/town_theme",
        "stream": true,
        "box": [
          [-20, 64, -20, 20, 80, 20],
          [30, 64, -10, 50, 80, 10]
        ],
        "dimension": "minecraft:overworld"
      }
    ]
  }
}
```

- Both defined areas share the same town theme.
- Walking between them does not restart the track, because they are the same sound event.

---

### Example 5: Custom Sounds Not Tied to Items

You can also define completely custom sound events that are not bound to any item or vanilla music disc:

```json
{
  "burosound.track1": {
    "category": "record",
    "sounds": [
      {
        "name": "records/track1",
        "stream": true,
        "box": [100, 64, 100, 120, 80, 120]
      }
    ]
  }
}
```

- The event `burosound.track1` plays `records/track1` when the player is inside the specified box.
- This event does not need to be linked to any physical item.

---

### Example 6: Block‑Activated Trigger (Right‑Click)

You can also make a sound behave like a region trigger, but activated by **right‑clicking a block** instead of walking into an area.  
Use the `block_trigger` field in the same sound definition:

```json
{
  "music_disc.secret_button": {
    "sounds": [
      {
        "name": "my_pack:music/secret_theme",
        "stream": true,
        "box": [10, 64, -5, 10, 64, -5],
        "dimension": "minecraft:overworld",

        "block_trigger": [10, 64, -5, "stone_button"]
      }
    ]
  }
}
```

- `block_trigger` can be written either as an array `[x, y, z, "block_id"]` or as an object:

```json
"block_trigger": {
"x": 10,
"y": 64,
"z": -5,
"block": "stone_button"
}
```

- If the block ID does not contain a namespace, `minecraft:` is added automatically (so `"stone_button"` becomes `"minecraft:stone_button"`).
- When the player **right‑clicks exactly that block in that dimension**, BuroSound behaves exactly as if the player had entered the corresponding box:
  - it starts the same sound that would play when entering the `box`,
  - it respects all flags: `exit`, `play_while_inside`, `next`, `ignore_note_blocks`, `allow_overlap`, and any chained tracks.

---

### Controls & Debugging

- `/burosound boxes`
  - Toggles the overlay that shows all currently active BuroSound boxes in your dimension.
- `/burosound stop`
  - Immediately stops all music managed by BuroSound.
  - Clears any queued “next” tracks.
- `F3+T`
  - Reloads resource packs.
  - BuroSound automatically re‑parses `sounds.json` on resource reload, so you can iterate quickly while editing.

---

### Note Blocks and Volume Ducking

BuroSound listens for note block sounds:

- When **note blocks play**, the mod temporarily reduces the volume of region music so you can hear the notes clearly.
- Once note blocks stop, the music volume gradually returns to normal.
