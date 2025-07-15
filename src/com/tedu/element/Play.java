package com.tedu.element;

import java.awt.Graphics;
import java.util.List; // 导入 List
import java.util.Map;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

public class Play extends ElementObj {

	// 原有的移动状态
	private boolean left = false;
	private boolean up = false;
	private boolean right = false;
	private boolean down = false;

	// 原有的方向和攻击状态
	private String fx = "up";
	private boolean pkType = false;

	// 新增：动画相关的变量
	private int frameIndex = 0; // 当前动画帧的索引
	private int frameDelay = 0; // 用于控制动画速度的计时器
	private int animationSpeed = 5; // 动画速度，数字越大动画越慢

	private boolean wantsToPlaceBubble = false; // 放置泡泡的状态

	public Play() {}

	// 这个构造函数可能不再需要，因为我们使用createElement
	public Play(int x, int y, int w, int h, ImageIcon icon) {
		super(x, y, w, h, icon);
	}

	/**
	 * @说明 创建玩家对象。用于处理来自动画精灵图的数据。
	 * @param str 格式为 "x,y,direction"，例如 "500,500,up"
	 */
	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		this.setX(Integer.parseInt(split[0]));
		this.setY(Integer.parseInt(split[1]));
		this.fx = split[2]; // 设置初始方向

		// 从GameLoad获取对应方向的动画帧列表
		List<ImageIcon> frames = GameLoad.imgMaps.get(this.fx);
		if (frames == null || frames.isEmpty()) {
			System.err.println("错误：无法加载方向为 " + this.fx + " 的玩家动画帧！");
			// 设置一个默认的空图标以防止程序崩溃
			this.setIcon(new ImageIcon());
			return this;
		}

		// 使用动画的第一帧来设置对象的尺寸和初始图标
		ImageIcon firstFrame = frames.get(0);
		this.setW(firstFrame.getIconWidth());
		this.setH(firstFrame.getIconHeight());
		this.setIcon(firstFrame); // 设置父类的icon为当前动画的第一帧

		return this;
	}

	/**
	 * @说明 绘制方法。绘制父类中存储的当前 getIcon()
	 */
	@Override
	public void showElement(Graphics g) {
		g.drawImage(this.getIcon().getImage(),
				this.getX(), this.getY(),
				this.getW(), this.getH(), null);
	}

	/**
	 * @说明 按键监听。正确地设置方向状态(fx)
	 */
	@Override
	public void keyClick(boolean bl, int key) {
		if (bl) {
			switch (key) {
				case 37:
					this.down = false; this.up = false;
					this.right = false; this.left = true; this.fx = "left"; break;
				case 38:
					this.right = false; this.left = false;
					this.down = false; this.up = true; this.fx = "up"; break;
				case 39:
					this.down = false; this.up = false;
					this.left = false; this.right = true; this.fx = "right"; break;
				case 40:
					this.right = false; this.left = false;
					this.up = false; this.down = true; this.fx = "down"; break;
				case 32:
					this.wantsToPlaceBubble = true;
					System.out.println("Play 对象收到指令：想要放置泡泡！"); // 增加日志
					break;
			}
		} else {
			switch (key) {
				case 37: this.left = false; break;
				case 38: this.up = false; break;
				case 39: this.right = false; break;
				case 40: this.down = false; break;
				case 32: break;
			}
		}
	}

	/**
	 * @说明 移动和动画更新。此方法在每个游戏循环中被调用。
	 */
	@Override
	public void move() {
		// 1. 更新位置
		if (this.left && this.getX() > 0) {
			this.setX(this.getX() - 5); // 稍微增加速度以获得更好的体验
		}
		if (this.up && this.getY() > 0) {
			this.setY(this.getY() - 5);
		}
		if (this.right && this.getX() < 900 - this.getW()) {
			this.setX(this.getX() + 5);
		}
		if (this.down && this.getY() < 600 - this.getH()) {
			this.setY(this.getY() + 5);
		}

		// 2. 更新动画帧
		// 只有在移动时才播放动画
		if (!isMoving()) {
			frameIndex = 0; // 如果不移动，回到站立的第一帧
			return; // 停止动画更新
		}

		frameDelay++;
		if (frameDelay > animationSpeed) {
			frameDelay = 0;
			frameIndex++;

			// 获取当前方向的动画帧总数
			List<ImageIcon> currentFrames = GameLoad.imgMaps.get(this.fx);
			// 如果帧索引超出范围，则从头开始，实现循环播放
			if (currentFrames != null && frameIndex >= currentFrames.size()) {
				frameIndex = 0;
			}
		}
	}

	// 辅助方法，用于判断角色是否在移动
	private boolean isMoving() {
		return left || right || up || down;
	}

	/**
	 * @说明 更新图像。这个方法现在是动画的核心。
	 * 它会根据当前方向(fx)和帧索引(frameIndex)来更新要显示的图标。
	 */
	@Override
	protected void updateImage() {
		// 从imgMaps中获取当前方向的动画帧列表
		List<ImageIcon> frames = GameLoad.imgMaps.get(this.fx);

		if (frames != null && !frames.isEmpty()) {
			// 确保 frameIndex 不会因为快速的方向切换而越界
			if (frameIndex >= frames.size()) {
				frameIndex = 0;
			}
			// 将父类的 icon 设置为当前应该显示的动画帧
			this.setIcon(frames.get(frameIndex));
		}
	}

	// 开火/攻击的方法
	private long filetime = 0;

	@Override
	protected void action(long gameTime) {
		// 检查是否要放置泡泡
		if (this.wantsToPlaceBubble) {
			this.wantsToPlaceBubble = false; // 处理完意图后，立刻重置状态

			System.out.println("Play.action() 正在执行：创建并放置泡泡...");

			ElementObj bubbleTemplate = GameLoad.getObj("bubble");
			if (bubbleTemplate != null) {
				String bubbleStr = (this.getX() + this.getW()/2) + "," + (this.getY() + this.getH()/2);
				ElementObj bubble = bubbleTemplate.createElement(bubbleStr);
				ElementManager.getManager().addElement(bubble, GameElement.BUBBLE);
				System.out.println("泡泡已成功创建并添加到管理器。");
			} else {
				System.err.println("创建泡泡失败！无法从 GameLoad 获取 'bubble' 的对象模板。");
			}
		}

	}

	// toString方法用于生成子弹
	@Override
	public String toString() {
		int x = this.getX();
		int y = this.getY();
		switch (this.fx) {
			case "up": x += 20; break;
			case "left": y += 20; break;
			case "right": x += 50; y += 20; break;
			case "down": y += 50; x += 20; break;
		}
		return "x:" + x + ",y:" + y + ",f:" + this.fx;
	}
}

//try {
//Class<?> forName = Class.forName("com.tedu.....");
//ElementObj element = forName.newInstance().createElement("");
//} catch (InstantiationException e) {
//// TODO Auto-generated catch block
//e.printStackTrace();
//} catch (IllegalAccessException e) {
//// TODO Auto-generated catch block
//e.printStackTrace();
//} //以后的框架学习中会碰到
//// 会帮助你返回对象的实体，并初始化数据
//catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//}





