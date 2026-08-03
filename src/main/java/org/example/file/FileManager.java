package org.example.file;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.data.Run;
import org.example.net.NetDataParse;
import org.example.net.NetWorkHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileManager {

    public static final List<String> undownloadedList = new ArrayList<>();

    private static final Gson gson = new Gson();

    public static void initialize() throws IOException {
        Path rootPath = Paths.get("paceman-twitch-dl");
        Files.createDirectory(rootPath);
        Files.createFile(Paths.get("paceman-twitch-dl", "pending_record.txt"));
        removeOutDatedRecord();
    }

    public static void makeUndownloadedList() throws IOException {
        Path path = Paths.get("paceman-twitch-dl", "pending_record.txt");
        String content = Files.readString(path);
        if (content.isBlank()) return;
        JsonObject writtenObject = gson.fromJson(content, JsonObject.class);
        JsonArray jsonArray = writtenObject.getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            if(!jsonElement.getAsJsonObject().get("isDownloaded").getAsBoolean()){
               undownloadedList.add(jsonElement.getAsJsonObject().get("worldId").getAsString());
            }
        }

    }

    public static void addNewRecord() throws IOException {
        Path path = Paths.get("paceman-twitch-dl", "pending_record.txt");
        String content = Files.readString(path);
        for(Run run: NetDataParse.runList){
            String worldId = run.getWorldId();
            if (content.contains(worldId)) return;
            JsonObject writtenObject = gson.fromJson(content, JsonObject.class);
            JsonArray jsonArray = writtenObject.getAsJsonArray();
            JsonObject toBeWritten = new JsonObject();
            toBeWritten.addProperty("worldId", worldId);
            long runStartTime = run.getRunStartTime();
            toBeWritten.addProperty("runStartTime", runStartTime);
            toBeWritten.addProperty("isDownloaded", false);
            jsonArray.add(toBeWritten);
            Files.writeString(path, jsonArray.getAsString(), StandardOpenOption.WRITE);
        }

    }

    public static void removeOutDatedRecord() throws IOException {
        Path path = Paths.get("paceman-twitch-dl", "pending_record.txt");
        String content = Files.readString(path);
        if (content.isBlank()) return;
        JsonObject writtenObject = gson.fromJson(content, JsonObject.class);
        JsonArray jsonArray = writtenObject.getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            long runStartTime = jsonElement.getAsJsonObject().get("runStartTime").getAsLong();
            long currentTime = System.currentTimeMillis();
            if (currentTime - runStartTime >= 24 * 60 * 60 * 1000 * 7){
                jsonArray.remove(jsonElement);
            }
        }
        Files.writeString(path, jsonArray.getAsString(), StandardOpenOption.WRITE);
    }

    public static void setDownloaded(String worldId) throws IOException {
        Path path = Paths.get("paceman-twitch-dl", "pending_record.txt");
        String content = Files.readString(path);
        if (content.isBlank()) return;
        JsonObject writtenObject = gson.fromJson(content, JsonObject.class);
        JsonArray jsonArray = writtenObject.getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            if(jsonElement.getAsJsonObject().get("worldId").getAsString().equals(worldId)){
                jsonElement.getAsJsonObject().remove("isDownloaded");
                jsonElement.getAsJsonObject().addProperty("isDownloaded",true);
                break;
            }
        }
        Files.writeString(path, jsonArray.getAsString(), StandardOpenOption.WRITE);

    }

    public static void prepareTheDownloading(String worldId) throws IOException {
        Path path = Paths.get("paceman-twitch-dl",worldId);
        Files.createDirectory(path);
        Path path1 = Paths.get("paceman-twitch-dl",worldId,"description.txt");
        Files.createFile(path1);
    }
}
