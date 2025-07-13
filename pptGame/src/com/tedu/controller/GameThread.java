package com.tedu.controller;

import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.Enemy;
import com.tedu.element.Play;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

/**
 * @说明 游戏的主线程，用于控制游戏加载，游戏关卡，游戏运行时自动化
 * 		游戏判定；游戏地图切换 资源释放和重新读取。。。
 * @author renjj
 * @继承 使用继承的方式实现多线程(一般建议使用接口实现)
 */
public class GameThread extends Thread{
	private ElementManager em;
	
	public GameThread() {
		em=ElementManager.getManager();
	}
	@Override
	public void run() {//游戏的run方法  主线程
		while(true) { //扩展,可以讲true变为一个变量用于控制结束
//		游戏开始前   读进度条，加载游戏资源(场景资源)
			gameLoad();
//		游戏进行时   游戏过程中
			gameRun();
//		游戏场景结束  游戏资源回收(场景资源)
			gameOver();
			try {
				sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * 游戏的加载
	 */
	private void gameLoad() {
		GameLoad.loadImg(); //加载图片
		GameLoad.MapLoad(5);//可以变为 变量，每一关重新加载  加载地图
//		加载主角
		GameLoad.loadPlay();//也可以带参数，单机还是2人
//		加载敌人NPC等
		
//		全部加载完成，游戏启动
	}
	/**
	 * @说明  游戏进行时
	 * @任务说明  游戏过程中需要做的事情：1.自动化玩家的移动，碰撞，死亡
	 *                                 2.新元素的增加(NPC死亡后出现道具)
	 *                                 3.暂停等等。。。。。
	 * 先实现主角的移动
	 * */
	
	private void gameRun() {
		long gameTime=0L;//给int类型就可以啦
		while(true) {// 预留扩展   true可以变为变量，用于控制管关卡结束等
			Map<GameElement, List<ElementObj>> all = em.getGameElements();
			List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
			List<ElementObj> files = em.getElementsByKey(GameElement.PLAYFILE);
			List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
			moveAndUpdate(all,gameTime);//	游戏元素自动化方法
			
			ElementPK(enemys,files);
			ElementPK(files,maps);
			
			gameTime++;//唯一的时间控制
			try {
				sleep(10);//默认理解为 1秒刷新100次 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void ElementPK(List<ElementObj> listA,List<ElementObj>listB) {
//		请大家在这里使用循环，做一对一判定，如果为真，就设置2个对象的死亡状态
		for(int i=0;i<listA.size();i++) {
			ElementObj enemy=listA.get(i);
			for(int j=0;j<listB.size();j++) {
				ElementObj file=listB.get(j);
				if(enemy.pk(file)) {
//					问题： 如果是boos，那么也一枪一个吗？？？？
//					将 setLive(false) 变为一个受攻击方法，还可以传入另外一个对象的攻击力
//					当收攻击方法里执行时，如果血量减为0 再进行设置生存为 false
//					扩展 留给大家
					System.out.println(listB);
					enemy.setLive(false);
					file.setLive(false);
					break;
				}
			}
		}
	}
	
	
	
	
//	游戏元素自动化方法
	public void moveAndUpdate(Map<GameElement, List<ElementObj>> all,long gameTime) {
//		GameElement.values();//隐藏方法  返回值是一个数组,数组的顺序就是定义枚举的顺序
		for(GameElement ge:GameElement.values()) {
			List<ElementObj> list = all.get(ge);
//			编写这样直接操作集合数据的代码建议不要使用迭代器。
//			for(int i=0;i<list.size();i++) {
			for(int i=list.size()-1;i>=0;i--){	
				ElementObj obj=list.get(i);//读取为基类
				if(!obj.isLive()) {//如果死亡
//					list.remove(i--);  //可以使用这样的方式
//					启动一个死亡方法(方法中可以做事情例如:死亡动画 ,掉装备)
					obj.die();//需要大家自己补充
					list.remove(i);
					continue;
				}
				obj.model(gameTime);//调用的模板方法 不是move
			}
		}	
	}
	

	
	/**游戏切换关卡*/
	private void gameOver() {
		
	}
	
}





