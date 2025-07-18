package com.tedu.element;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

/**
 * @说明 代表所有可被摧毁并掉落道具的箱子（Box1 到 Box8）。
 * @author xdedm0709
 */
public class Box extends ElementObj {

    // 核心属性
    private String boxType; // "box1", "box2", ..., "box8"

    // 爆炸效果相关属性
    private boolean isExploding = false;
    private long explosionStartTime = 0;
    private static final long EXPLOSION_DURATION = 300; // 爆炸效果持续0.3秒
    private static ImageIcon explosionIcon; // 所有箱子共享同一个爆炸贴图

    // 静态初始化块，用于加载所有Box类共享的资源
    static {
        // 从 GameLoad 获取预加载的爆炸图片
        explosionIcon = GameLoad.imgMap.get("explosion_effect"); // 假设你在GameData.pro中定义了这个键
    }

    public Box() {}

    /**
     * @说明 创建一个指定类型的箱子实例。
     * @param str 格式: "boxType,x,y"，例如: "box1,120,120"
     */
    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        this.boxType = arr[0];
        int x = Integer.parseInt(arr[1]);
        int y = Integer.parseInt(arr[2]);

        // 从 GameLoad 获取该类型箱子的初始图片
        ImageIcon icon = GameLoad.imgMap.get(this.boxType);

        if (icon != null) {
            this.setX(x);
            this.setY(y);
            this.setW(icon.getIconWidth());
            this.setH(icon.getIconHeight());
            this.setIcon(icon);
        } else {
            System.err.println("错误：无法为箱子加载图片: " + this.boxType);
            this.setLive(false); // 加载失败，直接设置为死亡
        }
        return this;
    }

    /**
     * @说明 绘制逻辑。会根据是否处于爆炸状态，绘制不同的图像。
     */
    @Override
    public void showElement(Graphics g) {
        if (!isLive()) return; // 如果已经死亡，不绘制任何东西

        if (isExploding) {
            // 如果正在爆炸，绘制爆炸效果
            if (explosionIcon != null) {
                g.drawImage(explosionIcon.getImage(), getX(), getY(), getW(), getH(), null);
            }
        } else {
            // 正常状态下，绘制箱子本身
            g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }

    /**
     * @说明 箱子的主要逻辑更新。负责检测是否被爆炸命中。
     */
    @Override
    protected void move() {
        // 如果已经死亡，则不进行任何操作
        if (!isLive()) return;

        // 检查是否与任何爆炸效果发生碰撞
        ElementManager em = ElementManager.getManager();
        List<ElementObj> explosions = em.getElementsByKey(GameElement.EXPLOSION);

        for (ElementObj explosion : explosions) {
            if (this.pk(explosion)) {
                // 如果被炸到，立即执行死亡逻辑
                this.die();
                break; // 只要被一个爆炸物命中就足够了
            }
        }
    }

    /**
     * @说明 在 action 钩子中处理爆炸结束后的逻辑
     */
    @Override
    protected void action(long gameTime) {}

    /**
     * @说明 当箱子死亡时，执行此方法来生成道具。
     */
    @Override
    public void die() {
        // 防止重复执行
        if (!isLive()) return;
        // 1. 调用父类的 die()，将自己的 isLive 设为 false
        super.die();

        // 2. 查询 GameLoad 中的映射关系，决定要生成哪种道具
        String itemID = GameLoad.BOX_TO_ITEM_MAP.get(this.boxType);

        if (itemID != null) {
            // 3. 如果找到了对应的道具ID，就创建它

            ElementObj itemTemplate = GameLoad.getObj(itemID);
            if (itemTemplate != null) {
                // 在箱子原来的位置创建道具
                String creationStr = this.getX() + "," + this.getY();
                ElementObj item = itemTemplate.createElement(creationStr);

                // 将新创建的道具添加到管理器的道具列表中
                ElementManager.getManager().addElement(item, GameElement.ITEMS);
            } else {
                System.err.println("  -> 生成失败！在 obj.pro 中找不到道具ID: " + itemID);
            }
        }
    }

    @Override
    public Rectangle getRectangle() {
        // 如果正在爆炸或已经死亡，返回一个没有体积的、无效的碰撞箱
        if (isExploding || !isLive()) {
            return new Rectangle(0, 0, 0, 0);
        }
        // 正常状态下返回真实的碰撞箱
        return super.getRectangle();
    }

    // 箱子不需要更新动画
    @Override
    protected void updateImage() {}
}