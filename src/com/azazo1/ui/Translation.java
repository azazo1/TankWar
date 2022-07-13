package com.azazo1.ui;

public class Translation {
    public String localPlayingPanelTitle = "Local Playing";
    public String launchButtonText = "Launch";
    public String playerNumLabelText = "Player Number: ";
    public String onlinePlayingPanelTitle = "Online Playing";
    public String ipAddressSeparator = ":";
    public String typeIPAddressLabelText = "Server IP Address: ";
    public String typeNameLabelText = "Nickname: ";
    public String menuPanelTitle = "Launch Menu";
    
    public Translation() {
    }
    
    public static class Chinese extends Translation {
        
        public Chinese() {
            localPlayingPanelTitle = "本地游戏";
            launchButtonText = "启动";
            playerNumLabelText = "玩家人数: ";
            onlinePlayingPanelTitle = "在线游戏";
            typeIPAddressLabelText = "服务器 IP 地址: ";
            typeNameLabelText = "昵称: ";
            menuPanelTitle = "启动菜单";
        }
        
    }
}
