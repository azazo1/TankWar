package com.azazo1.ui;

public class Translation {
    public static final Translation instance = new Translation();
    public final String ipAddressSeparator = ":";
    public final String hasBulletChar = "■";
    public final String emptyBulletChar = "□";
    public String localPlayingPanelTitle = "Local Playing";
    public String launchButtonText = "Launch";
    public String playerNumLabelText = "Player Number: ";
    public String wallMapFilesComboLabelText = "Wall Maps: ";
    public String onlinePlayingPanelTitle = "Online Playing";
    public String typeIPAddressLabelText = "Server IP Address: ";
    public String typeNameLabelText = "Nickname: ";
    public String menuPanelTitle = "Launch Menu";
    public String clickToFocusHint = "Click Here To Focus";
    public String errorTextFormat = """
            Exception occurred.
            From: %s
            Detail: %s""";
    public String errorTitle = "Exception occurred";
    public String playingModeNotChosen = "Which playing mode hasn't been chosen";
    public String FPSLabelText = "FPS: ";
    public String mapSizeLabelFormat = "Map Size: (%d, %d)";
    public String tankNumLabelText = "Tank Num: ";
    public String bulletNumLabelText = "Bullet Num: ";
    public String loading = "Loading...";
    public String qTreeDepthLabelText = "Quadtree depth: ";
    public String frameTitle = "Tank War(Practice) --Author: azazo1";
    public String basicTankInfoFormat = """
            <html>Tank %s (Seq: %d):<br>
            &emsp;&emsp;Rank: %d<br>
            &emsp;&emsp;Endurance (Now / Total): %d / %d<br>
            &emsp;&emsp;LivingTime: %d<br>
            </html>
            """;
    public String backToMenuButtonText = "Return to Launch Menu";
    public String resultTitle = "Game Result";
    public String onlineModeStillDeveloping = "Online mode is still developing";
    public String localPlayerNamesBoxHint = "Player Name List (Click me to refresh):";
    public String onlineClientListTitle = "Clients";
    public String onlinePlayerListTitle = "Players";
    public String gameModeSelectorTitle = "Select Game Mode";
    public String registerButtonText = "Register";
    public String gameIntroEditTitleFormat = """
            <html>
            Edit Game Config:<br>
            &emsp;&emsp;GameMap Size (%dx%d)
            </html>
            """;
    public String gameIntroEditTitle = "Edit Game Config";
    public String gameIntroEditButtonText = "Edit (only Host)";
    public String startGameButtonText = "Start Game (only Host)";
    public String nicknameInputTitle = "Input Nickname:";
    public String yourProfileFormat = """
            <html>
            Your nickname: %s<br>
            Your seq: %d<br>
            Are you host: %s<br>
            Your playing mode: %s
            </html>
            """;
    public String spectatorMode = "Spectator Mode";
    public String playerMode = "Player Mode";
    public String connectionInfoFormat = "Connected to: [%s:%d]";
    public String succeedTitle = "Succeed";
    public String registerSucceeded = "Registered succeeded";
    public String playerMaximum = "Maximum number of players, please switch to spectator mode";
    public String nameCollision = "Your nickname is the same as the other one, please change";
    public String notDecided = "Not Decided Yet";
    public String havingRegistered = "You have been registered";
    public String quitRoomButtonText = "Quit Room";
    public String queryResultButtonText = "Query Game Result";
    public String emptyResult = "Null";
    public String startGameFailed = "Failed to start game";
    public String startGameSucceeded = "Succeeded to start game";
    public String notEnoughPlayer = "Not enough players";
    public String clientInfoFormat = "Seq: %d, IP: [%s:%d], Host: %s";
    public String serverHasNoWallMapFile = "Server has no such wall map file";
    public String serverNotInWaitingState = "Server is not in waiting state";
    public String notHost = "You are not host";
    public String postGameIntroSucceeded = "Succeeded to edit the game configuration";
    public String confirmButtonText = "Confirm";
    public String invalidMapSizeInput = "Invalid map size";
    public String mapSizeEditTitle = "Map Size \"[width] x [height]\":";
    public String registeredFrameTitleFormat = "Hi, %s";
    public String connectedFrameTitle = "Please register, or you will be kicked out when the game starts";
    public String kicked = "You are kicked";
    public String msgSyncRate = "Msg sync rate (Hz):";

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
            errorTextFormat = """
                    出现错误
                    来自: %s
                    详情: %s""";
            errorTitle = "错误发生了";
            playingModeNotChosen = "没有选择游戏模式";
            FPSLabelText = "帧率: ";
            mapSizeLabelFormat = "地图大小: (%d, %d)";
            tankNumLabelText = "坦克数量: ";
            bulletNumLabelText = "子弹数量: ";
            loading = "加载中...";
            qTreeDepthLabelText = "四叉树深度: ";
            frameTitle = "坦克战争(练习) --作者:azazo1";
            basicTankInfoFormat = """
                    <html>坦克 %s (序号: %d):<br>
                    &emsp;&emsp;排名: %d<br>
                    &emsp;&emsp;生命 (现在 / 总共): %d / %d<br>
                    &emsp;&emsp;存活时间: %.1f s<br>
                    </html>
                    """;
            backToMenuButtonText = "回到启动菜单";
            resultTitle = "游戏结果";
            onlineModeStillDeveloping = "在线模式仍在开发中";
            localPlayerNamesBoxHint = "玩家列表 (点我刷新):";
            onlineClientListTitle = "客户端";
            onlinePlayerListTitle = "玩家";
            gameModeSelectorTitle = "选择游戏模式";
            registerButtonText = "注册";
            gameIntroEditTitleFormat = """
                    <html>
                    编辑本局游戏配置:<br>
                    &emsp;&emsp;游戏画布大小 (%dx%d)
                    </html>
                    """;
            gameIntroEditTitle = "编辑本局游戏配置";
            gameIntroEditButtonText = "编辑 (仅许房主)";
            startGameButtonText = "启动游戏 (仅许房主)";
            nicknameInputTitle = "输入昵称:";
            yourProfileFormat = """
                    <html>
                    你的昵称: %s<br>
                    你的序号: %d<br>
                    你是否是房主: %s<br>
                    你的游戏模式: %s
                    </html>
                    """;
            spectatorMode = "旁观者模式";
            playerMode = "玩家模式";
            connectionInfoFormat = "连接到: [%s:%d]";
            succeedTitle = "成功";
            registerSucceeded = "注册成功";
            playerMaximum = "游戏玩家数量达到最大值, 请切换到旁观者模式";
            nameCollision = "你的昵称和他人重名了, 请更改";
            notDecided = "仍未决定";
            havingRegistered = "你已经注册过了";
            quitRoomButtonText = "退出房间";
            queryResultButtonText = "查询游戏结果";
            emptyResult = "无结果";
            startGameFailed = "开始游戏失败";
            startGameSucceeded = "开启游戏成功";
            notEnoughPlayer = "玩家数量不足";
            clientInfoFormat = "序号: %d, IP: [%s:%d], 房主: %s";
            serverHasNoWallMapFile = "服务器没有对应的墙图文件";
            serverNotInWaitingState = "服务器不处在 等待 阶段";
            notHost = "您不是房主";
            postGameIntroSucceeded = "成功修改游戏配置";
            confirmButtonText = "确认";
            invalidMapSizeInput = "无效的地图尺寸";
            mapSizeEditTitle = "地图尺寸 \"[宽] x [高]\":";
            connectedFrameTitle = "请注册以进行游戏, 否则当游戏开始时你会被踢出房间";
            registeredFrameTitleFormat = "你好, %s";
            kicked = "你被踢出了";
            msgSyncRate = "消息同步速率 (Hz):";
        }
    }
}
