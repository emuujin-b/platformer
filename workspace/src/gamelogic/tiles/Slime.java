package gamelogic.tiles;

import java.awt.image.BufferedImage;

import gameengine.hitbox.RectHitbox;
import gamelogic.level.Level;

public class Slime extends Tile{

    public Slime(float x, float y, int size, BufferedImage image, Level level) {
		super(x, y, size, image, true, level);
		int offset =(int)(level.getLevelData().getTileSize()*0.1); //hitbox is offset by 10% of the tile size
		this.hitbox = new RectHitbox(x *size, y*size, 0, offset, size, size);
	}
	public String toString() {
		return "I'm a slime at "+this.position.x+" "+this.position.y;
	}
	
}
