package com.tedu.element;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * @说明 爆炸效果（顺序播放指定的4张爆炸图片）
 */
public class Explosion extends ElementObj {
    //存储用来播放的4张效果
    private List<ImageIcon> explosionFrames = new ArrayList<>();
    private int currentFrameIndex = 0;
    private int frameDelay = 0;
    private int frameInterval = 5;

    // 生命周期控制
    private long creationTime;
    private boolean isAnimationFinished = false;

    public Explosion() {
        // 初始化时按顺序加载4张爆炸图片
        loadExplosionFrames();
    }


    private void loadExplosionFrames() {
        // 清空原有帧列表
        explosionFrames.clear();

        // 按顺序加载4张图片
        String[] imagePaths = {
                "bin/bubble1.jpg",
                "bin/bubble2.jpg",
                "bin/bubble3.jpg",
                "bin/bubble4.jpg"
        };

        for (String path : imagePaths) {
            try {
                ImageIcon frame = new ImageIcon(path);
                if (frame.getIconWidth() > 0 && frame.getIconHeight() > 0) {
                    explosionFrames.add(frame);
                } else {
                    System.err.println("爆炸图片无效（宽高为0）：" + path);
                }
            } catch (Exception e) {
                System.err.println("加载爆炸图片失败：" + path + "，错误：" + e.getMessage());
            }
        }

        // 检查是否加载到有效图片
        if (explosionFrames.isEmpty()) {
            System.err.println("警告：未加载到任何有效爆炸图片！");
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        if (arr.length < 2) {
            System.err.println("爆炸效果参数错误，格式应为[x,y]");
            this.setLive(false);
            return this;
        }
        // 设置爆炸位置
        this.setX(Integer.parseInt(arr[0]));
        this.setY(Integer.parseInt(arr[1]));

        // 设置初始尺寸（使用第一张有效图片的尺寸）
        if (!explosionFrames.isEmpty()) {
            ImageIcon firstFrame = explosionFrames.get(0);
            this.setW(firstFrame.getIconWidth());
            this.setH(firstFrame.getIconHeight());
            this.setIcon(firstFrame);
        } else {
            // 无有效图片时使用默认尺寸
            this.setW(40);
            this.setH(40);
            this.setLive(false);
        }

        creationTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        if (isLive() && !explosionFrames.isEmpty() && currentFrameIndex < explosionFrames.size()) {
            ImageIcon currentFrame = explosionFrames.get(currentFrameIndex);
            g.drawImage(
                    currentFrame.getImage(),
                    getX(), getY(), getW(), getH(),
                    null
            );
        }
    }

    @Override
    protected void move() {
        // 动画播放完毕后标记为死亡
        if (isAnimationFinished) {
            this.setLive(false);
            return;
        }

        updateExplosionFrame();
    }

    /**
     * 更新爆炸动画帧（按顺序播放bubble1到bubble4）
     */
    private void updateExplosionFrame() {
        if (explosionFrames.isEmpty()) {
            isAnimationFinished = true;
            return;
        }

        // 累加延迟计数器
        frameDelay++;

        // 达到间隔时切换到下一帧
        if (frameDelay >= frameInterval) {
            frameDelay = 0; // 重置延迟
            currentFrameIndex++; // 切换到下一帧

            // 检查是否播放到最后一帧
            if (currentFrameIndex >= explosionFrames.size()) {
                isAnimationFinished = true; // 标记动画结束
            }
        }
    }

    @Override
    protected void updateImage() {
    }
}