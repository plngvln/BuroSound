package net.p4pingvin4ik.burosound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static net.p4pingvin4ik.burosound.Burosound.LOGGER;

public class SoundJsonReader {

    private static boolean readBoolean(JsonObject obj, String snakeCaseKey, String camelCaseKey) {
        if (obj.has(snakeCaseKey)) return obj.get(snakeCaseKey).getAsBoolean();
        if (obj.has(camelCaseKey)) return obj.get(camelCaseKey).getAsBoolean();
        return false;
    }


    public static void readSoundsConfig() {
        LOGGER.info("starting sound dump");

        BoxTriggerManager.activeBoxes.clear();
        BoxTriggerManager.blockTriggers.clear();
        BoxTriggerManager.nextSoundMap.clear();

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Identifier soundFile = Identifier.fromNamespaceAndPath("minecraft", "sounds.json");
        List<Resource> resources = resourceManager.getResourceStack(soundFile);

        for (Resource resource : resources) {
            if ("vanilla".equals(resource.sourcePackId())) {
                continue;
            }

            try (InputStream inputStream = resource.open();
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonObject rootObject = JsonParser.parseReader(reader).getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
                    String eventName = entry.getKey();
                    JsonObject eventData = entry.getValue().getAsJsonObject();

                    if (eventData.has("sounds")) {
                        JsonArray soundsArray = eventData.getAsJsonArray("sounds");
                        for (JsonElement s : soundsArray) {
                            if (s.isJsonObject()) {
                                JsonObject sObj = s.getAsJsonObject();

                                if (sObj.has("next")) {
                                    String nextStr = sObj.get("next").getAsString();
                                    if (!nextStr.contains(".")) {
                                        nextStr = "music_disc." + nextStr;
                                    }
                                    BoxTriggerManager.nextSoundMap.put(
                                            Identifier.fromNamespaceAndPath("minecraft", eventName),
                                            Identifier.fromNamespaceAndPath("minecraft", nextStr)
                                    );
                                }

                                String dimStr = sObj.has("dimension") ? sObj.get("dimension").getAsString() : "minecraft:overworld";
                                if (!dimStr.contains(":")) dimStr = "minecraft:" + dimStr;
                                Identifier dimensionId = Identifier.parse(dimStr);

                                boolean isExit = readBoolean(sObj, "exit", "isExit");
                                boolean playWhileInside = readBoolean(sObj, "play_while_inside", "playWhileInside");
                                boolean ignoreNoteBlocks = readBoolean(sObj, "ignore_note_blocks", "ignoreNoteBlocks");
                                boolean allowOverlap = readBoolean(sObj, "allow_overlap", "allowOverlap");

                                if (sObj.has("box")) {
                                    JsonArray boxArray = sObj.getAsJsonArray("box");
                                    if (!boxArray.isEmpty()) {
                                        JsonElement firstElement = boxArray.get(0);

                                        if (firstElement.isJsonArray()) {
                                            for (JsonElement boxElement : boxArray) {
                                                if (boxElement.isJsonArray()) {
                                                    registerBox(eventName, boxElement.getAsJsonArray(), dimensionId, isExit, playWhileInside, ignoreNoteBlocks, allowOverlap);
                                                }
                                            }
                                        }
                                        else if (firstElement.isJsonPrimitive() && firstElement.getAsJsonPrimitive().isNumber()) {
                                            registerBox(eventName, boxArray, dimensionId, isExit, playWhileInside, ignoreNoteBlocks, allowOverlap);
                                        }
                                    }
                                }

                                if (sObj.has("block_trigger") || sObj.has("blockTrigger")) {
                                    JsonElement blockElem = sObj.has("block_trigger") ? sObj.get("block_trigger") : sObj.get("blockTrigger");

                                    int x = 0;
                                    int y = 0;
                                    int z = 0;
                                    String blockIdStr = null;

                                    if (blockElem.isJsonArray()) {
                                        JsonArray arr = blockElem.getAsJsonArray();
                                        if (arr.size() >= 4) {
                                            x = arr.get(0).getAsInt();
                                            y = arr.get(1).getAsInt();
                                            z = arr.get(2).getAsInt();
                                            blockIdStr = arr.get(3).getAsString();
                                        }
                                    } else if (blockElem.isJsonObject()) {
                                        JsonObject obj = blockElem.getAsJsonObject();
                                        if (obj.has("x") && obj.has("y") && obj.has("z") && obj.has("block")) {
                                            x = obj.get("x").getAsInt();
                                            y = obj.get("y").getAsInt();
                                            z = obj.get("z").getAsInt();
                                            blockIdStr = obj.get("block").getAsString();
                                        }
                                    }

                                    if (blockIdStr != null) {
                                        if (!blockIdStr.contains(":")) blockIdStr = "minecraft:" + blockIdStr;
                                        Identifier blockId = Identifier.parse(blockIdStr);

                                        SoundBox blockBox = new SoundBox(
                                                eventName,
                                                x, y, z,
                                                x, y, z,
                                                dimensionId,
                                                isExit,
                                                playWhileInside,
                                                ignoreNoteBlocks,
                                                allowOverlap
                                        );

                                        BoxTriggerManager.blockTriggers.add(
                                                new BlockTrigger(blockBox, x, y, z, blockId)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.info("error loading sounds.json: {}", e.getMessage());
            }
        }

        LOGGER.info("loaded {} trigger boxes.", BoxTriggerManager.activeBoxes.size());
    }

    private static void registerBox(String eventName, JsonArray coords, Identifier dim, boolean isExit, boolean playWhileInside, boolean ignoreNoteBlocks, boolean allowOverlap) {
        if (coords.size() >= 6) {
            double x1 = coords.get(0).getAsDouble();
            double y1 = coords.get(1).getAsDouble();
            double z1 = coords.get(2).getAsDouble();
            double x2 = coords.get(3).getAsDouble();
            double y2 = coords.get(4).getAsDouble();
            double z2 = coords.get(5).getAsDouble();

            BoxTriggerManager.activeBoxes.add(new SoundBox(eventName, x1, y1, z1, x2, y2, z2, dim, isExit, playWhileInside, ignoreNoteBlocks, allowOverlap));
        }
    }
}
