package com.tedu.show;

import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * @说明 游戏的主要面板（视图层）。
 * @功能说明 它现在只负责将游戏世界中的所有元素绘制出来。
 * @author xdedm0709
 */
public class GameMainJPanel extends JPanel {

	// 联动管理器
	private ElementManager em;

	public GameMainJPanel() {
		init();
	}

	public void init() {
		em = ElementManager.getManager(); // 得到元素管理器对象
	}

	/**
	 * paintComponent 是进行绘画的核心方法。
	 * 它在每次被 repaint() 调用时执行。
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g); // 调用父类方法，清空背景

		Map<GameElement, List<ElementObj>> all = em.getGameElements();

		// 按照枚举定义的顺序绘制，可以控制元素的显示层级
		for (GameElement ge : GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			// 这里需要注意线程安全，但为了简化，我们暂时忽略
			// 在实际项目中，遍历时可能需要加锁或使用并发集合
			for (ElementObj obj : list) {
				if (obj.isLive()) { // 只绘制存活的元素
					obj.showElement(g);
					// 绘制碰撞箱，调试可用
					g.setColor(Color.RED); // 用红色画出边框
					Rectangle rect = obj.getRectangle();
					g.drawRect(rect.x, rect.y, rect.width, rect.height);
				}
			}
		}
	}
}