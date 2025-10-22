package io.jbnu.ac.kr;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Flag {
    public static int flagWidth = 80;
    public static int flagHeight = 100;
    public Sprite sprite;

    // 충돌 판정을 위한 사각 영역
    public Rectangle bounds;
    public Vector2 position;
    public Flag(float x, float y, Texture flagTexture)
    {
        this.position = new Vector2(x,y);
        this.sprite = new Sprite(flagTexture);
        this.sprite.setPosition(position.x, position.y);

        this.sprite.setSize(flagWidth,flagHeight);
        this.bounds = new Rectangle(position.x, position.y, sprite.getWidth(), sprite.getHeight());
    }

    public void draw(SpriteBatch batch)
    {
        this.sprite.draw(batch);
    }




}
