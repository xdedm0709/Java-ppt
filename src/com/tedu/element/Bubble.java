package com.tedu.element;

import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.show.GameJFrame;

public class Bubble extends ElementObj {

    private int frameIndex = 0;
    private int frameDelay = 0;
    private int animationSpeed = 8;
    private long creationTime;
    private long fuseTime = 2000; // 泡泡持续3秒后爆炸
    private int power = 2; // 爆炸威力（向外延伸2格）
    private static final int TILE_SIZE = 30; // 假设每个格子的尺寸是40x40
    // 定义泡泡的逻辑尺寸
    public static final int BUBBLE_SIZE = 30;

    public Bubble() {}

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        // 传入的坐标是玩家的大致位置
        int rawX = Integer.parseInt(arr[0]);
        int rawY = Integer.parseInt(arr[1]);

        // 使用全局 TILE_SIZE 来对齐坐标
        final int TILE_SIZE = GameJFrame.TILE_SIZE;
        // 计算对齐后的左上角坐标
        int alignedX = (rawX + TILE_SIZE / 2) / TILE_SIZE * TILE_SIZE;
        int alignedY = (rawY + TILE_SIZE / 2) / TILE_SIZE * TILE_SIZE;

        // 【核心修改 2】计算偏移量，使泡泡在格子里居中
        int offsetX = (TILE_SIZE - BUBBLE_SIZE) / 2;
        int offsetY = (TILE_SIZE - BUBBLE_SIZE) / 2;

        // 设置最终坐标
        this.setX(alignedX + offsetX);
        this.setY(alignedY + offsetY);

        // 使用定义的 BUBBLE_SIZE，而不是从图片获取
        this.setW(BUBBLE_SIZE);
        this.setH(BUBBLE_SIZE);

        // 获取动画帧用于显示
        List<ImageIcon> frames = GameLoad.imgMaps.get("bubble");
        if (frames != null && !frames.isEmpty()) {
            this.setIcon(frames.get(0));
        } else {
            System.err.println("错误：未能加载 'bubble' 动画帧。");
            this.setIcon(new ImageIcon()); // 设置空图标防止崩溃
        }

        this.creationTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
    }

    @Override
    protected void move() {
        long gameTime = System.currentTimeMillis(); // 获取当前时间

        // 1. 更新泡泡自身的动画
        updateAnimation();

        // 2. 检查是否到了爆炸时间
        if (gameTime - creationTime > fuseTime) {
            this.explode();
        }
    }

    private void updateAnimation() {
        frameDelay++;
        if (frameDelay > animationSpeed) {
            frameDelay = 0;
            frameIndex++;
            List<ImageIcon> frames = GameLoad.imgMaps.get("bubble");
            if (frameIndex >= frames.size()) {
                frameIndex = 0;
            }
            this.setIcon(frames.get(frameIndex));
        }
    }

    private void explode() {
        this.setLive(false); // 标记自己为死亡，以便被移除
        ElementManager em = ElementManager.getManager();
        final int TILE_SIZE = GameJFrame.TILE_SIZE; // 使用全局常量

        // 创建爆炸效果
        // 1. 中心
        String centerStr = getX() + "," + getY() + ",center";
        em.addElement(GameLoad.getObj("explosion").createElement(centerStr), GameElement.EXPLOSION);

        // 2. 向四个方向延伸
        for (int i = 1; i <= power; i++) {
            // 向上
            createExplosionPart(getX(), getY() - i * TILE_SIZE, i == power ? "up_end" : "v");
            // 向下
            createExplosionPart(getX(), getY() + i * TILE_SIZE, i == power ? "down_end" : "v");
            // 向左
            createExplosionPart(getX() - i * TILE_SIZE, getY(), i == power ? "left_end" : "h");
            // 向右
            createExplosionPart(getX() + i * TILE_SIZE, getY(), i == power ? "right_end" : "h");
        }
    }

    private void createExplosionPart(int x, int y, String type) {
        // 在这里可以添加逻辑，比如检查该位置是否是不可摧毁的墙
        String str = x + "," + y + "," + type;
        ElementManager.getManager().addElement(
                GameLoad.getObj("explosion").createElement(str), GameElement.EXPLOSION);
    }
}