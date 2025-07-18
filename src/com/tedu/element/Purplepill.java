package com.tedu.element;

public class Purplepill extends BoxObj {
    public Purplepill() {
        this.name = "box3"; // 对应原box3
    }

    @Override
    protected String[] getAnimationPaths() {
        return new String[]{
                "bin/image/Characters/jieyao1.jpg",
                "bin/image/Characters/jieyao2.jpg",
                "bin/image/Characters/jieyao3.jpg",
                "bin/image/Characters/jieyao4.jpg"
        };
    }

    @Override
    protected void grantAbility() {
        Play triggerPlayer = getPlayerInRange();
        if (triggerPlayer != null) {
            triggerPlayer.setReverseWalking(false); // 解除反向
            isAbilityGranted = true;
            System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "解除反向行走（Purplepill）");
        }
    }
}