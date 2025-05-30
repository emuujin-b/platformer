package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;
import gamelogic.tiles.Slime;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;


	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();
	private ArrayList<Water> waters = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];
//if water does something, need to clear water array afterwards
		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);
				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
					waters.add((Water)(tiles[x][y]));}
				else if (values[x][y] == 19){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
					waters.add((Water)(tiles[x][y]));}
				else if (values[x][y] == 20){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
					waters.add((Water)(tiles[x][y]));}
				else if (values[x][y] == 21){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
					waters.add((Water)(tiles[x][y]));}
				else if(values[x][y]==22){
					tiles[x][y]=new Slime(xPosition, y, yPosition, tileset.getImage("Slime"), this);
				}
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
					else
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 9, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}
			//write boolean to see if touching every water or something like that
			for(Water w:waters){
				if (w.getHitbox().isIntersecting(player.getHitbox())) {
					if(w.getFullness() == 1)
						water(w.getCol(), w.getRow(), map, 3);
					else
						water(w.getCol(), tileSize, map, 9);
					waters.remove(w);
					//i--;
					//System.out.println("Touching water");
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}

	//#############################################################################################################



//SLIME--------------------------------------------------------------------------------------------------
//precondition: there should be a tile, map, player, and slime block. also player needs to be able to jump
//postcondition: when the player jumps on the slime block, they jump higher. 
private void slime(float x, float y, int size,Level level){
	Slime s=new Slime(x, y, size, tileset.getImage("Slime"), this);

}


//GAS ---------------------------------------------------------------------------------------------------
//precondition: There should be a tile, map, player and gas flower. 
//postcondition: Gas appears when the player touches a gas flower and the gas spreads in a pattern. 
//post cont.: The gas spreads differently depending on where the player is and the boundaries/restrictions (map/block walls/restrictions).
	private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound){
		Gas g = new Gas(col, row, tileSize, tileset.getImage("GasOne"), this, 0);
		map.addTile(col, row, g);
		placedThisRound.add(g);
		numSquaresToFill--;
		int i = 0;
		while(numSquaresToFill>0 && i<placedThisRound.size()){
			Gas current = placedThisRound.get(i);
			int c = current.getCol();
			int r = current.getRow();
			//up
			if(numSquaresToFill>0 && r-1 >=0){
				if(!(map.getTiles()[c][r - 1] instanceof Gas) && !map.getTiles()[c][r - 1].isSolid()){
					Gas newG = new Gas(c, r - 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c, r - 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
				}
			}
			//up right
			if(numSquaresToFill>0 && r-1 >=0 && c+1<map.getTiles().length){
				if(!(map.getTiles()[c+1][r - 1] instanceof Gas) && !map.getTiles()[c+1][r - 1].isSolid()){
					Gas newG = new Gas(c+1, r - 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c+1, r - 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
				}
			}
			//up left
			if(numSquaresToFill>0 && r-1 >=0&&c-1 >=0){
				if(!(map.getTiles()[c-1][r - 1] instanceof Gas) && !map.getTiles()[c-1][r - 1].isSolid()){
					Gas newG = new Gas(c-1, r - 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c-1, r - 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
				}
			}
			//right
			if(numSquaresToFill > 0 &&c+1 < map.getTiles().length){
				if(!(map.getTiles()[c+1][r] instanceof Gas)&&!map.getTiles()[c + 1][r].isSolid()){
					Gas newG = new Gas(c + 1, r, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c +1,r, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
				}
			}
			//left
			if(numSquaresToFill>0&&c -1>= 0){
				if(!(map.getTiles()[c - 1][r] instanceof Gas) && !map.getTiles()[c - 1][r].isSolid()){
					Gas newG = new Gas(c - 1, r, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c - 1, r, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
				}
			}
			//down
			if(numSquaresToFill > 0 && r + 1 < map.getTiles()[0].length){
				if(!(map.getTiles()[c][r + 1] instanceof Gas) && !map.getTiles()[c][r + 1].isSolid()){
					Gas newG = new Gas(c, r + 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c, r + 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
				}
			}
			//down right
			if(numSquaresToFill > 0 && r + 1 < map.getTiles()[0].length&&c+1<map.getTiles().length){
				if(!(map.getTiles()[c+1][r + 1] instanceof Gas) && !map.getTiles()[c+1][r + 1].isSolid()){
					Gas newG = new Gas(c+1, r + 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c+1, r + 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
				}
			}
			//down left
			if(numSquaresToFill > 0 && r + 1 < map.getTiles()[0].length&&c-1>=0){
				if(!(map.getTiles()[c-1][r + 1] instanceof Gas) && !map.getTiles()[c-1][r + 1].isSolid()){
					Gas newG = new Gas(c-1, r + 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c-1, r + 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
				}
			}
			i++;
		}
	}

//WATER -----------------------------------------------------------------------------------------------
//precondition: There should be a map, player, and various water types. also a water flower
//postcondition: water spreads when the player touches a water flower and the water spread with different fullness across an area.
	private void water(int col, int row, Map map, int fullness) {
		//make water (You’ll need modify this to make different kinds of water such as half water and quarter water)
		//p = which image to get based on fullness
		String p="Full_water";
		Water w = new Water (col, row, tileSize, tileset.getImage(p), this, fullness);
		map.addTile(col, row, w);
		//waters.add(w); do this everytime new water is created. 
		Tile[][] t =map.getTiles();
		//in bounds
		if (col < 0 || col >= t.length || row < 0 || row >= t[0].length) {
			fullness=0;
			p="Full_water";
			map.addTile(col, row, w);
			waters.add(w);
			//return;
		}
		if (t[col][row] instanceof Water || t[col][row].isSolid()) {
			fullness=0;
			p="Full_water";
			map.addTile(col, row, w);
			waters.add(w);
			//return;
    	}
		//go down
		if (row + 1 < t[0].length && !t[col][row + 1].isSolid()) {
			fullness=3;
			p="Falling_water";
			map.addTile(col, row+1, w);
			waters.add(w);
    		//return;
    	}
    	//if we can’t go down go left and right.
		//right
		if (row + 1 < t[0].length && t[col][row + 1].isSolid()) {
			if (col + 1 < t.length && !(t[col + 1][row] instanceof Water) && !(t[col + 1][row].isSolid())) {
				fullness=2;
				p="Half_water";
				map.addTile(col+1, row, w);
				waters.add(w);
				if(col+2<t.length && !(t[col+2][row] instanceof Water) && !t[col+2][row].isSolid()){
					fullness=1;
					p="Quarter_water";
					map.addTile(col+2, row, w);
					waters.add(w);
				}
        	}
        	if (col - 1 >= 0 && !(t[col - 1][row] instanceof Water) && !t[col - 1][row].isSolid()) {
				fullness=2;
				p="Half_water";
				map.addTile(col-1, row, w);
				waters.add(w);
				if(col-2>=0 && !(t[col-2][row] instanceof Water)&& !t[col-2][row].isSolid()){
					fullness=1;
					p="Quarter_water";
					map.addTile(col-2,row,w);
					waters.add(w);
				}
        	}
    	}
	}

	public void draw(Graphics g) {
	   	 g.translate((int) -camera.getX(), (int) -camera.getY());
	   	 // Draw the map
	   	 for (int x = 0; x < map.getWidth(); x++) {
	   		 for (int y = 0; y < map.getHeight(); y++) {
	   			 Tile tile = map.getTiles()[x][y];
	   			 if (tile == null)
	   				 continue;
	   			 if(tile instanceof Gas) {
	   				
	   				 int adjacencyCount =0;
	   				 for(int i=-1; i<2; i++) {
	   					 for(int j =-1; j<2; j++) {
	   						 if(j!=0 || i!=0) {
	   							 if((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
	   								 if(map.getTiles()[x+i][y+j] instanceof Gas) {
	   									 adjacencyCount++;
	   								 }
	   							 }
	   						 }
	   					 }
	   				 }
	   				 if(adjacencyCount == 8) {
	   					 ((Gas)(tile)).setIntensity(2);
	   					 tile.setImage(tileset.getImage("GasThree"));
	   				 }
	   				 else if(adjacencyCount >5) {
	   					 ((Gas)(tile)).setIntensity(1);
	   					tile.setImage(tileset.getImage("GasTwo"));
	   				 }
	   				 else {
	   					 ((Gas)(tile)).setIntensity(0);
	   					tile.setImage(tileset.getImage("GasOne"));
	   				 }
	   			 }
	   			 if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
	   				 tile.draw(g);
	   		 }
	   	 }

	   	 // Draw the enemies
	   	 for (int i = 0; i < enemies.length; i++) {
	   		 enemies[i].draw(g);
	   	 }

	   	 // Draw the player
	   	 player.draw(g);

	   	 // used for debugging
	   	 if (Camera.SHOW_CAMERA)
	   		 camera.draw(g);
	   	 g.translate((int) +camera.getX(), (int) +camera.getY());
	    }

	

	
	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}