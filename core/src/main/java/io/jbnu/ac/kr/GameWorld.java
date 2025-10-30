package io.jbnu.ac.kr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.security.Key;
import java.util.Iterator;

public class GameWorld {

    public final float WORLD_GRAVITY = -9.8f * 200; // 초당 중력 값
    public final float FLOOR_LEVEL = 0;          // 바닥의 Y 좌표

    // --- 2. 월드 객체 ---
    private GameCharacter player;

    private final float OBJECT_SPAWN_TIME = 2.0f; // 2초마다 오브젝트 생성
    private float objectSpawnTimer = OBJECT_SPAWN_TIME; // 타이머
    private Array<CoinObject> objects; // 떨어지는 오브젝트들을 담을 배열
    private Array<Block> blocks; // 레벨에 배치될 오브젝트를 담은 배열

    private Flag flag;
    private int score;

    public int Level;

    public boolean isGameClear = false;



    private Texture playerTexture;
    private Texture objectTexture;
    private Texture blockTexture;




    private float worldWidth; // 랜덤 위치 생성을 위해 월드 너비 저장

    public GameWorld(Texture playerTexture, Texture objectTexture, Texture blockTexture, float worldWidth, int level) {
        this.playerTexture = playerTexture;
        this.objectTexture = objectTexture;
        this.blockTexture = blockTexture;
        this.worldWidth = worldWidth;

        player = new GameCharacter(playerTexture, worldWidth / 2, FLOOR_LEVEL);
        objects = new Array<>();
        blocks = new Array<>();
        score = 0;
        Level = level;

        loadGround(level * 2,350);
    }

    public void update(float delta) {
        float moveAmount = 0;

        // 1. 이동 방향에 따른 고정 이동 값 설정
        if (player.isMovingRight) {
            moveAmount = 7;
        } else if (player.isMovingLeft) {
            moveAmount = -7;
        } else {
            return; // 이동 입력이 없으면 함수 종료
        }

        // --- 1. 힘 적용 (중력, 저항) ---
        player.velocity.y += WORLD_GRAVITY * delta;
        updateSpawning(delta);

        // --- 2. '예상' 위치 계산 ---
        float expectedX = player.position.x + moveAmount;
        float newY = player.position.y + player.velocity.y * delta;

        //충돌 검사를 위해 임시 위치로 이동
        player.sprite.setX(expectedX);
        Rectangle playerBounds = player.sprite.getBoundingRectangle();

        boolean collison = false;

        // (이번 프레임에 이동할 거리)
        //float newX = player.position.x + player.velocity.x * delta;


        for (Iterator<CoinObject> iter = objects.iterator(); iter.hasNext(); ) {
            CoinObject obj = iter.next();
            obj.update(delta);
            // 화면 밖으로 나간 오브젝트는 제거
            if (obj.position.y < FLOOR_LEVEL - obj.sprite.getHeight()) {
                iter.remove();
            }
        }

        // --- 3 & 4. 충돌 검사 및 반응 ---
        for(Block block : blocks)
        {
            if(playerBounds.overlaps(block.getBounds())) {
                collison = true;


                if (moveAmount > 0) {
                    player.position.x = block.getBounds().x - player.sprite.getWidth();
                } else if (moveAmount < 0) {
                    player.position.x = block.getBounds().x + block.getBounds().width;
                }

                break;
            }
        }



        // 스크린 바닥(FLOOR_LEVEL)과 충돌 검사
        if (newY <= FLOOR_LEVEL) {
            newY = FLOOR_LEVEL;       // 바닥에 강제 고정
            player.velocity.y = 0;    // Y축 속도 리셋
            player.isGrounded = true; // '땅에 닿음' 상태로 변경
        } else {
            player.isGrounded = false; // 공중에 떠 있음
        }

        checkCollisions();

        // --- 5. 최종 위치 확정 ---

        // 모든 충돌 계산이 끝난 '최종' 위치를 반영


        if(!collison)
        {
            player.position.x = expectedX;
        }



        player.sprite.setX(player.position.x);
        player.position.set(player.position.x,newY);
        // --- 6. 그래픽 동기화 ---
        player.syncSpriteToPosition();
    }




    private void updateSpawning(float delta) {
        objectSpawnTimer -= delta;
        if (objectSpawnTimer <= 0) {
            objectSpawnTimer = OBJECT_SPAWN_TIME; // 타이머 리셋

            // 월드 너비 안에서 랜덤한 X 위치 선정
            //float randomX = MathUtils.random(0, worldWidth - objectTexture.getWidth());
            float randomX = MathUtils.random(0, worldWidth - CoinObject.CoinWidth);
            float startY = 720; // 월드 높이 (예시)
            float speed = -100f; // 떨어지는 속도

            System.out.println(objectTexture.getWidth());

            CoinObject newObject = new CoinObject(objectTexture, randomX, startY, speed*Level);
            objects.add(newObject);
        }
    }

    private void checkCollisions() {
        // 플레이어와 떨어지는 오브젝트들의 충돌 검사
        for (Iterator<CoinObject> iter = objects.iterator(); iter.hasNext(); ) {
            CoinObject obj = iter.next();
            if (player.sprite.getBoundingRectangle().overlaps(obj.bounds)) {
                // 충돌 발생!
                score++; // 점수 1점 증가
                System.out.println("Score: " + score); // 콘솔에 점수 출력 (테스트용)
                iter.remove(); // 충돌한 오브젝트는 즉시 제거
            }
        }
    }



    // 레벨에 따라 블록을
    private void loadGround(int numberOfBlock, float spaceSize )
    {
        float starX = 400 - Block.BlockWidth/2;
        float starY = 0;

        for (int i = 0; i<numberOfBlock; i++)
        {
            float x = starX + (i*spaceSize);

            blocks.add(new Block(x,starY, blockTexture));
            //blocks.add(new Block(x,starY+350, blockTexture));
        }

        flag = new Flag(blocks.get(numberOfBlock-1).bounds.x + 400f,0,new Texture("flag.png"));

    }

    public boolean isGameClear()
    {
        if(player.sprite.getBoundingRectangle().overlaps(flag.bounds))
            return true;
        else
            return false;
    }

    public int getScore() {
        return score;
    }

    public Array<CoinObject> getObjects() {
        return objects;
    }

    public  Array<Block> getBlock(){
        return blocks;
    }

    // GameScreen으로부터 '점프' 입력을 받음
    public void onPlayerJump() {
        player.jump();
    }

    public void onPlayerLeft() {
        player.moveLeft();
    }

    public void onPlayerRight() {
        player.moveRight();
    }

    // GameScreen이 그릴 수 있도록 객체를 제공
    public GameCharacter getPlayer() {
        return player;
    }

    public  Flag getFlag()
    {
        return flag;
    }








}
