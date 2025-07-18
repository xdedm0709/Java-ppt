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
		// 1. 计算目标格子 (1, 11) 的左上角像素坐标
		int gridX = 1;
		int gridY = 11;
		int gridPixelX = gridX * TILE_SIZE; // 1 * 30 = 30
		int gridPixelY = gridY * TILE_SIZE; // 11 * 30 = 330

		// 2. 计算偏移量，使玩家居中于格子
		int offsetX = (TILE_SIZE - Play.PLAYER_SIZE) / 2; // (30 - 30) / 2 = 0
		int offsetY = (TILE_SIZE - Play.PLAYER_SIZE) / 2; // (30 - 30) / 2 = 0

		// 3. 计算玩家最终的左上角坐标
		int playerX = gridPixelX + offsetX;
		int playerY = gridPixelY + offsetY;

		// 4. 调用 GameLoad 创建玩家
		GameLoad.loadPlayer(playerX, playerY, "up");


		// 1. 创建游戏面板实例
		GameMainJPanel gamePanel = new GameMainJPanel();

		// 2. 将面板设置到窗体上
		switchPanel(gamePanel);

		// 3. 添加键盘监听器
		if (this.getKeyListeners().length > 0) {
			this.removeKeyListener(this.getKeyListeners()[0]);
		}
		this.addKeyListener(new GameListener());
		this.setFocusable(true);
		this.requestFocusInWindow();

		// 4. 创建并启动游戏主线程，将面板和窗体的引用传给它
		System.out.println("启动游戏主线程...");
		GameThread gameThread = new GameThread(gamePanel, this);
		gameThread.start();
	}

	// 切换到游戏结束面板的方法
	public void switchToGameOverPanel() {
		// 确保UI更新操作在事件分发线程（EDT）中执行，这是Swing的最佳实践
		SwingUtilities.invokeLater(() -> {
			// 移除键盘监听器，因为在结束菜单上不需要它
			if (this.getKeyListeners().length > 0) {
				this.removeKeyListener(this.getKeyListeners()[0]);
			}
			switchPanel(new GameOverPanel(this));
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