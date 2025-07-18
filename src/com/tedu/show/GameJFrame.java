package com.tedu.show;

import javax.swing.*;

import com.tedu.controller.GameListener;
import com.tedu.controller.GameThread;
import com.tedu.element.Play;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.ElementManager;

import java.awt.event.MouseAdapter;
import java.util.List;

public class GameJFrame extends JFrame {
	public static final int CONTENT_PANE_WIDTH = 800;
	public static int GameX = 800;
	public static int GameY = 600;
	private JPanel currentPanel = null; // 当前正在显示的面板
	public static final int TILE_SIZE = 30; // 定义为公共静态常量
	public static final int CONTENT_PANE_HEIGHT = 529;
	public GameJFrame() {
		init();
	}

	public void init() {
		this.setSize(GameX, GameY);
		this.setTitle("泡泡堂");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		// 预加载所有游戏资源
		GameLoad.loadAll();
	}

	// 切换面板的核心方法
	private void switchPanel(JPanel newPanel) {
		if (currentPanel != null) {
			this.remove(currentPanel); // 移除旧面板
		}
		this.currentPanel = newPanel;
		this.add(currentPanel); // 添加新面板
		this.revalidate(); // 使布局管理器重新生效
		this.repaint(); // 重绘界面
		currentPanel.requestFocusInWindow(); // 为新面板请求焦点
	}

	// 提供给外部调用的切换指令

	public void switchToStartMenu() {
		switchPanel(new StartMenuPanel(this));
	}

	public void switchToGamePanel() {

		System.out.println("指令：切换到游戏面板...");

		// 加载游戏世界
		ElementManager.getManager().init();
		GameLoad.MapLoad(1);

		// 创建第一个玩家
		int gridX1 = 1;
		int gridY1 = 11;
		int gridPixelX1 = gridX1 * TILE_SIZE;
		int gridPixelY1 = gridY1 * TILE_SIZE;
		int offsetX1 = (TILE_SIZE - Play.PLAYER_SIZE) / 2;
		int offsetY1 = (TILE_SIZE - Play.PLAYER_SIZE) / 2;
		int playerX1 = gridPixelX1 + offsetX1;
		int playerY1 = gridPixelY1 + offsetY1;
		GameLoad.loadPlayer(playerX1, playerY1, "up", "player1");

		// 创建第二个玩家
		int gridX2 = 25; // 第二个玩家的初始格子X坐标
		int gridY2 = 1;  // 第二个玩家的初始格子Y坐标
		int gridPixelX2 = gridX2 * TILE_SIZE;
		int gridPixelY2 = gridY2 * TILE_SIZE;
		int offsetX2 = (TILE_SIZE - Play.PLAYER_SIZE) / 2;
		int offsetY2 = (TILE_SIZE - Play.PLAYER_SIZE) / 2;
		int playerX2 = gridPixelX2 + offsetX2;
		int playerY2 = gridPixelY2 + offsetY2;
		GameLoad.loadPlayer(playerX2, playerY2, "up", "player2");

		GameMainJPanel gamePanel = new GameMainJPanel();

		// 将面板设置到窗体上 (这是UI的切换)
		switchPanel(gamePanel);

		// 创建控制器线程
		GameThread gameThread = new GameThread(gamePanel, this);

		// 设置输入监听 (将监听器与控制器关联)
		if (this.getKeyListeners().length > 0) {
			this.removeKeyListener(this.getKeyListeners()[0]);
		}
		this.addKeyListener(new GameListener());

		this.setFocusable(true);
		this.requestFocusInWindow();

		// 启动游戏线程
		new GameThread(gamePanel, this).start();
	}
	// 切换到游戏结束面板的方法
	/**
	 * @说明 切换到游戏结束面板的方法
	 * @param winnerMessage 要在结束画面上显示的消息
	 */
	public void switchToGameOverPanel(String winnerMessage) {
		// 使用 SwingUtilities 来确保线程安全
		SwingUtilities.invokeLater(() -> {
			// 移除键盘监听器，因为在结束菜单上不再需要
			if (this.getKeyListeners().length > 0) {
				// 需要注意， getKeyListeners() 返回的是数组，可能需要遍历
				// 为了简单起见，假设只有一个
				this.removeKeyListener(this.getKeyListeners()[0]);
			}

			// 调用自己的 switchPanel 方法来切换内容
			switchPanel(new GameOverPanel(this, winnerMessage));
		});
	}



	public void switchToInstructionsPanel() {
		switchPanel(new InfoPanel(this, "instructions_screen"));
	}

	public void switchToItemInfoPanel() {
		switchPanel(new InfoPanel(this, "item_info_screen"));
	}

	public void start() {
		// 游戏启动时，首先显示开始菜单
		switchToStartMenu();
		this.setVisible(true);
		System.out.println("内容面板的实际高度是: " + this.getContentPane().getHeight());
	}
}