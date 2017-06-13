package com.nosaj.mathblast;

import com.badlogic.gdx.math.Rectangle;

public class Asteroid {

	private Rectangle hitbox;
	private boolean hit;
	
	public Asteroid(Rectangle h, boolean b) {
		setHitbox(h);
		setHit(b);
	}

	public Rectangle getHitbox() {
		return hitbox;
	}

	public void setHitbox(Rectangle hitbox) {
		this.hitbox = hitbox;
	}

	public boolean isHit() {
		return hit;
	}

	public void setHit(boolean hit) {
		this.hit = hit;
	}

}
