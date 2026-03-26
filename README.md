# BuroSound

> **BuroSound** is a client-side mod that allows map-makers and resource-pack creators to bind custom music and ambience to specific 3D regions in Minecraft. Enter an area, and a track smoothly starts. Leave it, and it fades out or transitions to another. Everything is configured entirely via `sounds.json`.

---

## Key Features

*   **Spatial Audio Without GUIs:** Setup requires no in-game interfaces—everything is controlled via versionable JSON in your resource pack.
*   **Playlists & Chains:** A track can automatically trigger the next one when it finishes.
*   **Smooth Transitions:** Supports audio fading when moving between locations and dedicated "exit zones" to stop playback.
*   **Note Block Compatibility (Ducking):** When note blocks play, BuroSound's background music temporarily lowers in volume so contraptions and melodies remain audible.
*   **Debugging Tools:** A visual in-game overlay displays the exact boundaries of your music zones.

---

## Installation

### For Players
1. Install **Fabric Loader** and **Fabric API**.
2. Place the **BuroSound** `.jar` file into your `mods` folder.
3. Enable a resource pack containing zone configurations (without it, the mod does nothing).

### For Content Creators
The mod reads the `assets/minecraft/sounds.json` file from your resource pack. Simply add specific parameters to your sound events inside the `sounds` array. To apply changes in-game, use `F3 + T` (reload resource packs).

---

## Parameter Reference (`sounds.json`)

The following fields can be added to any sound event within the `sounds` array. All coordinates are specified in blocks.

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `box` | `[int]` or `[[int]]` | *None* | 3D trigger zone for the sound. Format: `[x1, y1, z1, x2, y2, z2]` or an array of such zones. |
| `dimension` | `string` | `minecraft:overworld` | The dimension where the zone is active. |
| `exit` / `isExit` | `boolean` | `false` | If `true`, the zone acts as an exit trigger: it smoothly stops current music and clears the queue. |
| `next` | `string` | *None* | ID of the next track. If provided without a dot (`.`), the mod automatically adds the `music_disc.` prefix. |
| `play_while_inside` / `playWhileInside` | `boolean` | `false` | If `true`, the music fades out immediately upon leaving the zone. If `false`, the track plays to the end even if you leave. |
| `ignore_note_blocks` / `ignoreNoteBlocks`| `boolean` | `false` | If `true`, this track will not be ducked (lowered in volume) when note blocks play. |
| `allow_overlap` / `allowOverlap` | `boolean` | `false` | If `true`, the sound from this zone can play simultaneously with sounds from other zones. |
| `block_trigger` / `blockTrigger` | `array` or `object`| *None* | Alternative activation method: right-clicking a specific block at the specified coordinates. |

---

## Argument Examples

### 1. Basic Zone & Dimension (`box`, `dimension`)
A standard zone in the Nether. The track starts upon entry and plays to completion, even if the player leaves the area.

```json
{
  "music_disc.nether_ambient": {
    "sounds": [
      {
        "name": "my_pack:music/nether_ambient",
        "stream": true,
        "box": [10, 30, 10, 50, 80, 50],
        "dimension": "minecraft:the_nether"
      }
    ]
  }
}
```

### 2. Multi-Zones (`box` with an array of arrays)
The same track assigned to two different rooms. Walking between them will not restart the music.

```json
{
  "music_disc.town_theme": {
    "sounds": [
      {
        "name": "my_pack:music/town",
        "stream": true,
        "box": [
          [-20, 64, -20, 20, 80, 20],
          [100, 64, 100, 150, 80, 150]
        ]
      }
    ]
  }
}
```

### 3. Presence Dependency & Looping (`play_while_inside`, `next`)
The music plays **only** while the player is inside the cube. Upon exiting, it smoothly fades out. The `"next": "boss_theme"` parameter forces the track to trigger itself after finishing, creating an endless loop.

```json
{
  "music_disc.boss_theme": {
    "sounds": [
      {
        "name": "my_pack:music/boss",
        "stream": true,
        "box": [0, 60, 0, 30, 80, 30],
        "play_while_inside": true,
        "next": "boss_theme"
      }
    ]
  }
}
```

### 4. Creating a Playlist (`next` to a different track)
When `track_one` finishes, the mod automatically starts `track_two`.

```json
{
  "music_disc.track_one": {
    "sounds": [
      {
        "name": "my_pack:music/part1",
        "stream": true,
        "box": [0, 60, 0, 10, 70, 10],
        "next": "track_two"
      }
    ]
  },
  "music_disc.track_two": {
    "sounds": [
      {
        "name": "my_pack:music/part2",
        "stream": true,
        "box": [0, 60, 0, 10, 70, 10]
      }
    ]
  }
}
```

### 5. Ignoring Note Blocks & Overlapping (`ignore_note_blocks`, `allow_overlap`)
This background hum can play concurrently with other music and will not become quieter if a note block mechanism is active nearby.

```json
{
  "burosound.machine_hum": {
    "sounds": [
      {
        "name": "my_pack:ambient/hum",
        "stream": true,
        "box": [5, 64, 5, 10, 68, 10],
        "ignore_note_blocks": true,
        "allow_overlap": true
      }
    ]
  }
}
```

### 6. Exit Zone (`exit`)
This zone does not play music. If a player enters this corridor, any currently playing BuroSound music smoothly fades out and stops.

```json
{
  "exit_corridor": {
    "sounds": [
      {
        "name": "none",
        "box": [30, 60, -5, 40, 65, -10],
        "exit": true
      }
    ]
  }
}
```

### 7. Block Click Trigger (`block_trigger`)
Instead of entering a zone, the music is activated by right-clicking a specific block.

```json
{
  "music_disc.secret_button": {
    "sounds": [
      {
        "name": "my_pack:music/secret",
        "stream": true,
        "block_trigger": [10, 64, -5, "stone_button"]
      }
    ]
  }
}
```

---

## Commands & Controls

For debugging convenience, the mod provides client-side commands:

*   `/burosound boxes`
    Toggles the debug overlay. Displays colored outlines and labels for all active zones in your current dimension. Perfect for verifying coordinates.
*   `/burosound stop`
    Instantly stops all playing BuroSound music and completely clears the queue of upcoming tracks.