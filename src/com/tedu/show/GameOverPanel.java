package com.tedu.show;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import com.tedu.manager.GameLoad;

public class GameOverPanel extends JPanel {

    private GameJFrame gameFrame;
    private Image backgroundImage; // 用于存放背景图片的成员变量

    public GameOverPanel(GameJFrame frame) {
        this.gameFrame = frame;
        this.setLayout(null);

        // 直接从 GameLoad 的静态 Map 中获取已加载的图片
        // GameLoad.loadImg() 方法已经将 "game_over_screen" 键和对应的图片存入了 imgMap
        ImageIcon bgIcon = GameLoad.imgMap.get("game_over_screen");

        // 做一个健壮性检查，防止因配置错误或加载失败导致程序崩溃
        if (bgIcon != null) {
            this.backgroundImage = bgIcon.getImage();
        } else {
            System.err.println("错误: 无法从 GameLoad.imgMap 中获取 'game_over_screen' 图片。");
            System.err.println("请检查：1. GameData.pro 是否有此配置。 2. GameLoad.loadAll() 是否已在程序启动时被调用。");
            // 即使图片加载失败，backgroundImage 将为 null，paintComponent 中有备用方案
        }

        // 创建按钮
        JButton restartButton = new JButton("重新开始");
        JButton menuButton = new JButton("返回主菜单");

        // 设置按钮样式和位置
        int btnWidth = 200;
        int btnHeight = 50;
        int centerX = (GameJFrame.GameX - btnWidth) / 2; // 假设 GameJFrame.GameX 是有效的

        restartButton.setBounds(centerX, 250, btnWidth, btnHeight);
        menuButton.setBounds(centerX, 320, btnWidth, btnHeight);

        restartButton.setFont(new Font("宋体", Font.BOLD, 24));
        menuButton.setFont(new Font("宋体", Font.BOLD, 24));

        // 添加按钮到面板
        this.add(restartButton);
        this.add(menuButton);

        // 为按钮添加动作监听
        restartButton.addActionListener(e -> gameFrame.switchToGamePanel());
        menuButton.addActionListener(e -> gameFrame.switchToStartMenu());
    }

    // 重写 paintComponent 方法
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 优先绘制背景图片
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            // 如果图片加载失败，绘制一个黑色的备用背景，以确保界面不是空白
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}