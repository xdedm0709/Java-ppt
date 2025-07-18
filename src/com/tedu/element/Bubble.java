package com.tedu.element;

import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

public class Bubble extends ElementObj {
    private boolean isWaterTrap = false;//用于水雷那里判断是普通水珠还是水雷水珠
    private int frameIndex = 0;
    private int frameDelay = 0;
    private int animationSpeed = 8;
    private long creationTime;
    private long fuseTime = 2000; // 泡泡持续2秒后爆炸
    private int power; // 爆炸范围
    private static final int DEFAULT_POWER = 2; // 默认爆炸范围
    private static final int TILE_SIZE = 40;
    private Image explosionImage;
    private boolean isExploding = false;
    private long explosionStartTime;
    private long explosionShowTime = 500;

    public Bubble() {
        // 加载爆炸效果图片
        try {
            explosionImage = new ImageIcon("bin/bubble1.jpg").getImage();
        } catch (Exception e) {
            System.err.println("加载爆炸图片失败: " + e.getMessage());
            explosionImage = null;
        }
    }


    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        if (arr.length < 2) {
            System.err.println("泡泡参数错误：" + str);
            return null;
        }
        try {
            int x = Integer.parseInt(arr[0]);
            int y = Integer.parseInt(arr[1])+40;
            this.power = arr.length >= 3 ? Integer.parseInt(arr[2]) : 1;
            this.fuseTime = arr.length >= 4 ? Long.parseLong(arr[3]) : 2000;
            // 第5个参数标记是否为水珠包围泡泡（由box6生成时传入，也和box7超人卡防御有关系）
            this.isWaterTrap = arr.length >= 5 && "water_trap".equals(arr[4]);

            this.setX(x);
            this.setY(y);
            this.setW(TILE_SIZE);
            this.setH(TILE_SIZE);
            this.setIcon(new ImageIcon("bin/bubble1.jpg")); // 水珠泡泡图标
            this.creationTime = System.currentTimeMillis();

            // 若为水珠包围泡泡，立即检测目标玩家是否有抵抗能力
            if (isWaterTrap) {
                checkTargetResistAndDisappear();
            }

            return this;
        } catch (Exception e) {
            System.err.println("创建泡泡失败：" + e.getMessage());
            return null;
        }
    }
    private void checkTargetResistAndDisappear() {
        ElementManager em = ElementManager.getManager();
        List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
        if (players == null) return;

        // 水珠包围泡泡的中心坐标
        int bubbleCenterX = getX() + getW() / 2;
        int bubbleCenterY = getY() + getH() / 2;

        // 找到被包围的玩家（水珠周围的玩家）
        for (ElementObj player : players) {
            if (player instanceof Play) {
                Play targetPlayer = (Play) player;
                // 计算玩家与水珠的距离（判断是否为被包围的目标）
                int playerCenterX = targetPlayer.getX() + targetPlayer.getW() / 2;
                int playerCenterY = targetPlayer.getY() + targetPlayer.getH() / 2;
                double distance = Math.sqrt(
                        Math.pow(bubbleCenterX - playerCenterX, 2) +
                                Math.pow(bubbleCenterY - playerCenterY, 2)
                );

                // 若玩家在水珠周围且有抵抗能力
                if (distance <= 500 && targetPlayer.hasResistWaterTrap()) {
                    // 水珠直接消失（不爆炸）
                    this.setLive(false);
                    // 消耗玩家的抵抗能力（一次性）
                    targetPlayer.consumeResist();
                    System.out.println("玩家" + targetPlayer.getPlayerNumber() + "触发抵抗，水珠消失！");
                    break;
                }
            }
        }
    }

    @Override
    public void showElement(Graphics g) {
        if (isLive()) {
            // 未爆炸时显示水球动画
            g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        } else if (isExploding) {
            // 爆炸时显示爆炸效果
            if (explosionImage != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - explosionStartTime <= explosionShowTime) {
                    g.drawImage(explosionImage, getX(), getY(), getW(), getH(), null);
                } else {
                    isExploding = false;
                }
            } else {
                isExploding = false;
            }
        }
    }

    @Override
    protected void move() {
        long gameTime = System.currentTimeMillis();

        if (isLive()) {
            // 未爆炸时更新水球动画
            updateAnimation();
            // 检查是否到爆炸时间
            if (gameTime - creationTime > fuseTime) {
                this.explode();
            }
        } else if (isExploding) {
        }
    }

    private void updateAnimation() {
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
        this.setLive(false); // 标记水球死亡
        this.isExploding = true; // 标记开始爆炸
        this.explosionStartTime = System.currentTimeMillis(); // 记录爆炸开始时间

        ElementManager em = ElementManager.getManager();

        // 创建中心爆炸效果
        String centerStr = getX() + "," + getY() + ",center";
        em.addElement(GameLoad.getObj("explosion").createElement(centerStr), GameElement.EXPLOSION);

        // 创建四个方向的爆炸效果
        for (int i = 1; i <= power; i++) {
            createExplosionPart(getX(), getY() - i * TILE_SIZE, i == power ? "up_end" : "v");
            createExplosionPart(getX(), getY() + i * TILE_SIZE, i == power ? "down_end" : "v");
            createExplosionPart(getX() - i * TILE_SIZE, getY(), i == power ? "left_end" : "h");
            createExplosionPart(getX() + i * TILE_SIZE, getY(), i == power ? "right_end" : "h");
        }
    }

    private void createExplosionPart(int x, int y, String type) {
        String str = x + "," + y + "," + type;
        ElementManager.getManager().addElement(
                GameLoad.getObj("explosion").createElement(str), GameElement.EXPLOSION);
    }
}