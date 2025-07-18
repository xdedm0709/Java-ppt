package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public abstract class BoxObj extends ElementObj {
    protected String name; // 箱子类型（如box1对应Doubleb）
    protected boolean isExploded = false; // 是否已爆炸
    protected boolean isAbilityGranted = false; // 能力是否已赋予（避免重复触发）
    protected int hp = 1; // 默认血量（1次爆炸销毁）用来区分是否可摧毁
    protected boolean isExploding = false;
    protected long explosionStartTime = 0;
    protected static final long EXPLOSION_DURATION = 300; // 爆炸持续300ms
    protected static ImageIcon explosionIcon;

    // 动画相关
    protected List<ImageIcon> animationFrames = new ArrayList<>();
    protected int currentFrameIndex = 0;
    protected long lastFrameTime = 0;
    protected static final long FRAME_DURATION = 100; // 每帧100ms
    protected boolean isAnimating = false;
    protected static final int TRIGGER_RANGE = 40; // 触发范围40像素，一个格子大小事30像素

    // 静态加载爆炸图片
    static {
        try {
            explosionIcon = new ImageIcon("bin/image/boom/boom.png");
        } catch (Exception e) {
            System.err.println("爆炸图片加载失败：" + e.getMessage());
            explosionIcon = new ImageIcon();
        }
    }

    protected abstract void grantAbility();
    protected abstract String[] getAnimationPaths();
    @Override
    public void showElement(Graphics g) {
        // 1. 处理爆炸状态
        if (isExploding) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - explosionStartTime <= EXPLOSION_DURATION) {
                g.drawImage(explosionIcon.getImage(), getX(), getY(), getW(), getH(), null);
            } else {
                isExploding = false;
                isExploded = true;
                isAnimating = true;
                lastFrameTime = currentTime;
                loadAnimationFrames(); // 加载动画帧
            }
            return;
        }

        // 2. 处理动画状态
        if (isAnimating && !animationFrames.isEmpty()) {
            updateAnimationFrame();
            g.drawImage(animationFrames.get(currentFrameIndex).getImage(), getX(), getY(), getW(), getH(), null);
        }

        // 3. 爆炸后检测玩家距离，触发能力（仅一次）
        if (isExploded && isAnimating && !isAbilityGranted) {
            grantAbility();
        }

        // 4. 未爆炸时显示初始图片
        if (!isExploded && getIcon() != null && isLive()) {
            g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }
    protected void loadAnimationFrames() {
        animationFrames.clear();
        String[] paths = getAnimationPaths();
        for (String path : paths) {
            try {
                animationFrames.add(new ImageIcon(path));
            } catch (Exception e) {
                System.err.println(name + "动画帧加载失败（" + path + "）：" + e.getMessage());
            }
        }
    }
    protected void updateAnimationFrame() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= FRAME_DURATION) {
            currentFrameIndex = (currentFrameIndex + 1) % animationFrames.size();
            lastFrameTime = currentTime;
        }
    }

    // 检测玩家是否在触发范围内
    protected Play getPlayerInRange() {
        ElementManager em = ElementManager.getManager();
        List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
        if (players == null) return null;

        int boxCenterX = getX() + getW() / 2;
        int boxCenterY = getY() + getH() / 2;

        for (ElementObj obj : players) {
            if (obj instanceof Play) {
                Play player = (Play) obj;
                int playerCenterX = player.getX() + player.getW() / 2;
                int playerCenterY = player.getY() + player.getH() / 2;
                // 计算距离
                double distance = Math.sqrt(
                        Math.pow(boxCenterX - playerCenterX, 2) +
                                Math.pow(boxCenterY - playerCenterY, 2)
                );
                if (distance <= TRIGGER_RANGE) {
                    return player; // 返回范围内的玩家
                }
            }
        }
        return null;
    }

    // 获取另一名玩家
    protected Play getOtherPlayer(Play triggerPlayer) {
        ElementManager em = ElementManager.getManager();
        List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
        if (players == null || players.size() < 2) return null;

        for (ElementObj obj : players) {
            if (obj instanceof Play && obj != triggerPlayer) {
                return (Play) obj;
            }
        }
        return null;
    }

    // 开始爆炸
    public void startExplosion() {
        if (!isExploding) {
            isExploding = true;
            explosionStartTime = System.currentTimeMillis();
        }
    }

    // 检测是否被爆炸命中
    @Override
    protected void move() {
        if (!isLive() || isExploding) return;

        ElementManager em = ElementManager.getManager();
        List<ElementObj> explosions = em.getElementsByKey(GameElement.EXPLOSION);
        if (explosions == null) return;

        Rectangle boxRect = new Rectangle(getX(), getY(), getW(), getH());
        for (ElementObj explosion : explosions) {
            Rectangle explosionRect = new Rectangle(explosion.getX(), explosion.getY(), explosion.getW(), explosion.getH());
            if (boxRect.intersects(explosionRect)) {
                startExplosion();
                break;
            }
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        if (arr.length < 3) {
            System.err.println(name + "初始化格式错误：" + str);
            return null;
        }

        try {
            // 加载初始图片
            ImageIcon icon = new ImageIcon("image/wall/" + name + ".jpg");
            this.setX(Integer.parseInt(arr[1]));
            this.setY(Integer.parseInt(arr[2]));
            this.setW(icon.getIconWidth());
            this.setH(icon.getIconHeight());
            this.setIcon(icon);
            this.setLive(true);
            return this;
        } catch (Exception e) {
            System.err.println(name + "初始化失败：" + e.getMessage());
            return null;
        }
    }

    // Getter
    public String getName() {
        return name;
    }
}