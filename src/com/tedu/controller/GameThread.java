package com.tedu.controller;

import java.util.List;
import java.util.Map;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameState;
import com.tedu.show.GameJFrame;
import com.tedu.show.GameMainJPanel;
import com.tedu.element.*;
import com.tedu.show.GameOverPanel;

import javax.swing.*;

/**
 * @说明 游戏的主线程，用于控制游戏加载，游戏运行时自动化等。
 * @author xdedm0709
 */
public class GameThread extends Thread {
	private ElementManager em;
	private GameMainJPanel gamePanel; // 持有对视图面板的引用
	private GameJFrame gameFrame;     // 持有对主窗体的引用
	private GameState currentState;   // 线程自身管理游戏状态
	private Play player1;
	private Play player2;

	public GameThread(GameMainJPanel panel, GameJFrame frame) {
		this.em = ElementManager.getManager();
		this.gamePanel = panel;
		this.gameFrame = frame;
		this.currentState = GameState.PLAYING; // 线程一开始，游戏就处于进行时
		findPlayers();
	}

	@Override
	public void run() {
		while (this.currentState != GameState.EXIT) { // 游戏主循环
			// 游戏进行时
			if (this.currentState == GameState.PLAYING) {
				gameLogicUpdate(); // 更新游戏逻辑
				handleCollisions();  // 处理碰撞
				checkGameOverCondition();
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

	// 一个在启动时或需要时调用的方法，用于查找并分配玩家
	public void findPlayers() {
		for(ElementObj obj : em.getElementsByKey(GameElement.PLAY)) {
			Play p = (Play) obj;
			if ("player1".equals(p.getPlayerID())) {
				this.player1 = p;
			} else if ("player2".equals(p.getPlayerID())) {
				this.player2 = p;
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
		List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
		List<ElementObj> items = em.getElementsByKey(GameElement.ITEMS);

		// 爆炸与墙壁的碰撞
		for (ElementObj expObj : explosions) {
			if (!expObj.isLive()) continue;
			Explosion explosion = (Explosion) expObj;

			for (ElementObj mapObj : maps) {
				if (!mapObj.isLive()) continue;

				if (explosion.pk(mapObj)) {
					// 如果爆炸碰到了墙壁
					if (mapObj instanceof Brick) { // 如果是可摧毁的砖墙或草地
						mapObj.die();
					}
					else if (mapObj instanceof MapObj && ((MapObj) mapObj).getType().equals("IRON")) {
						// 如果是不可摧毁的铁墙
						// 只有当爆炸威力足够大时，才能摧毁铁墙
						if (explosion.getPower() >= 3) { // 假设需要威力达到3才能摧毁铁墙
							mapObj.die();
							System.out.println("高威力爆炸摧毁了铁墙！");
						}
					}
				}
			}
		}

		// 处理玩家与道具的碰撞
		for (ElementObj playerObj : players) {
			Play player = (Play) playerObj;
			for (ElementObj itemObj : items) {
				if (player.pk(itemObj) && itemObj instanceof Item) {

					// 直接调用 grantAbilityTo
					((Item) itemObj).grantAbilityTo(player);

					//  grantAbilityTo 内部会 setLive(false)，
					// 道具在下一帧就会被自动移除。
				}
			}
		}

		// 玩家与爆炸的碰撞
		for (ElementObj playerObj : players) {
			if (!playerObj.isLive()) continue;
			Play player = (Play) playerObj;

			for (ElementObj expObj : explosions) {
				if (player.pk(expObj)) {
					// 只标记玩家死亡，不再改变游戏状态
					player.die();
					System.out.println("玩家 " + player.getPlayerID() + " 被炸到了！");
					break; // 一个玩家在一帧内只会被炸到一次
				}
			}
		}
	}

	/**
	 * @说明 在每一帧的末尾检查游戏是否结束。
	 *      结束条件：存活的玩家数量 <= 1
	 */
	private void checkGameOverCondition() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);

		int aliveCount = 0;
		Play winner = null; // 用来记录最后的胜利者

		// 遍历所有玩家，统计存活人数
		for (ElementObj playerObj : players) {
			if (playerObj.isLive() && playerObj instanceof Play) {
				aliveCount++;
				winner = (Play) playerObj; // 暂时将这个存活的玩家视为胜利者
			}
		}

		// 如果存活玩家数量小于等于1，则游戏结束
		if (aliveCount <= 1) {
			this.currentState = GameState.GAME_OVER; // 切换游戏状态

			String winnerMessage;
			if (winner != null) {
				// 如果还有一个胜利者
				winnerMessage = "玩家 " + winner.getPlayerID() + " 胜利！";
			} else {
				// 如果存活人数为0 (比如同归于尽)
				winnerMessage = "平局！";
			}

			System.out.println("游戏结束: " + winnerMessage);

			// 命令主窗体切换到游戏结束画面，并告诉它谁赢了
			this.gameFrame.switchToGameOverPanel(winnerMessage);
		}
	}

}