package org.example.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.data.Run;
import org.example.twitch.VodData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetDataParse {
    public static final List<Run> runList = new ArrayList<>();

    public static boolean parseWeeklyData() {
        // 每次解析前清空列表，防止多次调用导致数据重复追加
        runList.clear();

        JsonArray individualRunsArray = keepSub10();
        if (individualRunsArray == null) {
            NetWorkHandler.changeAndPrintCurrentStatus("json error: time key lost");
            return false;
        }
        return makeRunList(individualRunsArray);
    }

    private static boolean makeRunList(JsonArray individualRunsArray) {
        for (JsonElement individualRun : individualRunsArray) {
            JsonObject runJsonObject = individualRun.getAsJsonObject();
            Run run = new Run();
            if (runJsonObject.has("time")) {
                run.setTime(runJsonObject.get("time").getAsInt());
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("error: time key lost");
                return false;
            }
            if (runJsonObject.has("worldId")) {
                run.setWorldId(runJsonObject.get("worldId").getAsString());
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("error: worldId key lost");
                return false;
            }
            if (runJsonObject.has("submitted")) {
                run.setRunStartTime(runJsonObject.get("submitted").getAsLong());
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("error: submitted key lost");
                return false;
            }
            runList.add(run);
        }
        return true;
    }

    private static JsonArray keepSub10() {
        // 调用修改后的 rootElement
        JsonArray individualRunsArray = NetWorkHandler.rootElement.getAsJsonArray();

        // 修复点 2：使用 Iterator 安全地进行遍历和删除
        Iterator<JsonElement> iterator = individualRunsArray.iterator();
        boolean isSub10 = true;

        while (iterator.hasNext()) {
            JsonObject singleRunObject = iterator.next().getAsJsonObject();
            if (isSub10) {
                if (singleRunObject.has("time")) {
                    int time = singleRunObject.get("time").getAsInt();
                    if (time >= 600000) {
                        iterator.remove();
                        isSub10 = false;
                    }
                } else {
                    return null;
                }
            } else {
                iterator.remove();
            }
        }
        return individualRunsArray;
    }

    public static boolean parseVodData() {
        // 调用修改后的 rootElement
        JsonObject vodJsonObject = NetWorkHandler.rootElement.getAsJsonObject();

        if (vodJsonObject.has("data")) {
            JsonObject dataObject = vodJsonObject.getAsJsonObject("data");

            // 确保 data 不为空（因为有时没关联 Twitch 会返回 null）
            if (dataObject == null || dataObject.isJsonNull()) {
                NetWorkHandler.changeAndPrintCurrentStatus("error: data object is null");
                return false;
            }

            if (dataObject.has("twitch")) {
                VodData.twitchName = dataObject.get("twitch").getAsString();
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("error: twitch key lost");
                return false;
            }
            if (dataObject.has("vodId")) {
                if(dataObject.get("vodId").isJsonNull()){
                    return true;
                }
                VodData.vodId = dataObject.get("vodId").getAsLong();
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("error: vodId key lost");
                return false;
            }

            // 修复点 3：纠正了 vodOffeset 拼写错误，保证和 JSON 字段匹配
            if (dataObject.has("vodOffset")) {
                VodData.offset = dataObject.get("vodOffset").getAsLong();
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("error: vodOffset key lost");
                return false;
            }
            if (dataObject.has("finish")) {
                VodData.finishIGT = dataObject.get("finish").getAsLong();
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("error: finish key lost");
                return false;
            }
            if (dataObject.has("finishRta")) {
                VodData.finishRTA = dataObject.get("finishRta").getAsLong();
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("error: finishRta key lost");
                return false;
            }

        } else {
            NetWorkHandler.changeAndPrintCurrentStatus("error: data key lost");
            return false;
        }
        return true;
    }
}