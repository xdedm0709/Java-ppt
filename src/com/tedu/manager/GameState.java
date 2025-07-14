package com.tedu.manager;

/**
 * @说明 定义游戏的所有可能状态
 */
public enum GameState {
    START_MENU,  // 开始菜单
    PLAYING,     // 游戏中
    INSTRUCTIONS,// 游戏说明
    ITEM_INFO,   // 道具说明
    GAME_OVER    // 游戏结束
}
