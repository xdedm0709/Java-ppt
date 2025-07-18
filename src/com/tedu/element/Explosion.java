package com.tedu.element;

import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;
import com.tedu.show.GameJFrame;

/**
 * @说明 爆炸效果（水柱）。它有自己的动画，并在播放完毕后消失。
 * @author xdedm0709
 */
public class Explosion extends ElementObj {
    private int power; // 爆炸的威力

    // 动画属性
    private int frameIndex = 0;
    private int frameDelay = 0;
    private int animationSpeed = 4; // 动画播放速度，数字越小越快
    private List<ImageIcon> frames; // 【核心】持有自己的动画帧列表，由 GameLoad 提供

    // 生命周期属性
    private long creationTime;
    private long duration = 400; // 整个爆炸效果的持续时间（毫秒）

    public Explosion() {}

    @Override
    public ElementObj createElement(String str) {
        // 新的创建字符串格式: "x,y,type,power"
        // 例如: "40,80,center,2"
        String[] arr = str.split(",");

        // 基本参数解析
        this.setX(Integer.parseInt(arr[0]));
        this.setY(Integer.parseInt(arr[1]));
        String type = arr[2];

        // 解析并存储威力值
        if (arr.length > 3) {
            this.power = Integer.parseInt(arr[3]);
        } else {
            this.power = 1; // 如果没有提供威力值，给一个默认值 1
        }

        // 1. 根据类型从 GameLoad 获取正确的动画帧列表
        String animationKey = "explosion_" + type;
        this.frames = GameLoad.imgMaps.get(animationKey);

        if (this.frames == null || this.frames.isEmpty()) {
            System.err.println("创建爆炸效果失败！无法加载键为 '" + animationKey + "' 的动画。");
            this.setLive(false);
            return this;
        }

        // 2. 设置初始状态
        ImageIcon firstFrame = this.frames.get(0);
        this.setIcon(firstFrame);
        // 爆炸效果的尺寸与格子大小保持一致
        this.setW(GameJFrame.TILE_SIZE);
        this.setH(GameJFrame.TILE_SIZE);

        this.creationTime = System.currentTimeMillis();
        return this;
    }

    public int getPower() {
        return this.power;
    }

    @Override
    public void showElement(Graphics g) {
        if (isLive() && this.getIcon() != null) {
            g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }

    // move() 方法负责状态更新
    @Override
    protected void move() {
        // 检查是否到了消失时间
        if (System.currentTimeMillis() - creationTime > duration) {
            this.setLive(false); // 标记自己为死亡
        }
    }

    // updateImage() 方法负责动画帧的更新
    @Override
    protected void updateImage() {
        if (!isLive() || this.frames == null) {
            return;
        }

        frameDelay++;
        if (frameDelay > animationSpeed) {
            frameDelay = 0;
            frameIndex++;
        }

        // 动画只播放一次，播放完毕后停在最后一帧，直到对象消失
        if (frameIndex >= this.frames.size()) {
            frameIndex = this.frames.size() - 1;
        }

        this.setIcon(this.frames.get(frameIndex));
    }
}