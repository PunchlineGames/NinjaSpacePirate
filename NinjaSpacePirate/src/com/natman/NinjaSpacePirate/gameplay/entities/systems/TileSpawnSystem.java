package com.natman.NinjaSpacePirate.gameplay.entities.systems;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.lostcode.javalib.entities.Entity;
import com.lostcode.javalib.entities.EntityWorld;
import com.lostcode.javalib.entities.components.physical.Transform;
import com.lostcode.javalib.entities.systems.EntitySystem;
import com.lostcode.javalib.utils.LogManager;
import com.lostcode.javalib.utils.Random;
import com.natman.NinjaSpacePirate.gameplay.StealthWorld;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.LocationTemplate;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.TileArgs;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.TileRow;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.locations.DualGuardPostLocation;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.locations.GuardPostLocation;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.locations.HullBreachLocation;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.locations.PitLocation;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.rows.DoorRow;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.rows.EnemyRow;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.rows.MidBreachRow;
import com.natman.NinjaSpacePirate.gameplay.entities.systems.spawn.rows.PotionRow;

/**
 * System that spawns the tiles making up the game world.
 * @author Natman64
 *
 */
public class TileSpawnSystem extends EntitySystem {

	//region Constants
	
	private static final float ROW_SPAWN_DISTANCE = 12f;
	
	private static final int DIFFICULTY_RANGE = 5;
	
	private static final float MIN_ROW_BUFFER = 7;
	private static final float ROW_BUFFER_DELTA = PlayerControlSystem.SPEED_DELTA;
	
	private static final float COIN_SPAWN_CHANCE = 33f;
	
	//endregion
	
	//region Fields
	
	private static Random r = new Random();
	
	private float rowBuffer = MIN_ROW_BUFFER;
	private int y = StealthWorld.TILE_SPAWN_Y;
	private LinkedList<String> rowsToSpawn = new LinkedList<String>();
	private HashMap<String, TileRow> rowTemplates = new HashMap<String, TileRow>();
	private HashMap<String, LocationTemplate> locationTemplates = new HashMap<String, LocationTemplate>();
	
	private float totalElapsedTime = 0f;
	private float difficulty = 0;
	
	private Array<String> availableLocations = new Array<String>();
	private int highestDifficulty = 0;
	
	private String lastLocation;
	
	private Entity player;
	
	//endregion
	
	//region Initialization
	
	/**
	 * Constructs and initializes. a TileSpawnSystem.
	 */
	public TileSpawnSystem() {
		buildRowTemplates();
		buildLocationTemplates();
		
		//find out how far difficulty can go
		Iterator<Entry<String, LocationTemplate>> it = locationTemplates.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, LocationTemplate> entry = it.next();
			
			int difficulty = entry.getValue().getDifficulty();
			if (difficulty > highestDifficulty) {
				highestDifficulty = difficulty;
			}
		}
		
