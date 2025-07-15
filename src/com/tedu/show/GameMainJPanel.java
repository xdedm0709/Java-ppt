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

	public GameMainJPanel() {
		init();
	}

	public void init() {
		em = ElementManager.getManager();//得到元素管理器对象
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
		Map<GameElement, List<ElementObj>> all = em.getGameElements();
//		GameElement.values();//隐藏方法  返回值是一个数组,数组的顺序就是定义枚举的顺序
		for(GameElement ge:GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			for(int i=0;i<list.size();i++) {
				ElementObj obj=list.get(i);//读取为基类
//				if(ge.equals(GameElement.PLAYFILE)) {
//					System.out.println(":::::::::::"+obj);
//				}
				obj.showElement(g);//调用每个类的自己的show方法完成自己的显示
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
			// 1. 【核心】游戏逻辑更新
			gameLogicUpdate();

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
			}
		}
	}


}






















