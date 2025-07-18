package com.tedu.element;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

/**
 * @说明 代表所有可被摧毁但不会掉落道具的地图元素，如砖墙(BRICK)和草地(GRASS)。
 * @author xdedm0709
 */
public class Brick extends ElementObj {

    private String type; // "BRICK" or "GRASS"

    // 爆炸效果
    private boolean isExploding = false;
    private long explosionStartTime = 0;
    private static final long EXPLOSION_DURATION = 300; // 爆炸效果持续0.3秒
    private static ImageIcon explosionIcon;

    // 静态块，用于加载共享的爆炸图片
    static {
        // 确保 explosion_effect 这个键在 GameData.pro 中有定义
        explosionIcon = GameLoad.imgMap.get("explosion_effect");
    }

    public Brick() {}

    @Override
    public ElementObj createElement(String str) {
        // 1. 创建一个新的 Brick 实例
        Brick newBrick = new Brick();

        // 2. 解析字符串，并设置新实例的属性
        String[] arr = str.split(",");
        newBrick.type = arr[0];
        int x = Integer.parseInt(arr[1]);
        int y = Integer.parseInt(arr[2]);
        ImageIcon icon = GameLoad.imgMap.get(newBrick.type);

        if (icon != null) {
            newBrick.setX(x);
            newBrick.setY(y);
            newBrick.setW(icon.getIconWidth());
            newBrick.setH(icon.getIconHeight());
            newBrick.setIcon(icon);
        } else {
            newBrick.setLive(false);
        }

        // 3. 返回
        return newBrick;
    }

    @Override
    public void showElement(Graphics g) {
        if (!isLive()) return;

        if (isExploding) {
            // 如果正在爆炸，绘制爆炸效果
            if (explosionIcon != null) {
                g.drawImage(explosionIcon.getImage(), getX(), getY(), getW(), getH(), null);
            }
        } else {
            // 正常状态下，绘制自己 (砖墙或草地)
            g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }

    @Override
    protected void move() {
        if (!isLive() || isExploding) return;

        // 检查是否被爆炸命中
        ElementManager em = ElementManager.getManager();
        List<ElementObj> explosions = em.getElementsByKey(GameElement.EXPLOSION);
        for (ElementObj explosion : explosions) {
            if (this.pk(explosion)) {
                this.isExploding = true;
                this.explosionStartTime = System.currentTimeMillis();
                break;
            }
        }
    }

    @Override
    protected void action(long gameTime) {
        // 在 action 钩子中处理爆炸结束后的逻辑
        if (isExploding) {
            if (gameTime - explosionStartTime > EXPLOSION_DURATION) {
                this.die(); // 爆炸效果结束，正式死亡
            }
        }
    }

    // 死亡时不做任何事（不掉落道具）
    @Override
    public void die() {
        super.die();
    }

    @Override
    public Rectangle getRectangle() {
        // 如果正在爆炸或已经死亡，返回一个无效的、没有体积的矩形
        if (isExploding || !isLive()) {
            return new Rectangle(0, 0, 0, 0);
        }
        // 否则，返回正常的碰撞箱
        return super.getRectangle();
    }

    @Override
    protected void updateImage() {}
}