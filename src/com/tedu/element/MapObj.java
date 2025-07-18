package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class MapObj extends ElementObj {
	// 基础属性
	private int hp;
	private String name; // 元素类型（如box1、box2、BRICK等）
	private boolean isBox1Exploded = false;
	private boolean isDoubleBubbleGranted = false;

	private boolean isBox2Exploded = false; // box2爆炸标记
	private boolean isRangeUpGranted = false;

	private boolean isBox3Exploded = false;
	// 解除反向能力是否已赋予（避免重复触发）
	private boolean isReverseCureGranted = false;

	private boolean isBox4Exploded = false;
	// 反向能力是否已赋予（避免重复触发）
	private boolean isReverseGranted = false;

	private boolean isBox5Exploded = false; // box5爆炸标记
	private boolean isOtherPlayerReverseGranted = false; // 是否已给其他玩家赋予反向

	private boolean isBox6Exploded = false; // box6爆炸标记
	private boolean isOtherPlayerSurrounded = false; // 是否已包围另一名玩家

	private boolean isBox7Exploded = false; // box7爆炸标记
	private boolean isResistGranted = false; // 是否已赋予抵抗能力

	private boolean isBox8Exploded = false;
	private boolean isOtherPlayerPaused = false; // 是否已暂停另一名玩家


	// 爆炸相关属性
	private boolean isExploding = false; // 是否处于爆炸状态
	private long explosionStartTime = 0; // 爆炸开始时间
	private static final long EXPLOSION_DURATION = 300; // 爆炸效果持续时间（300ms）
	private static ImageIcon explosionIcon = null; // 爆炸效果图片
	//触发范围改为40像素
	private static final int DOUBLE_BUBBLE_TRIGGER_RANGE = 40;

	// 动画相关属性
	private List<ImageIcon> animationFrames = new ArrayList<>(); // 动画帧列表
	private int currentFrameIndex = 0; // 当前播放帧索引
	private long lastFrameTime = 0; // 上一帧播放时间戳
	private static final long FRAME_DURATION = 100; // 每帧播放时长（100ms）
	private boolean isAnimating = false; // 是否处于动画循环状态
	private boolean powerUpCollected = false; // 是否已被玩家收集
	private boolean doubleBubbleCollected = false;
	private static final int TRIGGER_RANGE = 40; // 触发范围
	private static final int POWER_UP_RANGE = 40; // 触发范围

	// 存储每个box对应的动画帧路径
	private static final Map<String, String[]> BOX_ANIMATION_PATHS;
	static {
		BOX_ANIMATION_PATHS = new HashMap<>();
		BOX_ANIMATION_PATHS.put("box1", new String[]{
				"bin/image/Characters/paopao1.jpg",
				"bin/image/Characters/paopao2.jpg",
				"bin/image/Characters/paopao3.jpg",
				"bin/image/Characters/paopao4.jpg"
		});
		BOX_ANIMATION_PATHS.put("box2", new String[]{
				"bin/image/Characters/yaoping1.jpg",
				"bin/image/Characters/yaoping2.jpg",
				"bin/image/Characters/yaoping3.jpg",
				"bin/image/Characters/yaoping4.jpg"
		});
		BOX_ANIMATION_PATHS.put("box3", new String[]{
				"bin/image/Characters/jieyao1.jpg",
				"bin/image/Characters/jieyao2.jpg",
				"bin/image/Characters/jieyao3.jpg",
				"bin/image/Characters/jieyao4.jpg"
		});
		BOX_ANIMATION_PATHS.put("box4", new String[]{
				"bin/image/Characters/ghost1.jpg",
				"bin/image/Characters/ghost2.jpg",
				"bin/image/Characters/ghost3.jpg",
				"bin/image/Characters/ghost4.jpg"
		});
		BOX_ANIMATION_PATHS.put("box5", new String[]{
				"bin/image/Characters/redghost1.jpg",
				"bin/image/Characters/redghost2.jpg",
				"bin/image/Characters/redghost3.jpg",
				"bin/image/Characters/redghost4.jpg"
		});
		BOX_ANIMATION_PATHS.put("box6", new String[]{
				"bin/image/Characters/waterbomb1.jpg",
				"bin/image/Characters/waterbomb2.jpg",
				"bin/image/Characters/waterbomb3.jpg",
				"bin/image/Characters/waterbomb4.jpg"
		});
		BOX_ANIMATION_PATHS.put("box7", new String[]{
				"bin/image/Characters/diamondcard1.jpg",
				"bin/image/Characters/diamondcard2.jpg",
				"bin/image/Characters/diamondcard3.jpg",
				"bin/image/Characters/diamondcard4.jpg"
		});
		BOX_ANIMATION_PATHS.put("box8", new String[]{
				"bin/image/Characters/remotecontrol1.jpg",
				"bin/image/Characters/remotecontrol2.jpg",
				"bin/image/Characters/remotecontrol3.jpg",
				"bin/image/Characters/remotecontrol4.jpg"
		});
	}

	// 静态初始化爆炸图片
	static {
		try {
			explosionIcon = new ImageIcon("bin/image/boom/boom.png");
		} catch (Exception e) {
			System.err.println("爆炸图片加载失败（bin/image/boom/boom.png）：" + e.getMessage());
			explosionIcon = new ImageIcon(); // 避免空指针
		}
	}

	@Override
	public void showElement(Graphics g) {
		// 1. 优先处理爆炸状态（显示爆炸图片）
		if (isExploding) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - explosionStartTime <= EXPLOSION_DURATION) {
				g.drawImage(explosionIcon.getImage(), getX(), getY(), getW(), getH(), null);
			} else {
				// 爆炸结束：处理后续状态
				isExploding = false;
				if ("box8".equals(name)) {
					isBox8Exploded = true;
				}else if ("box7".equals(name)) {
					isBox7Exploded = true;
				}else if ("box6".equals(name)) {
					isBox6Exploded = true;
				}else if ("box5".equals(name)) {
					isBox5Exploded = true;
				}else if ("box3".equals(name)) {
					isBox3Exploded = true;
				}else if ("box4".equals(name)) {
					isBox4Exploded = true;
				} else if ("box1".equals(name)) {
					isBox1Exploded = true;
				} else if ("box2".equals(name)) {
					isBox2Exploded = true;
				}
				// BRICK爆炸后直接消失
				if ("BRICK".equals(name)) {
					this.setLive(false);
				}
				// box1-box8爆炸后切换到动画状态
				else if (BOX_ANIMATION_PATHS.containsKey(name)) {
					isAnimating = true;
					lastFrameTime = currentTime;
					if (animationFrames.isEmpty()) {
						loadBoxAnimationFrames(name);
					}
				}
			}
			return;
		}
		if ("box8".equals(name) && isBox8Exploded && isAnimating && !isOtherPlayerPaused) {
			checkPlayerDistanceForPause();
		}
		if ("box7".equals(name) && isBox7Exploded && isAnimating && !isResistGranted) {
			checkPlayerDistanceForResist();
		}
		if ("box6".equals(name) && isBox6Exploded && isAnimating && !isOtherPlayerSurrounded) {
			checkPlayerDistanceForSurroundOtherPlayer();
		}
		if ("box3".equals(name) && isBox3Exploded && isAnimating && !isReverseCureGranted) {
			checkPlayerDistanceForReverseCure();
		}
		if ("box4".equals(name) && isBox4Exploded && isAnimating && !isReverseGranted) {
			checkPlayerDistanceForReverse();
		}
		if ("box5".equals(name) && isBox5Exploded && isAnimating && !isOtherPlayerReverseGranted) {
			checkPlayerDistanceForOtherPlayerReverse();
		}
		if ("box2".equals(name) && isBox2Exploded && !isRangeUpGranted) {
			checkPlayerDistanceForRangeUp();
		}
		if ("box1".equals(name) && isBox1Exploded && !isDoubleBubbleGranted) {
			checkPlayerDistanceForDoubleBubble();
		}
		// 2. 处理动画循环状态（仅box1-box8）
		if (isAnimating && BOX_ANIMATION_PATHS.containsKey(name) && !animationFrames.isEmpty()) {
			updateAnimationFrame();
			g.drawImage(animationFrames.get(currentFrameIndex).getImage(),
					getX(), getY(), getW(), getH(), null);
			return;
		}

		// 3. 特殊处理：检测玩家与道具的碰撞(见第四点下面）
		// 4. 正常状态：显示初始图片
		if (!powerUpCollected && this.getIcon() != null && isLive()) {
			g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
		}
	}
	private void checkPlayerDistanceForResist() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
		if (players == null) return;
		// 计算box7动画的中心坐标
		int boxCenterX = getX() + getW() / 2;
		int boxCenterY = getY() + getH() / 2;

		for (ElementObj player : players) {
			if (player instanceof Play) {
				// 计算玩家中心坐标
				int playerCenterX = player.getX() + player.getW() / 2;
				int playerCenterY = player.getY() + player.getH() / 2;
				double distance = Math.sqrt(
						Math.pow(boxCenterX - playerCenterX, 2) +
								Math.pow(boxCenterY - playerCenterY, 2)
				);
				// 距离<40像素时赋予抵抗能力
				if (distance <= TRIGGER_RANGE) {
					((Play) player).setResistWaterTrap(true);
					isResistGranted = true;
					System.out.println("玩家" + ((Play) player).getPlayerNumber() + "获得抵抗水珠包围能力！");
					break;
				}
			}
		}
	}
	private void checkPlayerDistanceForSurroundOtherPlayer() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> allPlayers = em.getElementsByKey(GameElement.PLAY);
		if (allPlayers == null || allPlayers.size() < 2) {
			System.out.println("玩家数量不足2人，无法触发box6效果");
			return;
		}
		// 计算box6动画的中心坐标
		int boxCenterX = getX() + getW() / 2;
		int boxCenterY = getY() + getH() / 2;
		// 1. 找到距离box6动画<40像素的玩家
		Play triggerPlayer = null;
		for (ElementObj player : allPlayers) {
			if (player instanceof Play) {
				int playerCenterX = player.getX() + player.getW() / 2;
				int playerCenterY = player.getY() + player.getH() / 2;
				double distance = Math.sqrt(
						Math.pow(boxCenterX - playerCenterX, 2) +
								Math.pow(boxCenterY - playerCenterY, 2)
				);
				if (distance <= TRIGGER_RANGE) {
					triggerPlayer = (Play) player;
					break;
				}
			}
		}
		if (triggerPlayer == null) {
			// 没有玩家靠近，不触发
			return;
		}
		// 2. 找到另一名玩家
		Play targetPlayer = null;
		for (ElementObj player : allPlayers) {
			if (player instanceof Play && player != triggerPlayer) {
				targetPlayer = (Play) player;
				break;
			}
		}
		if (targetPlayer != null) {
			// 3. 生成泡泡包围目标玩家
			surroundPlayerWithBubbles(targetPlayer);
			isOtherPlayerSurrounded = true;
			System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "触发box6，玩家" + targetPlayer.getPlayerNumber() + "被泡泡包围！");
		}
	}
	private void surroundPlayerWithBubbles(Play targetPlayer) {
		ElementManager em = ElementManager.getManager();
		// 玩家中心坐标
		int gridSize = 45;
		int targetGridX = targetPlayer.getX() ;
		int targetGridY = targetPlayer.getY();
		int[][] surroundPositions = {
				{targetGridX, targetGridY - gridSize-60}, // 上
				{targetGridX, targetGridY + gridSize-60}, // 下
				{targetGridX - gridSize, targetGridY-60}, // 左
				{targetGridX + gridSize, targetGridY-60}  // 右
		};
		// 生成包围泡泡（爆炸延迟2秒，范围1格，避免立即炸死玩家）
		for (int[] pos : surroundPositions) {
			int bubbleX = pos[0];
			int bubbleY = pos[1];
			// 检查位置合法性（不超出边界、非障碍物）
			if (isValidBubblePosition(bubbleX, bubbleY)) {
				// 创建泡泡（格式：x,y,范围,延迟爆炸时间,water_trap）
				Bubble bubble = (Bubble) GameLoad.getObj("bubble").createElement(
						bubbleX + "," + bubbleY + ",1,2000,water_trap"
				);
				em.addElement(bubble, GameElement.BUBBLE);
			}
		}
	}
	private boolean isValidBubblePosition(int x, int y) {
		int gridSize = 40;
		if (x < 0 || x + gridSize > 800) return false;
		if (y < 0 || y + gridSize > 600) return false;
		// 障碍物检查（不与墙壁重叠）
		ElementManager em = ElementManager.getManager();
		List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
		for (ElementObj map : maps) {
			if (map instanceof MapObj) {
				String name = ((MapObj) map).getName();
				if (name != null && (name.equals("BRICK") || name.equals("IRON"))) {
					if (x == map.getX() && y == map.getY()) {
						return false; // 与障碍物重叠，不生成
					}
				}
			}
		}
		return true;
	}
	private void checkPlayerDistanceForOtherPlayerReverse() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> allPlayers = em.getElementsByKey(GameElement.PLAY);
		if (allPlayers == null || allPlayers.size() < 2) {
			System.out.println("玩家数量不足2人，无法触发box5效果");
			return;
		}
		// 计算box5动画的中心坐标
		int boxCenterX = getX() + getW() / 2;
		int boxCenterY = getY() + getH() / 2;
		// 1. 找到距离box5动画<40像素的玩家
		Play triggerPlayer = null;
		for (ElementObj player : allPlayers) {
			if (player instanceof Play) {
				int playerCenterX = player.getX() + player.getW() / 2;
				int playerCenterY = player.getY() + player.getH() / 2;
				double distance = Math.sqrt(
						Math.pow(boxCenterX - playerCenterX, 2) +
								Math.pow(boxCenterY - playerCenterY, 2)
				);
				if (distance <= TRIGGER_RANGE) {
					triggerPlayer = (Play) player;
					break;
				}
			}
		}
		if (triggerPlayer == null) {
			// 没有玩家靠近，不触发
			return;
		}
		// 2. 找到另一名玩家
		Play otherPlayer = null;
		for (ElementObj player : allPlayers) {
			if (player instanceof Play && player != triggerPlayer) {
				otherPlayer = (Play) player;
				break;
			}
		}
		if (otherPlayer != null) {
			// 3. 给另一名玩家赋予反向行走能力
			otherPlayer.setReverseWalking(true);
			isOtherPlayerReverseGranted = true;
			System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "触发box5，玩家" + otherPlayer.getPlayerNumber() + "获得反向行走！");
		}
	}

	private void checkPlayerDistanceForReverseCure() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
		if (players == null) return;

		// 计算box3动画的中心坐标
		int boxCenterX = getX() + getW() / 2;
		int boxCenterY = getY() + getH() / 2;

		for (ElementObj player : players) {
			if (player instanceof Play) {
				// 计算玩家中心坐标
				int playerCenterX = player.getX() + player.getW() / 2;
				int playerCenterY = player.getY() + player.getH() / 2;

				// 计算距离
				int dx = boxCenterX - playerCenterX;
				int dy = boxCenterY - playerCenterY;
				double distance = Math.sqrt(dx * dx + dy * dy);

				// 距离小于40像素时触发解除反向
				if (distance <= TRIGGER_RANGE) {
					isReverseCureGranted = true;
					((Play) player).setReverseWalking(false); // 解除反向状态
					System.out.println("玩家解除了反向行走能力！");
					break;
				}
			}
		}
	}
	private void checkPlayerDistanceForPause() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> allPlayers = em.getElementsByKey(GameElement.PLAY);
		if (allPlayers == null || allPlayers.size() < 2) {
			// 至少需要2名玩家才能触发
			System.out.println("玩家数量不足2人，无法触发box8效果");
			return;
		}

		// 计算box8动画的中心坐标
		int boxCenterX = getX() + getW() / 2;
		int boxCenterY = getY() + getH() / 2;

		// 1. 找到距离box8动画<40像素的玩家（触发者）
		Play triggerPlayer = null;
		for (ElementObj player : allPlayers) {
			if (player instanceof Play) {
				int playerCenterX = player.getX() + player.getW() / 2;
				int playerCenterY = player.getY() + player.getH() / 2;
				double distance = Math.sqrt(
						Math.pow(boxCenterX - playerCenterX, 2) +
								Math.pow(boxCenterY - playerCenterY, 2)
				);
				if (distance <= TRIGGER_RANGE) {
					triggerPlayer = (Play) player;
					break;
				}
			}
		}

		if (triggerPlayer == null) {
			// 没有玩家靠近，不触发
			return;
		}

		// 2. 找到另一名玩家（非触发者）
		Play targetPlayer = null;
		for (ElementObj player : allPlayers) {
			if (player instanceof Play && player != triggerPlayer) {
				targetPlayer = (Play) player;
				break;
			}
		}

		if (targetPlayer != null) {
			// 3. 暂停另一名玩家50帧（核心逻辑）
			targetPlayer.pauseForFrames(50);
			isOtherPlayerPaused = true; // 标记已暂停，避免重复
			System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "触发box8，玩家" + targetPlayer.getPlayerNumber() + "暂停50帧！");
		}
	}
	private void checkPlayerDistanceForReverse() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
		if (players == null) return;

		// 计算box4动画的中心坐标
		int boxCenterX = getX() + getW() / 2;
		int boxCenterY = getY() + getH() / 2;

		for (ElementObj player : players) {
			if (player instanceof Play) {
				// 计算玩家中心坐标
				int playerCenterX = player.getX() + player.getW() / 2;
				int playerCenterY = player.getY() + player.getH() / 2;

				// 计算距离
				int dx = boxCenterX - playerCenterX;
				int dy = boxCenterY - playerCenterY;
				double distance = Math.sqrt(dx * dx + dy * dy);

				// 距离小于40像素时触发反向能力
				if (distance <= TRIGGER_RANGE) {
					isReverseGranted = true;
					((Play) player).setReverseWalking(true); // 开启反向行走
					System.out.println("玩家获得反向行走能力！右→左，上→下");
					break;
				}
			}
		}
	}

	private void checkPlayerDistanceForRangeUp() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
		if (players == null) return;

		// 计算box2爆炸后的中心坐标
		int boxCenterX = getX() + getW() / 2;
		int boxCenterY = getY() + getH() / 2;

		for (ElementObj player : players) {
			if (player instanceof Play) {
				// 计算玩家中心坐标
				int playerCenterX = player.getX() + player.getW() / 2;
				int playerCenterY = player.getY() + player.getH() / 2;

				// 计算距离
				int dx = boxCenterX - playerCenterX;
				int dy = boxCenterY - playerCenterY;
				double distance = Math.sqrt(dx * dx + dy * dy);

				// 距离小于40像素时触发
				if (distance <= TRIGGER_RANGE) {
					isRangeUpGranted = true;
					//((Play) player).increaseBubbleRange(1); // 增加爆炸范围（+1格）
					System.out.println("玩家获得泡泡范围增大能力！当前范围+1");
					break;
				}
			}
		}
	}
	@Override
	protected void move() {
		if (!isLive() || (!"BRICK".equals(name) && !BOX_ANIMATION_PATHS.containsKey(name))) {
			return;
		}

		// 获取所有爆炸效果
		ElementManager em = ElementManager.getManager();
		List<ElementObj> explosions = em.getElementsByKey(GameElement.EXPLOSION);
		if (explosions == null || explosions.isEmpty()) {
			return;
		}

		// 计算当前地图元素的碰撞范围
		Rectangle mapObjRect = new Rectangle(getX(), getY(), getW(), getH());

		// 检测是否与爆炸范围重叠
		for (ElementObj explosion : explosions) {
			// 爆炸效果的碰撞范围
			Rectangle explosionRect = new Rectangle(
					explosion.getX(), explosion.getY(),
					explosion.getW(), explosion.getH()
			);
			// 若重叠且未处于爆炸状态，触发爆炸
			if (mapObjRect.intersects(explosionRect) && !isExploding) {
				startExplosion();
				break;
			}
		}
	}

	private void checkPlayerDistanceForDoubleBubble() {
		ElementManager em = ElementManager.getManager();
		List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
		if (players == null) return;

		// 计算box1动画的中心坐标
		int boxCenterX = getX() + getW() / 2;
		int boxCenterY = getY() + getH() / 2;

		for (ElementObj player : players) {
			if (player instanceof Play) {
				// 计算玩家中心坐标
				int playerCenterX = player.getX() + player.getW() / 2;
				int playerCenterY = player.getY() + player.getH() / 2;

				// 计算距离
				int dx = boxCenterX - playerCenterX;
				int dy = boxCenterY - playerCenterY;
				double distance = Math.sqrt(dx * dx + dy * dy);
				System.out.println(distance);
				// 距离小于40像素时触发
				if (distance <= TRIGGER_RANGE) {
					doubleBubbleCollected = true;
					((Play) player).setCanLaunchDoubleBubble(true); // 赋予双泡泡能力
					System.out.println("玩家获得一次性双泡泡能力！");
					break;
				}
			}
		}
	}
	@Override
	public ElementObj createElement(String str) {
		System.out.println("初始化元素：" + str);
		String[] arr = str.split(",");
		if (arr.length < 3) {
			System.err.println("元素格式错误（需[类型,x,y]）：" + str);
			return null;
		}
		ImageIcon icon = null;
		try {
			switch (arr[0]) {
				case "GRASS":
					icon = new ImageIcon("image/wall/grass1.jpg");
					break;
				case "BRICK":
					icon = new ImageIcon("image/wall/brick.jpg");
					break;
				case "RIVER":
					icon = new ImageIcon("image/wall/house1.jpg");
					break;
				case "IRON":
					icon = new ImageIcon("image/wall/tree.jpg");
					this.hp = 4;
					break;
				case "HOUSE":
					icon = new ImageIcon("image/wall/house2.jpg");
					break;
				// box1-box7：加载初始图片并预加载动画帧
				case "box1":
				case "box2":
				case "box3":
				case "box4":
				case "box5":
				case "box6":
				case "box7":
				case "box8":
					icon = new ImageIcon("image/wall/" + arr[0] + ".jpg");
					loadBoxAnimationFrames(arr[0]); // 预加载对应动画帧
					break;
				default:
					System.err.println("未知元素类型：" + arr[0]);
					return null;
			}
		} catch (Exception e) {
			System.err.println("加载初始图片失败（" + arr[0] + "）：" + e.getMessage());
			return null;
		}

		// 设置坐标和尺寸
		try {
			int x = Integer.parseInt(arr[1]);
			int y = Integer.parseInt(arr[2]);
			this.setX(x);
			this.setY(y);
			this.setW(icon.getIconWidth());
			this.setH(icon.getIconHeight());
			this.setIcon(icon);
			this.name = arr[0]; // 记录元素类型
		} catch (NumberFormatException e) {
			System.err.println("坐标格式错误：" + str);
			return null;
		}

		return this;
	}

	// 加载对应box的动画帧
	private void loadBoxAnimationFrames(String boxType) {
		animationFrames.clear();
		String[] framePaths = BOX_ANIMATION_PATHS.get(boxType);
		if (framePaths == null) {
			System.err.println("未找到" + boxType + "的动画路径配置");
			return;
		}

		// 加载所有帧图片
		for (String path : framePaths) {
			try {
				ImageIcon frame = new ImageIcon(path);
				animationFrames.add(frame);
			} catch (Exception e) {
				System.err.println("加载" + boxType + "动画帧失败（" + path + "）：" + e.getMessage());
				if (animationFrames.isEmpty()) {
					animationFrames.add(this.getIcon());
				}
			}
		}
	}

	// 更新动画帧
	private void updateAnimationFrame() {
		if (animationFrames == null || animationFrames.isEmpty()) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastFrameTime >= FRAME_DURATION) {
			currentFrameIndex = (currentFrameIndex + 1) % animationFrames.size();
			lastFrameTime = currentTime;
		}
	}

	@Override
	public void setLive(boolean live) {
		// 若设置为“死亡”（被攻击），触发爆炸
		if (!live) {
			if ("IRON".equals(name)) {
				this.hp--;
				if (this.hp > 0) {
					return;
				}
			}
			// 触发爆炸效果
			startExplosion();
			return;
		}
		// 若设置为“存活”，直接调用父类方法
		super.setLive(live);
	}

	// 开始爆炸效果
	public void startExplosion() {
		if (!isExploding) {
			isExploding = true;
			explosionStartTime = System.currentTimeMillis();
		}
	}

	// 获取元素类型名称
	public String getName() {
		return name;
	}

	// 重置元素状态（用于重新加载地图）
	public void reset() {
		this.setLive(true);
		this.isExploding = false;
		this.isAnimating = false;
		this.currentFrameIndex = 0;
		this.lastFrameTime = 0;
		this.powerUpCollected = false;
		this.doubleBubbleCollected = false;
		// 重置铁墙血量
		if ("IRON".equals(name)) {
			this.hp = 4;
		}
		// 重新加载动画帧
		if (BOX_ANIMATION_PATHS.containsKey(name)) {
			loadBoxAnimationFrames(name);
		}
	}
}