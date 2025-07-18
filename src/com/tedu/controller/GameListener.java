package com.tedu.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * @说明 监听类，用于监听用户的操作 KeyListener
 * @author renjj
 *
 */
public class GameListener implements KeyListener{
	private ElementManager em = ElementManager.getManager();

	// 使用一个线程安全的 Set 来记录当前被按下的键
	private final Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());

	public GameListener() {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		// 只有当这个键是“第一次”被按下时，才处理它
		if (pressedKeys.add(keyCode)) { // .add() 方法在添加成功时返回 true
			List<ElementObj> playList = em.getElementsByKey(GameElement.PLAY);
			for (ElementObj player : playList) {
				player.keyClick(true, keyCode);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();

		// 当键被松开时，将它从 Set 中移除
		if (pressedKeys.remove(keyCode)) { // .remove() 方法在移除成功时返回 true
			List<ElementObj> playList = em.getElementsByKey(GameElement.PLAY);
			for (ElementObj player : playList) {
				player.keyClick(false, keyCode);
			}
		}
	}
}
