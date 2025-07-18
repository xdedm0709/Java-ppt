package com.tedu.element;

public class Purpleghost extends BoxObj {
    public Purpleghost() {
        this.name = "box4"; // 对应原box4
    }

    @Override
    protected String[] getAnimationPaths() {
        return new String[]{
                "bin/image/Characters/ghost1.jpg",
                "bin/image/Characters/ghost2.jpg",
                "bin/image/Characters/ghost3.jpg",
                "bin/image/Characters/ghost4.jpg"
        };
    }

    @Override
    protected void grantAbility() {
        Play triggerPlayer = getPlayerInRange();
        if (triggerPlayer != null) {
            triggerPlayer.setReverseWalking(true); // 开启反向
            isAbilityGranted = true;
            System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "获得反向行走（Purpleghost）");
        }
    }
}