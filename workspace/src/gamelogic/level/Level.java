package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

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


public class Level{

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
	private ArrayList<Slime> slimes = new ArrayList<>();
	private ArrayList<Gas> gases = new ArrayList<>();
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
					tiles[x][y]=new Slime(xPosition, yPosition, tileSize, tileset.getImage("Slime"), this);
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
			for (Water w : waters) {
				if (w.getHitbox().isIntersecting(player.getHitbox())) {
					if (!player.isInWater) {
						player.waterStartTime = System.currentTimeMillis();
						player.isInWater=true;
					}
					long timeInWater=System.currentTimeMillis()-player.waterStartTime;
					if (timeInWater>=5000) {
						onPlayerDeath();
					}
					if (w.getFullness()==1) {
						water(w.getCol(), w.getRow(), map, 3);
					} else {
						water(w.getCol(), tileSize, map, 9);
					}
					waters.remove(w);
					break;
				}
			}
		}
		for(Gas g:gases){
			if(g.getHitbox().isIntersecting(player.getHitbox())){
				long timeInGas=System.currentTimeMillis()-player.gasStartTime;
				if (timeInGas>=5000 && timeInGas<=15000) {
					player.setC(Color.BLUE);
					player.walkSpeed=300;
				}
				if (timeInGas>=15000) {
					onPlayerDeath();
				}
				break;
			}
		}
			//change jump power when player is touching slime
			for(Slime s:slimes){
				if(s.getHitbox().isIntersecting(player.getHitbox())){
					player.jumpPower=1450;
				}
				else{
					player.jumpPower=1350;
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

			//if the player is not touching water, they are not in water (isInWater=false) and the timer is reset and stopped.
			if (!isPlayerTouchingWater()) {
    			player.isInWater = false;
    			player.waterStartTime = 0;
			}
			//if the player is not touching gas, they are not in gas (isInGas=false) and the timer is reset and stopped.
			if (!isPlayerTouchingGas()) {
    			player.isInGas=false;
    			player.gasStartTime=0;
			}
		}
//SLIME--------------------------------------------------------------------------------------------------
//precondition: there should be a tile, map, player, and slime block. also player needs to be able to jump
//postcondition: when the player jumps on the slime block, they jump higher. 
private void slime(float x, float y, int size,Level level){
	Slime s=new Slime(x, y, size, tileset.getImage("Slime"), this);
	slimes.add(s);
	if(s.getHitbox().isIntersecting(player.getHitbox())){
		player.jumpPower=1450;
	}
	else{
		player.jumpPower=1350;
	}
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
					gases.add(g);
				}
			}
			//up right
			if(numSquaresToFill>0 && r-1 >=0 && c+1<map.getTiles().length){
				if(!(map.getTiles()[c+1][r - 1] instanceof Gas) && !map.getTiles()[c+1][r - 1].isSolid()){
					Gas newG = new Gas(c+1, r - 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c+1, r - 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
					gases.add(g);
				}
			}
			//up left
			if(numSquaresToFill>0 && r-1 >=0&&c-1 >=0){
				if(!(map.getTiles()[c-1][r - 1] instanceof Gas) && !map.getTiles()[c-1][r - 1].isSolid()){
					Gas newG = new Gas(c-1, r - 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c-1, r - 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
					gases.add(g);
				}
			}
			//right
			if(numSquaresToFill > 0 &&c+1 < map.getTiles().length){
				if(!(map.getTiles()[c+1][r] instanceof Gas)&&!map.getTiles()[c + 1][r].isSolid()){
					Gas newG = new Gas(c + 1, r, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c +1,r, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
					gases.add(g);
				}
			}
			//left
			if(numSquaresToFill>0&&c -1>= 0){
				if(!(map.getTiles()[c - 1][r] instanceof Gas) && !map.getTiles()[c - 1][r].isSolid()){
					Gas newG = new Gas(c - 1, r, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c - 1, r, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
					gases.add(g);
				}
			}
			//down
			if(numSquaresToFill > 0 && r + 1 < map.getTiles()[0].length){
				if(!(map.getTiles()[c][r + 1] instanceof Gas) && !map.getTiles()[c][r + 1].isSolid()){
					Gas newG = new Gas(c, r + 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c, r + 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
					gases.add(g);
				}
			}
			//down right
			if(numSquaresToFill > 0 && r + 1 < map.getTiles()[0].length&&c+1<map.getTiles().length){
				if(!(map.getTiles()[c+1][r + 1] instanceof Gas) && !map.getTiles()[c+1][r + 1].isSolid()){
					Gas newG = new Gas(c+1, r + 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c+1, r + 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
					gases.add(g);
				}
			}
			//down left
			if(numSquaresToFill > 0 && r + 1 < map.getTiles()[0].length&&c-1>=0){
				if(!(map.getTiles()[c-1][r + 1] instanceof Gas) && !map.getTiles()[c-1][r + 1].isSolid()){
					Gas newG = new Gas(c-1, r + 1, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c-1, r + 1, newG);
					placedThisRound.add(newG);
					numSquaresToFill--;
					gases.add(g);
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
		if (col < 0 || col > map.getTiles().length || row < 0 || row > map.getTiles()[0].length)
			return;
		if (map.getTiles()[col][row].isSolid() || map.getTiles()[col][row] instanceof Water)
			return;
		if (fullness == 3) {
			map.addTile(col, row, new Water(col, row, tileSize, tileset.getImage("Full_water"), this, fullness)); 
		} else if (fullness == 2) {
			map.addTile(col, row, new Water(col, row, tileSize, tileset.getImage("Half_water"), this, fullness)); 
		} else if (fullness == 1) {
			map.addTile(col, row, new Water(col, row, tileSize, tileset.getImage("Quarter_water"), this, fullness)); 
		} else {
			map.addTile(col, row, new Water(col, row, tileSize, tileset.getImage("Falling_water"), this, fullness)); 
		}
		if (row + 1 < map.getTiles()[0].length && !map.getTiles()[col][row + 1].isSolid() && !(map.getTiles()[col][row + 1] instanceof Water)) {
			water(col, row + 1, map, 0);
			if(!(map.getTiles()[col][row+2].isSolid())){
				water(col, row + 1, map, 3);
			}
			//return;
		}
		if (col + 1 < map.getTiles().length && !map.getTiles()[col + 1][row].isSolid() && !(map.getTiles()[col + 1][row] instanceof Water)) {
			if (fullness == 3 && map.getTiles()[col + 1][row + 1].isSolid())
				water(col + 1, row, map, 2);
			else if ((fullness == 2 || fullness == 1) && map.getTiles()[col + 1][row + 1].isSolid())
				water(col + 1, row, map, 1);
			else if (!map.getTiles()[col + 1][row + 1].isSolid())
				water(col + 1, row + 1, map, 0);
		}
		if (col - 1 >= 0 && !map.getTiles()[col - 1][row].isSolid() && !(map.getTiles()[col - 1][row] instanceof Water)) {
			if (fullness == 3 && map.getTiles()[col - 1][row + 1].isSolid())
				water(col - 1, row, map, 2);
			else if ((fullness == 2 || fullness == 1) && map.getTiles()[col - 1][row + 1].isSolid())
				water(col - 1, row, map, 1);
			else if (!map.getTiles()[col - 1][row + 1].isSolid())
				water(col - 1, row + 1, map, 0);
		}
	}
	//---------------------------------------------------------------------------------------------------------------------------------------
	//precondition: there should be a waters array with Water objects, there should be a player with a hitbox and water should have a hitbox.
	//postcondition:checks if the player hitbox is intersecting the water hitbox. (player in/touching water)
	public boolean isPlayerTouchingWater() {
    	for (Water w : waters) {
        	if (w.getHitbox().isIntersecting(player.getHitbox())) {
            	return true;
        	}
    	}
    	return false;
	}
	//precondition: there should be a gases array with Gas objects, there should be a player with a hitbox and gas should have a hitbox.
	//postcondition: checks if the player hitbox is interesting the gas hitbox. (player in/touching gas)
	public boolean isPlayerTouchingGas() {
    	for (Gas g : gases) {
        	if (g.getHitbox().isIntersecting(player.getHitbox())) {
            	return true;
        	}
    	}
    	return false;
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