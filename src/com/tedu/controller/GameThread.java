package com.tedu.controller;

import java.util.List;
import java.util.Map;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameState;
import com.tedu.show.GameJFrame;
import com.tedu.show.GameOverPanel;
import com.tedu.show.GameMainJPanel;

/**
 * @说明 游戏的主线程，用于控制游戏加载，游戏运行时自动化等。
 * @author xdedm0709
 */
public class GameThread extends Thread {
	private ElementManager em;
	private GameMainJPanel gamePanel; // 持有对视图面板的引用
	private GameJFrame gameFrame;     // 持有对主窗体的引用
	private GameState currentState;   // 线程自身管理游戏状态

	public GameThread(GameMainJPanel panel, GameJFrame frame) {
		this.em = ElementManager.getManager();
		this.gamePanel = panel;
		this.gameFrame = frame;
		this.currentState = GameState.PLAYING; // 线程一开始，游戏就处于进行时
	}

	@Override
	public void run() {
		while (this.currentState != GameState.EXIT) { // 游戏主循环
			// 游戏进行时
			if (this.currentState == GameState.PLAYING) {
				gameLogicUpdate(); // 更新游戏逻辑
				handleCollisions();  // 处理碰撞
			}

			// 无论何种状态，都通知视图重绘
			gamePanel.repaint();

			try {
				sleep(10); // 控制游戏速度
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @说明 游戏核心逻辑更新，驱动所有元素的行为
	 */
	private void gameLogicUpdate() {
		Map<GameElement, List<ElementObj>> all = em.getGameElements();
		long gameTime = System.currentTimeMillis();

		for (GameElement ge : GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			for (int i = list.size() - 1; i >= 0; i--) {
				ElementObj obj = list.get(i);
				if (!obj.isLive()) {
					list.remove(i);
					continue;
				}
				obj.model(gameTime);
			}
		}
	}

	/**
	 * @说明 专门处理碰撞的方法
	 */
	private void handleCollisions() {
		List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
		List<ElementObj> explosions = em.getElementsByKey(GameElement.EXPLOSION);

		// 如果没有玩家或玩家已死亡，则无需检测
		if (players.isEmpty() || !players.get(0).isLive()) {
			return;
		}

		ElementObj player = players.get(0);

		for (ElementObj explosion : explosions) {
			if (player.pk(explosion)) {
				player.die();
				this.currentState = GameState.GAME_OVER;

				// 命令主窗体切换到游戏结束画面
				gameFrame.switchToGameOverPanel();

				System.out.println("玩家死亡！游戏结束。");
				break;
			}
		}
	}

	/**
	 * @说明 在主窗体上显示游戏结束菜单
	 */
	private void showGameOverMenu() {
		// 这个方法现在由控制器线程调用，它会直接操作JFrame
		GameOverPanel gameOverPanel = new GameOverPanel(this.gameFrame);
		gameOverPanel.setBounds(0, 0, GameJFrame.GameX, GameJFrame.GameY);
		gameFrame.add(gameOverPanel, 0); // 直接添加到JFrame的最上层
		gameFrame.revalidate();
		gameFrame.repaint();
	}
}