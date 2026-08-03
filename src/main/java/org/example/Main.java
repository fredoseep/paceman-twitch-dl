package org.example;

import org.example.file.FileManager;
import org.example.net.NetDataParse;
import org.example.net.NetWorkHandler;
import org.example.twitch.TwitchDownloader;
import org.example.twitch.VodData;

import java.io.File;
import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
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
        for(String worldId: FileManager.undownloadedList){
            NetWorkHandler.fetchVodData(worldId);
            if(!NetDataParse.parseVodData()){
                NetWorkHandler.changeAndPrintCurrentStatus("error: fail to parse vod data");
                return;
            }
            FileManager.prepareTheDownloading(worldId);
            TwitchDownloader.download(VodData.vodId,VodData.offset);
            TwitchDownloader.writeMetaDataToDescription();
            FileManager.setDownloaded(worldId);
        }
    }
}