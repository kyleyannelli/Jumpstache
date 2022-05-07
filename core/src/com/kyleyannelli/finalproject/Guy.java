package com.kyleyannelli.finalproject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Guy {
    private final float MAX_JUMP_VELOCITY = 20f;
    public boolean jumpCancel;
    private TextureRegion currentSprite, neutral, crouch;
    private Animation<TextureRegion> walkingLeft, walkingRight;
    public float deltaJumpTimer, frameTime, jumpTime, jumpTimeTwo, maxJumpTime = .76f;
    public boolean jumping, left, neu;
    private Rectangle rect;
    private Vector2 pos;
    public float maxSpeed = 200, speed = 50, acceleration = maxSpeed/.6f,
                    maxJumpSpeed = 600f, jumpSpeed = maxJumpSpeed, jumpAcceleration = 225f,
                    gravityAccel = 300, gravitySpeed = 0, gravityMaxSpeed = 2000f;
    private boolean accelerating;

    public Guy(float x, float y) {
        this.neutral = new TextureRegion(new Texture("character/characterNeutral.png"));
        this.crouch = new TextureRegion(new Texture("character/characterCrouch.png"));
        TextureRegion[] walkingLeft = new TextureRegion[4];
        walkingLeft[0] = new TextureRegion(new Texture("character/walking/walkLeft2.png"));
        walkingLeft[1] = new TextureRegion(new Texture("character/walking/walkLeft3.png"));
        walkingLeft[2] = new TextureRegion(new Texture("character/walking/walkLeft4.png"));
        walkingLeft[3] = new TextureRegion(new Texture("character/walking/walkLeft3.png"));
        this.walkingRight = new Animation<TextureRegion>(.2f, walkingLeft);
        TextureRegion[] walkingRight = new TextureRegion[4];
        walkingRight[0] = new TextureRegion(new Texture("character/walking/walkRight2.png"));
        walkingRight[1] = new TextureRegion(new Texture("character/walking/walkRight3.png"));
        walkingRight[2] = new TextureRegion(new Texture("character/walking/walkRight4.png"));
        walkingRight[3] = new TextureRegion(new Texture("character/walking/walkRight3.png"));
        this.walkingLeft = new Animation<TextureRegion>(.2f, walkingRight);
        this.currentSprite = neutral;
        this.pos = new Vector2(x, y);
        this.pos = pos;
        this.rect = new Rectangle(x, y, neutral.getRegionWidth(), neutral.getRegionHeight());
//        this.groundLevel = y;
    }

    public Guy(float x, float y, String neutralPath) {
        this.neutral = new TextureRegion(new Texture(neutralPath));
        this.crouch = new TextureRegion(new Texture("character/characterCrouch.png"));
        TextureRegion[] walkingLeft = new TextureRegion[4];
        walkingLeft[0] = new TextureRegion(new Texture("character/walking/walkLeft2.png"));
        walkingLeft[1] = new TextureRegion(new Texture("character/walking/walkLeft3.png"));
        walkingLeft[2] = new TextureRegion(new Texture("character/walking/walkLeft4.png"));
        walkingLeft[3] = new TextureRegion(new Texture("character/walking/walkLeft3.png"));
        this.walkingRight = new Animation<TextureRegion>(.2f, walkingLeft);
        TextureRegion[] walkingRight = new TextureRegion[4];
        walkingRight[0] = new TextureRegion(new Texture("character/walking/walkRight2.png"));
        walkingRight[1] = new TextureRegion(new Texture("character/walking/walkRight3.png"));
        walkingRight[2] = new TextureRegion(new Texture("character/walking/walkRight4.png"));
        walkingRight[3] = new TextureRegion(new Texture("character/walking/walkRight3.png"));
        this.walkingLeft = new Animation<TextureRegion>(.2f, walkingRight);
        this.currentSprite = neutral;
        this.pos = new Vector2(x, y);
        this.pos = pos;
        this.rect = new Rectangle(x, y, neutral.getRegionWidth(), neutral.getRegionHeight());
//        this.groundLevel = y;
    }

    public void update(float delta) {
        rect.height = currentSprite.getRegionHeight();
        rect.width = currentSprite.getRegionWidth();
        rect.x = pos.x;
        rect.y = pos.y;
        accelerating = false;
    }

    public float requestedWalkPos(boolean left, float delta) {
        if(left) {
            return pos.x + (float) Math.cos(Math.toRadians(180)) * (speed * delta);
        }
        else {
            return pos.x + (float) Math.cos(Math.toRadians(0)) * (speed * delta);
        }
    }

    public void walk(boolean left, float delta) {
        if(jumping) {
            if(left) {
                currentSprite = walkingLeft.getKeyFrame(frameTime, true);
                pos.x += (float) Math.cos(Math.toRadians(180)) * (maxSpeed * delta);
            }
            else {
                currentSprite = walkingRight.getKeyFrame(frameTime, true);
                pos.x += (float) Math.cos(Math.toRadians(0)) * (maxSpeed * delta);
            }
        }
        else {
            accelerate(delta);
            if(left) {
                currentSprite = walkingLeft.getKeyFrame(frameTime, true);
                pos.x += (float) Math.cos(Math.toRadians(180)) * (speed * delta);
            }
            else {
                currentSprite = walkingRight.getKeyFrame(frameTime, true);
                pos.x += (float) Math.cos(Math.toRadians(0)) * (speed * delta);
            }
        }
    }

    public void jump(float delta) {
            if(!jumpCancel) {
                jumpAcceleration = maxJumpSpeed / jumpTimeTwo;
                if((jumpSpeed - jumpAcceleration * delta) > 1f)  jumpSpeed -= jumpAcceleration * delta;
                pos.y += (float) Math.cos(Math.toRadians(0)) * ((jumpSpeed * 3)* delta);
            }
    }

    public void applyGravity(float delta) {
        if((gravitySpeed + gravityAccel * delta) < gravityMaxSpeed)  gravitySpeed += gravityAccel * delta;
        pos.y -= (float) Math.cos(Math.toRadians(0)) * ((gravitySpeed) * delta);
    }

    private void accelerate(float delta) {
        accelerating = true;
        if((speed + acceleration * delta) < maxSpeed)  speed += acceleration * delta;
    }

    public void decelerate(float delta) {
        if(!accelerating) {
            //decrease speed
            if((speed - (acceleration * 1) * delta) > 15.0f)  speed -= (acceleration * 1) * delta;
                //account for case where float speed still holds a value
            else speed = 0f;
            if(left) {
                if(speed != 0) walkAnimation(delta);
                pos.x += (float) Math.cos(Math.toRadians(180)) * ((speed) * delta);
            }
            else {
                if(speed != 0) walkAnimation(delta);
                pos.x += (float) Math.cos(Math.toRadians(0)) * ((speed) * delta);
            }
        }
    }

    //uses global left to determine which animation to apply to current sprite
    private void walkAnimation(float delta) {
        if(left) {
            //step through walking frame
            frameTime += delta;
            //base frame duration on current speed.
            walkingLeft.setFrameDuration((maxSpeed / .7f) / speed);
            currentSprite = walkingLeft.getKeyFrame(frameTime, true);
        }
        else {
            //step through walking frame
            frameTime += delta;
            //base frame duration on current speed.
            walkingRight.setFrameDuration((maxSpeed / .7f) / speed);
            currentSprite = walkingRight.getKeyFrame(frameTime, true);
        }
    }

    public TextureRegion currentSprite() { return currentSprite; }

    public Vector2 pos() { return pos; }

    public Rectangle rect() { return rect; }

    public void setNeutral() { currentSprite = neutral; }

    public void jumpSprite() { currentSprite = crouch; }

    public void adjustPos(Vector2 vector2, float delta) {
        vector2 = Vector.normalize2d(vector2);
        pos.scl(vector2.scl(delta));
    }

    public void setPos(Vector2 vector2) {
        pos = vector2;
    }
}
