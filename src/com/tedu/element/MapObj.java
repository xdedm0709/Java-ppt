package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;

public class MapObj extends ElementObj {
	private String type; // "GRASS", "IRON", "RIVER", "HOUSE"

	public MapObj() {}

	@Override
	public ElementObj createElement(String str) {
		// 1. 创建一个新的实例
		MapObj newMapObj = new MapObj();

		// 2. 解析字符串，并设置新实例的属性
		String[] arr = str.split(",");
		newMapObj.type = arr[0];
		int x = Integer.parseInt(arr[1]);
		int y = Integer.parseInt(arr[2]);
		ImageIcon icon = GameLoad.imgMap.get(newMapObj.type);

		if (icon != null) {
			newMapObj.setX(x);
			newMapObj.setY(y);
			newMapObj.setW(icon.getIconWidth());
			newMapObj.setH(icon.getIconHeight());
			newMapObj.setIcon(icon);
		} else {
			newMapObj.setLive(false);
		}

		// 3. 返回这个【新实例】
		return newMapObj;
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