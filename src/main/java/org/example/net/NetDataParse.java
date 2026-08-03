package org.example.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.data.Run;
import org.example.twitch.VodData;

import java.util.ArrayList;
import java.util.List;

public class NetDataParse {
    public static final List<Run> runList = new ArrayList<>();



    public static boolean parseWeeklyData(){
        JsonArray individualRunsArray = keepSub10();
        if(individualRunsArray==null){
            NetWorkHandler.changeAndPrintCurrentStatus("json error: time key lost");
            return false;
        }
        return makeRunList(individualRunsArray);
    }

    private static boolean makeRunList(JsonArray individualRunsArray) {
        for(JsonElement individualRun:individualRunsArray){
           JsonObject runJsonObject = individualRun.getAsJsonObject();
           Run run = new Run();
           if(runJsonObject.has("time")){
               run.setTime(runJsonObject.get("time").getAsInt());
           }
           else {
               NetWorkHandler.changeAndPrintCurrentStatus("error: time key lost");
               return false;
           }
           if(runJsonObject.has("worldId")){
               run.setWorldId(runJsonObject.get("worldId").getAsString());
           }
           else {
               NetWorkHandler.changeAndPrintCurrentStatus("error: worldId key lost");
               return false;
           }
           if(runJsonObject.has("submitted")){
               run.setRunStartTime(runJsonObject.get("submitted").getAsLong());
           }
           else {
               NetWorkHandler.changeAndPrintCurrentStatus("error: submitted key lost");
               return false;
           }
           runList.add(run);
        }
        return true;
    }

    private static JsonArray keepSub10() {
        JsonArray individualRunsArray = NetWorkHandler.rootObject.getAsJsonArray();
        boolean isSub10 = true;
        for (int i = 0; i < individualRunsArray.size(); i++) {
            JsonObject singleRunObject = individualRunsArray.get(i).getAsJsonObject();
            if (isSub10) {
                if (singleRunObject.has("time")) {
                    int time = singleRunObject.get("time").getAsInt();
                    if (time >= 600000) {
                        individualRunsArray.remove(i);
                        isSub10 = false;
                    }
                } else {
                    return null;
                }
            } else {
                individualRunsArray.remove(i);
            }
        }
        return individualRunsArray;
    }

    public static boolean parseVodData() {
        JsonObject vodJsonObject = NetWorkHandler.rootObject;
        if(vodJsonObject.has("data")){
            JsonObject dataObject = vodJsonObject.getAsJsonObject("data");
            if(dataObject.has("twitch")){
                VodData.twitchName = dataObject.get("twitch").getAsString();
            }
            else{
                NetWorkHandler.changeAndPrintCurrentStatus("error: twitch key lost");
                return false;
            }
            if(dataObject.has("vodId")){
                VodData.vodId = dataObject.get("vodId").getAsLong();
            }
            else{
                NetWorkHandler.changeAndPrintCurrentStatus("error: vodId key lost");
                return false;
            }
            if(dataObject.has("vodOffeset")){
                VodData.offset = dataObject.get("vodOffset").getAsLong();
            }
            else{
                NetWorkHandler.changeAndPrintCurrentStatus("error: vodOffset key lost");
                return false;
            }
        }
        else{
            NetWorkHandler.changeAndPrintCurrentStatus("error: data key lost");
            return false;
        }
        return true;
    }
}
