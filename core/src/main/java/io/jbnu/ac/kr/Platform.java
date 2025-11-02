package io.jbnu.ac.kr;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Platform {
    public static int platformWidth = 100;
    public static int platformHeight = 100;

    public Sprite sprite;
    public Rectangle bounds;
    public Vector2 position;

    public Platform(float x, float y, Texture platformTexture) {
        this.position = new Vector2(x, y);
        this.sprite = new Sprite(platformTexture);
        this.sprite.setPosition(position.x, position.y);
        this.sprite.setSize(platformWidth, platformHeight);
        this.bounds = new Rectangle(position.x, position.y, sprite.getWidth(), sprite.getHeight());
    }

    public void update() {
        bounds.setPosition(position.x, position.y);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}


