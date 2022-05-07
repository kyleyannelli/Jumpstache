package com.kyleyannelli.finalproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.security.Key;
import java.util.HashMap;

public class GameScreen implements Screen {
    final FinalProject game;
    final int RENDER_WIDTH = 1280;
    final int RENDER_HEIGHT = 400;
    private int cameraOffset = 0;
    private Guy guy;
    private EvilGuy evilGuy;
    private TiledMap tiledMap;
    private MapLayer collisionLayerOne;
    private MapObjects collisionObjects;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private BitmapFont font;
    private float deltaTimer, deltaTimerEvil = -2;
    private int lastEvilInput;
    private HashMap<Integer, Integer> followGuy;
    OrthographicCamera camera;
    private GuyRevamp revamp;


    public GameScreen(final FinalProject game) {
        this.game = game;
        //initialize camera
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, RENDER_WIDTH, RENDER_HEIGHT);
        this.guy = new Guy(RENDER_WIDTH/2, 200);
//        this.evilGuy = new EvilGuy(1131, 1233);
        this.evilGuy = new EvilGuy(RENDER_WIDTH/2, 200, "character/evilNeutral.png");
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        // get rects from tiledMap
        this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
        this.collisionObjects = collisionLayerOne.getObjects();
        this.font = new BitmapFont();
        this.followGuy = new HashMap<>();
        this.revamp = new GuyRevamp(RENDER_WIDTH/2, 200);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) reloadMap();
        ScreenUtils.clear(0, 0, 0, 1);
        adjustObjects();
        guy.update(delta);
        handleGuyGravityAndCollision(delta, this.guy);
        guy.decelerate(delta);
        jump(delta, this.guy);
        if(followGuy.get((int)deltaTimerEvil) != null) {
            lastEvilInput = followGuy.get((int)deltaTimerEvil);
            handleGuyGravityAndCollisionIntInput(delta, this.evilGuy, lastEvilInput);
        } else {
            handleGuyGravityAndCollisionIntInput(delta, this.evilGuy, lastEvilInput);
        }
        jump(delta, this.evilGuy, lastEvilInput);
        debug();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
//        game.batch.draw(guy.currentSprite(), guy.pos().x, guy.pos().y);
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
//        game.batch.draw(evilGuy.currentSprite(), evilGuy.pos().x, evilGuy.pos().y);
        evilGuy.update(delta);
