package com.tedu.show;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.tedu.element.ElementObj;
import com.tedu.element.Play;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameState;

/**
 * @说明 游戏的主要面板
 * @author renjj
 * @功能说明 主要进行元素的显示，同时进行界面的刷新(多线程)
 *
 * @题外话 java开发实现思考的应该是：做继承或者是接口实现
 *
 * @多线程刷新 1.本类实现线程接口
 *             2.本类中定义一个内部类来实现
 */
public class GameMainJPanel extends JPanel implements Runnable{
	//	联动管理器
	private ElementManager em;
	private GameState currentState; // 用于管理游戏状态的变量
	private GameJFrame gameFrame;   // 持有对主窗体的引用，以便把引用传递给结束菜单

	public GameMainJPanel(GameJFrame frame) {
		this.gameFrame = frame;
		this.setLayout(null); // 为能够添加子面板，设置布局为null
		init();
	}

	public void init() {
		em = ElementManager.getManager();//得到元素管理器对象
		this.currentState = GameState.PLAYING; // 游戏面板一创建，状态就是 PLAYING
	}
	/**
	 * paint方法是进行绘画元素。
	 * 绘画时是有固定的顺序，先绘画的图片会在底层，后绘画的图片会覆盖先绘画的
	 * 约定：本方法只执行一次,想实时刷新需要使用 多线程
	 */
	@Override  //用于绘画的    Graphics 画笔 专门用于绘画的
	public void paint(Graphics g) {
		super.paint(g);  //调用父类的paint方法
//		map  key-value  key是无序不可重复的。
//		set  和map的key一样 无序不可重复的
		if (currentState == GameState.PLAYING) {
			Map<GameElement, List<ElementObj>> all = em.getGameElements();
			for (GameElement ge : GameElement.values()) {
				List<ElementObj> list = all.get(ge); // 从map中获取列表
				for (ElementObj obj : list) {
					obj.showElement(g);
				}
			}
		}


//		Set<GameElement> set = all.keySet(); //得到所有的key集合
//		for(GameElement ge:set) { //迭代器
//			List<ElementObj> list = all.get(ge);
//			for(int i=0;i<list.size();i++) {
//				ElementObj obj=list.get(i);//读取为基类
//				obj.showElement(g);//调用每个类的自己的show方法完成自己的显示
//			}
//		}

	}
	@Override
	public void run() {  //接口实现
		while (true) {
			// 只有当游戏处于 PLAYING 状态时，才运行游戏逻辑
			if (currentState == GameState.PLAYING) {
				// 1. 游戏逻辑更新
				gameLogicUpdate();
			}

			// 2. 界面重绘
			this.repaint();

			// 3. 休眠，控制游戏速度
			try {
				Thread.sleep(10); // 大约100 FPS
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @说明 游戏核心逻辑，在这里驱动所有元素的行为
	 */
	private void gameLogicUpdate() {
		Map<GameElement, List<ElementObj>> all = em.getGameElements();
		long gameTime = System.currentTimeMillis();
		// 遍历所有类型的元素
		for (GameElement ge : GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			// 倒序遍历，防止在遍历中删除元素时出错
			for (int i = list.size() - 1; i >= 0; i--) {
				ElementObj obj = list.get(i);
				// 如果元素已经死亡，就将它从列表中移除，并跳过后续处理
				if (!obj.isLive()) {
					list.remove(i);
					continue;
				}
				// 调用模板方法，驱动元素的 移动、换装、开火 等所有行为
				obj.model(gameTime);
				handleCollisions();
			}
		}
	}
	/**
	 * 专门处理碰撞和死亡的方法
	 */
	private void handleCollisions() {
		// 如果游戏已经结束，就不再需要检测碰撞
		if (this.currentState != GameState.PLAYING) {
			return;
		}
		List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
		List<ElementObj> explosions = em.getElementsByKey(GameElement.EXPLOSION);

		// 如果没有玩家，或者玩家已经死了，就不再检测
		if (players.isEmpty() || !players.get(0).isLive()) {
			return;
		}

		ElementObj player = players.get(0);

		for (ElementObj explosion : explosions) {
			if (player.pk(explosion)) {
				player.die(); // 玩家死亡
				this.currentState = GameState.GAME_OVER; // 切换游戏状态
				showGameOverMenu(); // 显示游戏结束菜单
				System.out.println("玩家死亡！游戏结束。");
				break;
			}
		}
	}

	/**
	 * 显示游戏结束菜单的方法
	 */
	private void showGameOverMenu() {
		// 创建游戏结束面板，并把 GameJFrame 的引用传给它
		GameOverPanel gameOverPanel = new GameOverPanel(this.gameFrame);
		// 设置它的位置和大小以覆盖整个游戏面板
		gameOverPanel.setBounds(0, 0, GameJFrame.GameX, GameJFrame.GameY);
		// 将它添加到本面板(GameMainJPanel)的最上层
		this.add(gameOverPanel, 0); // 添加到索引0，确保它在最上面
		this.revalidate();
		this.repaint();
	}


}






















