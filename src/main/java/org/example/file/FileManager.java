package org.example.file;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.data.Run;
import org.example.net.NetDataParse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static final List<String> undownloadedList = new ArrayList<>();
    private static final Gson gson = new Gson();
    private static final String DIR_NAME = "paceman-twitch-dl";
    private static final String RECORD_FILE = "pending_record.txt";

    public static void initialize() throws IOException {
        Path rootPath = Paths.get(DIR_NAME);
        if (!Files.exists(rootPath)) {
            Files.createDirectory(rootPath);
        }

        Path recordPath = Paths.get(DIR_NAME, RECORD_FILE);
        if (!Files.exists(recordPath)) {
            Files.createFile(recordPath);
            // 初始化为空的 JSON 数组，防止后续解析报错
            Files.writeString(recordPath, "[]", StandardOpenOption.WRITE);
        }

        removeOutDatedRecord();
    }

    public static void makeUndownloadedList() throws IOException {
        Path path = Paths.get(DIR_NAME, RECORD_FILE);
        String content = Files.readString(path);
        if (content.isBlank()) return;

        JsonArray jsonArray = gson.fromJson(content, JsonArray.class);
        for (JsonElement jsonElement : jsonArray) {
            JsonObject record = jsonElement.getAsJsonObject();
            if (!record.get("isDownloaded").getAsBoolean()) {
                undownloadedList.add(record.get("worldId").getAsString());
            }
        }
    }

    public static void addNewRecord() throws IOException {
        Path path = Paths.get(DIR_NAME, RECORD_FILE);
        String content = Files.readString(path);

        // 如果文件为空，初始化一个 JsonArray
        JsonArray jsonArray = content.isBlank() ? new JsonArray() : gson.fromJson(content, JsonArray.class);
        boolean hasUpdates = false;

        for (Run run : NetDataParse.runList) {
            String worldId = run.getWorldId();
            // 如果记录已存在，使用 continue 跳过当前循环，而不是 return 终止所有操作
            if (content.contains(worldId)) {
                continue;
            }

            JsonObject toBeWritten = new JsonObject();
            toBeWritten.addProperty("worldId", worldId);
            toBeWritten.addProperty("runStartTime", run.getRunStartTime());
            toBeWritten.addProperty("isDownloaded", false);
            jsonArray.add(toBeWritten);
            hasUpdates = true;
        }

        if (hasUpdates) {
            // 使用 gson.toJson 序列化，并使用 TRUNCATE_EXISTING 覆盖旧文件内容
            Files.writeString(path, gson.toJson(jsonArray), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    public static void removeOutDatedRecord() throws IOException {
        Path path = Paths.get(DIR_NAME, RECORD_FILE);
        String content = Files.readString(path);
        if (content.isBlank() || content.equals("[]")) return;

        JsonArray jsonArray = gson.fromJson(content, JsonArray.class);
        long currentTime = System.currentTimeMillis();
        boolean hasUpdates = false;

        // 使用倒序循环安全地移除元素，避免 ConcurrentModificationException
        for (int i = jsonArray.size() - 1; i >= 0; i--) {
            JsonObject record = jsonArray.get(i).getAsJsonObject();
            long runStartTime = record.get("runStartTime").getAsLong();

            // 注意：7天对应的毫秒数超出了 int 的最大值，需要使用长整型 (L) 进行计算
            if (currentTime - runStartTime >= 24L * 60 * 60 * 1000 * 7) {
                jsonArray.remove(i);
                hasUpdates = true;
            }
        }

        if (hasUpdates) {
            Files.writeString(path, gson.toJson(jsonArray), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    public static void setDownloaded(String worldId) throws IOException {
        Path path = Paths.get(DIR_NAME, RECORD_FILE);
        String content = Files.readString(path);
        if (content.isBlank()) return;

        JsonArray jsonArray = gson.fromJson(content, JsonArray.class);
        boolean hasUpdates = false;

        for (JsonElement jsonElement : jsonArray) {
            JsonObject record = jsonElement.getAsJsonObject();
            if (record.get("worldId").getAsString().equals(worldId)) {
                record.addProperty("isDownloaded", true); // addProperty 会自动覆盖同名键
                hasUpdates = true;
                break;
            }
        }

        if (hasUpdates) {
            Files.writeString(path, gson.toJson(jsonArray), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    public static void prepareTheDownloading(String worldId) throws IOException {
        Path path = Paths.get(DIR_NAME, worldId);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        Path path1 = Paths.get(DIR_NAME, worldId, "description.txt");
        if (!Files.exists(path1)) {
            Files.createFile(path1);
        }
    }
}