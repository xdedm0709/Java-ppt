package com.tedu.show;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tedu.controller.GameListener;
import com.tedu.manager.GameLoad;
import com.tedu.manager.ElementManager;

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
		System.out.println("指令：切换到游戏面板..."); // 增加调试日志，确认此方法被调用

		// 1. 【新增】清理上一局的游戏元素
		//    这可以确保每次开始游戏都是一个全新的状态。
		ElementManager.getManager().init();

		// 2. 【新增】加载本关卡的地图
		System.out.println("正在加载地图...");
		GameLoad.MapLoad(1); // 假设从第一关开始

		// 3. 【新增】创建玩家实例并加入到管理器中
		System.out.println("正在加载玩家...");
		GameLoad.loadPlay();

		// 4. 创建并设置游戏面板
		GameMainJPanel gamePanel = new GameMainJPanel();

		// 5. 添加键盘监听器
		//    为了避免重复添加监听器导致问题，最好先移除旧的。
		if (this.getKeyListeners().length > 0) {
			this.removeKeyListener(this.getKeyListeners()[0]);
		}
		this.addKeyListener(new GameListener());

		// 6. 为窗体请求焦点
		this.setFocusable(true);
		this.requestFocusInWindow();

		// 7. 切换到游戏面板
		switchPanel(gamePanel);

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