//        font.draw(game.batch, ""+(int)guy.pos().y, guy.pos().x, guy.pos().y + guy.currentSprite().getRegionHeight());
        revamp.update(delta, collisionObjects);
        game.batch.draw(revamp.currentSprite(), revamp.pos().x, revamp.pos().y);
        game.batch.end();
        cameraFollowGuy();

        deltaTimer += delta;
        deltaTimerEvil += delta;
    }

    private void debug() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) guy.setPos(new Vector2(RENDER_WIDTH/2, 200));
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) guy.setPos(new Vector2(RENDER_WIDTH/2 - 100, 1300));
    }

    private void reloadMap() {
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    private void jump(float delta, Guy guy) {
        if(guy.jumpCancel) {
            guy.jumpSpeed = 0f;
        }
        if(!Gdx.input.isKeyPressed(Input.Keys.SPACE) && guy.jumpTime < guy.jumpTimeTwo) {
            guy.jumpTime += delta;
            guy.jump(delta);
        }
    }

    private void jump(float delta, Guy guy, int in) {
        if(guy.jumpCancel) {
            guy.jumpSpeed = 0f;
        }
        if(!(in != 1) && guy.jumpTime < guy.jumpTimeTwo) {
            guy.jumpTime += delta;
            guy.jump(delta);
        }
    }

    public void handleGuyGravityAndCollision(float delta, Guy guy) {
        boolean gravityCollisionCheck = false;
        boolean userWantsLeft = false;
        boolean allowLeft = true;
        boolean userWantsRight = false;
        boolean allowRight = true;

        if(Gdx.input.isKeyPressed(Input.Keys.SPACE) && !guy.jumping) {
            followGuy.put((int)deltaTimer, 1);
            guy.jumpSprite();
            if(guy.jumpTimeTwo < guy.maxJumpTime)guy.jumpTimeTwo += delta;
            guy.jumpTime = 0;
            guy.deltaJumpTimer = 0;
        }
        else if(!guy.jumping && Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            followGuy.put((int)deltaTimer, 2);
            userWantsLeft = true;
            guy.left = true;
            guy.frameTime += delta;
        }
        else if(!guy.jumping && Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            followGuy.put((int)deltaTimer, 3);
            userWantsRight = true;
            guy.left = false;
            guy.neu = false;
            guy.frameTime += delta;
        }
        else if(!guy.jumping) {
            followGuy.put((int)deltaTimer, 0);
            guy.frameTime = 0;
            guy.setNeutral();
            guy.decelerate(delta);
            guy.left = false;
            guy.neu = true;
        }
        //force direction selected when releasing jump
        if(guy.jumping) {
            switch(guy.left ? 2 : 0 + (guy.neu ? 1 : 0)) {
                case 0:
                    userWantsRight = true;
                    guy.frameTime += delta;
                    break;
                case 1:
                    userWantsLeft = false;
                    userWantsRight = false;
                    break;
                case 2:
                    userWantsLeft = true;
                    guy.frameTime += delta;
                    break;
            }
        }

        for(RectangleMapObject rect : collisionObjects.getByType(RectangleMapObject.class)) {
            if(Intersector.overlaps(rect.getRectangle(), guy.rect())) {
                if(guy.jumping) {
                    guy.jumpCancel = true;
                }
                gravityCollisionCheck = true;
            }
            if(Intersector.overlaps(rect.getRectangle(), new Rectangle(guy.pos().x, guy.pos().y, guy.rect().width, guy.rect().height))) {
                if(guy.jumping) {
                    guy.jumpCancel = true;
                }
            }
            if(userWantsLeft && !move(rect.getRectangle(), guy.requestedWalkPos(true, delta) - 5, guy.pos().y + 10)[0]) {
                allowLeft = false;
            }
            if(userWantsRight && !move(rect.getRectangle(), guy.requestedWalkPos(false, delta) + 12, guy.pos().y + 10)[0]) {
                allowRight = false;
            }
            if(!allowLeft && gravityCollisionCheck || !allowRight && gravityCollisionCheck) break;
        }
        if(!gravityCollisionCheck) {
            guy.applyGravity(delta);
            guy.jumping = true;
        }
        else {
            guy.jumping = false;
            guy.jumpCancel = false;
            guy.gravitySpeed = 0;
            guy.jumpSpeed = guy.maxJumpSpeed * guy.jumpTimeTwo;
            if((guy.deltaJumpTimer += delta) > .1) guy.jumpTimeTwo = 0;
        }
        if(allowLeft && userWantsLeft) {
            guy.left = true;
            guy.neu = false;
            guy.walk(true, delta);
        }
        if(allowRight && userWantsRight) {
            guy.walk(false, delta);
            guy.left = false;
            guy.neu = false;
        }
    }

//    public void correctForCollisionTiming(Rectangle r, Guy guy) {
//        // top intersection
//        if(guy.pos().y - r.y)
//    }

    public void handleGuyGravityAndCollisionIntInput(float delta, Guy guy, int input) {
        boolean gravityCollisionCheck = false;
        boolean userWantsLeft = false;
        boolean allowLeft = true;
        boolean userWantsRight = false;
        boolean allowRight = true;
        /**
         * 1: jump
         * 2: left
         * 3: right
         */
        if(input == 1 && !guy.jumping) {
            guy.jumpSprite();
            if(guy.jumpTimeTwo < guy.maxJumpTime)guy.jumpTimeTwo += delta;
            guy.jumpTime = 0;
            guy.deltaJumpTimer = 0;
        }
        else if(!guy.jumping && input == 2) {
            userWantsLeft = true;
            guy.left = true;
            guy.frameTime += delta;
        }
        else if(!guy.jumping && input == 3) {
            userWantsRight = true;
            guy.left = false;
            guy.neu = false;
            guy.frameTime += delta;
        }
        else if(!guy.jumping) {
            guy.frameTime = 0;
            guy.setNeutral();
            guy.decelerate(delta);
            guy.left = false;
            guy.neu = true;
        }
        //force direction selected when releasing jump
        if(guy.jumping) {
            switch(guy.left ? 2 : 0 + (guy.neu ? 1 : 0)) {
                case 0:
                    userWantsRight = true;
                    guy.frameTime += delta;
                    break;
                case 1:
                    userWantsLeft = false;
                    userWantsRight = false;
                    break;
                case 2:
                    userWantsLeft = true;
                    guy.frameTime += delta;
                    break;
            }
        }

        for(RectangleMapObject rect : collisionObjects.getByType(RectangleMapObject.class)) {
            if(Intersector.overlaps(rect.getRectangle(), guy.rect())) {
                if(guy.jumping) guy.jumpCancel = true;
                gravityCollisionCheck = true;
            }
            if(Intersector.overlaps(rect.getRectangle(), new Rectangle(guy.rect().x, guy.rect().y + 10, guy.rect().width, guy.rect().height))) {
                if(guy.jumping) guy.jumpCancel = true;
            }
            if(userWantsLeft && !move(rect.getRectangle(), guy.requestedWalkPos(true, delta) - 5, guy.pos().y + 10)[0]) {
                allowLeft = false;
            }
            if(userWantsRight && !move(rect.getRectangle(), guy.requestedWalkPos(false, delta) + 12, guy.pos().y + 10)[0]) {
                allowRight = false;
            }
            if(!allowLeft && gravityCollisionCheck || !allowRight && gravityCollisionCheck) break;
        }
        if(guy.jumping && userWantsLeft & !allowLeft) {
            guy.left = false;
            guy.neu = false;
        }
        else if(guy.jumping && userWantsRight & !allowRight) {
            guy.left = true;
            guy.neu = false;
        }
        if(!gravityCollisionCheck) {
            guy.applyGravity(delta);
            guy.jumping = true;
        }
        else {
            guy.jumping = false;
            guy.jumpCancel = false;
            guy.gravitySpeed = 0;
            guy.jumpSpeed = guy.maxJumpSpeed * guy.jumpTimeTwo;
            if((guy.deltaJumpTimer += delta) > .05) guy.jumpTimeTwo = 0;
        }
        if(allowLeft && userWantsLeft) {
            guy.left = true;
            guy.neu = false;
            guy.walk(true, delta);
        }
        if(allowRight && userWantsRight) {
            guy.walk(false, delta);
            guy.left = false;
            guy.neu = false;
        }

    }

    public boolean[] move(Rectangle buildingRect, float x, float y) {
        boolean acceptOrDenyXorY[] = new boolean[2];
        if(!Intersector.overlaps(buildingRect, new Rectangle(x, y, guy.currentSprite().getRegionWidth(), guy.currentSprite().getRegionHeight()))) {
            //accept x
            acceptOrDenyXorY[0] = true;
            //accept y
            acceptOrDenyXorY[1] = true;
        }
        else if(!Intersector.overlaps(buildingRect, new Rectangle(guy.pos().x, y, guy.currentSprite().getRegionWidth(), guy.currentSprite().getRegionHeight()))) {
            //deny x
            acceptOrDenyXorY[0] = false;
            //accept y
            acceptOrDenyXorY[1] = true;
        }
        else if(!Intersector.overlaps(buildingRect, new Rectangle(x, guy.pos().y, guy.currentSprite().getRegionWidth(), guy.currentSprite().getRegionHeight()))) {
            //accept x
            acceptOrDenyXorY[0] = true;
            //deny y
            acceptOrDenyXorY[1] = false;
        }
        else {
            //deny x
            acceptOrDenyXorY[0] = false;
            //accept y
            acceptOrDenyXorY[1] = false;
        }
        return acceptOrDenyXorY;
    }

    // handle collision object change at different levels. minimizes for loop on bigger maps
    public void adjustObjects() {
        if(revamp.getRect().y >= 1300) {
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor2");
            this.collisionObjects = collisionLayerOne.getObjects();
        } else {
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
            this.collisionObjects = collisionLayerOne.getObjects();
        }
    }

    public static boolean checkIfContainedInRectangle(Rectangle r, float x, float y) {
        float rectX[] = new float[2];
        float rectY[] = new float[2];
        rectX[0] = r.x - 20;
        rectX[1] = r.x + r.width;
        rectY[0] = r.y - 20;
        rectY[1] = r.y + r.height;
        if((rectX[0] <= x && x <= rectX[1]) && (rectY[0] <= y && y <= rectY[1])) return true;
        return false;
    }

    public void cameraFollowGuy() {
        if(cameraFollow(2200, 99999, 95)) { }
        else if(cameraFollow(1270, 2200,75)){ }
        else if(cameraFollow(450, 1270,50)) { }
        else if(revamp.getRect().y < 450 && camera.position.y >= 200){
            cameraOffset = 0;
            camera.position.y -= 50;
        }
    }

    private boolean cameraFollow(int posInitial, int posMax, int offset) {
        boolean change = false;
        if(revamp.getRect().y < posMax && guy.pos().y >= posInitial && cameraOffset >= offset + 2) {
            change = true;
            cameraOffset -= 2;
            camera.position.y -= cameraOffset;
        }
        else if(revamp.getRect().y >= posInitial && cameraOffset < offset) {
            cameraOffset += 2;
            camera.position.y += cameraOffset;
            change = true;
        }
        return change;
    }

    public void createConcretes() {
//        concretes = new Array<>();
//        MapLayer layer = tiledMap.getLayers().get("concrete");
//        System.out.println("HERE");
//        int i = 0;
//        for(MapObject o : layer.getObjects()) {
//            o.
//            System.out.println(++i);
//            concretes.add(new Concrete((float) o.getProperties().get("x"), (float) o.getProperties().get("y")));
//        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        game.batch.setProjectionMatrix(camera.combined);
        camera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
//        guy.dispose();
    }
}
