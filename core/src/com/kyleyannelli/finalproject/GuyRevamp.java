package com.kyleyannelli.finalproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GuyRevamp {
    //may want to consider changing DECEL_FACTOR to a mutable in the case of an slippery platform, lower decel factor...
    private final int MAX_SPEED = 250, ACCELERATION = 200, DECEL_FACTOR = 6;
    private float FRAME_DURATION_FACTOR = 7f;
    private TextureRegion currentSprite, neutral, crouch;
    private Animation<TextureRegion> walkingLeft, walkingRight;
    public float deltaJumpTimer, frameTime, jumpTime, jumpTimeTwo, maxJumpTime = .76f;
    public boolean jumping, left, neu;
    private boolean accelerating, jumpCancel, onGround, collisionDetected;
    private Rectangle rect;
    private Vector2 pos;
    private float speed, delta,
            gravityAccel = 300, gravitySpeed = 0, gravityMaxSpeed = 2000f,
            maxJumpSpeed = 600f, jumpSpeed = maxJumpSpeed, jumpAcceleration = 225f;

    public GuyRevamp(float x, float y) {
        initializeSprites();
        this.pos = new Vector2(x, y);
        this.rect = new Rectangle(x, y, neutral.getRegionWidth(), neutral.getRegionHeight());
    }

    // constructor clean up
    private void initializeSprites() {
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
    }

    //update method without collision detection. possible use if you want the collision detection to be done in a controller
    public void update(float delta) {
        //position vector is the only modified variable in methods, update sprite and collision box
        rect.height = currentSprite.getRegionHeight();
        rect.width = currentSprite.getRegionWidth();
        rect.x = pos.x;
        rect.y = pos.y;
        //update global delta
        this.delta = delta;
        //set acceleration false, will get set to true if user is moving left or right
        accelerating = false;
        //handle input
        handleInput();
        //if(!acceleration) decelerate
        decelerate();
    }

    //update method with collision detection.
    public void update(float delta, MapObjects collisionObjects) {
        //update global delta
        this.delta = delta;
        //set acceleration false, will get set to true if user is moving left or right
        accelerating = false;
        //gravity is applied in handle collision
        handleCollision(collisionObjects);
        //handle input
        handleInput();
        jump();
        //position vector is the only modified variable in methods, update sprite and collision box
        rect.height = currentSprite.getRegionHeight();
        rect.width = currentSprite.getRegionWidth();
        rect.x = pos.x;
        rect.y = pos.y;
        //if(!acceleration) decelerate
        decelerate();
    }

    //i was having a lot of trouble with collision. the left right top bottom concept is from here http://www.jeffreythompson.org/collision-detection/line-rect.php
    private void handleCollision(MapObjects collisionObjects) {
        collisionDetected = false;
        float[] closest = {99999, 99999};
        for(RectangleMapObject rect : collisionObjects.getByType(RectangleMapObject.class)) {
            boolean one = false, two = false, three = false, four = false;
            Array<RectangleHelper> rects = new Array<>();
            //convert rectmapobj to libgdx rectangle for Intersector use
            Rectangle r = rect.getRectangle();
            //this is kind of clunky but converting the rectangle to lines by using zero height (or width).
            //one starts at bottom left of rectangle, lines go clockwise
            Rectangle rectOne = new Rectangle(r.x, r.y, 0, r.height),
                    rectTwo = new Rectangle(r.x, r.y + r.height, r.width, 0),
                    rectThree = new Rectangle(r.x + r.width, r.y, 0, r.height),
                    rectFour = new Rectangle(r.x, r.y, r.width, 0);
            if(Intersector.overlaps(this.rect, rectOne)) {
                rects.add(new RectangleHelper(rectOne, (byte)0));
                one = true;
            }
            if(Intersector.overlaps(this.rect, rectTwo)) {
                rects.add(new RectangleHelper(rectTwo, (byte)1));
                two = true;
            }
            if(!one && Intersector.overlaps(this.rect, rectThree)) {
                rects.add(new RectangleHelper(rectThree, (byte)2));
                three = true;
            }
            if(!two && Intersector.overlaps(this.rect, rectFour)) {
                rects.add(new RectangleHelper(rectFour, (byte)3));
                four = true;
            }
            //if a collision detected, adjust position accordingly, then break
            if(one || two || three || four) {
                if(jumping && four) {
                    jumpCancel = true;
                }
                gravitySpeed = 0;
                collisionDetected = true;
                float[] tempPos =  determineClosestRect(rects);
                //determine which is closest
                if(Vector.distance(new Vector2(closest[0], closest[1]), this.pos) > Vector.distance(new Vector2(tempPos[0], tempPos[1]), this.pos)) {
                    closest = tempPos;
                }
            }
        }
        if(!onGround && !collisionDetected) {
            applyGravity();
            jumping = true;
            jumpCancel = false;
        }
        else {
            this.pos = new Vector2(closest[0], closest[1]);
            onGround = false;
            jumping = false;
            gravitySpeed = 0;
            jumpSpeed = maxJumpSpeed * jumpTimeTwo;
            if((deltaJumpTimer += delta) > .1) jumpTimeTwo = 0;
        }
    }

    private float[] determineClosestRect(Array<RectangleHelper> rects) {
        float[] closest = new float[2];
        float closestDistance = Float.MAX_VALUE;
        for(RectangleHelper r : rects) {
            //if left or right side
            if(r.id == (byte)0 || r.id == (byte)2) {
                if(Vector.distance(this.pos, new Vector2(r.r.x, this.rect.y)) < closestDistance) {
                    //collision is the right side of character
                    if(r.id == (byte) 0) {
                        closest[0] = r.r.x - this.rect.width; closest[1] = this.rect.y;
                    }
                    //collision is the left side of character
                    else {
                        closest[0] = r.r.x; closest[1] = this.rect.y;
                    }
                    closestDistance = Vector.distance(this.pos, new Vector2(closest[0], closest[1]));
                }
            }
            //if top or bottom
            else {
                if(Vector.distance(this.pos, new Vector2(this.rect.x, r.r.y)) < closestDistance) {
                    //if foot collision
                    if(r.id == (byte)1) {
                        onGround = true;
                        closest[0] = this.rect.x; closest[1] = r.r.y;
                    }
                    //if head collision
                    else {
                        closest[0] = this.rect.x; closest[1] = r.r.y - this.rect.height;
                    }

                    closestDistance = Vector.distance(this.pos, new Vector2(closest[0], closest[1]));
                }
            }
        }
        return closest;
    }

    private void handleInput() {
        int direction = 1;
        //force direction selected when releasing jump
        if(onGround) {
            switch(left ? 2 : 0 + (neu ? 1 : 0)) {
                case 0:
                    direction = 0;
                    break;
                case 1:
                    direction = 1;
                    break;
                case 2:
                    direction = 2;
                    break;
            }
            handleInput(direction);
        }
        else {
            if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                if(jumpTimeTwo < maxJumpTime) jumpTimeTwo += delta;
                jumpTime = 0;
                deltaJumpTimer = 0;
                currentSprite = crouch;
            }
            else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                //if guy was just walking right, abruptly stop
                if(!left) {
                    speed = 0;
                    frameTime = 0;
                }
                walkAnimation();
                accelerate();
                pos.x += (float) Math.cos(Math.toRadians(180)) * (speed * delta);
                left = true;
                neu = false;
            }
            else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                // if guy was just walking left, abruptly stop
                if(left) {
                    speed = 0;
                    frameTime = 0;
                }
                walkAnimation();
                accelerate();
                pos.x += (float) Math.cos(Math.toRadians(0)) * (speed * delta);
                left = false;
                neu = false;
            }
            else {
                //neutral, set sprite appropriately
                currentSprite = neutral;
                neu = true;
            }
        }
    }

    private void handleInput(int forced) {
        if(forced == 2){
            //if guy was just walking right, abruptly stop
            if(!left) {
                speed = 0;
                frameTime = 0;
            }
            walkAnimation();
            accelerate();
            pos.x += (float) Math.cos(Math.toRadians(180)) * (speed * delta);
            left = true;
            neu = false;
        }
        else if(forced == 0) {
            // if guy was just walking left, abruptly stop
            if(left) {
                speed = 0;
                frameTime = 0;
            }
            walkAnimation();
            accelerate();
            pos.x += (float) Math.cos(Math.toRadians(0)) * (speed * delta);
            left = false;
            neu = false;
        }
        else {
            //neutral, set sprite appropriately
            currentSprite = neutral;
            neu = true;
        }
    }

    private void jump() {
        if(jumpCancel) {
            jumpSpeed = 0;
        }
        if(!collisionDetected && !Gdx.input.isKeyPressed(Input.Keys.SPACE) && jumpTime < jumpTimeTwo) {
            jumpTime += delta;
            jumpAcceleration = maxJumpSpeed / jumpTimeTwo;
            if((jumpSpeed - jumpAcceleration * delta) > 1f)  {
                onGround = false;
                jumpSpeed -= jumpAcceleration * delta;
            }
            pos.y += (float) Math.cos(Math.toRadians(0)) * ((jumpSpeed * 3)* delta);
        }
    }

    private void applyGravity() {
        if((gravitySpeed + gravityAccel * delta) < gravityMaxSpeed)  gravitySpeed += gravityAccel * delta;
        pos.y -= (float) Math.cos(Math.toRadians(0)) * ((gravitySpeed) * delta);
    }

    private void accelerate() {
        accelerating = true;
        if(jumping) {
            speed = MAX_SPEED;
        }
        else if((speed + ACCELERATION * delta) < MAX_SPEED) {
            speed += ACCELERATION * delta;
        }
    }

    private void decelerate() {
        if(!accelerating) {
            //reset frame time, unlikely an overflow could happen but lower the chance
            frameTime = 0f;
            //decrease speed
            if((speed - (ACCELERATION * DECEL_FACTOR) * delta) > 0)  speed -= (ACCELERATION * DECEL_FACTOR) * delta;
            //account for case where float speed still holds a value
            else speed = 0f;
            if(left) {
                if(speed != 0) walkAnimation();
                pos.x += (float) Math.cos(Math.toRadians(180)) * ((speed) * delta);
            }
            else {
                if(speed != 0) walkAnimation();
                pos.x += (float) Math.cos(Math.toRadians(0)) * ((speed) * delta);
            }
        }
    }

    //uses global left to determine which animation to apply to current sprite
    private void walkAnimation() {
        if(left) {
            //step through walking frame
            frameTime += delta;
            //base frame duration on current speed.
            walkingLeft.setFrameDuration((MAX_SPEED / FRAME_DURATION_FACTOR) / speed);
            currentSprite = walkingLeft.getKeyFrame(frameTime, true);
        }
        else {
            //step through walking frame
            frameTime += delta;
            //base frame duration on current speed.
            walkingRight.setFrameDuration((MAX_SPEED / FRAME_DURATION_FACTOR) / speed);
            currentSprite = walkingRight.getKeyFrame(frameTime, true);
        }
    }

    //get current sprite, mainly for batch
    public TextureRegion currentSprite() { return currentSprite; }

    public Vector2 pos() { return pos; }

    public Rectangle getRect() { return rect; }
}