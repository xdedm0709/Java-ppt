package com.tedu.element;

public class Doubleb extends BoxObj {
    public Doubleb() {
        this.name = "box1"; // 对应原box1
    }

    @Override
    protected String[] getAnimationPaths() {
        return new String[]{
                "bin/image/Characters/paopao1.jpg",
                "bin/image/Characters/paopao2.jpg",
                "bin/image/Characters/paopao3.jpg",
                "bin/image/Characters/paopao4.jpg"
        };
    }
    @Override
    protected void grantAbility() {
        Play triggerPlayer = getPlayerInRange();
        if (triggerPlayer != null) {
            triggerPlayer.setCanLaunchDoubleBubble(true);
            isAbilityGranted = true;
            System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "获得双泡泡能力（Doubleb）");
        }
    }
}