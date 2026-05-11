package engine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 22/09/2016 ==========
 *
 * - added snake - added a background image - drawing snake - drawing grid lines
 * (after the snake has been drawn, so that the grid divides the snake into
 * blocks) - snake wrapping when going out of borders (left-to-right,
 * right-to-left, top-to-bottom, bottom-to-top) <--- TRY THIS, does it wrap
 * correctly in all cases? - added control to slow down snake movement
 * (moveTick/TICKS_UNTIL_MOVE) <--- TRY THIS, change TICKS_UNTIL_MOVE
 *
 * 15/09/2016 ==========
 *
 * There are some improvements to be made. Especially the KeyInput class
 * requires some work.
 *
 * Something to think about: The KeyInput.update() will copy the keys array to
 * lastKeys array. Why we should not call that method before processing input?
 * When should we even call it?
 */
public class Framework extends JFrame implements Runnable {
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 600;
	public static final int MAX_SNAKE_LENGTH = 30 * 21;
	public static final int BOARD_WIDTH = 30; // game board could be a class ->
												// do it if you want
	public static final int BOARD_HEIGHT = 21;
	public static final int CELL_SIZE = 25;
	public static final int X_OFFSET = 25; // x offset from canvas's left edge
											// to the left edge of the grid
	public static final int Y_OFFSET = 27; // y offset from canvas's top edge to
											// the top edge of the grid

	// movement directions could be an enum, I did it like this because I didn't
	// want to introduce language features that are not necessary
	public static final int MOVE_LEFT = 0;
	public static final int MOVE_UP = 1;
	public static final int MOVE_RIGHT = 2;
	public static final int MOVE_DOWN = 3;
	public static final int NO_MOVE = 4;

	private int points;

	public static final int TICKS_UNTIL_MOVE = 10; // number of updates call
													// before snake should move

	private Canvas canvas;
	private volatile boolean running;
	private Thread gameThread;
	private BufferStrategy bufferStrategy;
	private KeyInput keys;
	private FrameRate fps;
	private boolean gameOver;
	private BufferedImage foodImage;
	private BufferedImage backgroundImage;
	private Point foodLocation;
	private Point[] snake; // snake represented as a collection of points, that
							// correspond to grid cells
	private int snakeLength = 3; // current snake length
	private int moveDirection = MOVE_RIGHT; // current snake's move direction
	private int moveTick = 0; // used to slow down the snake movement speed
	private int newMoveDirection = MOVE_RIGHT;

	public Framework() {
	}

	protected void createAndShowWindow() {
		canvas = new Canvas();
		canvas.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		canvas.setIgnoreRepaint(true); // we do not want to react to paint
										// messages

		this.setIgnoreRepaint(true);
		this.getContentPane().add(canvas);
		this.setResizable(false);
		this.setTitle("Framework");
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		keys = new KeyInput();
		canvas.addKeyListener(keys);
		canvas.requestFocus();

		canvas.createBufferStrategy(2); // double buffering -> front buffer and
										// one offscreen buffer
		bufferStrategy = canvas.getBufferStrategy();

		gameThread = new Thread(this, "SHIT"); // run game logic in a separate
												// thread
		gameThread.start(); // start the thread -> call the Runnable object's
							// run() method
	}

	@Override
	public void run() {
		running = true;
		init();

		long realTime = System.currentTimeMillis();
		long gameTime = realTime;
		while (running) {
			// think about the relationship between gameTime and realTime and
			// how this
			// works...

			realTime = System.currentTimeMillis();
			// update game world at a fixed interval (16 milliseconds)
			if (gameTime < realTime) {
				gameTime += 16L;
				update();
			}
			// render to screen; we are really rendering to an offscreen buffer
			// and then showing that on screen
			fps.update(); // this could be called in the render() method
			render();
		}
	}

