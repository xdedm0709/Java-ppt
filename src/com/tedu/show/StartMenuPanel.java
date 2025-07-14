package com.tedu.show;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import com.tedu.manager.GameLoad;

public class StartMenuPanel extends JPanel {

    // 按钮区域
    private Rectangle singlePlayerButton = new Rectangle(300, 320, 200, 50);
    private Rectangle instructionsButton = new Rectangle(300, 390, 200, 50);
    private Rectangle itemInfoButton = new Rectangle(300, 460, 200, 50);

    // 持有对主窗体的引用，以便可以命令它切换面板
    private GameJFrame gameFrame;

    public StartMenuPanel(GameJFrame frame) {
        this.gameFrame = frame;
        this.setLayout(null); // 我们手动绘制，不需要布局管理器

        // 添加鼠标监听
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        });
    }

    // 处理鼠标点击逻辑
    private void handleMouseClick(Point p) {
        if (singlePlayerButton.contains(p)) {
            // 命令窗体切换到游戏面板
            gameFrame.switchToGamePanel();
        } else if (instructionsButton.contains(p)) {
            // 命令窗体切换到游戏说明面板
            gameFrame.switchToInstructionsPanel();
        } else if (itemInfoButton.contains(p)) {
            // 命令窗体切换到道具说明面板
            gameFrame.switchToItemInfoPanel();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 绘制背景图
        g.drawImage(GameLoad.imgMap.get("start_screen").getImage(), 0, 0, this.getWidth(), this.getHeight(), null);

        // 绘制按钮
        g.setColor(Color.ORANGE);
        g.setFont(new Font("宋体", Font.BOLD, 32));

        g.drawRect(singlePlayerButton.x, singlePlayerButton.y, singlePlayerButton.width, singlePlayerButton.height);
        g.drawString("开始游戏", singlePlayerButton.x + 35, singlePlayerButton.y + 38);

        g.drawRect(instructionsButton.x, instructionsButton.y, instructionsButton.width, instructionsButton.height);
        g.drawString("游戏说明", instructionsButton.x + 35, instructionsButton.y + 38);

        g.drawRect(itemInfoButton.x, itemInfoButton.y, itemInfoButton.width, itemInfoButton.height);
        g.drawString("道具说明", itemInfoButton.x + 35, itemInfoButton.y + 38);
    }
}