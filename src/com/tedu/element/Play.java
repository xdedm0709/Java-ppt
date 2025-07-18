package com.tedu.element;

import java.awt.Graphics;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.show.GameJFrame;
import java.awt.Rectangle;

public class Play extends ElementObj {

	public static final int PLAYER_SIZE = 30; // 定义玩家的逻辑尺寸

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
		this.fx = split[2];

		// 从GameLoad获取动画帧列表
		List<ImageIcon> frames = GameLoad.imgMaps.get(this.fx);
		if (frames == null || frames.isEmpty()) {
			System.err.println("错误：无法加载玩家动画帧！");
			this.setIcon(new ImageIcon());
			// 即使加载失败，也设置一个尺寸，防止宽高为0
			this.setW(PLAYER_SIZE);
			this.setH(PLAYER_SIZE);
			return this;
		}

		// 不再从图片获取尺寸，而是使用我们定义的常量
		this.setW(PLAYER_SIZE);
		this.setH(PLAYER_SIZE);

		// 仍然需要设置一个初始图标用于显示
		this.setIcon(frames.get(0));

		return this;
	}

	/**
	 * @说明 绘制方法。这里的 getW() 和 getH() 现在返回的是定义的 PLAYER_SIZE。
	 *       drawImage 会自动将原始图片缩放到这个尺寸进行绘制。
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
		int dx = 0;
		int dy = 0;

		// 1. 根据按键意图，计算 X 和 Y 方向的位移增量
		if (this.left)  { dx = -5; }
		if (this.up)    { dy = -5; }
		if (this.right) { dx = 5;  }
		if (this.down)  { dy = 5;  }

		// 处理 X 轴移动
		if (dx != 0) {
			// 预测只在 X 方向移动后的新位置
			Rectangle nextXRect = new Rectangle(getX() + dx, getY(), getW(), getH());
			if (!checkCollisionWithWalls(nextXRect)) { // 如果 X 方向没有撞墙
				setX(getX() + dx); // 才真正更新 X 坐标
			}
		}

		// 处理 Y 轴移动
		if (dy != 0) {
			// 预测只在 Y 方向移动后的新位置 (注意：Y坐标是基于当前X坐标的)
			Rectangle nextYRect = new Rectangle(getX(), getY() + dy, getW(), getH());
			if (!checkCollisionWithWalls(nextYRect)) { // 如果 Y 方向没有撞墙
				setY(getY() + dy); // 才真正更新 Y 坐标
			}
		}

		// 3. 更新动画帧
		updateAnimationFrames();
	}

	/**
	 * 辅助方法，用于检查给定的矩形是否与任何墙壁碰撞。
	 * @param rect 要检查的碰撞矩形
	 * @return boolean 如果发生碰撞则返回 true，否则返回 false
	 */
	private boolean checkCollisionWithWalls(Rectangle rect) {
		// 1. X 轴边界检测 (0 到 800)
		if (rect.x < 0 || rect.x + rect.width > 780) {
			return true;
		}

		// 2. Y 轴边界检测 (0 到 529)
		//    它现在会使用我们设置的精确值 529 作为下边界
		if (rect.y < 0 || rect.y + rect.height > GameJFrame.CONTENT_PANE_HEIGHT) {
			return true;
		}

		// 3. 墙体碰撞检测
		ElementManager em = ElementManager.getManager();
		List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
		for (ElementObj wall : maps) {
			if (rect.intersects(wall.getRectangle())) {
				return true;
			}
		}

		return false; // 所有检测都通过，没有发生碰撞
	}

	/**
	 * 辅助方法，将动画更新的逻辑提取出来，使 move() 更整洁
	 */
	private void updateAnimationFrames() {
		if (!isMoving()) {
			frameIndex = 0;
			return;
		}
		frameDelay++;
		if (frameDelay > animationSpeed) {
			frameDelay = 0;
			frameIndex++;
			List<ImageIcon> currentFrames = GameLoad.imgMaps.get(this.fx);
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

	@Override
	protected void action(long gameTime) {
		// 检查是否要放置泡泡
		if (this.wantsToPlaceBubble) {
			this.wantsToPlaceBubble = false; // 处理完意图后，立刻重置状态

			ElementObj bubbleTemplate = GameLoad.getObj("bubble");
			if (bubbleTemplate != null) {
				System.out.println("Play.action() 正在执行：创建并放置泡泡...");

				// --- 【核心修正】使用玩家的中心点来决定放置的格子 ---

				// 1. 获取全局格子大小
				final int TILE_SIZE = GameJFrame.TILE_SIZE;

				// 2. 计算玩家的中心点像素坐标
				int playerCenterX = this.getX() + this.getW() / 2;
				int playerCenterY = this.getY() + this.getH() / 2;

				// 3. 根据中心点，计算出玩家当前所在的格子索引
				//    这比用左上角要稳定得多！
				int gridX = playerCenterX / TILE_SIZE;
				int gridY = playerCenterY / TILE_SIZE;

				// 4. 将格子索引转换回该格子的左上角像素坐标
				int alignedX = gridX * TILE_SIZE;
				int alignedY = gridY * TILE_SIZE;

				// 5. 将这个精确对齐的坐标传递给泡泡
				//    Bubble 的 createElement 会自己处理居中
				String bubbleStr = alignedX + "," + alignedY;
				ElementObj bubble = bubbleTemplate.createElement(bubbleStr);

				ElementManager.getManager().addElement(bubble, GameElement.BUBBLE);
				System.out.println("泡泡已在玩家中心所在的格子 (" + gridX + "," + gridY + ") 创建并添加。");
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