		queueSpawnLocation("HallSegment");
	}
	
	//endregion
	
	//region Rows
	
	private void buildRowTemplates() {
		//region TileArgs
		
		TileArgs floor = new TileArgs("Floor", false);
		TileArgs floorVent = new TileArgs("FloorVent", false);
		TileArgs floorGrate = new TileArgs("FloorGrate", false);
		TileArgs floorLight = new TileArgs("FloorLight", false);
		TileArgs floorHole = new TileArgs("FloorHole", false);
		TileArgs floorGreen = new TileArgs("FloorGreen", false);
		TileArgs floorGreenBlocked = new TileArgs("FloorGreen", true);
		TileArgs floorGreenDoor = new TileArgs("FloorGreenDoor", true);
		TileArgs floorDamaged0 = new TileArgs("FloorDamaged0", false);
		TileArgs floorDamaged1 = new TileArgs("FloorDamaged1", false);
		TileArgs floorDamaged2 = new TileArgs("FloorDamaged2", false);
		TileArgs floorDamaged3 = new TileArgs("FloorDamaged3", false);
		TileArgs floorPotionPad = new TileArgs("FloorPotionPad", false);
		
		TileArgs floorGuardRight = new TileArgs("Floor", false) {

			@Override
			public void onCreated(EntityWorld world, float x, float y) {
				world.createEntity("NPC", "whiteSuitMan", new Vector2(x, y), "", "Enemies", "Guard", 0f);
			}
			
		};
		
		TileArgs floorGuardLeft = new TileArgs("Floor", false) {

			@Override
			public void onCreated(EntityWorld world, float x, float y) {
				world.createEntity("NPC", "whiteSuitMan", new Vector2(x, y), "", "Enemies", "Guard", (float) Math.toRadians(180));
			}
			
		};
		
		TileArgs whiteWallVertical = new TileArgs("WhiteWallVertical", true);
		TileArgs whiteWallVentEast = new TileArgs("WhiteWallVentEast", true);
		TileArgs whiteWallVentWest = new TileArgs("WhiteWallVentWest", true);
		TileArgs whiteWallGreenLightEast = new TileArgs("WhiteWallGreenLightEast", true);
		TileArgs whiteWallGreenLightWest = new TileArgs("WhiteWallGreenLightWest", true);
		TileArgs whiteWallRedLightEast = new TileArgs("WhiteWallRedLightEast", true);
		TileArgs whiteWallRedLightWest = new TileArgs("WhiteWallRedLightWest", true);
		TileArgs whiteWallVerticalDamaged = new TileArgs("WhiteWallVerticalDamaged", true);
		
		TileArgs floorCoin = new TileArgs("Floor", false) {
			@Override
			public void onCreated(EntityWorld world, float x, float y) {
				if (r.percent(COIN_SPAWN_CHANCE)) {
					world.createEntity("Coin", new Vector2(x, y));
				}
			}
		};
		
		TileArgs floorDamagedCoin = new TileArgs("FloorDamaged3", false) {
			@Override
			public void onCreated(EntityWorld world, float x, float y) {
				if (r.percent(COIN_SPAWN_CHANCE)) {
					world.createEntity("Coin", new Vector2(x, y));
				}
			}
		};
		
		//endregion
		
		//region Hall
		
		TileArgs[] args = new TileArgs[TileRow.ROW_SIZE];
		
		args[0] = whiteWallVertical;
		args[1] = floorLight;
		args[2] = floor;
		args[3] = floor;
		args[4] = floor;
		args[5] = floorLight;
		args[6] = whiteWallVertical;
		
		rowTemplates.put("HallSegment", new TileRow(args));
		rowTemplates.put("HallSegmentEnemy", new EnemyRow(args));
		
		args[0] = whiteWallVentEast;
		args[6] = whiteWallVentWest;
		
		rowTemplates.put("HallSegmentWallVents", new TileRow(args));
		
		args[0] = whiteWallVertical;
		args[3] = floorVent;
		args[6] = whiteWallVertical;
		
		rowTemplates.put("HallSegmentFloorVent", new TileRow(args));
		
		args[0] = whiteWallGreenLightEast;
		args[3] = floor;
		args[6] = whiteWallGreenLightWest;
		
		rowTemplates.put("HallSegmentWallRedLights", new TileRow(args));
		
		args[0] = floorGreenBlocked;
		args[1] = floorCoin;
		args[5] = floorCoin;
		args[6] = floorGreenBlocked;
		
		rowTemplates.put("HallSegmentDoors", new DoorRow(args));
		
		//endregion
		
		//region Pits
		
		args[0] = whiteWallVertical;
		args[1] = floorHole;
		args[2] = floorHole;
		args[3] = floorHole;
		args[4] = floorHole;
		args[5] = floorHole;
		args[6] = whiteWallVertical;
		
		args[1] = floorGrate;
		
		rowTemplates.put("PitGrate0", new TileRow(args));
		
		args[1] = floorHole;
		args[2] = floorGrate;
		
		rowTemplates.put("PitGrate1", new TileRow(args));
		
		args[2] = floorHole;
		args[3] = floorGrate;
		
		rowTemplates.put("PitGrate2", new TileRow(args));
		
		args[3] = floorHole;
		args[4] = floorGrate;
		
		rowTemplates.put("PitGrate3", new TileRow(args));
		
		args[4] = floorHole;
		args[5] = floorGrate;
		
		rowTemplates.put("PitGrate4", new TileRow(args));
		
		//endregion
		
		//region Hull Breach
		
		//Left side
		args[0] = whiteWallVerticalDamaged;
		args[1] = floorDamaged0;
		args[2] = floor;
		args[3] = floor;
		args[4] = floor;
		args[5] = floorLight;
		args[6] = whiteWallVertical;
		
		rowTemplates.put("LeftBreach0", new TileRow(args));
		
		args[0] = floorHole;
		args[1] = floorDamaged3;
		args[2] = floorDamaged1;
		
		rowTemplates.put("LeftBreach1", new TileRow(args));
		
		args[1] = floorHole;
		args[2] = floorDamagedCoin;
		
		rowTemplates.put("LeftBreach2", new TileRow(args));
		
		args[3] = floorDamaged2;
		
		rowTemplates.put("LeftBreach3", new MidBreachRow(true, args));
		
		args[2] = floorDamaged3;
		args[3] = floor;
		
		rowTemplates.put("LeftBreach4", new TileRow(args));
		
		args[1] = floorDamaged0;
		
		rowTemplates.put("LeftBreach5", new TileRow(args));
		
		args[0] = whiteWallVerticalDamaged;
		args[1] = floorDamaged3;
		args[2] = floor;
		
		rowTemplates.put("LeftBreach6", new TileRow(args));
		
		//Right side
		args[0] = whiteWallVertical;
		args[1] = floorLight;
		args[2] = floor;
		args[3] = floor;
		args[4] = floor;
		args[5] = floorDamaged0;
		args[6] = whiteWallVerticalDamaged;
		
		rowTemplates.put("RightBreach0", new TileRow(args));
		
		args[6] = floorHole;
		args[5] = floorDamaged3;
		args[4] = floorDamaged1;
		
		rowTemplates.put("RightBreach1", new TileRow(args));
		
		args[5] = floorHole;
		args[4] = floorDamagedCoin;
		
		rowTemplates.put("RightBreach2", new TileRow(args));
		
		args[3] = floorDamaged2;
		
		rowTemplates.put("RightBreach3", new MidBreachRow(false, args));
		
		args[4] = floorDamaged3;
		args[3] = floor;
		
		rowTemplates.put("RightBreach4", new TileRow(args));
		
		args[5] = floorDamaged0;
		
		rowTemplates.put("RightBreach5", new TileRow(args));
		
		args[6] = whiteWallVerticalDamaged;
		args[5] = floorDamaged3;
		args[4] = floor;
		
		rowTemplates.put("RightBreach6", new TileRow(args));
		
		//endregion
	
		//region Guard Posts
		
		//region Left
		
		args[0] = whiteWallRedLightEast;
		args[1] = floorLight;
		args[2] = floor;
		args[3] = floor;
		args[4] = floor;
		args[5] = floorLight;
		args[6] = whiteWallVertical;
		
		rowTemplates.put("LeftGuardPost0", new TileRow(args));
		rowTemplates.put("LeftGuardPost2", new TileRow(args));
		
		args[0] = floorGreenDoor;
		args[1] = floorCoin;
		args[2] = floorGuardRight;
		
		rowTemplates.put("LeftGuardPost1", new TileRow(args));
		
		//endregion
		
		//region Right
		
		args[0] = whiteWallVertical;
		args[1] = floorLight;
		args[2] = floor;
		args[3] = floor;
		args[4] = floor;
		args[5] = floorLight;
		args[6] = whiteWallRedLightWest;
		
		rowTemplates.put("RightGuardPost0", new TileRow(args));
		rowTemplates.put("RightGuardPost2", new TileRow(args));
		
		args[4] = floorGuardLeft;
		args[5] = floorCoin;
		args[6] = floorGreenDoor;
		
		rowTemplates.put("RightGuardPost1", new TileRow(args));
		
		//endregion
		
		//region Both
		
		args[0] = whiteWallRedLightWest;
		args[1] = floorLight;
		args[2] = floor;
		args[3] = floor;
		args[4] = floor;
		args[5] = floorLight;
		args[6] = whiteWallRedLightWest;
		
		rowTemplates.put("DualGuardPost0", new TileRow(args));
		rowTemplates.put("DualGuardPost2", new TileRow(args));
		
		args[0] = floorGreenDoor;
		args[1] = floorGuardRight;
		args[3] = floorCoin;
		args[5] = floorGuardLeft;
		args[6] = floorGreenDoor;
		
		rowTemplates.put("DualGuardPost1", new TileRow(args));
		
		//endregion
		
		//endregion
		
		//region Potion
		
		args[0] = whiteWallGreenLightEast;
		args[1] = floorLight;
		args[2] = floorGreen;
		args[3] = floorGreen;
		args[4] = floorGreen;
		args[5] = floorLight;
		args[6] = whiteWallGreenLightWest;
		
		rowTemplates.put("Potion0", new TileRow(args));
		
		rowTemplates.put("Potion2", new TileRow(args));
		
		args[0] = whiteWallVertical;
		args[1] = floorLight;
		args[2] = floorGreen;
		args[3] = floorPotionPad;
		args[4] = floorGreen;
		args[5] = floorLight;
		args[6] = whiteWallVertical;
		
		rowTemplates.put("Potion1", new PotionRow(args));
		
		//endregion
		
	}
	
	//endregion
	
	//region Locations
	
	private void buildLocationTemplates() {
		String[] loc = new String[15];
		loc[0] = "HallSegment";
		loc[1] = "HallSegment";
		loc[2] = "HallSegment";
		loc[3] = "HallSegmentWallVents";
		loc[4] = "HallSegment";
		loc[5] = "HallSegment";
		loc[6] = "HallSegment";
		loc[7] = "HallSegmentFloorVent";
		loc[8] = "HallSegment";
		loc[9] = "HallSegment";
		loc[10] = "HallSegment";
		loc[11] = "HallSegmentWallVents";
		loc[12] = "HallSegment";
		loc[13] = "HallSegment";
		loc[14] = "HallSegment";
		
		locationTemplates.put("HallSegment", new LocationTemplate(loc, -1, 1));
		
		String[] potionLoc = new String[7];
		potionLoc[0] = "HallSegment";
		potionLoc[1] = "Potion0";
		potionLoc[2] = "Potion1";
		potionLoc[3] = "Potion2";
		potionLoc[4] = "HallSegment";
		potionLoc[5] = "HallSegment";
		potionLoc[6] = "HallSegment";
		
		locationTemplates.put("PotionLocation", new LocationTemplate(potionLoc, -1, 1));
		
		loc = null;
		loc = new String[5];
		loc[0] = "HallSegmentEnemy";
		loc[1] = "HallSegment";
		loc[2] = "HallSegment";
		loc[3] = "HallSegment";
		loc[4] = "HallSegment";
		
		locationTemplates.put("EnemyLocation1", new LocationTemplate(loc, 2, 2));
		
		loc = null;
		loc = new String[8];
		loc[0] = "HallSegmentEnemy";
		loc[1] = "HallSegment";
		loc[2] = "HallSegment";
		loc[3] = "HallSegmentEnemy";
		loc[4] = "HallSegment";
		loc[5] = "HallSegment";
		loc[6] = "HallSegmentEnemy";
		locationTemplates.put("EnemyLocation2", new LocationTemplate(loc, 4, 1));
		
		loc = null;
		loc = new String[15];
		loc[0] = "HallSegment";
		loc[1] = "HallSegment";
		loc[2] = "HallSegment";
		loc[3] = "HallSegmentWallVents";
		loc[4] = "HallSegment";
		loc[5] = "HallSegment";
		loc[6] = "HallSegmentWallRedLights";
		loc[7] = "HallSegmentDoors";
		loc[8] = "HallSegmentWallRedLights";
		loc[9] = "HallSegment";
		loc[10] = "HallSegment";
		loc[11] = "HallSegmentWallVents";
		loc[12] = "HallSegment";
		loc[13] = "HallSegment";
		loc[14] = "HallSegment";
		
		locationTemplates.put("HallSegmentDoors", new LocationTemplate(loc, 1, 3));
		
		locationTemplates.put("GuardPost", new GuardPostLocation(2, 2));
		locationTemplates.put("DualGuardPost", new DualGuardPostLocation(4, 2));
		
		LocationTemplate pitLocation = new PitLocation(1, 2);
		
		locationTemplates.put("PitGrate", pitLocation);
		
		LocationTemplate breachLocation = new HullBreachLocation(3, 2);
		
		locationTemplates.put("Breach", breachLocation);
	}
	
	//endregion
	
	//region Disposal
	
	@Override
	public void dispose() { }

	//endregion
	
	//region Processing
	
	@Override
	public void processEntities() {
		super.processEntities();
		
		if (player == null) return;
		
		calculateDifficulty(deltaSeconds());
		
		Transform t = player.getComponent(Transform.class);
		
		if (t == null) return;
		
		rowBuffer += ROW_BUFFER_DELTA * deltaSeconds();
		
		if (rowsToSpawn.size() <= (int) rowBuffer) {
			queueNextLocation();
		}
		
		while (t.getPosition().y + ROW_SPAWN_DISTANCE >= y) {
			spawnRow(rowsToSpawn.removeFirst());
		}
	}
	
	private void calculateDifficulty(float deltaTime) {
		totalElapsedTime += deltaTime;
		
		int oldDifficulty = (int) difficulty;
		if (oldDifficulty == 0) oldDifficulty = -1;
		difficulty = (totalElapsedTime + 30f) / 30f; //add the extra count to avoid 0 casting.
		
		if (difficulty > highestDifficulty) difficulty = highestDifficulty;
		
		if ((int) difficulty - oldDifficulty >= 1) {
			Iterator<Entry<String, LocationTemplate>> it = locationTemplates.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, LocationTemplate> entry = it.next();
				
				if (entry.getValue().getDifficulty() <= difficulty && !availableLocations.contains(entry.getKey(), false)) {
					for (int i = 0; i < entry.getValue().getWeight(); i++) {
						availableLocations.add(entry.getKey()); //weight the picking
					}
				} else if (availableLocations.contains(entry.getKey(), false) //don't spawn locations that are too easy now
						&& difficulty - entry.getValue().getDifficulty() > DIFFICULTY_RANGE && entry.getValue().getDifficulty() > 0) {
					
					while (availableLocations.contains(entry.getKey(), false)) {
						availableLocations.removeValue(entry.getKey(), false); //remove all occurrences
					}
				}
			}
		}
		
	}
	
	//endregion
	
	//region Entity Processing
	
	@Override
	protected void process(Entity e) { 
		this.player = e;
	}

	@Override
	public boolean canProcess(Entity e) { 
		return e.getTag().equals("Player");
	}
	
	//endregion

	//region Helpers
	
	private void spawnRow(String rowKey) {
		TileRow row = rowTemplates.get(rowKey);
		
		if (row == null) {
			LogManager.error("Space Pirate Log", "Invalid row key passed: " + rowKey);
			return;
		}
		
		int i = 0;
		for (int x = -TileRow.ROW_SIZE / 2; x <= TileRow.ROW_SIZE / 2; x++, i++) {
			TileArgs args = row.tiles[i];
			
			world.createEntity("Tile", args.spriteKey, new Vector2(x, y), args.blocked);
			args.onCreated(world, x, y);
		}
		
		row.onCreated(world, y);
		
		LogManager.debug("World", rowKey + " row spawned");
		
		y++;
	}
	
	private void queueSpawnRow(String rowKey) {
		rowsToSpawn.add(rowKey);
		
		LogManager.debug("World", rowKey + " row queued");
	}
	
	private void queueSpawnLocation(String locKey) {
		String[] loc = locationTemplates.get(locKey).getRows();
		
		for (String row : loc) {
			queueSpawnRow(row);
		}
		
		locationTemplates.get(locKey).onQueue(world);
		
		LogManager.debug("World", locKey + " location queued");
	}
	
	private void queueNextLocation() {
		//This is where the system decides what obstacle to throw at the player next.
		
		String locToSpawn = "";
		
		while (locToSpawn.isEmpty()) {
			int index = r.nextInt(availableLocations.size);
			
			String loc = availableLocations.get(index);
			
			if (!loc.equals(lastLocation)) {
				locToSpawn = loc;
			}
		}
		
		lastLocation = locToSpawn; //never spawn the same location twice in a row
		queueSpawnLocation(locToSpawn);
	}
	
	//endregion
	
}
