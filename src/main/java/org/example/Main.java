package org.example;

import org.example.file.FileManager;
import org.example.net.NetDataParse;
import org.example.net.NetWorkHandler;
import org.example.twitch.TwitchDownloader;
import org.example.twitch.VodData;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        FileManager.initialize();
        NetWorkHandler.fetchWeeklyData();
        if(!NetDataParse.parseWeeklyData()){
            NetWorkHandler.changeAndPrintCurrentStatus("error: fail to parse Weekly Data");
            return;
        }
        FileManager.addNewRecord();
        FileManager.makeUndownloadedList();
        download();
    }

    private static void download() throws IOException {
        if(FileManager.undownloadedList.isEmpty()){
            NetWorkHandler.changeAndPrintCurrentStatus("nothing to download,exit...");
            return;
        }
        for(String worldId: FileManager.undownloadedList){
            NetWorkHandler.fetchVodData(worldId);
            if(!NetDataParse.parseVodData()){
                NetWorkHandler.changeAndPrintCurrentStatus("error: fail to parse vod data for " + worldId);
                // 改为 continue：如果当前世界没有 VOD，跳过它继续处理下一个，而不是直接退出程序
                continue;
            }
            FileManager.prepareTheDownloading(worldId);

            // 获取下载器返回的状态
            boolean isSuccess = TwitchDownloader.download(worldId, VodData.vodId, VodData.offset,VodData.twitchName,VodData.finishIGT, VodData.finishRTA);

            // 只有完整下载成功，才写入简介并标记为已下载
            if(isSuccess){
                TwitchDownloader.writeMetaDataToDescription(worldId);
                FileManager.setDownloaded(worldId);
                System.out.println(worldId + " 处理完毕，已标记为下载。");
            } else {
                NetWorkHandler.changeAndPrintCurrentStatus("warning: download failed or interrupted for " + worldId);
            }
        }
    }
}