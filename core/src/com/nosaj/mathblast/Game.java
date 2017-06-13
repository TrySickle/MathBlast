package com.nosaj.mathblast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Game extends ApplicationAdapter implements InputProcessor {

	SpriteBatch 			batch; // drawing
	Texture 				starFieldTexture, laserTexture, asteroidTexture; // textures
	Rectangle 				starField1, starField2, start, ship, back, play, instructions; // scrolling background rects
	ArrayList<Rectangle> 	lasers; // contains laser rects
	ArrayList<Asteroid> 	asteroids; // contains 4 asteroids
	ArrayList<Integer> 		answers; // contains the 4 answers, 1 correct, 3 random
	String 					problem; // contains the question in string format
	Asteroid 				asteroid1, asteroid2, asteroid3, asteroid4; // the 4 asteroids
	Random 					random; // rand gen
	BitmapFont 				font;
	Animation 				shipAnimation, expAnimation; // animation objects       
	Texture 				shipSheet, explosionSheet; // sprite sheets
	TextureRegion[] 		shipFrames, expFrames; // contains all frames
	TextureRegion 			currentFrame, curFrame2; // the current frame
	Sound					laserSound, expSound, refSound;
	Music					theme;
	Vector2					lastTouch;
	
	private static final int FRAME_COLS = 1; // for the ship         
	private static final int FRAME_ROWS = 2; // animation
	private static final int COLS2 = 17; // for the explosion
	private static final int ROWS2 = 1; // animation

	float laserWidth, laserHeight, hOffset1, vOffset1, vOffset2, laserSpeed; // laser variables
	float asteroidX, asteroidDim, asteroidSpeed, asteroidDiff; // asteroid variables
	float explosionX; // need to keep explosion stationary

	boolean firstFire; // only one laser fire at a time
	boolean notExploded; // for asteroid
	boolean firstGen; // for problem generation
	boolean reflected;
	boolean moving, shooting;
	int movePointer, shootPointer;
	int correct; // the correct answer's index within scrambled
	int score; // player score
	float stateTime, stateTime2, loadDelay, deathDelay; // animation counter                                        
	int scrollSpeed; // speed of background
	int level;
	int counter;

	enum GameState {
		MENU, INSTRUCTIONS, LOADING, PLAYING, GAMEOVER
	}

	GameState state;

	public void initialize() {
		// variable initialization
		scrollSpeed = Gdx.graphics.getWidth() / 200;
		ship = new Rectangle(Gdx.graphics.getWidth() / 100, Gdx.graphics.getHeight() / 10, Gdx.graphics.getWidth() / 7, Gdx.graphics.getWidth() / 7);
		firstFire = true;
		laserWidth = ship.width / 3;
		laserHeight = ship.width / 10;
		laserSpeed = Gdx.graphics.getWidth() / 50;
		hOffset1 = ship.width / 2;
		vOffset1 = ship.width / 4;
		vOffset2 = ship.width * 5 / 8;
		asteroidX = Gdx.graphics.getWidth() - asteroidDim;
		asteroidDim = Gdx.graphics.getHeight() / 6;
		asteroidSpeed = 1000;
		asteroidDiff = Gdx.graphics.getHeight() - asteroidDim;
		notExploded = true;
		random = new Random();
		firstGen = true;
		level = 1;
		correct = 0;
		score = 0;
		problem = "";
		reflected = false;
		counter = 0;
		loadDelay = 0;
		Gdx.input.setInputProcessor(this);
		moving = false;
		shooting = false;

		// rectangles and containers
		starField1 = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		starField2 = new Rectangle(Gdx.graphics.getWidth(), 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		lasers = new ArrayList<Rectangle>();
		asteroids = new ArrayList<Asteroid>();
		answers = new ArrayList<Integer>();
		asteroid1 = new Asteroid (new Rectangle(asteroidX, 0, asteroidDim, asteroidDim), false);
		asteroid2 = new Asteroid (new Rectangle(asteroidX, asteroidDiff / 3, asteroidDim, asteroidDim), false);
		asteroid3 = new Asteroid (new Rectangle(asteroidX, asteroidDiff * 2 / 3, asteroidDim, asteroidDim), false);
		asteroid4 = new Asteroid (new Rectangle(asteroidX, Gdx.graphics.getHeight() - asteroidDim, asteroidDim, asteroidDim), false);
		asteroids.add(asteroid1);
		asteroids.add(asteroid2);
		asteroids.add(asteroid3);
		asteroids.add(asteroid4);
		
		start = new Rectangle(Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 2, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 6);
		instructions = new Rectangle(Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() * 2 / 3, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 6);
		back = new Rectangle(0, Gdx.graphics.getHeight() * 5 / 6, Gdx.graphics.getWidth() / 6, Gdx.graphics.getHeight() / 5);
		play = new Rectangle(Gdx.graphics.getWidth() * 5 / 6, Gdx.graphics.getHeight() * 5 / 6, Gdx.graphics.getWidth() / 6, Gdx.graphics.getHeight() / 5);
	}

	public void load() {
		// file loading
		batch = new SpriteBatch();
		starFieldTexture = new Texture("stars.png");
		shipSheet = new Texture("shipsheet2.png");
		laserTexture = new Texture("laserBlue03.png");
		asteroidTexture = new Texture("asteroid.png");
		explosionSheet = new Texture("explosion.png");
		font = new BitmapFont(Gdx.files.internal("minecraft.fnt"));
		laserSound = Gdx.audio.newSound(Gdx.files.internal("slimeball.wav"));
		expSound = Gdx.audio.newSound(Gdx.files.internal("8bit_bomb_explosion.wav"));
		theme = Gdx.audio.newMusic(Gdx.files.internal("through space.mp3"));
		refSound = Gdx.audio.newSound(Gdx.files.internal("flaunch.wav"));
	}

	public void animate() {
		// ship animation copypasta
		TextureRegion[][] tmp = TextureRegion.split(shipSheet, shipSheet.getWidth()/FRAME_COLS, shipSheet.getHeight()/FRAME_ROWS);  
		shipFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		int index = 0;
		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				shipFrames[index++] = tmp[i][j];
			}
		}

		// explosion animation
		tmp = TextureRegion.split(explosionSheet, explosionSheet.getWidth()/COLS2, explosionSheet.getHeight()/ROWS2);
		expFrames = new TextureRegion[COLS2 * ROWS2];
		index = 0;
		for (int i = 0; i < ROWS2; i++) {
			for (int j = 0; j < COLS2; j++) {
				expFrames[index++] = tmp[i][j];
			}
		}

		// frame delay
		shipAnimation = new Animation(0.050f, shipFrames);    
		stateTime = 0f;                         

		expAnimation = new Animation(.025f, expFrames);    
		stateTime2 = 0f; 
	}

	@Override
	public void create () {
		state = GameState.MENU;
		load();
		initialize();
		animate();

		theme.setLooping(true);
		theme.play();
	}

	public void createRandomMath(int level) {
		// switch on level probably
		switch (level) {
		case 1:
			asteroidSpeed = 900;
			createRandomAddition(10);
			break;
		case 2:
			asteroidSpeed = 900;
			createRandomSubtraction(10);
			break;
		case 3:
			asteroidSpeed = 800;
			createRandomAddition(50);
			break;
		case 4:
			asteroidSpeed = 800;
			createRandomSubtraction(50);
			break;
		case 5:
			asteroidSpeed = 700;
			createRandomMulti(12);
			break;
		case 6:
			asteroidSpeed = 700;
			createRandomDivision(12);
			break;
		default:
			asteroidSpeed = 600;
			createRandomDivision(15);
			break;
		}
	}

	// creates random multiplication problem from 1-12
	// adds answer and 3 randoms to shuffled arraylist
	public void createRandomMulti(int x) {
		answers.clear();
		int first = random.nextInt(x + 1);
		int second = random.nextInt(x + 1);
		int answer = first * second;

		String problem = first + " * " + second + " = ?";
		this.problem = problem;

		this.answers.add(answer);
		while(answers.size() < 4) {
			int randomAnswer = random.nextInt(x * x + x);
			if (randomAnswer != answer) {
				answers.add(randomAnswer);
			}
		}

		Collections.shuffle(answers);
		correct = answers.indexOf(answer);
	}

	public void createRandomDivision(int x) {
		answers.clear();
		int answer = random.nextInt(x + 1);
		int first = answer * random.nextInt(x + 1);
		int second = first / answer;
		while (second == 0) {
			second = random.nextInt(x + 1);
		}

		String problem = first + " / " + second + " = ?";
		this.problem = problem;

		this.answers.add(answer);
		while(answers.size() < 4) {
			int randomAnswer = random.nextInt(x + x);
			if (randomAnswer != answer) {
				answers.add(randomAnswer);
			}
		}

		Collections.shuffle(answers);
		correct = answers.indexOf(answer);
	}
	
	// 1-20 random addition
	public void createRandomAddition(int x) {
		answers.clear();
		int first = random.nextInt(x + 1);
		int second = random.nextInt(x + 1);
		int answer = first + second;

		String problem = first + " + " + second + " = ?";
		this.problem = problem;

		this.answers.add(answer);
		while(answers.size() < 4) {
			int randomAnswer = random.nextInt(x * 3);
			if (randomAnswer != answer) {
				answers.add(randomAnswer);
			}
		}

		Collections.shuffle(answers);
		correct = answers.indexOf(answer);
	}
	
	public void createRandomSubtraction(int x) {
		answers.clear();
		int first = random.nextInt(x + 1);
		int second = random.nextInt(x + 1);
		int answer = first - second;

		String problem = first + " - " + second + " = ?";
		this.problem = problem;

		this.answers.add(answer);
		while(answers.size() < 4) {
			int randomAnswer = random.nextInt(x + 5);
			if (random.nextInt(2) > 0)
				randomAnswer *= -1;
			if (randomAnswer != answer) {
				answers.add(randomAnswer);
			}
		}

		Collections.shuffle(answers);
		correct = answers.indexOf(answer);
	}

	public void updateLasers() {
		// update laser position
		Iterator<Rectangle> iter = lasers.iterator();
		while (iter.hasNext()) {
			Rectangle l = iter.next();
			l.setX(l.x + laserSpeed);

			if (l.x > Gdx.graphics.getWidth() || l.x < 0) {
				iter.remove();
				firstFire = true;
			}

			if (lasers.size() == 0) {
				laserSpeed = Gdx.graphics.getWidth() / 50;
				reflected = false;
			}
		}
	}

	public void updateShip() {
		move(moving);
		shoot(shooting);
	}
	
	public void update() {
		updateShip();
		
		updateLasers();

		checkAsteroidCollision();

		checkLevel();
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Vector2 newTouch = new Vector2(screenX, screenY);
		//System.out.printf("ScreenX: %d ScreenY: %d CurrentX: %d\n", screenX, screenY, Gdx.input.getX(pointer));
		
		Vector2 delta = newTouch.cpy().sub(lastTouch);
		System.out.println(delta.x);
	    lastTouch = newTouch;
	    if (Math.abs(delta.x) > Gdx.graphics.getWidth() / 20) {
			asteroidSpeed = 200;
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void move(boolean b) {
		
		if (b && ship.y <= Gdx.graphics.getHeight() - ship.width && Gdx.graphics.getHeight() - Gdx.input.getY(movePointer) > ship.y + ship.width / 2
				&& Gdx.input.getX(movePointer) < Gdx.graphics.getWidth() / 2) {
			float newY = ship.y + Gdx.graphics.getHeight() / 100;
			ship.setY(newY);
		}
		if (b && ship.y >= 0 && Gdx.graphics.getHeight() - Gdx.input.getY(movePointer) < ship.y + ship.width / 2 && Gdx.input.getX(movePointer) < Gdx.graphics.getWidth() / 2) {
			ship.setY(ship.y - Gdx.graphics.getHeight() / 100);
		}
	}
	
	public void shoot(boolean b) {
		if (b && Gdx.input.getX(shootPointer) > Gdx.graphics.getWidth() / 2 && firstFire == true) {
			firstFire = false;
			lasers.add(new Rectangle(ship.x + hOffset1, ship.y + vOffset1, laserWidth, laserHeight));
			lasers.add(new Rectangle(ship.x + hOffset1, ship.y + vOffset2, laserWidth, laserHeight));
			laserSound.play();
		}
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		lastTouch = new Vector2(screenX, screenY);
		if ((ship.y <= Gdx.graphics.getHeight() - ship.width && Gdx.graphics.getHeight() - screenY > ship.y + ship.width / 2
				&& screenX < Gdx.graphics.getWidth() / 2) || (ship.y >= 0 && Gdx.graphics.getHeight() - screenY < ship.y + ship.width / 2 && screenX < Gdx.graphics.getWidth() / 2)) {
			moving = true;
			movePointer = pointer;
		}
		if (screenX > Gdx.graphics.getWidth() / 2 && firstFire == true) {
			shooting = true;
			shootPointer = pointer;
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (pointer == movePointer) {
			moving = false;
		}
		if (pointer == shootPointer) {
			shooting = false;
		}
		return true;
	}

	public void checkLevel() {
		if (counter % 5 == 0 && counter != 0) {
			level += 1;
			loadDelay = 0;
			state = GameState.LOADING;
			counter = 0;
			asteroidX = Gdx.graphics.getWidth() - asteroidDim;
			for (Asteroid a : asteroids) {
				a.getHitbox().setX(asteroidX);
				a.setHit(false);
			}
			firstGen = true;
		}
	}

	public void checkAsteroidCollision() {
		// check if ship collides
		for (Asteroid r : asteroids) {
			if (!r.isHit()) {
				if (ship.overlaps(r.getHitbox())) {
					state = GameState.GAMEOVER;
					expSound.play();
				}
			}
		}
	}

	public void checkLaserCollision() {
		if (!lasers.isEmpty()) {
			for (int i = 0; i < 4; i++) {
				if (!asteroids.get(i).isHit()) {
					if (lasers.get(0).overlaps(asteroids.get(i).getHitbox()) || lasers.get(1).overlaps(asteroids.get(i).getHitbox())) {
						// only explodes if the correct asteroid is hit
						if (i == correct) {
							expSound.play();
							asteroids.get(i).setHit(true);
							lasers.clear();
							firstFire = true;
							explosionX = asteroids.get(i).getHitbox().x;
							score += 100;
							counter += 1;
							break;
						} else {
							laserSpeed = -Gdx.graphics.getWidth() / 50;
							refSound.play();
							reflected = true;
							break;
						}
					}
				}
			}
		}

		if (!lasers.isEmpty()) {
			if ((lasers.get(0).overlaps(ship) || lasers.get(1).overlaps(ship)) && reflected == true) {
				state = GameState.GAMEOVER;
				expSound.play();
			}
		}
	}


	public void checkPlayAgain() {
		if (Gdx.input.isTouched() && deathDelay >= .5) {
			state = GameState.LOADING;
			initialize();
		} else {
			deathDelay += Gdx.graphics.getDeltaTime();
		}
	}

	public void checkStart() {
		if (Gdx.input.isTouched()) {
			if (start.contains(Gdx.input.getX(), Gdx.input.getY())) {
				state = GameState.LOADING;
			}
			if (instructions.contains(Gdx.input.getX(), Gdx.input.getY())) {
				state = GameState.INSTRUCTIONS;
			}
		}
	}

	public void checkPlay() {
		if (loadDelay >= 1) {
			loadDelay = 0;
			createRandomMath(level);
			state = GameState.PLAYING;
		} else {
			loadDelay += Gdx.graphics.getDeltaTime();
		}
	}
	
	public void checkInstructions() {
		if (Gdx.input.isTouched()) {
			if (back.contains(Gdx.input.getX(), Gdx.input.getY())) {
				state = GameState.MENU;
			}
			if (play.contains(Gdx.input.getX(), Gdx.input.getY())) {
				state = GameState.LOADING;
			}
		}
	}

	public void drawBackground() {
		starField1.setX(starField1.x - scrollSpeed);
		starField2.setX(starField2.x - scrollSpeed);

		if (starField1.x < -Gdx.graphics.getWidth()) {
			starField1.setX(0);
			starField2.setX(Gdx.graphics.getWidth());
		}

		batch.draw(starFieldTexture, starField1.x, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.draw(starFieldTexture, starField2.x, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}


	public void drawAsteroids() {
		checkLaserCollision();

		// correct behavior: explode once if hit and correct, otherwise no reaction and continue drawing asteroids
		for (Asteroid a : asteroids) {
			// possibly redundant/not used
			if (a.isHit() && notExploded) {
				drawExplosion(explosionX, a.getHitbox().y, a.getHitbox().width, a.getHitbox().height);
			} else if (!a.isHit()){
				batch.draw(asteroidTexture, a.getHitbox().x, a.getHitbox().y, a.getHitbox().width, a.getHitbox().height);
			}
		}

		// reset asteroids
		if (asteroidX < 0 - asteroidDim) {
			asteroidX = Gdx.graphics.getWidth() - asteroidDim;
			for (Asteroid a : asteroids) {
				a.setHit(false);
			}
			firstGen = true;
			notExploded = true;
			stateTime2 = 0f;
		} else {
			// update asteroids
			asteroidX -= Gdx.graphics.getWidth() / asteroidSpeed;
			for (Asteroid r : asteroids) {
				r.getHitbox().setX(asteroidX);
			}
		}
	}

	// make sure only explodes once, 17 * interval
	public void drawExplosion(float x, float y, float width, float height) {
		curFrame2 = expAnimation.getKeyFrame(stateTime2, true);
		stateTime2 += Gdx.graphics.getDeltaTime();
		if (stateTime2 <= .425) {
			batch.draw(curFrame2, x, y, width, height);
		} else {
			// may be redundant/not used
			notExploded = false;
		}
	}

	// draw problem and answers
	public void drawText() {
		if (firstGen) {
			createRandomMath(level);
			firstGen = false;
		}

		if (ship.y <= 10) {
			font.draw(batch, problem, ship.x + 20, ship.y + ship.width + 20);
		} else {
			font.draw(batch, problem, ship.x + 20, ship.y);
		}

		for (int i = 0; i < 4; i++) {
			if (!asteroids.get(i).isHit()) {
				font.draw(batch, answers.get(i).toString(), asteroids.get(i).getHitbox().x + asteroidDim / 2, asteroids.get(i).getHitbox().y + asteroidDim / 2);
			}
		}
	}

	public void drawLasers() {
		if (!lasers.isEmpty()) {
			batch.draw(laserTexture, lasers.get(0).x, lasers.get(0).y, laserWidth, laserHeight);
			batch.draw(laserTexture, lasers.get(1).x, lasers.get(1).y, laserWidth, laserHeight);
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		switch (state) {
		case MENU:
			checkStart();

			batch.begin();
			drawBackground();
			font.draw(batch, "MATHBLAST", Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() * 2 / 3, 100, 10, false);
			font.draw(batch, "PLAY", Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 2);
			font.draw(batch, "INSTRUCTIONS", Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 3);
			batch.end();
			break;
		case INSTRUCTIONS:
			checkInstructions();
			
			batch.begin();
			drawBackground();
			font.draw(batch, "Control the ship by dragging along the left side of the screen\n" +
					  "Tap the right side of the screen to shoot\n" +
					  "Shoot the correct answer and swipe left to increase speed", Gdx.graphics.getWidth() / 6, Gdx.graphics.getHeight() * 2 / 3);
			font.draw(batch, "BACK", Gdx.graphics.getWidth() / 10, Gdx.graphics.getHeight() / 6);
			font.draw(batch, "PLAY", Gdx.graphics.getWidth() * 5 / 6, Gdx.graphics.getHeight() / 6);
			batch.end();
			break;
		case LOADING:
			checkPlay();

			batch.begin();
			drawBackground();
			font.draw(batch, "LEVEL " + level, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 2);
			batch.end();
			break;
		case PLAYING:
			update();
			

			stateTime += Gdx.graphics.getDeltaTime();
			currentFrame = shipAnimation.getKeyFrame(stateTime, true);

			batch.begin();
			drawBackground();
			drawAsteroids();
			batch.draw(currentFrame, ship.x, ship.y, ship.width, ship.width);
			drawText();
			drawLasers();
			font.draw(batch, "Level " + level, Gdx.graphics.getWidth() * 9 / 10, Gdx.graphics.getHeight() * 29 / 30);
			font.draw(batch, "Score: " + score, Gdx.graphics.getWidth() / 100, Gdx.graphics.getHeight() * 29 / 30);
			batch.end();
			break;
		case GAMEOVER:
			checkPlayAgain();

			batch.begin();
			drawBackground();
			font.draw(batch, "GAME OVER\nTAP TO PLAY AGAIN", Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 2);
			batch.end();
			break;
		}
	}
}
