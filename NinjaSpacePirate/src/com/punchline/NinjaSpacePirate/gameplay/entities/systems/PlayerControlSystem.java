package com.punchline.NinjaSpacePirate.gameplay.entities.systems;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector2;
import com.punchline.NinjaSpacePirate.gameplay.entities.processes.PatrolProcess;
import com.punchline.javalib.entities.Entity;
import com.punchline.javalib.entities.components.physical.Velocity;
import com.punchline.javalib.entities.systems.InputSystem;
import com.punchline.javalib.utils.Convert;

/**
 * The PlayerControlSystem.
 * @author Natman64
 *
 */
public class PlayerControlSystem extends InputSystem {
	
	private static final float MIN_HORIZONTAL_SPEED = 0f;
	private static final float MAX_HORIZONTAL_SPEED = 1f;
	
	private static final float MIN_VERTICAL_SPEED = 1f;
	private static final float MAX_VERTICAL_SPEED = 3f;
	
	private float xVelocity = 0f;
	private float yVelocity = 0f;
	
	/**
	 * Constructs the PlayerControlSystem.
	 * @param input
	 */
	public PlayerControlSystem(InputMultiplexer input) {
		super(input);
	}

	@Override
	public boolean canProcess(Entity e) {
		return e.getTag().equals("Player");
	}

	@Override
	protected void process(Entity e) {
		Velocity v = e.getComponent(Velocity.class);
		
		v.setLinearVelocity(new Vector2(xVelocity, yVelocity));
	}

	@Override
	public void processEntities() {		
		super.processEntities();
	}

	//region Keyboard Controls
	
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		
		case Keys.LEFT:
			xVelocity = -MAX_HORIZONTAL_SPEED;
			return true;
			
		case Keys.RIGHT:
			xVelocity = MAX_HORIZONTAL_SPEED;
			return true;
			
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		
		case Keys.LEFT:
			xVelocity += MAX_HORIZONTAL_SPEED;
			return true;
			
		case Keys.RIGHT:
			xVelocity -= MAX_HORIZONTAL_SPEED;
			return true;
		
		}
			
		return false;
	}

	//endregion
	
	//region Tilt Controls
	
	@Override
	protected void onTiltX(float x) {
		
	}

	@Override
	protected void onTiltY(float y) {
		
	}

	@Override
	protected void onTiltZ(float z) {
		
	}
	
	//endregion
	
}
