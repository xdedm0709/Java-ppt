package com.tedu.element;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.show.GameJFrame;

public class Bubble extends ElementObj {
    private boolean isWaterTrap = false;//用于水雷那里判断是普通水珠还是水雷水珠
    // 核心属性
    private int power;          // 爆炸威力
    private String ownerId;     // 泡泡的所有者ID

    // 动画与计时属性
    private int frameIndex = 0;
    private int frameDelay = 0;
    private int animationSpeed = 8;
    private long creationTime;
    private long fuseTime = 2000; // 默认2秒后爆炸

    public Bubble() {}

    @Override
    public ElementObj createElement(String str) {
        // 新的、统一的创建字符串格式: "x,y,range,ownerId"
        String[] arr = str.split(",");

        // 1. 解析坐标、威力和所有者
        int x = Integer.parseInt(arr[0]);
        int y = Integer.parseInt(arr[1]);
        this.power = Integer.parseInt(arr[2]);
        this.ownerId = arr[3];

        // 2. 设置泡泡的尺寸和对齐后的位置
        final int TILE_SIZE = GameJFrame.TILE_SIZE;
        final int BUBBLE_SIZE = 30; // 假设泡泡尺寸也是30
        int offsetX = (TILE_SIZE - BUBBLE_SIZE) / 2;
        int offsetY = (TILE_SIZE - BUBBLE_SIZE) / 2;

        this.setX(x + offsetX);
        this.setY(y + offsetY);
        this.setW(BUBBLE_SIZE);
        this.setH(BUBBLE_SIZE);

        // 3. 设置动画
        List<ImageIcon> frames = GameLoad.imgMaps.get("bubble");
        if (frames != null && !frames.isEmpty()) {
            this.setIcon(frames.get(0));
        } else {
            System.err.println("错误：未能加载 'bubble' 动画帧。");
        }

        // 4. 记录创建时间
        this.creationTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
    }

    // move() 方法现在是模板方法的一部分，负责状态更新
    @Override
    protected void move() {
        // 检查是否到了爆炸时间
        if (System.currentTimeMillis() - creationTime > fuseTime) {
            this.explode();
        }
    }

    @Override
    protected void updateImage() {
        frameDelay++;
        if (frameDelay > animationSpeed) {
            frameDelay = 0;
            frameIndex++;
            List<ImageIcon> frames = GameLoad.imgMaps.get("bubble");
            if (frames != null && !frames.isEmpty()) {
                if (frameIndex >= frames.size()) {
                    frameIndex = 0;
                }
                this.setIcon(frames.get(frameIndex));
            }
        }
    }

    private void explode() {
        this.setLive(false); // 标记自己死亡，以便主循环移除

        final int TILE_SIZE = GameJFrame.TILE_SIZE;

        // 1. 创建中心爆炸效果，并正确传递威力
        createExplosionPart(this.getX(), this.getY(), "center", this.power);

        // 2. 向四个方向延伸
        boolean continueUp = true, continueDown = true, continueLeft = true, continueRight = true;
        for (int i = 1; i <= this.power; i++) {
            String partType;

            // 向上
            if (continueUp) {
                partType = (i == this.power) ? "up_end" : "v";
                continueUp = createExplosionPart(getX(), getY() - i * TILE_SIZE, partType, this.power);
            }

            // 向下
            if (continueDown) {
                partType = (i == this.power) ? "down_end" : "v";
                continueDown = createExplosionPart(getX(), getY() + i * TILE_SIZE, partType, this.power);
            }

            // 向左
            if (continueLeft) {
                partType = (i == this.power) ? "left_end" : "h";
                continueLeft = createExplosionPart(getX() - i * TILE_SIZE, getY(), partType, this.power);
            }

            // 向右
            if (continueRight) {
                partType = (i == this.power) ? "right_end" : "h";
                continueRight = createExplosionPart(getX() + i * TILE_SIZE, getY(), partType, this.power);
            }
        }
    }


    /**
     * @说明 创建一个爆炸部件，并检查碰撞。现在会传递威力值。
     * @param x 目标x坐标
     * @param y 目标y坐标
     * @param type 爆炸部件类型 ("center", "v", "h", etc.)
     * @param power 本次爆炸的威力
     * @return boolean 返回 true 如果可以继续延伸, 返回 false 如果被阻挡
     */
    private boolean createExplosionPart(int x, int y, String type, int power) {
        ElementManager em = ElementManager.getManager();
        // 使用一个更小的矩形进行碰撞检测，可以防止误触相邻格子的墙
        Rectangle targetRect = new Rectangle(x + 5, y + 5, getW() - 10, getH() - 10);

        // 检查这个位置是否与任何地图方块重叠
        for (ElementObj wall : em.getElementsByKey(GameElement.MAPS)) {
            if (!wall.isLive()) continue;

            if (wall.getRectangle().intersects(targetRect)) {
                // 【修正】根据墙体类型决定行为
                if (wall instanceof Brick) { // 如果是可摧毁的砖墙或草地
                    wall.die(); // 摧毁它
                }
                // 假设 MapObj 是不可摧毁的铁墙等
                else if (wall instanceof MapObj) {
                    return false; // 遇到不可摧毁的墙，直接停止延伸
                }
                // 假设 Box 也是可摧毁的
                else if (wall instanceof Box) {
                    wall.die();
                }
                // 无论摧毁了什么，爆炸都到此为止
                return false;
            }
        }

        // 如果目标位置没有墙，正常创建爆炸部件
        // 将威力值追加到创建字符串中
        String creationStr = x + "," + y + "," + type + "," + power;
        em.addElement(GameLoad.getObj("explosion").createElement(creationStr), GameElement.EXPLOSION);
        return true; // 可以继续延伸
    }

    // 公共方法，让外部可以查询泡泡的所有者
    public String getOwnerId() {
        return this.ownerId;
    }
}