package com.tedu.element;

import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

public class Bubble extends ElementObj {

    private int frameIndex = 0;
    private int frameDelay = 0;
    private int animationSpeed = 8;
    private long creationTime;
    private long fuseTime = 2000; // 泡泡持续3秒后爆炸
    private int power = 2; // 爆炸威力（向外延伸2格）
    private static final int TILE_SIZE = 40; // 假设每个格子的尺寸是40x40

    public Bubble() {}

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        // 坐标需要对齐到格子
        int rawX = Integer.parseInt(arr[0]);
        int rawY = Integer.parseInt(arr[1]);
        this.setX( (rawX + TILE_SIZE / 2) / TILE_SIZE * TILE_SIZE );
        this.setY( (rawY + TILE_SIZE / 2) / TILE_SIZE * TILE_SIZE );

        List<ImageIcon> frames = GameLoad.imgMaps.get("bubble");
        this.setW(frames.get(0).getIconWidth());
        this.setH(frames.get(0).getIconHeight());
        this.setIcon(frames.get(0));

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