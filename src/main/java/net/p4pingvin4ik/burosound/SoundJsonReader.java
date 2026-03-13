package net.p4pingvin4ik.burosound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

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
        BoxTriggerManager.nextSoundMap.clear();

        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        Identifier soundFile = Identifier.of("minecraft", "sounds.json");
        List<Resource> resources = resourceManager.getAllResources(soundFile);

        for (Resource resource : resources) {
            if ("vanilla".equals(resource.getPackId())) {
                continue;
            }

            try (InputStream inputStream = resource.getInputStream();
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
                                            Identifier.of("minecraft", eventName),
                                            Identifier.of("minecraft", nextStr)
                                    );
                                }

                                String dimStr = sObj.has("dimension") ? sObj.get("dimension").getAsString() : "minecraft:overworld";
                                if (!dimStr.contains(":")) dimStr = "minecraft:" + dimStr;
                                Identifier dimensionId = Identifier.of(dimStr);

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