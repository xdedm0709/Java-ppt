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
	private boolean hasResistWaterTrap = false;
	private int playerNumber;
	public static final int PLAYER_SIZE = 30;
	private int maxBubbles = 2;

	// 移动状态
	private boolean left = false;
	private boolean up = false;
	private boolean right = false;
	private boolean down = false;
	private String fx = "up";

	// 泡泡相关
	private int bubbleRange = 2;  // 泡泡爆炸范围
	//private int maxBubbles = 1;
	private int currentBubbles = 0;  // 当前已放置的泡泡数量
	private int pauseFramesLeft = 0;
	// 双泡泡能力
	private boolean canLaunchDoubleBubble = false;  // 是否拥有双泡泡能力
	private boolean doubleBubbleUsed = false;  // 双泡泡是否已使用（一次性）
	private boolean isReverseWalking = false;
	private String playerImgPrefix;
	// 动画相关
	private int frameIndex = 0;
	private int frameDelay = 0;
	private int animationSpeed = 5;

	// 放置泡泡请求
	private boolean wantsToPlaceBubble = false;
	private boolean hasDoubleBubble = false;
	// 标记是否已使用
	private boolean isDoubleBubbleUsed = false;
	public void pauseForFrames(int frames) {
		this.pauseFramesLeft = frames;
		System.out.println("玩家" + playerNumber + "被暂停" + frames + "帧");
	}

	public int getPlayerNumber() {
		return playerNumber;
	}
	public Play(int playerNumber) {
		this.playerNumber = playerNumber;
		this.playerImgPrefix = "player" + playerNumber;
	}

	public void setResistWaterTrap(boolean has) {
		this.hasResistWaterTrap = has;
	}
	public boolean hasResistWaterTrap() {
		return hasResistWaterTrap;
	}
	public void consumeResist() {
		this.hasResistWaterTrap = false;
	}
	public void setReverseWalking(boolean reverse) {
		// 如果是解除反向状态（reverse=false），则立即生效
		if (!reverse) {
			this.isReverseWalking = false;
			System.out.println("反向行走已解除！");
			return;
		}
		this.isReverseWalking = reverse;
		System.out.println("反向行走已激活！右→左，上→下");
		// 可添加持续时间（如50秒后自动取消）
		if (reverse) {
			new Thread(() -> {
				try {
					Thread.sleep(50000); // 持续50秒
					isReverseWalking = false;
					System.out.println("反向行走能力结束！");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] arr = str.split(",");
		if (arr.length >= 4) {
			this.playerNumber = Integer.parseInt(arr[1]);
			this.setX(Integer.parseInt(arr[2]));
			this.setY(Integer.parseInt(arr[3]));
			// 初始化时更新图片前缀（避免构造方法和参数解析冲突）
			this.playerImgPrefix = "player" + playerNumber;
		}

		// 加载对应玩家的图片（区分玩家1和玩家2）
		List<ImageIcon> frames = GameLoad.imgMaps.get(fx);
		if (frames != null && !frames.isEmpty()) {
			this.setW(PLAYER_SIZE);
			this.setH(PLAYER_SIZE);
			this.setIcon(frames.get(0));
		} else {
			// 如果动画帧加载失败，加载默认玩家图片
			try {
				// 使用玩家编号对应的图片
				ImageIcon defaultIcon = new ImageIcon("image/player/" + playerImgPrefix + ".jpg");
				this.setIcon(defaultIcon);
				this.setW(PLAYER_SIZE);
				this.setH(PLAYER_SIZE);
			} catch (Exception e) {
				System.err.println("玩家" + playerNumber + "图片加载失败：" + e.getMessage());
				this.setIcon(new ImageIcon());
			}
		}
		return this;
	}

	@Override
	public void showElement(Graphics g) {
		g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);

		// 显示双泡泡状态（调试用）
		if (canLaunchDoubleBubble && !doubleBubbleUsed) {
			g.drawString("双泡泡就绪!", getX(), getY() - 10);
		}
	}

	@Override
	public void keyClick(boolean bl, int key) {
		// 玩家1控制（方向键+空格）
		if (playerNumber == 1) {
			if (bl) {
				switch (key) {
					case 37: left = true; fx = "left"; break;
					case 38: up = true; fx = "up"; break;
					case 39: right = true; fx = "right"; break;
					case 40: down = true; fx = "down"; break;
					case 32: wantsToPlaceBubble = true; break;
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
		// 玩家2控制（WASD+回车）
		else if (playerNumber == 2) {
			if (bl) {
				switch (key) {
					case 65: left = true; fx = "left"; break;  // A
					case 87: up = true; fx = "up"; break;    // W
					case 68: right = true; fx = "right"; break;  // D
					case 83: down = true; fx = "down"; break;  // S
					case 10: wantsToPlaceBubble = true; break;  // 回车放置泡泡
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

	@Override
	public void move() {
		if (pauseFramesLeft > 0) {
			pauseFramesLeft--; // 减少暂停帧数
			if (pauseFramesLeft == 0) {
				System.out.println("玩家" + playerNumber + "暂停结束");
			}
			return; // 暂停期间禁止移动
		}
		int dx = 0, dy = 0;
		// 根据反向状态处理按键与方向的映射
		if (isReverseWalking) {
			// 反向：右→左，左→右，上→下，下→上
			if (right) dx = -5; // 原右→现左
			if (left) dx = 5;  // 原左→现右
			if (up) dy = 5;    // 原上→现下
			if (down) dy = -5; // 原下→现上
		} else {
			// 正常方向
			if (left) dx = -5;
			if (right) dx = 5;
			if (up) dy = -5;
			if (down) dy = 5;
		}

		// 碰撞检测后移动
		if (dx != 0) {
			Rectangle nextX = new Rectangle(getX() + dx, getY(), getW(), getH());
			if (!checkCollision(nextX)) {
				setX(getX() + dx);
				// 朝向（反向时同步修正朝向显示）
				fx = isReverseWalking ? (right ? "left" : "right") : (left ? "left" : "right");
			}
		}
		if (dy != 0) {
			Rectangle nextY = new Rectangle(getX(), getY() + dy, getW(), getH());
			if (!checkCollision(nextY)) {
				setY(getY() + dy);
				// 朝向（反向时同步修正朝向显示）
				fx = isReverseWalking ? (up ? "down" : "up") : (up ? "up" : "down");
			}
		}
		updateAnimation();
	}


	// 碰撞检测
	private boolean checkCollision(Rectangle rect) {
		// 边界检查
		if (rect.x < 0 || rect.x + rect.width > GameJFrame.CONTENT_PANE_WIDTH) {
			return true;
		}
		if (rect.y < 0 || rect.y + rect.height > GameJFrame.CONTENT_PANE_HEIGHT) {
			return true;
		}

		// 地图元素碰撞
		ElementManager em = ElementManager.getManager();
		List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
		for (ElementObj mapObj : maps) {
			// 修改：使用instanceof判断类型，而不是getName()
			if (mapObj instanceof MapObj) {
				MapObj map = (MapObj) mapObj;
				if (map.getName() != null &&
						(map.getName().equals("BRICK") ||
								map.getName().equals("IRON") ||
								map.getName().startsWith("box"))) {
					if (rect.intersects(map.getRectangle())) {
						return true;
					}
				}
			}
		}

		// 泡泡碰撞
		List<ElementObj> bubbles = em.getElementsByKey(GameElement.BUBBLE);
		for (ElementObj bubble : bubbles) {
			if (rect.intersects(bubble.getRectangle())) {
				return true;
			}
		}

		return false;
	}

	// 更新动画帧
	private void updateAnimation() {
		if (!isMoving()) {
			frameIndex = 0;
			return;
		}

		frameDelay++;
		if (frameDelay > animationSpeed) {
			frameDelay = 0;
			frameIndex++;

			// 加载对应玩家的动画帧（通过前缀区分）
			List<ImageIcon> frames = GameLoad.imgMaps.get(playerImgPrefix + "_" + fx);
			if (frames != null && !frames.isEmpty()) {
				frameIndex = frameIndex % frames.size();
				this.setIcon(frames.get(frameIndex));
			} else {
				// 如果对应动画帧不存在，使用默认方向图
				try {
					this.setIcon(new ImageIcon("image/player/" + playerImgPrefix + "_" + fx + ".jpg"));
				} catch (Exception e) {
					System.err.println("玩家" + playerNumber + "动画帧加载失败（" + fx + "）：" + e.getMessage());
				}
			}
		}
	}


	// 判断是否在移动
	private boolean isMoving() {
		return left || right || up || down;
	}

	@Override
	protected void action(long gameTime) {
		// 处理放置泡泡请求
		if (wantsToPlaceBubble) {
			wantsToPlaceBubble = false;
			placeBubbles();
		}

		// 更新泡泡计数（检查已消失的泡泡）
		updateBubbleCount();
	}

	// 放置泡泡（支持双泡泡能力）
	private void placeBubbles() {
		// 检查是否可以放置泡泡
		if (currentBubbles >= maxBubbles) {
			return;
		}

		ElementManager em = ElementManager.getManager();

		// 计算泡泡放置位置（居中到格子）
		int bubbleX = (getX() + PLAYER_SIZE / 2) / 40 * 40;
		int bubbleY = (getY() + PLAYER_SIZE / 2) / 40 * 40;

		// 检查该位置是否已有泡泡
		List<ElementObj> bubbles = em.getElementsByKey(GameElement.BUBBLE);
		for (ElementObj bubble : bubbles) {
			if (bubble.getX() == bubbleX && bubble.getY() == bubbleY) {
				return;  // 已有泡泡，不重复放置
			}
		}

		// 创建第一个泡泡
		String bubbleStr = bubbleX + "," + bubbleY + "," + bubbleRange;
		Bubble bubble = (Bubble) GameLoad.getObj("bubble").createElement(bubbleStr);
		em.addElement(bubble, GameElement.BUBBLE);
		currentBubbles++;

		if (hasDoubleBubble && !isDoubleBubbleUsed) {
			// 计算第二个泡泡位置（与玩家朝向一致，距离40像素）
			int secondX = bubbleX;
			int secondY = bubbleY;
			switch (fx) {
				case "up":
					secondY -= 40;
					break;
				case "down":
					secondY += 40;
					break;
				case "left":
					secondX -= 40;
					break;
				case "right":
					secondX += 40;
					break;
			}

			// 检查第二个泡泡位置是否合法（边界+障碍物）
			if (isValidBubblePosition(secondX, secondY)) {
				Bubble secondBubble = (Bubble) GameLoad.getObj("bubble").createElement(secondX + "," + secondY + ",2");
				em.addElement(secondBubble, GameElement.BUBBLE);
				currentBubbles++;
				isDoubleBubbleUsed = true; // 标记已使用（一次性）
				hasDoubleBubble = false; // 消耗能力
				System.out.println("玩家使用了双泡泡！");
			}
		}
	}
		private boolean isValidBubblePosition(int x, int y) {
			// 边界检查
			if (x < 0 || x + 40 > GameJFrame.CONTENT_PANE_WIDTH) return false;
			if (y < 0 || y + 40 > GameJFrame.CONTENT_PANE_HEIGHT) return false;

			// 障碍物检查
			Rectangle bubbleRect = new Rectangle(x, y, 40, 40);
			ElementManager em = ElementManager.getManager();
			List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
			for (ElementObj map : maps) {
				if (map instanceof MapObj && map.getName() != null
						&& (map.getName().equals("BRICK") || map.getName().equals("IRON"))) {
					if (bubbleRect.intersects(map.getRectangle())) return false;
				}
			}
			return true;
		}

	// 更新当前泡泡计数
	private void updateBubbleCount() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> bubbles = em.getElementsByKey(GameElement.BUBBLE);

		int count = 0;
		if (bubbles != null) {
			for (ElementObj bubble : bubbles) {
				if (bubble instanceof Bubble && bubble.isLive()) {
					count++;
				}
			}
		}

		currentBubbles = count;
	}
	public void setCanLaunchDoubleBubble(boolean canLaunch) {
		this.canLaunchDoubleBubble = canLaunch;
		this.doubleBubbleUsed = false;  // 获得能力时重置使用状态
		System.out.println("玩家" + playerNumber + "获得双泡泡能力!");
	}
	// 设置双泡泡能力
    /*
    // 增加泡泡爆炸范围
    public void increaseBubbleRange(int amount) {
        this.bubbleRange += amount;
        System.out.println("玩家" + playerNumber + "泡泡范围增加到: " + bubbleRange);
    }
    */
	// 保留增加最大泡泡数量的功能
	public void increaseMaxBubbles(int amount) {
		this.maxBubbles += amount;
		System.out.println("玩家" + playerNumber + "最大泡泡数增加到: " + maxBubbles);
	}
}