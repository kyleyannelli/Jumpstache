package com.kyleyannelli.finalproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class EvilLaserGuy {
    private TextureRegion currentTexture;
    private final float[] pos;
    private float timer;
    private Array<EvilBullet> bullets;

    public EvilLaserGuy(float x, float y) {
        pos = new float[]{x, y};
        initializeSprites();
        bullets = new Array<EvilBullet>();
    }

    public void update(float delta, GuyRevamp revamp, MapObjects collisionObjects) {
        //update delta timer
        timer += delta;
        if(revamp.pos().y > 1200) if (timer > 5f) {
            //as maxSpeed is set to jump speed while holding space, prevent prediction during this time
            boolean spaceIsHeld = false;
            if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) spaceIsHeld = ! spaceIsHeld;
            float predictedRotationToPlayer = 0;
            // % of speed player must be going for proper prediction
            if(!spaceIsHeld && revamp.getGravitySpeed() >= revamp.getGravityMaxSpeed() * 0.10f) {
                predictedRotationToPlayer = getProjectedRotation(revamp.pos(), revamp.getGravitySpeed(), revamp.getGravityMaxSpeed());
                predictedRotationToPlayer -= 90f;
            }
            else if(!spaceIsHeld && revamp.getJumpSpeed() >= revamp.getMaxJumpSpeed() * 0.10f) {
                predictedRotationToPlayer = getProjectedRotation(revamp.pos(), revamp.getJumpSpeed(), revamp.getMaxJumpSpeed());
                predictedRotationToPlayer -= 90f;
            }
            else {
                predictedRotationToPlayer = (float) (Math.atan2(revamp.pos().y - pos[1], revamp.pos().x - pos[0]) * 180 / Math.PI);
            }
            float x = pos[0] + currentTexture.getRegionWidth() / 2;
            float y = pos[1] + currentTexture.getRegionHeight() / 2;
            spawnBullet(x, y, predictedRotationToPlayer);
            timer = 0;
        }
        //would use delta time if i didnt care for lifespan. roughly 5 seconds.
        for(int i = 0; i < bullets.size; i++)
            if (bullets.get(i).lifespan - System.currentTimeMillis() > 5) bullets.removeIndex(i);
    }

    //from my asteroids proj 1
    private void spawnBullet(float x, float y, float rotation) {
        float width = currentTexture.getRegionWidth();
        float height = currentTexture.getRegionHeight();
        // square polygon
        Polygon bullet = new Polygon(new float[]{
                //bottom left
                0, 0,
                //bottom right
                width, 0,
                //top right
                width, height,
                //top left
                0, height
        });
        bullet.setPosition(x, y);
        bullet.setOrigin(width / 2, height / 2);
        bullet.setRotation(rotation);
        bullets.add(new EvilBullet(bullet, rotation));
    }

    //originally from my project 4
    public float[] getProjectPosition(Vector2 target, float playerSpeed, float targetMaxSpeed) {
        Vector2 positionTemp = new Vector2(pos[0] + currentTexture.getRegionWidth(), pos[1] + currentTexture.getRegionHeight() / 2);
        Vector2 distance = positionTemp.sub(target);
        int T = (int) (distance.len() / targetMaxSpeed);
        //newPos = player position + player velocity * T
        float projectedX = target.x + playerSpeed * T;
        float projectedY = target.y + playerSpeed * T;
        return new float[] {projectedX, projectedY};
    }
    //originally from my project 4
    public float getProjectedRotation(Vector2 target, float targetSpeed, float targetMaxSpeed) {
        float[] projectedXY = getProjectPosition(target, targetSpeed, targetMaxSpeed);
        return (float) (Math.atan2(projectedXY[1] - pos[0], projectedXY[0] - pos[1]) * 180 / Math.PI);
    }

    private void initializeSprites() {
        currentTexture = new TextureRegion(new Texture("character/characterEvilLeft.png"));
    }

    public float[] pos() { return pos; }

    public TextureRegion currentSprite() { return currentTexture; }

    public Array<EvilBullet> getBullets() { return bullets; }
}
