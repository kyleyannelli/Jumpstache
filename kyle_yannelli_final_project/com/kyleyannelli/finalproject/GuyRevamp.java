package com.kyleyannelli.finalproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GuyRevamp {
    //may want to consider changing DECEL_FACTOR to a mutable in the case of an slippery platform, lower decel factor...
    public final int MAX_SPEED = 250, ACCELERATION = 200, DECEL_FACTOR = 6;
    private float FRAME_DURATION_FACTOR = 7f;
    private float bulletForceTimer;
    private TextureRegion currentSprite, neutral, crouch, fallen, fallingLeft, fallingRight, apple;
    private Animation<TextureRegion> walkingLeft, walkingRight;
    public float deltaJumpTimer, frameTime, jumpTime, jumpTimeTwo, maxJumpTime = .8f, oneTime;
    public boolean jumping, left, neu;
    private boolean accelerating, jumpCancel, onGround, collisionDetected, inputTouchedPostFall, bouncing, gotApple;
    private Rectangle rect;
    private Vector2 pos;
    private float groundRange[], lastGroundY, previousY;
    private float speed, delta,
            gravityAccel = 600f, gravitySpeed = 0, gravityMaxSpeed = 600f,
            maxJumpSpeed = 600f, jumpSpeed = maxJumpSpeed, jumpAcceleration = 225f;

    public GuyRevamp(float x, float y) {
        initializeSprites();
        this.pos = new Vector2(x, y);
        this.rect = new Rectangle(x, y, neutral.getRegionWidth(), neutral.getRegionHeight());
        this.left = false;
        this.neu = true;
        this.groundRange = new float[3];
        this.inputTouchedPostFall = true;
    }

    // constructor clean up
    private void initializeSprites() {
        this.neutral = new TextureRegion(new Texture("character/characterNeutral.png"));
        this.crouch = new TextureRegion(new Texture("character/characterCrouch.png"));
        this.fallen = new TextureRegion(new Texture("character/characterFallen.png"));
        this.fallingLeft = new TextureRegion(new Texture("character/characterFallingRight.png"));
        this.fallingRight = new TextureRegion(new Texture("character/characterFallingLeft.png"));
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
        this.lastGroundY = 0;
        this.apple = new TextureRegion(new Texture("character/appleCharacterNeutral.png"));
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
        if(jumpTimeTwo < 1f) gravityMaxSpeed = 2000f;
        else gravityMaxSpeed = 600f;
        //update global delta
        this.delta = delta;
        //set acceleration false, will get set to true if user is moving left or right
        accelerating = false;
        if(lastGroundY - groundRange[2] >= 400) inputTouchedPostFall = false;
        //gravity is applied in handle collision
        handleCollision(collisionObjects);
        //handle input
        handleInput();
        jump();
        if(!inputTouchedPostFall && onGround) currentSprite = fallen;
        //position vector is the only modified variable in methods, update sprite and collision box
        rect.height = currentSprite.getRegionHeight();
        rect.width = currentSprite.getRegionWidth();
        rect.x = pos.x;
        rect.y = pos.y;
        //if(!acceleration) decelerate
        decelerate();
        //if an apple was obtained
        if(gotApple) {
            //for 25f of time, GuyRevamp is safe from wind
            if((oneTime += delta) < 25f) {
                //flip textures
                TextureRegion r = neutral;
                neutral = apple;
                apple = r;
            }
            else {
                //flip textures back
                TextureRegion r = neutral;
                neutral = apple;
                apple = r;
                gotApple = false;
            }
        }
        if(pos.y < 3000) {
            // GuyRevamp has fallen below wind area, reset apple timer
            oneTime = 0f;
        }
    }

    //i was having a lot of trouble with collision. the left right top bottom concept is from here http://www.jeffreythompson.org/collision-detection/line-rect.php
    private void handleCollision(MapObjects collisionObjects) {
        collisionDetected = false;
        boolean unchanged = true;
        float[] closest = {99999, 99999, -1};
        for(RectangleMapObject rect : collisionObjects.getByType(RectangleMapObject.class)) {
            boolean one = false, two = false, three = false, four = false;
            //uses helper class that just adds a byte id
            Array<RectangleHelper> rects = new Array<>();
            //convert rectmapobj to libgdx rectangle for Intersector use
            Rectangle r = rect.getRectangle();
            //create rects with zero height to emulate lines
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
            //currently works best if it only considers 1 corner at a time, thus !one (and !two 5 lines below)
            if(Intersector.overlaps(this.rect, rectThree)) {
                rects.add(new RectangleHelper(rectThree, (byte)2));
                three = true;
            }
            if(Intersector.overlaps(this.rect, rectFour)) {
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
                    unchanged = false;
                }
            }
        }
        if(!onGround && !collisionDetected) {
            applyGravity();
            jumping = true;
            jumpCancel = false;
            if(pos.y > lastGroundY) lastGroundY = pos.y;
        }
        else {
            if(!unchanged) this.pos = new Vector2(closest[0], closest[1]);
            jumping = false;
            gravitySpeed = 0;
            jumpSpeed = maxJumpSpeed * jumpTimeTwo;
            if((deltaJumpTimer += delta) > .1) jumpTimeTwo = 0;
            if(onGround && !((groundRange[0] - rect.width) < pos.x && pos.x < groundRange[1])) {
                applyGravity();
                onGround = false;
            }
        }
        //handle direction swap
        //closest[2] holds the id of rectangle side. 0 and 2 ids declare left or right
        if((closest[2] == 0 || closest[2] == 2) && !onGround && collisionDetected) {
            bouncing = true;
            left = ! left;
            jumpCancel = true;
            if(pos.y > lastGroundY) lastGroundY = pos.y;
        }
        else if(onGround && collisionDetected) {
            jumpTimeTwo = 0;
            bouncing = false;
        }
        if(onGround) {
            lastGroundY = 0;
        }
    }

    private float[] determineClosestRect(Array<RectangleHelper> rects) {
        float[] closest = new float[3];
        float closestDistance = Float.MAX_VALUE;
        for(RectangleHelper r : rects) {
            //if left or right side
            if(r.id == (byte)0 || r.id == (byte)2) {
                if(Vector.distance(this.pos, new Vector2(r.r.x, this.rect.y)) < closestDistance) {
                    //collision is the right side of character
                    if(r.id == (byte) 0) {
                        closest[2] = 0;
                        onGround = false;
                        closest[0] = r.r.x - this.rect.width;
                    }
                    //collision is the left side of character
                    else {
                        closest[2] = 2;
                        onGround = false;
                        closest[0] = r.r.x;
                    }
                    closest[1] = this.rect.y;
                    closestDistance = Vector.distance(this.pos, new Vector2(closest[0], closest[1]));
                }
            }
            //if top or bottom
            else {
                if(Vector.distance(this.pos, new Vector2(this.rect.x, r.r.y)) < closestDistance) {
                    //if foot collision
                    if(r.id == (byte)1) {
                        closest[2] = 1;
                        onGround = true;
                        closest[0] = this.rect.x; closest[1] = r.r.y;
                        groundRange[0] = r.r.x; groundRange[1] = r.r.x + r.r.width; groundRange[2] = r.r.y + r.r.height;
                    }
                    //if head collision
                    else {
                        closest[2] = 3;
                        onGround = false;
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
        if(!onGround) {
            switch(left ? 2 : (neu ? 1 : 0)) {
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
                if(!inputTouchedPostFall) {
                    inputTouchedPostFall = true;
                }
            }
            else if(inputTouchedPostFall && Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
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
            else if(inputTouchedPostFall && Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
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
            else if(inputTouchedPostFall){
                //neutral, set sprite appropriately
                currentSprite = neutral;
                neu = true;
                if(speed == 0) left = false;
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
            if(bouncing) {
                fallAnimation();
            }else {
                walkAnimation();
            }
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
            if(bouncing) {
                fallAnimation();
            }
            else {
                walkAnimation();
            }
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
            if(!neu) speed = MAX_SPEED;
            onGround = false;
            jumpTime += delta;
            jumpAcceleration = maxJumpSpeed / jumpTimeTwo;
            if((jumpSpeed - jumpAcceleration * delta) > 1f)  {
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
        if((speed + ACCELERATION * delta) < MAX_SPEED) speed += ACCELERATION * delta;
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

    //uses global left to determine which animation to apply to current sprite
    private void fallAnimation() {
        if(left) {
            currentSprite = fallingLeft;
        }
        else {
            currentSprite = fallingRight;
        }
        if(!inputTouchedPostFall && onGround) currentSprite = fallen;
    }

    public void pushGuy(float direction, float speed) {
        if(!gotApple) pos.x += (float) Math.cos(Math.toRadians(direction)) * speed * delta;
    }

    public void applyBulletForce(boolean justHit, int side) {
        if(justHit) bulletForceTimer = 0f;
        if((bulletForceTimer += delta) < 1f) {
            switch(side) {
                //push to the right
                case 0:
                    pos.x += (float) Math.cos(Math.toRadians(0)) * ((50) * delta);
                    break;
                //push to down
                case 1:
                    pos.y -= (float) Math.cos(Math.toRadians(0)) * ((50) * delta);
                    break;
                //push to the left
                case 2:
                    pos.x += (float) Math.cos(Math.toRadians(180)) * ((50) * delta);
                    break;
                //push up
                case 3:
                    pos.y += (float) Math.cos(Math.toRadians(0)) * ((50) * delta);
                    break;
            }
        }
    }

    //get current sprite, mainly for batch
    public TextureRegion currentSprite() { return currentSprite; }

    public Vector2 pos() { return pos; }

    public Rectangle getRect() { return rect; }

    public void setPos(Vector2 vector2) {
        pos = vector2;
    }

    public void gotApple() { gotApple = true; }

    public float getGravitySpeed() { return gravitySpeed; }

    public float getSpeed() { return speed; }

    public float getGravityMaxSpeed() { return gravityMaxSpeed; }

    public float getJumpSpeed() { return jumpSpeed; }

    public float getMaxJumpSpeed() { return maxJumpSpeed; }
}
