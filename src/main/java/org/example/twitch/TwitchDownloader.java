package org.example.twitch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TwitchDownloader {


    // 保留 boolean 返回值，供 Main.java 判断是否下载成功
    public static boolean download(String worldId, long vodId, long offset, String twitchId, long finishIGT, long finishRTA) {
        long finishIGTSeconds = finishIGT /1000;
        long durationSeconds = finishRTA/1000;
        long startSeconds = offset;
        long endSeconds = offset + durationSeconds+20;
        long minutes = finishIGTSeconds / 60;
        long seconds = finishIGTSeconds % 60;
        String videoUrl = "https://www.twitch.tv/videos/" + vodId;
        String outputPath = Paths.get("paceman-twitch-dl", worldId, twitchId+" "+minutes+"_"+seconds+".mp4").toString();

        System.out.println("开始下载 VOD: " + vodId + ", 截取时间段: " + startSeconds + "s - " + endSeconds + "s");
        String proxyUrl = "http://127.0.0.1:7897";

        // 最精简的单阶段下载：网络路由已交由底层的 Clash 规则引擎全权接管
        ProcessBuilder processBuilder = new ProcessBuilder(
                "yt-dlp",
                "--proxy", proxyUrl, // <--- 新增这行：强制 yt-dlp 走本地代理
                "--download-sections", "*" + startSeconds + "-" + endSeconds,
                "-o", outputPath,
                videoUrl
        );

        // 重定向输出到控制台，实时查看下载进度
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // 验证退出码：0 代表无任何错误顺利完成
            if (exitCode == 0) {
                System.out.println("VOD 下载完成: " + outputPath);
                return true;
            } else {
                System.out.println("下载遇到错误，yt-dlp 退出码: " + exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("启动下载进程失败。");
            e.printStackTrace();
            return false;
        }
    }

    public static void writeMetaDataToDescription(String worldId) {
        try {
            Path descriptionPath = Paths.get("paceman-twitch-dl", worldId, "description.txt");

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd/yyyy", Locale.ENGLISH);
            String currentDate = dateFormat.format(new Date());

            String metaData = String.format("%s\nTwitch/@%s\nhttps://www.twitch.tv/videos/%d\n",
                    currentDate,
                    VodData.twitchName,
                    VodData.vodId);

            Files.writeString(descriptionPath, metaData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Metadata 写入成功。");

        } catch (IOException e) {
            System.out.println("写入 Metadata 失败。");
            e.printStackTrace();
        }
    }
}