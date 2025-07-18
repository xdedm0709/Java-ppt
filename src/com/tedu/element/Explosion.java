package com.tedu.element;

import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;

/**
 * @说明 爆炸效果（水柱）。它有自己的动画，并在播放完毕后消失。
 * @author YourName
 */
public class Explosion extends ElementObj {

    // --- 动画属性 ---
    private int frameIndex = 0;
    private int frameDelay = 0;
    private int animationSpeed = 4; // 动画播放速度，数字越小越快
    private List<ImageIcon> frames; // 持有自己的动画帧列表

    // --- 生命周期属性 ---
    private long creationTime;
    private long duration = 400; // 整个爆炸效果的持续时间（毫秒）

    public Explosion() {}

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        this.setX(Integer.parseInt(arr[0]));
        this.setY(Integer.parseInt(arr[1]));
        String type = arr[2]; // "center", "up_end", "v", 等.

        // 1. 根据类型从 GameLoad 获取正确的动画帧列表
        String animationKey = "explosion_" + type;
        this.frames = GameLoad.imgMaps.get(animationKey);

        // 2. 健壮性检查
        if (this.frames == null || this.frames.isEmpty()) {
            System.err.println("创建爆炸效果失败！无法加载键为 '" + animationKey + "' 的动画。");
            this.setLive(false); // 直接标记为死亡
            return this;
        }

        // 3. 设置初始状态
        ImageIcon firstFrame = this.frames.get(0);
        this.setIcon(firstFrame);
        this.setW(firstFrame.getIconWidth());
        this.setH(firstFrame.getIconHeight());

        this.creationTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        // 检查是否有有效的图像可供绘制
        if (this.getIcon() != null) {
            g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }

    /**
     * @说明 爆炸效果的核心逻辑，在每一帧被 model() 调用
     */
    @Override
    protected void move() {
        // 这个方法现在不负责移动，只负责更新状态
        long gameTime = System.currentTimeMillis();
        // 检查是否到了消失时间
        if (gameTime - creationTime > duration) {
            this.setLive(false); // 标记自己为死亡
        }
    }

    /**
     * @说明 爆炸效果的动画更新，在每一帧被 model() 调用
     */
    @Override
    protected void updateImage() {
        // 只有在存活时才更新动画
        if (!isLive() || this.frames == null) {
            return;
        }

        frameDelay++;
        if (frameDelay > animationSpeed) {
            frameDelay = 0;
            frameIndex++;
        }

        // 动画只播放一次，播放完毕后停在最后一帧
        if (frameIndex >= this.frames.size()) {
            frameIndex = this.frames.size() - 1;
        }

        this.setIcon(this.frames.get(frameIndex));
    }
}