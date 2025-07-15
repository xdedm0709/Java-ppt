package com.tedu.manager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.MapObj;

/**
 * @说明  加载器(工具：用户读取配置文件的工具)工具类,大多提供的是 static方法
 * @author xdedm0709
 *
 */
public class GameLoad {
	//	得到资源管理器
	private static ElementManager em=ElementManager.getManager();

	//	图片集合  使用map来进行存储     枚举类型配合移动(扩展)
	public static Map<String,ImageIcon> imgMap = new HashMap<>();
	// 新的Map，用于存储动画帧（图像序列）
	// 用它来处理玩家
	public static Map<String, List<ImageIcon>> imgMaps = new HashMap<>();

	//	用户读取文件的类
	private static Properties pro =new Properties();
	/**
	 * 扩展： 使用配置文件，来实例化对象 通过固定的key(字符串来实例化)
	 * @param args
	 */
	private static Map<String, Class<?>> objMap = new HashMap<>();
	/**
	 * @说明 传入地图id有加载方法依据文件规则自动产生地图文件名称，加载文件
	 * @param mapId  文件编号 文件id
	 */
	public static void MapLoad(int mapId) {
//		得到啦我们的文件路径
		String mapName="com/tedu/text/"+mapId+".map";
//		使用io流来获取文件对象   得到类加载器
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream maps = classLoader.getResourceAsStream(mapName);
		if(maps ==null) {
			System.out.println("配置文件读取异常,请重新安装");
			return;
		}
		try {
//			以后用的 都是 xml 和 json
			pro.clear();
			pro.load(maps);
//			可以直接动态的获取所有的key，有key就可以获取 value
			Enumeration<?> names = pro.propertyNames();
			while(names.hasMoreElements()) {//获取是无序的
//				这样的迭代都有一个问题：一次迭代一个元素。
				String key=names.nextElement().toString();
				System.out.println(pro.getProperty(key));
//				就可以自动创建和加载地图
				String [] arrs=pro.getProperty(key).split(";");
				for(int i=0;i<arrs.length;i++) {
					ElementObj element = new MapObj().createElement(key+","+arrs[i]);
					System.out.println(element);
					em.addElement(element, GameElement.MAPS);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		System.out.println("所有资源加载完成。");
	}

	/**
	 * @说明 加载图片。此版本已修正为使用 ClassLoader 并增加了详细的调试输出。
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

		pro.clear();
		try {
			pro.load(configStream);
			Set<Object> keys = pro.keySet();

			for (Object keyObj : keys) {
				String key = keyObj.toString();
				String path = pro.getProperty(key);

				// 使用 ClassLoader 获取资源的URL，这是最关键的修正
				URL imageUrl = classLoader.getResource(path);

				if (imageUrl == null) {
					System.err.println("加载失败：在类路径中找不到图片 -> " + path + " (对应的键: " + key + ")");
					continue; // 跳过这个错误的资源
				}

				// 判断是玩家精灵图还是普通图片
				if (key.equals("player")) {
					// 如果是玩家，执行精灵图切割逻辑
					slicePlayerSprite(imageUrl);
				}
				else if (key.equals("bubble")) { // 处理泡泡
					sliceBubbleAnimation(imageUrl);
				}
				else if (key.equals("explosion_sprite")) { // 处理爆炸
					sliceExplosionSprites(imageUrl);
				}
				else {
					// 否则，作为单个图片加载
					imgMap.put(key, new ImageIcon(imageUrl));
					System.out.println("成功加载图片: " + key + " -> " + path);
				}
			}

		} catch (IOException e) {
			System.err.println("读取图片配置文件时发生IO错误！");
			e.printStackTrace();
		}
	}

	/**
	 * @说明 专门用于切割玩家精灵图的方法
	 * @param imageUrl 精灵图的URL
	 */
	private static void slicePlayerSprite(URL imageUrl) {
		ImageIcon masterIcon = new ImageIcon(imageUrl);
		Image masterImage = masterIcon.getImage();

		// 检查图片是否成功加载
		if (masterIcon.getIconWidth() <= 0 || masterIcon.getIconHeight() <= 0) {
			System.err.println("加载玩家精灵图失败，图片尺寸无效: " + imageUrl.getPath());
			return;
		}

		System.out.println("开始切割玩家精灵图: " + imageUrl.getPath());

		// 为了方便裁剪，将 Image 转换为 BufferedImage
		BufferedImage bufferedMaster = new BufferedImage(
				masterIcon.getIconWidth(),
				masterIcon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		bufferedMaster.getGraphics().drawImage(masterImage, 0, 0, null);

		int spriteWidth = masterIcon.getIconWidth() / 4;
		int spriteHeight = masterIcon.getIconHeight() / 4;
		String[] directions = {"down", "left", "right", "up"};

		for (int i = 0; i < directions.length; i++) {
			List<ImageIcon> frames = new ArrayList<>();
			for (int j = 0; j < 4; j++) {
				int x = j * spriteWidth;
				int y = i * spriteHeight;
				BufferedImage croppedSprite = bufferedMaster.getSubimage(x, y, spriteWidth, spriteHeight);
				frames.add(new ImageIcon(croppedSprite));
			}
			imgMaps.put(directions[i], frames);
		}
		System.out.println("玩家动画已全部加载。");
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
	 * 新增方法：专门用于切割爆炸水柱的各个部分。
	 * 由于这张精灵图布局不规则，我们需要手动指定每个动画序列的坐标。
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
	 * @param masterImage 包含所有动画帧的主图
	 * @param x 动画条第一个帧的左上角 x 坐标
	 * @param y 动画条第一个帧的左上角 y 坐标
	 * @param w 每个动画帧的宽度
	 * @param h 每个动画帧的高度
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
	 * @说明 加载玩家。现在它只负责创建玩家实例。
	 */
	public static void loadPlay() {
		// 假设 objMap 已经被 loadObj() 加载完毕
		ElementObj objTemplate = getObj("play");
		if (objTemplate != null) {
			ElementObj play = objTemplate.createElement("500,500,up");
			em.addElement(play, GameElement.PLAY);
			System.out.println("玩家实例创建成功。");
		} else {
			System.err.println("创建玩家失败，因为找不到 'play' 的对象模板。");
		}
	}


	public static ElementObj getObj(String str) {
		try {
			Class<?> class1 = objMap.get(str);
			if(class1 == null) {
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
	//	用于测试
	public static void main(String[] args) {
		MapLoad(5);
	}

}

