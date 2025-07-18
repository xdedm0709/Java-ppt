package com.tedu.element;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.show.GameJFrame;

public class Play extends ElementObj {
	// --- 核心属性 ---
	public static final int PLAYER_SIZE = 30;
	private String playerID;

	// --- 状态属性 ---
	private boolean left = false, up = false, right = false, down = false;
	private String fx = "up";
	private boolean wantsToPlaceBubble = false;

	// --- 道具效果属性 ---
	private int maxBubbles = 1;         // 可同时放置的泡泡最大数量
	private int bubbleRange = 2;        // 泡泡爆炸威力
	private boolean hasDoubleBubblePower = false; // 是否拥有一次性双泡泡能力
	private boolean isReverseWalking = false;   // 是否处于反向行走状态
	private long reverseWalkEndTime = 0;      // 反向行走结束的时间戳
	private int pauseFramesLeft = 0;          // 暂停剩余的帧数
	private boolean hasResistCard = false; // 抵抗卡状态

	// --- 动画属性 ---
	private int frameIndex = 0;
	private int frameDelay = 0;
	private int animationSpeed = 5;

	public Play() {}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		this.setX(Integer.parseInt(split[0]));
		this.setY(Integer.parseInt(split[1]));
		this.fx = split[2];
		this.playerID = split[3]; // 设置玩家身份

		// 根据 playerID 和方向获取正确的动画帧
		String animationKey = this.playerID + "_" + this.fx;
		List<ImageIcon> frames = GameLoad.imgMaps.get(animationKey);

		if (frames == null || frames.isEmpty()) {
			System.err.println("错误：无法加载玩家动画帧！");
			this.setIcon(new ImageIcon());
			// 即使加载失败，也设置一个尺寸，防止宽高为0
			this.setW(PLAYER_SIZE);
			this.setH(PLAYER_SIZE);
			return this;
		}
		// 不再从图片获取尺寸，而是使用定义的常量
		this.setW(PLAYER_SIZE);
		this.setH(PLAYER_SIZE);

		// 仍然需要设置一个初始图标用于显示
		this.setIcon(frames.get(0));

