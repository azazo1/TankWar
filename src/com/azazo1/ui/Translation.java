package com.azazo1.ui;

public class Translation {
    public String localPlayingPanelTitle = "Local Playing";
    public String launchButtonText = "Launch";
    public String playerNumLabelText = "Player Number: ";
    public String onlinePlayingPanelTitle = "Online Playing";
    
    public Translation() {
    }
    
    public static class Chinese extends Translation {
        
        public Chinese() {
            localPlayingPanelTitle = "本地游戏";
            launchButtonText = "启动";
            playerNumLabelText = "玩家人数: ";
            onlinePlayingPanelTitle = "在线游戏";
        }
        
    }
}
