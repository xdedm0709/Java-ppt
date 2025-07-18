package com.tedu.element;

public class Remotecontrol extends BoxObj {
    public Remotecontrol() {
        this.name = "box8"; // 对应原box8
    }

    @Override
    protected String[] getAnimationPaths() {
        return new String[]{
                "bin/image/Characters/remotecontrol1.jpg",
                "bin/image/Characters/remotecontrol2.jpg",
                "bin/image/Characters/remotecontrol3.jpg",
                "bin/image/Characters/remotecontrol4.jpg"
        };
    }

    @Override
    protected void grantAbility() {
        Play triggerPlayer = getPlayerInRange();
        if (triggerPlayer == null) return;

        Play targetPlayer = getOtherPlayer(triggerPlayer);
        if (targetPlayer != null) {
            // 暂停另一名玩家50帧
            targetPlayer.pauseForFrames(50);
            isAbilityGranted = true;
            System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "触发Diamondcard，玩家" + targetPlayer.getPlayerNumber() + "暂停50帧");
        }
    }
}