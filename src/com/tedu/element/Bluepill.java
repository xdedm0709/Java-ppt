package com.tedu.element;

public class Bluepill extends BoxObj {
    public Bluepill() {
        this.name = "box2";
    }

    @Override
    protected String[] getAnimationPaths() {
        return new String[]{
                "bin/image/Characters/yaoping1.jpg",
                "bin/image/Characters/yaoping2.jpg",
                "bin/image/Characters/yaoping3.jpg",
                "bin/image/Characters/yaoping4.jpg"
        };
    }

    @Override
    protected void grantAbility() {
        Play triggerPlayer = getPlayerInRange();
        if (triggerPlayer != null) {
            //triggerPlayer.increaseBubbleRange(1);
            isAbilityGranted = true;
            System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "泡泡范围+1（Bluepill）");
        }
    }
}