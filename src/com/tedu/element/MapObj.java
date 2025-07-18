package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;

public class MapObj extends ElementObj {
	private String type; // "GRASS", "IRON", "RIVER", "HOUSE"

	public MapObj() {}

	@Override
	public ElementObj createElement(String str) {
		// 格式: "TYPE,x,y"  例如: "IRON,120,120"
		String[] arr = str.split(",");
		this.type = arr[0];
		int x = Integer.parseInt(arr[1]);
		int y = Integer.parseInt(arr[2]);

		// 从 GameLoad 获取预加载的图片
		ImageIcon icon = GameLoad.imgMap.get(this.type);

		if (icon != null) {
			this.setX(x);
			this.setY(y);
			this.setW(icon.getIconWidth());
			this.setH(icon.getIconHeight());
			this.setIcon(icon);
		} else {
			System.err.println("错误：无法为地图对象加载图片: " + this.type);
			this.setLive(false);
		}
		return this;
	}

	// 静态地图元素不需要移动或行动
	@Override protected void move() {}
	@Override protected void action(long gameTime) {}
	@Override protected void updateImage() {}

	@Override
	public void showElement(Graphics g) {
		g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
	}

	public String getType() {
		return this.type;
	}
}