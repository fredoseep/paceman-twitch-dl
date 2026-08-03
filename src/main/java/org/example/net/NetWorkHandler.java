package org.example.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NetWorkHandler {
    private static final String WEEKLY_API = "https://paceman.gg/api/cs/leaderboard?filter=1&removeDuplicates=0&date=";
    private static final String VOD_DATA_API = "https://paceman.gg/stats/api/getWorld/?worldId=";
    private static String CURRENT_STATUS = "IDLE";
    private static Gson gson = new Gson();

    // 修复点 1：将 JsonObject 改为更通用的 JsonElement，以兼容 Array 和 Object 的根节点
    public static JsonElement rootElement;

    public static void fetchWeeklyData() throws MalformedURLException {
        String API_WITH_DATE = getApiWithDate();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(API_WITH_DATE);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            changeAndPrintCurrentStatus("TRYING_CONNECTION(weekly)");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                changeAndPrintCurrentStatus("connection error(weekly), response code: " + responseCode+" request url: "+ API_WITH_DATE);
                return;
            }
            changeAndPrintCurrentStatus("SUCCESS(weekly), GOT RESPONSE.");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            bufferedReader.close();

            // 使用 JsonElement 进行解析
            rootElement = gson.fromJson(stringBuilder.toString(), JsonElement.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fetchVodData(String worldId) {
        String API_WITH_WORLD_ID = VOD_DATA_API + worldId;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(API_WITH_WORLD_ID);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            changeAndPrintCurrentStatus("TRYING_CONNECTION(VOD)");

            int responseCode = connection.getResponseCode();
            if(responseCode != 200){
                changeAndPrintCurrentStatus("connection error(VOD), response code: " + responseCode+" request url: "+ API_WITH_WORLD_ID);
                return; // 遇到错误应该尽早 return，防止后续解析空数据
            }
            // 修复了这里复制粘贴遗留的打印信息
            changeAndPrintCurrentStatus("SUCCESS(VOD), GOT RESPONSE.");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            bufferedReader.close();
            System.out.println("debug: vod response: "+stringBuilder.toString());
            // 使用 JsonElement 进行解析
            rootElement = gson.fromJson(stringBuilder.toString(), JsonElement.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getApiWithDate() {
        return WEEKLY_API + System.currentTimeMillis();
    }

    public static void changeAndPrintCurrentStatus(String newStatus) {
        CURRENT_STATUS = newStatus;
        System.out.println(CURRENT_STATUS);
    }
}