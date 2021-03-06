package com.natman.NinjaSpacePirate.gameplay.entities.templates;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.lostcode.javalib.entities.Entity;
import com.lostcode.javalib.entities.EntityWorld;
import com.lostcode.javalib.entities.components.generic.Health;
import com.lostcode.javalib.entities.components.generic.View;
import com.lostcode.javalib.entities.components.physical.Body;
import com.lostcode.javalib.entities.components.physical.Collidable;
import com.lostcode.javalib.entities.components.render.Renderable;
import com.lostcode.javalib.entities.events.EventCallback;
import com.lostcode.javalib.entities.processes.ProcessState;
import com.lostcode.javalib.entities.templates.EntityTemplate;
import com.lostcode.javalib.utils.Convert;
import com.lostcode.javalib.utils.SoundManager;
import com.natman.NinjaSpacePirate.gameplay.GameScore;
import com.natman.NinjaSpacePirate.gameplay.Stats;
import com.natman.NinjaSpacePirate.gameplay.StealthWorld;
import com.natman.NinjaSpacePirate.gameplay.entities.components.EnemyCollisionHandler;
import com.natman.NinjaSpacePirate.gameplay.entities.components.render.OldMultiRenderable;
import com.natman.NinjaSpacePirate.gameplay.entities.components.render.npcs.NPCMultiRenderable;
import com.natman.NinjaSpacePirate.gameplay.entities.processes.ChasePlayerProcess;
import com.natman.NinjaSpacePirate.gameplay.entities.processes.MovementProcess;
import com.natman.NinjaSpacePirate.gameplay.entities.processes.powerups.GhostPowerup;
import com.natman.NinjaSpacePirate.gameplay.stats.IntStat;

/**
 * Template used to create NPCs.
 * @author Natman64
 *
 */
public class NPCTemplate implements EntityTemplate {
	
	//Body constants
	private static final float BODY_RADIUS = Convert.pixelsToMeters(2.5f);
	
	//View constants
	private static final float VIEW_RANGE = Convert.pixelsToMeters(17.5f);
	private static final float VIEW_FOV = 0.18f;
	
	private BodyDef bodyDef;
	private FixtureDef fixtureDef;
	private CircleShape shape;
	
	/**
	 * Constructs the NPCTemplate.
	 */
	public NPCTemplate() {
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.KinematicBody;
		
		shape = new CircleShape();
		shape.setRadius(BODY_RADIUS);
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
	}
	
	@Override
	public Entity buildEntity(final Entity e, EntityWorld world, Object... args) {		
		String spriteKey = (String) args[0];
		Vector2 position = (Vector2) args[1];
		String tag = (String) args[2];
		String group = (String) args[3];
		String type = (String) args[4];
		
		Vector2 velocity = new Vector2();
		Float rotation = (float)Math.toRadians(270);
		MovementProcess movement = null;
		if (args.length > 5) {
			Object optionalArg = args[5];
			
			if (optionalArg instanceof Vector2) {
				velocity = (Vector2) args[5];
			} else if (optionalArg instanceof Float) {
				rotation = (Float) args[5];
			} else if (optionalArg instanceof MovementProcess) {
				movement = (MovementProcess) optionalArg;
			}
		}
		
		e.init(tag, group, type);
		
		Body body = new Body(world, e, bodyDef, fixtureDef);
		body.setPosition(position);
		body.setRotation(rotation);
		body.setLinearVelocity(velocity);
		e.addComponent(body);

		Collidable collidable = new EnemyCollisionHandler();
		e.addComponent(collidable);
		
		Health health = new Health(e, world, 5f);
		health.deleteOnDeath = false;
		
		health.onDeath.addCallback(this, new EventCallback() {

			@Override
			public void invoke(Entity e, Object... args) {
				NPCMultiRenderable sprite = (NPCMultiRenderable) e.getComponent(Renderable.class);
				
				sprite.die();
				sprite.setLayer(2);
				
				Body body = e.getComponent(Body.class);
				body.setLinearVelocity(new Vector2(0, 0));
				
				body.getBody().getFixtureList().get(0).setSensor(true);
				
				SoundManager.playSound("Player_Hit");
				
				ChasePlayerProcess ps = e.getComponent(ChasePlayerProcess.class);
				if (ps != null) ps.end(ProcessState.ABORTED);
				
				IntStat stat = (IntStat) Stats.getStat("Enemies Killed");
				stat.increment();
				
				StealthWorld world = (StealthWorld) args[0];
				GameScore score = world.getScore();
				score.kills++;
			}
			
		});
		
		e.addComponent(health);
		
		View view = new View(e, VIEW_RANGE, VIEW_FOV) {

			ChasePlayerProcess ps;
			
			@Override
			public void onDetected(Entity es, EntityWorld world) {
				super.onDetected(es, world);
				
				if (!es.hasComponent(GhostPowerup.class) && ps == null)
				{
					if (owner.hasComponent(MovementProcess.class)) {
						MovementProcess m = owner.getComponent(MovementProcess.class);
						owner.removeComponent(m);
					}
					
					ps = new ChasePlayerProcess(world, e, ((StealthWorld) world).getPlayer());
					world.getProcessManager().attach(ps);
					((IntStat) Stats.getStat("Times Seen")).increment();
				}

			}
			
		};
		e.addComponent(view);
		
		OldMultiRenderable mr = new NPCMultiRenderable(world.getSpriteSheet(), spriteKey, view);
		
		e.addComponent(mr);
		
		if (movement != null) {
			e.addComponent(movement);
		}
		
		return e;
	}
	
	@Override
	public void dispose() {
		shape.dispose();
	}

}
