package io.jbnu.ac.kr;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Block
{
    public static int BlockWidth = 50;
    public static int BlockHeight = 50;
    public Sprite sprite;

    // 충돌 판정을 위한 사각 영역
    public Rectangle bounds;
    public Vector2 position;
    public Block(float x, float y, Texture blockTexture)
    {
        this.position = new Vector2(x,y);
        this.sprite = new Sprite(blockTexture);
        this.sprite.setPosition(position.x, position.y);

        this.sprite.setSize(BlockWidth,BlockHeight);

        // 충돌 영역 초기화
        this.bounds = new Rectangle(position.x, position.y, sprite.getWidth(), sprite.getHeight());
    }

    public void draw(SpriteBatch batch)
    {
        this.sprite.draw(batch);
    }

    /*public float getWidth()
    {
        return BlockWidth;
    }

    public float getHeight()
    {
        return BlockHeight;
    }*/


    public Rectangle getBounds() {
        return bounds;
    }

}

