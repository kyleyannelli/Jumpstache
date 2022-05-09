package com.kyleyannelli.finalproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    final FinalProject game;
    final int RENDER_WIDTH = 1280;
    final int RENDER_HEIGHT = 400;
    private int cameraOffset = 0;
    private TiledMap tiledMap;
    private MapLayer collisionLayerOne;
    private MapObjects collisionObjects, appleObjects, safeAreas;
    private Animation<TextureRegion> bg, bgFlipped, snow, snowBg, snowBgFlipped;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private BitmapFont font;
    private float timing;
    /**
     * first is to fix a bug where tiled map doesn't fully load map snow.
     * Havent figured out why. it just uses this boolean to check if its the first time the camera
     *  is moving on map two, then reloads the map once and it loads the rest...
     */
    private boolean alwaysFollow, first, launch;
    OrthographicCamera camera;
    private GuyRevamp revamp;
    private EvilLaserGuy evilOne;
    private Music trackOne, trackTwo;
    private int currentMap;
    private boolean revampBulletHit;
    private int side;


    public GameScreen(final FinalProject game) {
        this.game = game;
        //initialize camera
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, RENDER_WIDTH, RENDER_HEIGHT);
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap2.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        // get rects from tiledMap
        this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
        this.collisionObjects = collisionLayerOne.getObjects();
        this.font = new BitmapFont();
        this.revamp = new GuyRevamp(RENDER_WIDTH/2.0f, 250);
        this.evilOne = new EvilLaserGuy(32, 1720);
        this.trackOne = Gdx.audio.newMusic(Gdx.files.internal("music/trackOneSlowed.ogg"));
        trackOne.setVolume(1.0f);
        trackOne.setLooping(true);
        trackOne.play();
        this.trackTwo = Gdx.audio.newMusic(Gdx.files.internal("music/trackTwo.ogg"));
        trackTwo.setVolume(1.0f);
        trackTwo.setLooping(true);
        alwaysFollow = false;
        loadBgFrames();
        loadSnowFrames();
        this.revampBulletHit = false;
        //hide mouse
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("mouse/literallyNothing8bit.png")), 0, 0));
    }

    private void loadSnowFrames() {
        this.snow = pngPathToAnim(98, .01f, "snowanim/");
        this.snowBg = pngPathToAnim(80, .1f, "snowbg/");
        this.snowBgFlipped = pngPathToAnim(80, .1f, "snowbg/flipped/");
    }

    private void loadBgFrames() {
        this.bg = pngPathToAnim(32, .1f, "bganim/");
        this.bgFlipped = pngPathToAnim(32, .1f, "bganim/flipped/");
    }

    /**
     * @param numberOfFrames: total amount of pngs
     * @param duration: frame duration
     * @param path: The path must be a string. Assume file name frame_i.png The path must end in /
     * @return pngs as a texture region animation
     */
    private Animation<TextureRegion> pngPathToAnim(int numberOfFrames, float duration, String path) {
        TextureRegion[] frames = new TextureRegion[numberOfFrames];
        for(int i = 0; i < numberOfFrames; i++) {
            frames[i] = new TextureRegion(new Texture(path + "frame_" + i + ".png"));
        }
        return new Animation<TextureRegion>(duration, frames);
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //timing is used for keyframes
        timing += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            //muting handling
            if(trackOne.getVolume() > .5f) {
                trackOne.setVolume(0f);
                trackTwo.setVolume(0f);
            }
            else {
                trackOne.setVolume(1f);
                trackTwo.setVolume(1f);
            }
        }
        //to reduce looping each map has sections of rectangle obj and it has to adjust based on player pos
        adjustObjects();
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        //choose map 1 or 2 based on position
        if(revamp.pos().y < 3000) {
            levelOne(delta);
        }
        else {
            levelTwo(delta);
        }
    }

    private void levelOne(float delta) {
        // DEBUGGING/CREATION: reload map to show new changes made in Tiled
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) reloadMap("betamap2");
        // DEBUGGING: Reset entire game
        if(Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            if(Gdx.input.isKeyPressed(Input.Keys.R)) {
                reset();
            }
        }
        // If the map has just been loaded in
        if(currentMap != 0) {
            //stop any previous tracks (should probably make the music an array)
            trackTwo.stop();
            trackOne.play();
            //map 1 (trees), start at 0 int
            currentMap = 0;
            //load TiledMap into object
            reloadMap("betamap2");
            //get rects
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
            this.collisionObjects = collisionLayerOne.getObjects();
        }
        game.batch.begin();
        //animate backgrounds
        if(revamp.pos().y > 1200) {
            game.batch.draw(bgFlipped.getKeyFrame(timing, true), 0, 750);
            game.batch.draw(bg.getKeyFrame(timing, true), 0, 750*2);
            game.batch.draw(bgFlipped.getKeyFrame(timing, true), 0, 750*3);
        }
        else {
            game.batch.draw(bg.getKeyFrame(timing, true), 0, 0);
            game.batch.draw(bgFlipped.getKeyFrame(timing, true), 0, 750);
        }
        game.batch.end();
        //tiled map has to come in between bg and player otherwise it shows black
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        game.batch.begin();
        drawAndUpdateBullets(delta);
        game.batch.draw(revamp.currentSprite(), revamp.pos().x, revamp.pos().y);
        game.batch.draw(evilOne.currentSprite(), evilOne.pos()[0], evilOne.pos()[1]);
        game.batch.end();
        //wait for loading, timer kinda crappy way to do this..
        if(timing > 3f) revamp.update(delta, collisionObjects);
        evilOne.update(delta, revamp, collisionObjects);
        revamp.applyBulletForce(revampBulletHit, side);
        //camera follow user
        cameraFollowGuy(delta);
        //debug method allows teleporting
        debug();
    }

    private void drawAndUpdateBullets(float delta) {
        //convert user rect to a polygon to determine if theres a collision
        //have to make each one a line to determine which side was hit
        Rectangle rects[] = { new Rectangle(revamp.getRect().x, revamp.getRect().y, 0, revamp.getRect().height),
                new Rectangle(revamp.getRect().x, revamp.getRect().y + revamp.getRect().height, revamp.getRect().width, 0),
                new Rectangle(revamp.getRect().x + revamp.getRect().width, revamp.getRect().y, 0, revamp.getRect().height),
                new Rectangle(revamp.getRect().x, revamp.getRect().y, revamp.getRect().width, 0)};
        Polygon p[] = new Polygon[4];
        for(int i = 0; i < rects.length; i++) {
            p[i] = new Polygon(new float[] {
                    0, 0,
                    rects[i].width, 0,
                    rects[i].width, rects[i].height,
                    0, rects[i].height });
            p[i].setPosition(rects[i].x, rects[i].y);
        }
        revampBulletHit = false;
        for(EvilBullet b : evilOne.getBullets()) {
            game.batch.draw(b.getSprite(), b.hitbox.getX(), b.hitbox.getY(), b.hitbox.getOriginX(), b.hitbox.getOriginY(), b.getSprite().getRegionWidth(), b.getSprite().getRegionHeight(), 1, 1, b.startingRotation);
            b.update(delta);
            for(int i = 0; i < p.length; i++) {
                if(revampBulletHit) break;
                if(Intersector.overlapConvexPolygons(p[i], b.hitbox)) {
                    revampBulletHit = true;
                    switch(i) {
                        case 0:
                            side = 0;
                            break;
                        case 1:
                            side = 1;
                            break;
                        case 2:
                            side = 2;
                            break;
                        case 3:
                            side = 3;
                            break;
                    }
                    evilOne.getBullets().removeValue(b, true);
                    break;
                }
            }
        }
    }

    private void levelTwo(float delta) {
        // If the map has just been loaded in
        if(currentMap != 1) {
            //first movement of camera
            first = true;
            //dont follow
            alwaysFollow = false;
            //stop any previous tracks (should probably make the music an array)
            trackOne.stop();
            trackTwo.play();
            //map 2 snow, int 1
            currentMap = 1;
            //load TiledMap into object
            reloadMap("betamapsnow");
            //get rects
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
            this.collisionObjects = collisionLayerOne.getObjects();
            //get apples
            MapLayer a =  (MapLayer) tiledMap.getLayers().get("apple");
            this.appleObjects = a.getObjects();
            //get safe area, a safe area is where wind doesnt affect player
            MapLayer sA = (MapLayer) tiledMap.getLayers().get("safearea");
            this.safeAreas = sA.getObjects();
        }
        //camera follow user
        followRevampLevelTwo(delta);
        //draw bg
        game.batch.begin();
        //draw backgrounds
        game.batch.draw(snowBg.getKeyFrame(timing, true), 0, 3000);
        //determine frame height, next background starts at that height + initial offset
        float snowBgHeight = snowBg.getKeyFrame(timing, true).getRegionHeight();
        game.batch.draw(snowBgFlipped.getKeyFrame(timing, true), 0, 3000 + snowBgHeight);
        game.batch.draw(snowBg.getKeyFrame(timing, true), 0, 3000 + snowBgHeight * 2);
        game.batch.end();
        //draw tilemap
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        //draw characters
        game.batch.begin();
        game.batch.draw(revamp.currentSprite(), revamp.pos().x, revamp.pos().y);
//        game.font.draw(game.batch, ""+(int)revamp.pos().y, revamp.pos().x, revamp.pos().y + revamp.getRect().height);
        //draw windy snow!
        game.batch.draw(snow.getKeyFrame(timing, true), 0, 3000);
        float snowHeight = snow.getKeyFrame(timing, true).getRegionHeight();
        game.batch.draw(snow.getKeyFrame(timing, true), 0, 3000 + snowHeight);
        game.batch.end();
        revamp.update(delta, collisionObjects);
        //handle wind logic
        handleWind();
        //handle apple logic
        handleApple();
        //DEBUGGING
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) revamp.setPos(new Vector2(RENDER_WIDTH/2.0f, 250));
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) reloadMap("betamapsnow");
    }

    private void handleWind() {
        //if it is not an area blocked by wind
        boolean safeArea = false;
        //go through safe area array, check to see if user is in safe area
        for(RectangleMapObject r : safeAreas.getByType(RectangleMapObject.class)) {
            if(Intersector.overlaps(r.getRectangle(), revamp.getRect())) {
                //user is in safe area
                safeArea = true;
                //no longer need to loop
                break;
            }
        }
        //if user is not in safe area, apply wind force
        if(!safeArea) revamp.pushGuy(0, 40);
    }

    private void handleApple() {
        //go through ellipse array, Tiled does not support circles otherwise i would have used those
        for(EllipseMapObject e : appleObjects.getByType(EllipseMapObject.class)) {
            //all ellipses I created are perfectly even, so converting to circle is fine
            //this is needed for built in Intersector
            Circle c = new Circle(e.getEllipse().x, e.getEllipse().y, e.getEllipse().width / 2);
            //if circle hits user
            if(Intersector.overlaps(c, revamp.getRect())) {
                //tell revamp it got an apple
                revamp.gotApple();
                //apples are no longer attainable
                tiledMap.getLayers().get("apples").setVisible(false);
                //remove this apple object from array
                appleObjects.remove(e);
            }
        }
    }

    private void followRevampLevelTwo(float delta) {
        if(revamp.pos().y < 3839) {
            if(camera.position.y > 3450) camera.position.y -= 200 * delta;
            else camera.position.y = 3450;
        }
        else {
            if(first) {
                reloadMap("betamapsnow");
                first = false;
            }
            camera.position.y += (revamp.getRect().y - camera.position.y) * 0.01f * revamp.getGravitySpeed() * delta;
        }
    }

    private void debug() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) revamp.setPos(new Vector2(RENDER_WIDTH/2.0f, 200));
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) revamp.setPos(new Vector2(RENDER_WIDTH/2.0f - 100, 1300));
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) revamp.setPos(new Vector2(130, 3500));
    }

    private void reset() {
        //initialize camera
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, RENDER_WIDTH, RENDER_HEIGHT);
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap2.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        // get rects from tiledMap
        this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
        this.collisionObjects = collisionLayerOne.getObjects();
        this.font = new BitmapFont();
        this.revamp = new GuyRevamp(RENDER_WIDTH/2.0f, 250);
        this.evilOne = new EvilLaserGuy(32, 1720);
        this.trackOne = Gdx.audio.newMusic(Gdx.files.internal("music/trackOneSlowed.ogg"));
        trackOne.setVolume(1.0f);
        trackOne.setLooping(true);
        trackOne.play();
        loadBgFrames();
    }

    private void reloadMap(String mapName) {
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load(mapName + ".tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    // handle collision object change at different levels. minimizes for loop on bigger maps
    public void adjustObjects() {
        if(revamp.getRect().y >= 1300) {
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor2");
        }
        else {
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
        }
        this.collisionObjects = collisionLayerOne.getObjects();
    }

    public void cameraFollowGuy(float delta) {
        //stops before map change
        if(((camera.position.y < 2550 || revamp.pos().y < 2500))) {
            if (revamp.getGravitySpeed() > 1f) {
                camera.position.y += (revamp.getRect().y - camera.position.y) * 0.01f * revamp.getGravitySpeed() * delta;
            } else {
                camera.position.y += (revamp.getRect().y - camera.position.y) * 0.1f * delta;
            }
        }
        else if(camera.position.y >= 2550 && revamp.pos().y > 2000) {
            camera.position.y = 2550;
        }
    }

    private boolean cameraFollow(int posInitial, int posMax, int offset) {
        boolean change = false;
        if(revamp.getRect().y < posMax && revamp.pos().y >= posInitial && cameraOffset >= offset + 2) {
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
        trackOne.dispose();
    }
}
