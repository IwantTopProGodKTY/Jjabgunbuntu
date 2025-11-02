package io.jbnu.ac.kr;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


import java.security.Key;
import java.util.Random;



/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    Sound effectSound;

    GameWorld world;
    public int Level = 2;
    private Texture objectTexture; // 떨어지는 오브젝트 텍스처
    private Texture playerTexture;
    private Texture pauseTexture;
    private Texture blockTexture; // 블록 텍스쳐
    private Texture startTexture;
    private Texture platformTexture;
    private BitmapFont scoreFont;
    private boolean reFlag; //R키를 눌렀을때 모든 월드를 초기화하기

    //카메라
    private OrthographicCamera camera;
    private Viewport viewport;
    public CameraManager camManager;
    private ShapeRenderer shapeRenderer;


    private final float WORLD_WIDTH = 1280;
    private final float WORLD_HEIGHT = 720;

    private enum GameState {
        START,
        RUNNING,
        PAUSE,
        CLEARED,
        STAGECLEARED,
    }

    GameState currentState;


    @Override
    public void create() {
        batch = new SpriteBatch();
        effectSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        playerTexture = new Texture("t.png");
        objectTexture = new Texture("coin.jpg");
        pauseTexture = new Texture("pause.png");
        blockTexture = new Texture("block.png");
        startTexture = new Texture("start.png");
        platformTexture = new Texture("ground.png");
        shapeRenderer = new ShapeRenderer();

        //월드 생성
        world = new GameWorld(playerTexture, objectTexture, blockTexture,platformTexture, this.WORLD_WIDTH, Level);



        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.setToOrtho(false, 1280, 720);
        //카메라 흔들림 제어 클래스 생성
        camManager = new CameraManager(camera);

        scoreFont = new BitmapFont(); // 기본 비트맵 폰트 생성
        scoreFont.getData().setScale(2);


        currentState = GameState.START;
        reFlag = false;



    }

    @Override
    public void render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        input();
        logic();
        draw();
        endingCheck();
    }

    private void logic() {
        if(currentState == GameState.RUNNING) {
            world.update(Gdx.graphics.getDeltaTime());

            // 게임오버 체크
            if(world.isGameOver) {
                currentState = GameState.START;
            }
        }

        if(currentState == GameState.STAGECLEARED) {
            if(Level < 3) {
                Level++;
                NewWorld(Level);
                currentState = GameState.RUNNING;
            } else {
                currentState = GameState.CLEARED;
            }
        }

        if(reFlag) {
            Level = 1;
            NewWorld(Level);
            currentState = GameState.RUNNING;
            reFlag = false;
        }
    }


    private void draw() {
        batch.begin();

        if(currentState == GameState.RUNNING || currentState == GameState.PAUSE) {
            camera.position.set(world.getPlayer().position.x, camera.position.y, 0);
            camera.update();
            batch.setProjectionMatrix(viewport.getCamera().combined);

            // *** 플랫폼(땅) 렌더링 ***
            for(Rectangle platform : world.getPlatforms()) {
                batch.draw(platformTexture, platform.x, platform.y, platform.width, platform.height);
            }

            // 오브젝트 렌더링
            world.getPlayer().draw(batch);
            for(CoinObject obj : world.getObjects()) {
                obj.draw(batch);
            }

            for(Block block : world.getBlock()) {
                block.draw(batch);
            }

            world.getFlag().draw(batch);

            // UI 그리기
            scoreFont.draw(batch, "HP: " + world.getPlayer().Hp, camera.position.x-640, WORLD_HEIGHT - 20);
            scoreFont.draw(batch, "Score: " + world.getScore(), camera.position.x-640, WORLD_HEIGHT - 60);
            scoreFont.draw(batch, "Stage: " + Level, camera.position.x-640, WORLD_HEIGHT - 100);

            if(currentState == GameState.PAUSE) {
                batch.draw(pauseTexture, camera.position.x - (pauseTexture.getWidth() / 2), camera.position.y - (pauseTexture.getHeight() / 2));
            }

            batch.end(); // batch 끝내기

            // 낭떠러지를 ShapeRenderer로 그리기 (batch 종료 후)
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 0, 0, 0.3f); // 반투명 빨강



            shapeRenderer.end();

            batch.begin(); // 다시 batch 시작 (다음 프레임 준비)
        }
        else if(world.isGameOver) {
            // 게임오버 화면
            camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
            camera.update();
            batch.setProjectionMatrix(camera.combined);

            scoreFont.draw(batch, "GAME OVER", 500, 400);
            scoreFont.draw(batch, "Score: " + world.getScore(), 500, 350);
            scoreFont.draw(batch, "Press R to Restart", 450, 300);
        }
        else if(currentState == GameState.CLEARED) {
            camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
            camera.update();
            batch.setProjectionMatrix(camera.combined);

            scoreFont.draw(batch, "GAME CLEAR!", 500, 400);
            scoreFont.draw(batch, "Total Score: " + world.getScore(), 450, 350);
            scoreFont.draw(batch, "Press R to Restart", 450, 300);
        }
        else if(currentState == GameState.STAGECLEARED) {
            camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
            camera.update();
            batch.setProjectionMatrix(camera.combined);

            scoreFont.draw(batch, "Stage " + Level + " Clear!", 450, 400);
            scoreFont.draw(batch, "Score: " + world.getScore(), 450, 350);
            scoreFont.draw(batch, "Next Stage...", 450, 300);
        }
        else {
            // 시작 화면
            camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
            camera.update();
            batch.setProjectionMatrix(camera.combined);

            batch.draw(startTexture, 640 - (startTexture.getWidth()/2), 360 - (startTexture.getHeight()/2));
        }

        batch.end();
    }


    private void input() {
        if(currentState == GameState.START) {
            if(Gdx.input.isKeyJustPressed(Keys.S)) {
                currentState = GameState.RUNNING;
            }
        }
        else if(currentState == GameState.RUNNING) {
            // 게임 진행 중 키 처리
            if (Gdx.input.isKeyPressed(Keys.SPACE)) {
                world.onPlayerJump();
            }
            if (Gdx.input.isKeyPressed(Keys.ENTER)) {
                currentState = GameState.PAUSE;
            }
        }
        else if(currentState == GameState.PAUSE) {
            if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
                currentState = GameState.RUNNING;
            }
        }

        // 모든 게임오버, 클리어 상태에서 R키로 재시작 가능
        if((world.isGameOver || currentState == GameState.CLEARED || currentState == GameState.STAGECLEARED)
            && Gdx.input.isKeyJustPressed(Keys.R)) {

            currentState = GameState.START;
            reFlag = true;
            resetCameraForStartScreen();
        }
    }


    public void NewWorld(int level) {
        // 다시 새로운 객체를 생성 레벨에 따라 레벨을 코인의 속도에 곱합
        world = new GameWorld(playerTexture, objectTexture, blockTexture, platformTexture, this.WORLD_WIDTH, level);

    }

    @Override
    public void dispose() {
        playerTexture.dispose();
        objectTexture.dispose();
        scoreFont.dispose();
        shapeRenderer.dispose();
        batch.dispose();
    }


    //기존 카메라함수 오버라이드
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

    }


    // 카메라 매니저
    private void endingCheck() {
        if (world.isGameClear() && (currentState != Main.GameState.CLEARED) && Level == 3) {
            currentState = GameState.CLEARED;
            startEffect();
        }
        else if(world.isGameClear() && (currentState != Main.GameState.CLEARED) && Level != 3)
        {
            currentState = GameState.STAGECLEARED;
        }
    }

    public void startEffect() {
        camManager.startShake(3.0f, 80f); //3초간 강하게 흔든다
        camManager.isShakedStarted = true;
    }

    public void updateEffect(float delta) {
        camManager.updateShake(delta);
    }

    public void resetCameraForStartScreen() {
        // 원하는 위치에 카메라 중앙 지정
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.zoom = 1f; // 기본 줌값
        camera.update();

        // 뷰포트도 카메라에 맞춰 업데이트
        viewport.setWorldSize(WORLD_WIDTH, WORLD_HEIGHT);
        viewport.apply(true);
    }

}
