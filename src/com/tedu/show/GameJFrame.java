package com.tedu.show;

import javax.swing.*;

import com.tedu.controller.GameListener;
import com.tedu.element.Play;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.ElementManager;

import java.util.List;

public class GameJFrame extends JFrame {
	public static int GameX = 800;
	public static int GameY = 600;

	private JPanel currentPanel = null; // 当前正在显示的面板

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

	// --- 提供给外部调用的切换指令 ---

	public void switchToStartMenu() {
		switchPanel(new StartMenuPanel(this));
	}

	public void switchToGamePanel() {
		System.out.println("指令：切换到游戏面板...");

		ElementManager em = ElementManager.getManager();
		// 1. 清理上一局的游戏元素
		em.init();

		// 2. 加载本关卡的地图
		System.out.println("正在加载地图...");
		GameLoad.MapLoad(1);

		// 3. 【核心修改】直接在这里创建玩家，不再使用 loadPlay()
		System.out.println("正在创建玩家...");

		// a. 定义玩家在左下角的坐标
		int playerX = 50; // 离左边框50像素
		int playerY = GameJFrame.GameY - 150; // GameY是窗口高度，减去一个值让玩家在底部

		// b. 确保玩家的动画帧已加载
		List<ImageIcon> playerFrames = GameLoad.imgMaps.get("up"); // 获取一个默认方向的动画

		if (playerFrames != null && !playerFrames.isEmpty()) {
			// c. 创建 Play 实例
			Play player = new Play();

			// d. 手动设置所有属性
			player.setX(playerX);
			player.setY(playerY);

			ImageIcon firstFrame = playerFrames.get(0);
			player.setW(firstFrame.getIconWidth());
			player.setH(firstFrame.getIconHeight());
			player.setIcon(firstFrame);
			// 你可以在这里设置其他需要的初始属性，比如 fx

			// e. 将创建好的玩家添加到管理器
			em.addElement(player, GameElement.PLAY);
			System.out.println("玩家已在左下角创建并添加。坐标:(" + playerX + "," + playerY + ")");

		} else {
			System.err.println("创建玩家失败！因为无法从 GameLoad.imgMaps 获取玩家动画帧。");
		}

		// 4. 创建并设置游戏面板
		GameMainJPanel gamePanel = new GameMainJPanel();

		// 5. 添加键盘监听器
		if (this.getKeyListeners().length > 0) {
			this.removeKeyListener(this.getKeyListeners()[0]);
		}
		this.addKeyListener(new GameListener());

		// 6. 为窗体请求焦点
		this.setFocusable(true);


		// 7. 切换到游戏面板
		switchPanel(gamePanel);
		this.requestFocusInWindow();

		// 8. 启动游戏线程
		System.out.println("启动游戏线程...");
		new Thread(gamePanel).start();
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
	}
}