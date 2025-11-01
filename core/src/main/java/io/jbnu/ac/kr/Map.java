package io.jbnu.ac.kr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;


public class Map {
    private Array<CoinObject> coins;
    private Array<Block> blocks;
    private Flag flag;

    private Texture coinTexture;
    private Texture blockTexture;

    public Map(int level)
    {
        this.coinTexture = coinTexture;
        this.blockTexture = blockTexture;
        coins = new Array<>();
        blocks = new Array<>();

        createStage(level*2, 500);
    }


    public void createStage(int numberOfBlock, float spaceSize)
    {
        float starX = 400 - Block.BlockWidth/2;
        float starY = 0;

        for (int i = 0; i<numberOfBlock; i++)
        {
            float x = starX + (i*spaceSize);

            blocks.add(new Block(x,starY, blockTexture));
            //blocks.add(new Block(x,starY+350, blockTexture));
        }

        //flag = new Flag(blocks.get(numberOfBlock-1).bounds.x + 400f,0,new Texture("flag.png"));
        flag = new Flag(1999,0,new Texture("flag.png"));
    }


    public  Flag getFlag()
    {
        return flag;
    }

    public Array<CoinObject> getCoins() {
        return coins;
    }

    public  Array<Block> getBlock(){
        return blocks;
    }
}
