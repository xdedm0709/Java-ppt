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
/**
 * @说明 游戏的说明面板（视图层）。
 * @功能说明 两个说明面板
 * @author xdedm0709
 */
public class InfoPanel extends JPanel {

    private String backgroundImageKey; // 要显示的背景图的Key
    private Rectangle backButton = new Rectangle(20, 20, 100, 40);
    private GameJFrame gameFrame;

    public InfoPanel(GameJFrame frame, String imageKey) {
        this.gameFrame = frame;
        this.backgroundImageKey = imageKey;
        this.setLayout(null);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (backButton.contains(e.getPoint())) {
                    // 命令窗体返回到开始菜单
                    gameFrame.switchToStartMenu();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 绘制背景
        g.drawImage(GameLoad.imgMap.get(backgroundImageKey).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);

        // 绘制返回按钮
        g.setColor(Color.CYAN);
        g.setFont(new Font("宋体", Font.BOLD, 24));
        g.drawRect(backButton.x, backButton.y, backButton.width, backButton.height);
        g.drawString("返回", backButton.x + 25, backButton.y + 30);
    }
}