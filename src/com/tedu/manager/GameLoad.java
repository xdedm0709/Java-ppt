package com.tedu.manager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.MapObj;
import com.tedu.element.Play;

/**
 * @说明  加载器(工具：用户读取配置文件的工具)工具类,大多提供的是 static方法
 * @author xdedm0709
 *
 */
public class GameLoad {
	//	得到资源管理器
	private static ElementManager em = ElementManager.getManager();
	//	图片集合  使用map来进行存储     枚举类型配合移动(扩展)
	public static Map<String, ImageIcon> imgMap = new HashMap<>();
	// 新的Map，用于存储动画帧（图像序列）
	// 用它来处理玩家
	public static Map<String, List<ImageIcon>> imgMaps = new HashMap<>();

	//	用户读取文件的类
	private static Properties pro = new Properties();
	/**
	 * 扩展： 使用配置文件，来实例化对象 通过固定的key(字符串来实例化)
	 *
	 * @param args
	 */
	private static Map<String, Class<?>> objMap = new HashMap<>();

	/**
	 * @说明 传入地图id，加载并解析地图文件，创建所有地图元素。
	 *      它能够根据方块类型，智能地选择正确的类来创建实例。
	 * @param mapId 地图文件的编号
	 */
	public static void MapLoad(int mapId) {
		String mapName = "com/tedu/text/" + mapId + ".map";
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream mapsStream = classLoader.getResourceAsStream(mapName);

		if (mapsStream == null) {
			System.err.println("致命错误：无法加载地图文件 -> " + mapName);
			return;
		}

		// 使用一个临时的 Properties 对象，避免与全局静态 pro 冲突
		Properties mapProps = new Properties();
		try {
			// 使用 InputStreamReader 来确保能正确读取包含特殊字符的文件
			mapProps.load(new InputStreamReader(mapsStream, StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// 遍历地图文件中的所有类型定义 (如 BRICK, IRON, box1, GRASS 等)
		for (Object keyObj : mapProps.keySet()) {
			String typeKey = keyObj.toString(); // "BRICK", "GRASS", "IRON", "box1" etc.
			String[] positions = mapProps.getProperty(typeKey).split(";");

			String objectId; // 将要从 obj.pro 获取模板的 ID

			// 【核心逻辑】根据类型名进行分类，决定使用哪个对象模板
			if (typeKey.startsWith("box")) {
				objectId = "box";       // 所有 "box1", "box2" 等都使用 Box 类的模板
			} else if (typeKey.equals("BRICK") || typeKey.equals("GRASS")) {
				objectId = "brick";     // "BRICK" 和 "GRASS" 都使用 Brick 类的模板 (可摧毁)
			} else {
				// 其他所有类型 (IRON, HOUSE1, etc.) 都当作不可摧毁的 MapObj
				objectId = "map_obj";
			}

			// 从我们预加载的对象工厂中获取对应的模板实例
			ElementObj template = getObj(objectId);
			if (template == null) {
				System.err.println("创建地图失败：在 obj.pro 中找不到 ID 为 '" + objectId + "' 的对象模板。");
				continue; // 跳过这个未知的类型
			}

			// 遍历该类型所有的坐标，并使用正确的模板来创建实例
			for (String pos : positions) { // pos is "x,y"
				// 将类型名和坐标拼接成创建字符串
				String creationStr = typeKey + "," + pos; // e.g., "box1,120,120" or "GRASS,80,80"

				ElementObj element = template.createElement(creationStr);
				if (element != null) {
					em.addElement(element, GameElement.MAPS);
				}
			}
		}
		System.out.println("地图 " + mapId + ".map 加载完成。");
	}

	/**
	 *@说明 加载图片代码
	 *加载图片 代码和图片之间差 一个 路径问题
	 */
	/**
	 * @说明 统一的加载方法，控制所有资源的加载顺序
	 */
	public static void loadAll() {
		System.out.println("开始加载所有游戏资源...");
		loadObj(); // 1. 首先加载对象配置，因为创建玩家时需要它
		loadImg(); // 2. 然后加载所有图片资源
		loadItemAnimations(); // 加载所有道具
		System.out.println("所有资源加载完成。");
	}

	/**
	 * @说明 加载图片。使用 ClassLoader 并增加了详细的调试输出。
	 */
	public static void loadImg() {
		String configUrl = "com/tedu/text/GameData.pro";
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream configStream = classLoader.getResourceAsStream(configUrl);

		if (configStream == null) {
			System.err.println("致命错误：无法加载图片配置文件 -> " + configUrl);
			System.err.println("请确认 'text' 文件夹是否已在IDE中被标记为资源目录(Resources Root)！");
			return;
		}

		Properties tempPro = new Properties();
		try {
			tempPro.load(new InputStreamReader(configStream, StandardCharsets.UTF_8));

			for (Object keyObj : tempPro.keySet()) {
				String key = keyObj.toString();
				String path = tempPro.getProperty(key);
				URL imageUrl = classLoader.getResource(path);

				if (imageUrl == null) {
					System.err.println("加载失败：在类路径中找不到图片 -> " + path + " (对应的键: " + key + ")");
					continue; // 跳过这个错误的资源
				}

				// 使用 switch 语句来处理不同的资源类型
				switch (key) {
					case "player1": // 对应 player1
						slicePlayerSprite(imageUrl, "player1");
						break;
					case "player2": // 对应 player2
						slicePlayerSprite(imageUrl, "player2");
						break;
					case "bubble":
						sliceBubbleAnimation(imageUrl);
						break;
					case "explosion_sprite":
						sliceExplosionSprites(imageUrl);
						break;
					default:
						imgMap.put(key, new ImageIcon(imageUrl));
						System.out.println("  成功加载图片: " + key);
						break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @说明 加载所有在 GameData.pro 中以 "item" 开头的道具动画。
	 *      它会为每一张独立的道具图片切割出动画序列。
	 */
	public static void loadItemAnimations() {
		System.out.println("--- 正在加载所有道具动画(基于GameData.pro)... ---");

		// 1. 加载 GameData.pro 配置文件
		String configUrl = "com/tedu/text/GameData.pro";
		InputStream configStream = GameLoad.class.getClassLoader().getResourceAsStream(configUrl);
		if (configStream == null) { /* ... 错误处理 ... */ return; }

		Properties gameDataProps = new Properties();
		try {
			gameDataProps.load(new InputStreamReader(configStream, StandardCharsets.UTF_8));
		} catch (IOException e) { e.printStackTrace(); return; }

		// 2. 遍历配置文件，找到所有以 "item" 开头的键 (item1, item2, etc.)
		for (Object keyObj : gameDataProps.keySet()) {
			String key = keyObj.toString();

			// 我们只关心 "item1", "item2" ... "item8" 这些键
			if (key.startsWith("item")) {
				String imagePath = gameDataProps.getProperty(key);
				URL imageUrl = GameLoad.class.getClassLoader().getResource(imagePath);

				if (imageUrl == null) {
					System.err.println("  加载失败：找不到道具图片 -> " + imagePath + " (键: " + key + ")");
					continue;
				}

				// 3. 调用切割方法为这张图片创建动画
				//    假设所有道具动画都是4帧的
				List<ImageIcon> frames = cutTopStripAnimation(imageUrl, 4);

				// 4. 使用 item1_anim, item2_anim 等作为键存入动画库
				String animationKey = key + "_anim";
				imgMaps.put(animationKey, frames);
				System.out.println("  成功加载道具动画: " + animationKey + " from " + imagePath);
			}
		}
	}

	/**
	 * @说明 专门用于切割玩家精灵图的方法。
	 *      它能为不同玩家加载动画，并使用正确的键名。
	 * @param imageUrl 精灵图的URL
	 * @param playerID 用于标识玩家的字符串, 如 "player1" 或 "player2"
	 */
	private static void slicePlayerSprite(URL imageUrl, String playerID) {
		System.out.println("  -- 开始切割 " + playerID + " 的精灵_图: " + imageUrl.getPath());
		ImageIcon masterIcon = new ImageIcon(imageUrl);

		if (masterIcon.getIconWidth() <= 0) {
			System.err.println("    加载 " + playerID + " 精灵图失败，图片尺寸无效。");
			return;
		}

		BufferedImage bufferedMaster = new BufferedImage(
				masterIcon.getIconWidth(),
				masterIcon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		bufferedMaster.getGraphics().drawImage(masterIcon.getImage(), 0, 0, null);

		// 假设所有玩家精灵图都是 4x4 的布局
		int cols = 4;
		int rows = 4;
		int spriteWidth = masterIcon.getIconWidth() / cols;
		int spriteHeight = masterIcon.getIconHeight() / rows;
		String[] directions = {"down", "left", "right", "up"};

		// i 对应行 (方向)
		for (int i = 0; i < rows; i++) {
			List<ImageIcon> frames = new ArrayList<>(); // 用于存放一个方向的所有动画帧
			// j 对应列 (单个动画帧)
			for (int j = 0; j < cols; j++) {
				int x = j * spriteWidth;
				int y = i * spriteHeight;
				BufferedImage croppedSprite = bufferedMaster.getSubimage(x, y, spriteWidth, spriteHeight);
				frames.add(new ImageIcon(croppedSprite));
			}

			// 【核心修正】使用 playerID 和方向作为唯一的键来存储动画序列
			String key = playerID + "_" + directions[i];

			// 将切割好的动画帧列表与正确的键关联起来
			imgMaps.put(key, frames);

			System.out.println("    成功加载动画: " + key);
		}
		System.out.println("  -- " + playerID + " 的动画已加载。");
	}

	/**
	 * 专门用于切割泡泡的动画
	 */
	private static void sliceBubbleAnimation(URL imageUrl) {
		System.out.println("  -- 开始切割泡泡动画...");
		ImageIcon masterIcon = new ImageIcon(imageUrl);
		Image masterImage = masterIcon.getImage();

		// 检查图片是否成功加载
		if (masterIcon.getIconWidth() <= 0 || masterIcon.getIconHeight() <= 0) {
			System.err.println("加载泡泡精灵图失败，图片尺寸无效: " + imageUrl.getPath());
			return;
		}

		// 为了方便裁剪，将 Image 转换为 BufferedImage
		BufferedImage bufferedMaster = new BufferedImage(
				masterIcon.getIconWidth(),
				masterIcon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		// 使用 Graphics2D 绘制以确保透明度被保留
		Graphics2D g2d = bufferedMaster.createGraphics();
		g2d.drawImage(masterImage, 0, 0, null);
		g2d.dispose();

		int cols = 4;
		int rows = 4;
		int spriteWidth = masterIcon.getIconWidth() / cols;
		int spriteHeight = masterIcon.getIconHeight() / rows;

		List<ImageIcon> frames = new ArrayList<>();

		// 按照从左到右，从上到下的顺序切割
		for (int i = 0; i < rows; i++) { // i 代表行号
			for (int j = 0; j < cols; j++) { // j 代表列号
				int x = j * spriteWidth;
				int y = i * spriteHeight;

				// 使用 getSubimage 方法从主图中切割出子图
				BufferedImage subImg = bufferedMaster.getSubimage(x, y, spriteWidth, spriteHeight);

				// 将切割出的子图转换为 ImageIcon 并添加到列表中
				frames.add(new ImageIcon(subImg));
			}
		}

		// 将包含所有动画帧的列表存入 imgMaps 中，使用 "bubble" 作为键
		imgMaps.put("bubble", frames);
	}

	/**
	 * @说明 辅助方法：专门用于切割一张图片上半部分的水平动画条。
	 *      它会自动计算帧尺寸并忽略下半部分的空白。
	 * @param imageUrl 图片的URL
	 * @param frameCount 动画包含的帧数
	 * @return 包含所有动画帧的列表
	 */
	private static List<ImageIcon> cutTopStripAnimation(URL imageUrl, int frameCount) {
		List<ImageIcon> frames = new ArrayList<>();
		ImageIcon masterIcon = new ImageIcon(imageUrl);

		if (masterIcon.getIconWidth() <= 0 || frameCount <= 0) {
			return frames; // 返回空列表
		}

		BufferedImage bufferedMaster = new BufferedImage(
				masterIcon.getIconWidth(),
				masterIcon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		bufferedMaster.getGraphics().drawImage(masterIcon.getImage(), 0, 0, null);

		// 【核心逻辑】
		// 1. 计算单帧的宽度
		int frameWidth = masterIcon.getIconWidth() / frameCount;

		// 2. 确定有效内容的高度。我们不关心下半部分，
		//    所以可以假设单帧的高度等于宽度（对于方形道具），或者取图片总高度。
		//    一个更健壮的方法是，假设动画帧是正方形的。
		int frameHeight = frameWidth; // 假设道具帧是正方形

		// 3. 切割上半部分的水平动画条
		for (int i = 0; i < frameCount; i++) {
			int x = i * frameWidth;
			int y = 0; // 只在最顶行 (y=0) 切割

			// 做一个边界检查，防止 frameHeight 超出图片实际高度
			if (y + frameHeight <= bufferedMaster.getHeight()) {
				BufferedImage frameImage = bufferedMaster.getSubimage(x, y, frameWidth, frameHeight);
				frames.add(new ImageIcon(frameImage));
			} else {
				System.err.println("警告：切割动画时，计算出的帧高度超出了图片边界 -> " + imageUrl.getPath());
				break;
			}
		}
		return frames;
	}

	/**
	 * 新增方法：专门用于切割爆炸水柱的各个部分。
	 * 由于这张精灵图布局不规则，我们需要手动指定每个动画序列的坐标。
	 *
	 * @param imageUrl 爆炸效果精灵图 (Animations.png) 的URL
	 */
	private static void sliceExplosionSprites(URL imageUrl) {
		System.out.println("  -- 开始切割爆炸精灵图...");
		ImageIcon masterIcon = new ImageIcon(imageUrl);
		Image masterImage = masterIcon.getImage();

		if (masterIcon.getIconWidth() <= 0 || masterIcon.getIconHeight() <= 0) {
			System.err.println("加载爆炸精灵图失败，图片尺寸无效: " + imageUrl.getPath());
			return;
		}

		BufferedImage bufferedMaster = new BufferedImage(
				masterIcon.getIconWidth(),
				masterIcon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bufferedMaster.createGraphics();
		g2d.drawImage(masterImage, 0, 0, null);
		g2d.dispose();

		// =======================================================================
		// 注意：下面的坐标 (x, y) 和尺寸 (w, h) 是估算值！
		// 你需要用图像编辑工具打开 Animations.png，精确测量每个动画条的
		// 左上角像素坐标和单个帧的宽高，然后替换掉这里的数字。
		// =======================================================================

		// 假设每个动画帧是 32x32 像素, 每个动画有4帧
		int spriteWidth = 32;
		int spriteHeight = 32;
		int frameCount = 4; // 假设大部分动画是4帧

		// 切割中心爆炸动画 (假设从 x=0, y=40 开始)
		List<ImageIcon> centerAnim = cutAnimation(bufferedMaster, 0, 40, spriteWidth, spriteHeight, frameCount);
		imgMaps.put("explosion_center", centerAnim);

		// 切割上端水柱动画 (假设从 x=0, y=0 开始)
		List<ImageIcon> upAnim = cutAnimation(bufferedMaster, 0, 0, spriteWidth, spriteHeight, frameCount);
		imgMaps.put("explosion_up_end", upAnim);

		// 切割下端水柱动画 (假设从 x=128, y=0 开始)
		List<ImageIcon> downAnim = cutAnimation(bufferedMaster, 128, 0, spriteWidth, spriteHeight, frameCount);
		imgMaps.put("explosion_down_end", downAnim);

		// 切割左端水柱动画 (假设从 x=0, y=120 开始)
		List<ImageIcon> leftAnim = cutAnimation(bufferedMaster, 0, 120, spriteWidth, spriteHeight, frameCount);
		imgMaps.put("explosion_left_end", leftAnim);

		// 切割右端水柱动画 (假设从 x=128, y=120 开始)
		List<ImageIcon> rightAnim = cutAnimation(bufferedMaster, 128, 120, spriteWidth, spriteHeight, frameCount);
		imgMaps.put("explosion_right_end", rightAnim);

		// 切割垂直水柱动画 (假设从 x=0, y=80 开始)
		List<ImageIcon> verticalAnim = cutAnimation(bufferedMaster, 0, 80, spriteWidth, spriteHeight, frameCount);
		imgMaps.put("explosion_v", verticalAnim);

		// 切割水平水柱动画 (假设从 x=128, y=80 开始)
		List<ImageIcon> horizontalAnim = cutAnimation(bufferedMaster, 128, 80, spriteWidth, spriteHeight, frameCount);
		imgMaps.put("explosion_h", horizontalAnim);

		System.out.println("  -- 爆炸水柱动画已全部加载。");
	}

	/**
	 * 辅助方法：从主图上切割出一个水平的动画条。
	 *
	 * @param masterImage 包含所有动画帧的主图
	 * @param x           动画条第一个帧的左上角 x 坐标
	 * @param y           动画条第一个帧的左上角 y 坐标
	 * @param w           每个动画帧的宽度
	 * @param h           每个动画帧的高度
	 * @param framesCount 这个动画条包含多少帧
	 * @return 一个包含所有动画帧图像的列表
	 */
	private static List<ImageIcon> cutAnimation(BufferedImage masterImage, int x, int y, int w, int h, int framesCount) {
		List<ImageIcon> frames = new ArrayList<>();
		for (int i = 0; i < framesCount; i++) {
			// 假设动画帧是水平排列的
			int frameX = x + (i * w);
			BufferedImage subImg = masterImage.getSubimage(frameX, y, w, h);
			frames.add(new ImageIcon(subImg));
		}
		return frames;
	}

	/**
	 * @说明 创建玩家实例，并将其放置在指定位置。
	 * @param x 玩家的初始 x 坐标
	 * @param y 玩家的初始 y 坐标
	 * @param direction 玩家的初始方向, 如 "up"
	 */
	public static void loadPlayer(int x, int y, String direction, String playerID) {
		System.out.println("正在创建玩家...");
		ElementObj playerTemplate = getObj("play");

		if (playerTemplate instanceof Play) {
			String creationStr = x + "," + y + "," + direction + "," + playerID; // 新格式
			Play player = (Play) playerTemplate.createElement(creationStr);

			em.addElement(player, GameElement.PLAY);
			System.out.println(playerID + " 已创建并添加。");
		} else {
			System.err.println("创建玩家失败！因为 'play' 的对象模板不是 Play 类型或未找到。");
		}
	}


	public static ElementObj getObj(String str) {
		try {
			Class<?> class1 = objMap.get(str);
			if (class1 == null) {
				System.err.println("错误: 在obj.pro的配置中找不到键为 '" + str + "' 的对象");
				return null;
			}
			Object newInstance = class1.newInstance();
			if (newInstance instanceof ElementObj) {
				return (ElementObj) newInstance;
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @说明 使用配置文件，来实例化对象
	 */
	public static void loadObj() {
		String configUrl = "com/tedu/text/obj.pro";
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream configStream = classLoader.getResourceAsStream(configUrl);

		if (configStream == null) {
			System.err.println("致命错误：无法加载对象配置文件 -> " + configUrl);
			return;
		}

		pro.clear();
		try {
			pro.load(configStream);
			Set<Object> set = pro.keySet();
			for (Object o : set) {
				String key = o.toString();
				String classUrl = pro.getProperty(key);
				Class<?> forName = Class.forName(classUrl);
				objMap.put(key, forName);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// 【新增】用于存储“箱子类型”到“道具类型”映射的 Map
	public static final Map<String, String> BOX_TO_ITEM_MAP = new HashMap<>();

	// 使用静态初始化块来填充这个映射
	static {
		System.out.println("--- 初始化箱子到道具的映射... ---");
		// 右边的值必须与 obj.pro 中定义的道具键完全一致
		BOX_TO_ITEM_MAP.put("box1", "item_surround_trap");
		BOX_TO_ITEM_MAP.put("box2", "item_range_up");
		BOX_TO_ITEM_MAP.put("box3", "item_reverse_cure");
		BOX_TO_ITEM_MAP.put("box4", "item_reverse_walk");
		BOX_TO_ITEM_MAP.put("box5", "item_reverse_other");
		BOX_TO_ITEM_MAP.put("box6", "item_pause_other");
		BOX_TO_ITEM_MAP.put("box7", "item_add_bubble");
		BOX_TO_ITEM_MAP.put("box8", "item_resist_card");
	}
}

