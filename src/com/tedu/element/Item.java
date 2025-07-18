package com.tedu.element;

import com.tedu.element.ElementObj;
import com.tedu.element.Play;
import com.tedu.manager.GameLoad;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.util.List;

/**
 * @说明 所有道具的抽象基类。
 */
public abstract class Item extends ElementObj {

    // 动画属性
    protected List<ImageIcon> frames;
    private int frameIndex = 0;
    private int frameDelay = 0;
    private int animationSpeed = 10;

    public Item() {}

    // 通用的 createElement 方法
    /**
     * @param str 坐标字符串 "x,y"
     * @param animationKey 在 GameLoad.imgMaps 中该道具动画的键名
     * @return 返回初始化完成的道具对象
     */
    protected ElementObj createElement(String str, String animationKey) {
        String[] arr = str.split(",");
        this.setX(Integer.parseInt(arr[0]));
        this.setY(Integer.parseInt(arr[1]));

        this.frames = GameLoad.imgMaps.get(animationKey);

        if (this.frames != null && !this.frames.isEmpty()) {
            ImageIcon firstFrame = this.frames.get(0);
            this.setW(firstFrame.getIconWidth());
            this.setH(firstFrame.getIconHeight());
            this.setIcon(firstFrame);
        } else {
            System.err.println("错误：未能加载动画帧: " + animationKey);
            this.setLive(false);
        }
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        if (isLive() && this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }

    @Override
    protected void updateImage() {
        if (this.frames == null || this.frames.isEmpty()) return;
        frameDelay++;
        if (frameDelay > animationSpeed) {
            frameDelay = 0;
            frameIndex = (frameIndex + 1) % this.frames.size();
            this.setIcon(this.frames.get(frameIndex));
        }
    }

    // 道具是静态的，通常不需要移动或行动
    @Override
    protected void move() {}
    @Override
    protected void action(long gameTime) {}

    /**
     * @说明 抽象方法，定义道具的具体效果。
     */
    public abstract void grantAbilityTo(Play player);
}