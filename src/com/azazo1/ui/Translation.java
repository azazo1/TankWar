package com.azazo1.ui;

public class Translation {
    public static final Translation instance = new Translation();
    public String localPlayingPanelTitle = "Local Playing";
    public String launchButtonText = "Launch";
    public String playerNumLabelText = "Player Number: ";
    public String wallMapFilesComboLabelText = "Wall Maps: ";
    public String onlinePlayingPanelTitle = "Online Playing";
    public String ipAddressSeparator = ":";
    public String typeIPAddressLabelText = "Server IP Address: ";
    public String typeNameLabelText = "Nickname: ";
    public String menuPanelTitle = "Launch Menu";
    public String clickToFocusHint = "Click Here To Focus";
    public String readingWallMapErrorText = "Exception occurred when read wall map file";
    public String errorTitle = "Exception occurred";
    public String playingModeNotChosen = "Which playing mode hasn't been chosen";
    public String FPSLabelText = "FPS: ";
    public String mapSizeLabelFormat = "Map Size: (%d, %d)";
    public String tankNumLabelText = "Tank Num: ";
    public String bulletNumLabelText = "Bullet Num: ";
    public String loading = "Loading...";
    
    public Translation() {
    }
    
    public static class Chinese extends Translation {
        public static final Chinese instance = new Chinese();
        
        public Chinese() {
            localPlayingPanelTitle = "本地游戏";
            launchButtonText = "启动";
            playerNumLabelText = "玩家人数: ";
            wallMapFilesComboLabelText = "游戏墙图: ";
            onlinePlayingPanelTitle = "在线游戏";
            typeIPAddressLabelText = "服务器 IP 地址: ";
            typeNameLabelText = "昵称: ";
            menuPanelTitle = "启动菜单";
            clickToFocusHint = "点击这里聚焦";
            readingWallMapErrorText = "读取墙图文件时出现错误";
            errorTitle = "错误发生了";
            playingModeNotChosen = "没有选择游戏模式";
            FPSLabelText = "帧率: ";
            mapSizeLabelFormat = "地图大小: (%d, %d)";
            tankNumLabelText = "坦克数量: ";
            bulletNumLabelText = "子弹数量: ";
            loading = "加载中...";
        }
        
    }
}