	private void init() {
		try {
			backgroundImage = ImageIO.read(new File("res/chalk_board.jpg"));
			foodImage = ImageIO.read(new File("res/apple.png"));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// create and init fps counter
		fps = new FrameRate();
		fps.init();

		// create snake
		snake = new Point[MAX_SNAKE_LENGTH];
		snake[0] = new Point(12, 10);
		snake[1] = new Point(11, 10);
		snake[2] = new Point(10, 10);

		// create food
		foodLocation = generateFoodLocation();
	}

	private Point generateFoodLocation() {
		int x;
		int y;
		do {
			x = (int) (Math.random() * BOARD_WIDTH);
			y = (int) (Math.random() * BOARD_HEIGHT);
		} while (snakeCollidesWith(x, y));
		return new Point(x, y);
	}

	private boolean snakeCollidesWith(int x, int y) {
		for (int i = 0; i < snakeLength; i++) {
			if (snake[i].x == x && snake[i].y == y) {
				return true;
			}
		}
		return false;
	}

	private boolean snakeSelfCollision() {
		for (int i = 1; i < snakeLength; i++) {
			if (snake[i].x == snake[0].x && snake[i].y == snake[0].y) {
				return true;
			}
		}
		return false;
	}

	private void update() {
		if (!gameOver) {

			// if key was pressed change movement direction
			if (keys.keyHit(KeyEvent.VK_LEFT)) {
				newMoveDirection = (moveDirection == MOVE_RIGHT) ? MOVE_RIGHT
						: MOVE_LEFT;
			}
			if (keys.keyHit(KeyEvent.VK_UP)) {
				newMoveDirection = (moveDirection == MOVE_DOWN) ? MOVE_DOWN
						: MOVE_UP;
			}
			if (keys.keyHit(KeyEvent.VK_RIGHT)) {
				newMoveDirection = (moveDirection == MOVE_LEFT) ? MOVE_LEFT
						: MOVE_RIGHT;
			}
			if (keys.keyHit(KeyEvent.VK_DOWN)) {
				newMoveDirection = (moveDirection == MOVE_UP) ? MOVE_UP
						: MOVE_DOWN;
			}

			// move snake only if TICK_UNTIL_MOVE ticks (updates) has occurred
			// since last snake move
			++moveTick;
			if (moveTick == TICKS_UNTIL_MOVE) {
				moveTick = 0;

				moveDirection = newMoveDirection;
				// copy snake positions, draw this into a paper or something to
				// get a feel what is happening
				for (int i = snakeLength; i > 0; --i) {
					snake[i] = snake[i - 1];
				}

				// new snake head
				Point head = snake[0];

				if (moveDirection == MOVE_LEFT) {
					snake[0] = new Point(head.x - 1, head.y);
				} // Think: why do we create a new Point?
				if (moveDirection == MOVE_RIGHT) {
					snake[0] = new Point(head.x + 1, head.y);
				}
				if (moveDirection == MOVE_DOWN) {
					snake[0] = new Point(head.x, head.y + 1);
				}
				if (moveDirection == MOVE_UP) {
					snake[0] = new Point(head.x, head.y - 1);
				}

				// check snake self collision
				if (snakeSelfCollision()) {
					gameOver = true;
				}

				// check if snake is still on the board; if not wrap to opposite
				// edge
				if (snake[0].x < 0) {
					snake[0].x = BOARD_WIDTH - 1;
				} // wrap to right
				if (snake[0].x >= BOARD_WIDTH) {
					snake[0].x = 0;
				} // wrap to left
				if (snake[0].y < 0) {
					snake[0].y = BOARD_HEIGHT - 1;
				} // wrap to bottom
				if (snake[0].y >= BOARD_HEIGHT) {
					snake[0].y = 0;
				} // wrap to top

				// check collision with food
				if (snakeCollidesWith(foodLocation.x, foodLocation.y)) {
					foodLocation = generateFoodLocation();
					++snakeLength;
					points += 10;
				}
			}
		}
		keys.update();
	}

	private void render() {
		Graphics g = bufferStrategy.getDrawGraphics();

		// ***** BEGIN DRAW *****
		// clear the whole drawing area
		// g.setColor( new Color( 255, 0, 0 ) ); // not needed since the
		// background image fills the whole canvas
		// g.fillRect( 0, 0, canvas.getWidth(), canvas.getHeight() );

		g.drawImage(backgroundImage, 0, 0, null);

		// draw snake
		g.setColor(Color.YELLOW);
		for (int i = 0; i < snakeLength; ++i) {
			int x = snake[i].x;
			int y = snake[i].y;

			g.fillRect(X_OFFSET + x * CELL_SIZE, Y_OFFSET + y * CELL_SIZE,
					CELL_SIZE, CELL_SIZE);
		}

		g.drawImage(foodImage, X_OFFSET + foodLocation.x * CELL_SIZE, Y_OFFSET
				+ foodLocation.y * CELL_SIZE, null);

		// draw board grid
		g.setColor(new Color(30, 30, 30));
		int boardWidthInPixels = BOARD_WIDTH * CELL_SIZE;
		int boardHeightInPixels = BOARD_HEIGHT * CELL_SIZE;

		// vertical grid lines
		for (int i = 0; i < BOARD_WIDTH; ++i) {
			g.drawLine(X_OFFSET + i * CELL_SIZE, Y_OFFSET, X_OFFSET + i
					* CELL_SIZE, Y_OFFSET + boardHeightInPixels);
		}
		// horizontal grid lines
		for (int i = 0; i < BOARD_HEIGHT; ++i) {
			g.drawLine(X_OFFSET, Y_OFFSET + i * CELL_SIZE, X_OFFSET
					+ boardWidthInPixels, Y_OFFSET + i * CELL_SIZE);
		}

		// draw fps
		g.setColor(Color.WHITE);
		g.setFont(new Font("MonoSpaced", Font.BOLD, 18));
		g.drawString("FPS: " + fps.getFPS(), 10, 20);

		// draw game over
		g.drawString("Game over: " + gameOver, 220, Y_OFFSET - 8);

		// draw current points
		g.drawString("Score: " + points, WINDOW_WIDTH - 150, Y_OFFSET - 8);
		if (gameOver) {
			g.setFont(new Font("MonoSpaced", Font.BOLD, 96));
			g.setColor(Color.RED);
			String gameOverString = "GAME OVER";
			int stringWidth = g.getFontMetrics().stringWidth(gameOverString);
			g.drawString(gameOverString, (WINDOW_WIDTH - stringWidth) / 2,
					WINDOW_HEIGHT / 2);
		}

		// ***** END DRAW *****
		g.dispose();
		bufferStrategy.show(); // show what we have drawn
	}

	protected void onWindowClosing() {
		running = false;
		try {
			gameThread.join(); // wait gameThread to finish
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		this.dispose(); // enables termination of the program
		// System.exit( 0 );
	}

	public static void launch(final Framework app) {
		app.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				app.onWindowClosing();
			}
		});

		SwingUtilities.invokeLater(new Runnable() { // run in swing edt
					@Override
					public void run() {
						app.createAndShowWindow();
					}
				});
	}
}
