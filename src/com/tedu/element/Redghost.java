package com.tedu.element;

public class Redghost extends BoxObj {
    public Redghost() {
        this.name = "box5"; // 对应原box5
    }

    @Override
    protected String[] getAnimationPaths() {
        return new String[]{
                "bin/image/Characters/redghost1.jpg",
                "bin/image/Characters/redghost2.jpg",
                "bin/image/Characters/redghost3.jpg",
                "bin/image/Characters/redghost4.jpg"
        };
    }

    @Override
    protected void grantAbility() {
        Play triggerPlayer = getPlayerInRange();
        if (triggerPlayer == null) return;

        Play otherPlayer = getOtherPlayer(triggerPlayer);
        if (otherPlayer != null) {
            otherPlayer.setReverseWalking(true);
            isAbilityGranted = true;
            System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "触发Redghost，玩家" + otherPlayer.getPlayerNumber() + "获得反向");
        }
    }
}