		return this;
	}

	@Override
	public void showElement(Graphics g) {
		g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
	}

	@Override
	public void keyClick(boolean bl, int key) {
		// 【调试】打印出哪个玩家收到了哪个按键事件
		String action = bl ? "按下" : "松开";
		System.out.println("ID: " + this.playerID + " 收到 " + action + " 事件, 键码: " + key);

		if ("player1".equals(this.playerID)) {
			handleArrowKeys(bl, key);
		} else if ("player2".equals(this.playerID)) {
			handleWASDKeys(bl, key);
		}
	}

	/**
	 * @return 返回该玩家的唯一标识符, 如 "player1" 或 "player2".
	 */
	public String getPlayerID() {
		return this.playerID;
	}

	@Override
	public void move() {
		// 1. 处理暂停效果
		if (pauseFramesLeft > 0) {
			pauseFramesLeft--;
			return; // 暂停期间，不做任何事
		}

		// 2. 处理反向行走计时
		if (isReverseWalking && System.currentTimeMillis() > reverseWalkEndTime) {
			setReverseWalking(false); // 效果时间到，恢复正常
		}

		// 3. 计算位移
		int dx = 0, dy = 0;
		if (isReverseWalking) {
			if (right) dx = -5; if (left) dx = 5; if (up) dy = 5; if (down) dy = -5;
		} else {
			if (left) dx = -5; if (right) dx = 5; if (up) dy = -5; if (down) dy = 5;
		}

		// 4. 碰撞检测与移动
		if (dx != 0) {
			Rectangle nextX = new Rectangle(getX() + dx, getY(), getW(), getH());
			if (!checkCollision(nextX)) setX(getX() + dx);
		}
		if (dy != 0) {
			Rectangle nextY = new Rectangle(getX(), getY() + dy, getW(), getH());
			if (!checkCollision(nextY)) setY(getY() + dy);
		}
	}

	// 更新动画帧
	@Override
	protected void updateImage() {
		// 只有在移动时才更新动画帧，不移动则停在第一帧
		if (!isMoving()) {
			frameIndex = 0;
		} else {
			frameDelay++;
			if (frameDelay > animationSpeed) {
				frameDelay = 0;
				frameIndex++;
			}
		}

		// 使用 playerID 获取正确的动画键
		String animationKey = this.playerID + "_" + this.fx;
		List<ImageIcon> frames = GameLoad.imgMaps.get(animationKey);

		if (frames != null && !frames.isEmpty()) {
			// 确保帧索引循环播放
			if (frameIndex >= frames.size()) {
				frameIndex = 0;
			}
			this.setIcon(frames.get(frameIndex));
		} else {
			// 如果找不到动画，只打印一次错误，而不是每帧都尝试加载
			System.err.println("警告: 玩家 " + playerID + " 找不到动画帧: " + animationKey);
		}
	}

	@Override
	protected void action(long gameTime) {
		if (this.wantsToPlaceBubble) {
			this.wantsToPlaceBubble = false;
			placeBubble();
		}
	}

	// 泡泡放置逻辑

	private void placeBubble() {
		ElementManager em = ElementManager.getManager();

		// 1. 计算当前场上由“我”放置的泡泡数量
		int myBubbleCount = 0;
		for (ElementObj bubble : em.getElementsByKey(GameElement.BUBBLE)) {
			if (bubble instanceof Bubble && ((Bubble) bubble).getOwnerId().equals(this.playerID)) {
				myBubbleCount++;
			}
		}

		// 2. 检查第一个泡泡是否能放置
		if (myBubbleCount >= this.maxBubbles) {
			return; // 连第一个都放不了，直接返回
		}

		// 3. 计算第一个泡泡的位置（玩家所在的格子）并检查是否合法
		final int TILE_SIZE = GameJFrame.TILE_SIZE;
		int gridX = (this.getX() + getW() / 2) / TILE_SIZE;
		int gridY = (this.getY() + getH() / 2) / TILE_SIZE;
		int firstBubbleX = gridX * TILE_SIZE;
		int firstBubbleY = gridY * TILE_SIZE;

		if (isOccupied(new Rectangle(firstBubbleX, firstBubbleY, TILE_SIZE, TILE_SIZE))) {
			return; // 玩家脚下有东西，不能放
		}

		// 4. 放置第一个泡泡
		createAndAddBubble(firstBubbleX, firstBubbleY);
		myBubbleCount++; // 计数+1

		// 5. 【双泡泡逻辑】检查是否拥有能力，并且还有放第二个泡泡的余量
		if (this.hasDoubleBubblePower && myBubbleCount < this.maxBubbles) {

			// 计算第二个泡泡的位置（朝向的下一个格子）
			int secondBubbleX = firstBubbleX;
			int secondBubbleY = firstBubbleY;
			switch (this.fx) {
				case "up":    secondBubbleY -= TILE_SIZE; break;
				case "down":  secondBubbleY += TILE_SIZE; break;
				case "left":  secondBubbleX -= TILE_SIZE; break;
				case "right": secondBubbleX += TILE_SIZE; break;
			}

			// 检查第二个位置是否合法
			if (!isOccupied(new Rectangle(secondBubbleX, secondBubbleY, TILE_SIZE, TILE_SIZE))) {
				createAndAddBubble(secondBubbleX, secondBubbleY);
				System.out.println("玩家 " + playerID + " 使用了双泡泡！");
			}

			// 无论第二个是否成功放置，一次性的能力都会被消耗
			this.hasDoubleBubblePower = false;
		}
	}

	/**
	 * @说明 辅助方法，用于创建一个泡泡并将其添加到管理器
	 */
	private void createAndAddBubble(int x, int y) {
		// 使用 Play 类的 bubbleRange 和 playerID 来创建泡泡
		String bubbleStr = x + "," + y + "," + this.bubbleRange + "," + this.playerID;
		// 假设 Bubble 的 createElement 会正确解析这个字符串
		ElementObj bubble = GameLoad.getObj("bubble").createElement(bubbleStr);
		if (bubble != null) {
			ElementManager.getManager().addElement(bubble, GameElement.BUBBLE);
		}
	}

	private boolean isOccupied(Rectangle rect) {
		ElementManager em = ElementManager.getManager();
		for (ElementObj wall : em.getElementsByKey(GameElement.MAPS)) {
			if (rect.intersects(wall.getRectangle())) return true;
		}
		for (ElementObj bubble : em.getElementsByKey(GameElement.BUBBLE)) {
			if (rect.intersects(bubble.getRectangle())) return true;
		}
		return false;
	}

	// --- 辅助与道具方法 ---

	private boolean isMoving() {
		return left || right || up || down;
	}

	private boolean checkCollision(Rectangle rect) {
		ElementManager em = ElementManager.getManager();

		// 1. 边界检查
		if (rect.x < 0 || rect.x + rect.width > GameJFrame.CONTENT_PANE_WIDTH ||
				rect.y < 0 || rect.y + rect.height > GameJFrame.CONTENT_PANE_HEIGHT) {
			return true;
		}

		// 2. 地图元素碰撞 (墙壁等)
		for (ElementObj wall : em.getElementsByKey(GameElement.MAPS)) {
			if (rect.intersects(wall.getRectangle())) {
				return true;
			}
		}

		// 3. 【核心修正】智能的泡泡碰撞
		Rectangle myCurrentRect = this.getRectangle(); // 获取玩家当前的碰撞箱

		for (ElementObj bubbleObj : em.getElementsByKey(GameElement.BUBBLE)) {
			Rectangle bubbleRect = bubbleObj.getRectangle();

			// 检查玩家“未来”的位置是否会碰到这个泡泡
			if (rect.intersects(bubbleRect)) {
				// 只有当玩家“当前”不在这个泡泡里时，才算作真正的碰撞。
				// 换句话说，如果玩家本来就在这个泡泡里，他就可以自由离开。
				if (!myCurrentRect.intersects(bubbleRect)) {
					return true; // 这是一个从外部撞向泡泡的有效碰撞
				}
			}
		}

		return false; // 所有检测都通过，没有发生碰撞
	}

	/**
	 * @说明 增加可放置泡泡的最大数量
	 */
	public void increaseMaxBubbles(int amount) {
		this.maxBubbles += amount;
		System.out.println("玩家 " + playerID + " 最大泡泡数增加到: " + maxBubbles);
	}

	/**
	 * @说明 赋予玩家一次性的双泡泡能力
	 */
	public void grantDoubleBubblePower() {
		this.hasDoubleBubblePower = true;
		System.out.println("玩家 " + this.playerID + " 获得了一次性双泡泡能力！");
	}

	public void setReverseWalking(boolean reverse) {
		this.isReverseWalking = reverse;
		if (reverse) {
			this.reverseWalkEndTime = System.currentTimeMillis() + 5000; // 持续5秒
			System.out.println("玩家 " + playerID + " 进入反向行走状态！");
		} else {
			System.out.println("玩家 " + playerID + " 反向行走已解除！");
		}
	}

	public void pauseForFrames(int frames) {
		this.pauseFramesLeft = frames;
		System.out.println("玩家 " + playerID + " 被暂停 " + frames + " 帧");
	}

	/**
	 * @说明 赋予玩家一次性的抵抗卡能力
	 */
	public void setHasResistCard(boolean hasCard) {
		this.hasResistCard = hasCard;
		if (hasCard) {
			System.out.println("玩家 " + this.playerID + " 获得了抵抗卡！");
		}
	}

	/**
	 * @说明 检查玩家是否拥有抵抗卡
	 */
	public boolean hasResistCard() {
		return this.hasResistCard;
	}

	/**
	 * @说明 消耗玩家的抵抗卡
	 */
	public void consumeResistCard() {
		this.hasResistCard = false;
		System.out.println("玩家 " + this.playerID + " 的抵抗卡已被消耗。");
	}

	/**
	 * @说明 增加该玩家的泡泡爆炸威力
	 * @param amount 增加的量
	 */
	public void increaseBubbleRange(int amount) {
		this.bubbleRange += amount;
		System.out.println("玩家 " + this.playerID + " 的泡泡威力提升至: " + this.bubbleRange);
	}

	/**
	 * @说明 获取当前玩家的泡泡威力
	 */
	public int getBubbleRange() {
		return this.bubbleRange;
	}

	// 处理方向键的辅助方法
	private void handleArrowKeys(boolean bl, int key) {
		if (bl) {
			switch (key) {
				case 37: fx = "left"; left = true; break;
				case 38: fx = "up"; up = true; break;
				case 39: fx = "right"; right = true; break;
				case 40: fx = "down"; down = true; break;
				case 32: wantsToPlaceBubble = true; break; // 假设空格键是通用的
			}
		} else {
			switch (key) {
				case 37: left = false; break;
				case 38: up = false; break;
				case 39: right = false; break;
				case 40: down = false; break;
			}
		}
	}

	// 处理WASD键的辅助方法 (W=87, A=65, S=83, D=68)
	private void handleWASDKeys(boolean bl, int key) {
		if (bl) {
			switch (key) {
				case 65: fx = "left"; left = true; break; // A
				case 87: fx = "up"; up = true; break;   // W
				case 68: fx = "right"; right = true; break;// D
				case 83: fx = "down"; down = true; break; // S
				// 假设玩家2用 Shift 键放泡泡 (Shift=16)
				case 16: wantsToPlaceBubble = true; break;
			}
		} else {
			switch (key) {
				case 65: left = false; break;
				case 87: up = false; break;
				case 68: right = false; break;
				case 83: down = false; break;
			}
		}
	}
}