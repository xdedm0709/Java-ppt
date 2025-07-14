package com.tedu.manager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL; // 导入 URL
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
				} else {
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

