import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

import javax.swing.JPanel;

/**
 * 
 * @author Kanstantsin Zhuk 250934
 *
 */
/**
 * 
 * GamePanel class witch extends JPanel and implements ActionListener
 *
 */
public class GamePanel extends JPanel implements ActionListener
{
	/**
	 * Screen width
	 */
	static final int SCREEN_WIDTH = 600;
	/**
	 * Screen height
	 */
	static final int SCREEN_HEIGHT = 600;
	/**
	 * Cell size
	 */
	static final int UNIT_SIZE = 15;
	static final int GAME_UNITS = (SCREEN_WIDTH*SCREEN_HEIGHT)/UNIT_SIZE;
	static final int DELAY = 80;
	
	/**
	 * number of walls on board
	 */
	static final int WALL_NUMBER = 15;
	/**
	 * array of walls x coordinates
	 */
	final int wallx[] = new int[WALL_NUMBER];
	/**
	 * array of walls y coordinates
	 */
	final int wally[] = new int[WALL_NUMBER];
	
	/**
	 * array of player's snake x coordinates
	 */
	final int x[] = new int[GAME_UNITS];
	/**
	 * array of player's snake y coordinates
	 */
	final int y[] = new int[GAME_UNITS];
		
	/**
	 * array of AI's snake x coordinates
	 */
	final int AIx[] = new int[GAME_UNITS];
	/**
	 * array of AI's snake x coordinates
	 */
	final int AIy[] = new int[GAME_UNITS];

	/**
	 * number of apples on board
	 */
	static final int APPLE_NUMBER = 2;
	/**
	 * array of apples's x coordinates
	 */
	final int appleX[] = new int[APPLE_NUMBER];
	/**
	 * array of apples's y coordinates
	 */
	final int appleY[] = new int[APPLE_NUMBER];
	
	/**
	 * toad move rate (f.e. if = 4 then toad will move every 4'th frame)
	 */
	static final int TOAD_SPEED = 4;
	/**
	 * x coordinate of toad
	 */
	int toadX;
	/**
	 * y coordinate of toad
	 */
	int toadY;
	/**
	 * auxiliary variable for toad movement at x
	 */
	int probtoadX;
	/**
	 * auxiliary variable for toad movement at y
	 */
	int probtoadY;
	int toadmoverate = 1;
	int location;
	
	/**
	 * Player's snake head direction
	 */
	char direction = 'R';
	/**
	 * AI's snake head direction
	 */
	char AIdirection = 'L';	
	/**
	 * AI's snake length
	 */
	int AIbodyParts = 6;
	/**
	 * Player's snake length
	 */
	int bodyParts = 6;
	
	/**
	 * Player's score
	 */
	int applesEaten;
	/**
	 * AI's score
	 */
	int AIeaten;
	
	boolean running = false;
	Timer timer;
	Random random;
	
	/**
	 * Random() function for toad moving. I use it to random number from 1 to 4 to get toad direction
	 * @param min
	 * @param max
	 */
	public int getRandomNumber(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}
	
	/**
	 * 
	 * Thread for Apple. Uses checkApple() function
	 * @see checkApple()
	 *
	 */
	public class Apple implements Runnable
	{
		@Override
		public void run()
		{
			checkApple();
		}
	}
	
	/**
	 * 
	 * Thread for Toad. Uses moveToad() function
	 * @see moveToad()
	 *
	 */
	public class Toad implements Runnable
	{
		public void run()
		{
			moveToad();			
		}
	}
	
	/**
	 * 
	 * Thread for AI snake. Uses pathFinder() and AImove() functions
	 * @see pathFinder()
	 * @see AImove()
	 */
	public class AI implements Runnable
	{
		public void run()
		{
			pathFinder();
			AImove();
		}
	}
	
	/**
	 * 
	 * Thread for player's snake. Uses move() function
	 * @see move()
	 *
	 */
	public class Player implements Runnable
	{
		public void run()
		{
			move();
		}
	}
	
	/**
	 * GamePanel 'constructor'
	 */
	GamePanel()
	{
		timer = new Timer (DELAY, this);
		random = new Random();
		this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
		this.setBackground(Color.white);
		this.setFocusable(true);
		this.addKeyListener(new MyKeyAdapter());
		startGame();
	}

	/**
	 * Start Game function. Set primary locations for player's and AI's snakes, apples, toad and walls.
	 * Starts the timer for frames
	 */
	public void startGame()
	{
		
		for (int i = 0; i < WALL_NUMBER; ++i)
		{
			wallx[i] = getRandomNumber(1, (SCREEN_WIDTH-UNIT_SIZE)/UNIT_SIZE)*UNIT_SIZE;
			wally[i] = getRandomNumber(1, (SCREEN_HEIGHT-UNIT_SIZE)/UNIT_SIZE)*UNIT_SIZE;
		}
		
		direction = 'R';
		AIdirection = 'L';
		bodyParts = 6;
		AIbodyParts = 6;
		for (int i = 0; i < bodyParts; ++i)
		{
			x[i] = bodyParts*UNIT_SIZE - (1+i)*UNIT_SIZE;
			y[i] = 0;
		}
		for (int i = 0; i < AIbodyParts; ++i)
		{
			AIx[i] = -AIbodyParts*UNIT_SIZE + i*UNIT_SIZE + SCREEN_WIDTH;
			AIy[i] = SCREEN_HEIGHT-UNIT_SIZE;
		}
		
		applesEaten = 0;
		AIeaten = 0;
		for(int i = 0; i < APPLE_NUMBER; ++i)
			newApple(i);
		newToad();
		running = true;
		//timer = new Timer(100, this);
		timer.start();
		//new Timer(DELAY, this).start();
		
	}
	
	/**
	 * paintComponent function from javax.swing.JFrame for displaying game
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		draw(g);
	}
	
	/**
	 * Function that draws snakes, apples, toad and walls if game is running or call gameOver(g) function if not
	 * @param g
	 */
	public void draw(Graphics g)
	{
		if(running)
		{
			//draw walls
			for (int i = 0; i < WALL_NUMBER; ++i)
			{
				g.setColor(new Color(0, 0, 0));
				g.fillRect(wallx[i], wally[i], UNIT_SIZE, UNIT_SIZE);
			}
				
			//draw apple
			for(int i = 0; i < APPLE_NUMBER; ++i)
			{
				g.setColor(new Color(200, 200, 0));
				g.fillOval(appleX[i], appleY[i], UNIT_SIZE, UNIT_SIZE);
			}
			
			//draw toad
			g.setColor(new Color(0, 200, 0));
			g.fillOval(toadX, toadY, UNIT_SIZE, UNIT_SIZE);
			
			//draw snake
			for(int i = 0; i < bodyParts; ++i)
			{
				if (i == 0)
				{
					g.setColor(new Color(45, 0, 180));
					g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
				}
				else
				{
					g.setColor(Color.blue);
					g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
				}
			}
			
			//draw AIsnake
			for(int i = 0; i < AIbodyParts; ++i)
			{
				if (i == 0)
				{
					g.setColor(new Color(180, 45, 0));
					g.fillRect(AIx[i], AIy[i], UNIT_SIZE, UNIT_SIZE);
				}
				else
				{
					g.setColor(Color.red);
					g.fillRect(AIx[i], AIy[i], UNIT_SIZE, UNIT_SIZE);
				}
			}
			
			//draw Score
			g.setColor(new Color (0, 255, 255));
			g.setFont(new Font("SansSerif", Font.BOLD, 20));
			FontMetrics metrics = getFontMetrics(g.getFont());
			g.drawString("Score: "+applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: "+applesEaten))/2, g.getFont().getSize());
			//AI Score
			g.setColor(new Color (0, 255, 255));
			g.setFont(new Font("SansSerif", Font.BOLD, 20));
			FontMetrics metrics1 = getFontMetrics(g.getFont());
			g.drawString("AIScore: "+AIeaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: "+applesEaten))/2, SCREEN_HEIGHT - g.getFont().getSize());
			
		}
		else 
			gameOver(g);
	}
	
	/**
	 * Sets coordinates for new apple
	 * @param i number of needed apple from apple list
	 */
	public void newApple(int i)
	{
		boolean cantry = false;
		
		while(cantry != true)
		{
			appleX[i] = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
			appleY[i] = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
			for (int j = 0; j < bodyParts; ++j)
				if ((appleX[i] != x[j]) || (appleY[i] != y[j]))
					cantry = true;
				else 
				{
					cantry = false;
					j = bodyParts;
				}
			if (cantry == true)
			{
				for (int j = 0; j < AIbodyParts; ++j)
					if ((appleX[i] != AIx[j]) || (appleY[i] != AIy[j]))
						cantry = true;
					else 
					{
						cantry = false;
						j = AIbodyParts;
					}
			}
			if (cantry == true)
			{
				for (int n = 0; n < APPLE_NUMBER; ++n)
				{
					if (n != i)
					{
						if ((appleX[i] != appleX[n]) || (appleY[i] != appleY[n]))
							cantry = true;
						else 
						{
							cantry = false;
							n = APPLE_NUMBER;
						}
					}					
				}
			}
			if (cantry == true)
			{
				for (int j = 0; j < WALL_NUMBER; ++j)
				{
					if ((appleX[i] != wallx[j]) || (appleY[i] != wally[j]))
						cantry = true;
					else 
					{
						cantry = false;
						j = WALL_NUMBER;
					}
				}
			}
		}
	}
	
	
	/**
	 * set coordinates for new toad
	 */
	public void newToad()
	{
		boolean cantry = false;
		
		while(cantry != true)
		{
			toadX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
			toadY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
			for (int i = 0; i < bodyParts; ++i)
				if ((toadX != x[i]) || (toadY != y[i]))
					cantry = true;
				else 
				{
					cantry = false;
					i = bodyParts;
				}
			if (cantry == true)
			{
				for (int i = 0; i < AIbodyParts; ++i)
					if ((toadX != AIx[i]) || (toadY != AIy[i]))
						cantry = true;
					else 
					{
						cantry = false;
						i = AIbodyParts;
					}
			}
			if (cantry == true)
			{
				for (int j = 0; j < WALL_NUMBER; ++j)
				{
					if ((toadX != wallx[j]) || (toadY != wally[j]))
						cantry = true;
					else 
					{
						cantry = false;
						j = WALL_NUMBER;
					}
				}
			}
		}
	}
	
	
	/**
	 * calculate player's snake coordinates depending on current direction
	 */
	public void move()
	{
		for(int i = bodyParts; i > 0; i--)
		{
			x[i] = x[i-1];
			y[i] = y[i-1];
		}
		
		switch(direction)
		{
		case'U':
			y[0] = y[0] - UNIT_SIZE;
			break;
		case'D':
			y[0] = y[0] + UNIT_SIZE;
			break;
		case'L':
			x[0] = x[0] - UNIT_SIZE;
			break;
		case'R':
			x[0] = x[0] + UNIT_SIZE;
			break;
		}
	}
	
	
	/**
	 * calculate AI's snake coordinates depending on AIdirection witch calculates in pathFinder() function
	 */
	public void AImove()
	{
		for(int i = AIbodyParts; i > 0; i--)
		{
			AIx[i] = AIx[i-1];
			AIy[i] = AIy[i-1];
		}
		
		switch(AIdirection)
		{
		case'U':
			AIy[0] = AIy[0] - UNIT_SIZE;
			break;
		case'D':
			AIy[0] = AIy[0] + UNIT_SIZE;
			break;
		case'L':
			AIx[0] = AIx[0] - UNIT_SIZE;
			break;
		case'R':
			AIx[0] = AIx[0] + UNIT_SIZE;
			break;
		}
	}
	
	/**
	 * check if any apple is eaten by any of snakes
	 */
	public void checkApple()
	{
		for(int i = 0; i < APPLE_NUMBER; ++i)
		{
			if((x[0] == appleX[i]) && (y[0] == appleY[i]))
			{
				bodyParts++;
				applesEaten++;
				newApple(i);
			}
			if((AIx[0] == appleX[i]) && (AIy[0] == appleY[i]))
			{
				AIbodyParts++;
				AIeaten++;
				newApple(i);
			}
		}
	}
	
	/**
	 * check if toad is eaten by any of snakes
	 */
	public void checkToad()
	{
		if((x[0] == toadX) && (y[0] == toadY))
		{
			bodyParts ++;
			applesEaten += 2;
			newToad();
		}
		if((AIx[0] == toadX) && (AIy[0] == toadY))
		{
			AIbodyParts++;
			AIeaten += 2;
			newToad();
		}
	}
	
	/**
	 * auxiliary function to get random direction for toad
	 */
	public void nextToadStep()
	{
		location = getRandomNumber (1, 4);
		probtoadX = toadX;
		probtoadY = toadY;
		switch (location)
		{
			case 1:
				probtoadX += UNIT_SIZE;
				break;
			case 2:
				probtoadX -= UNIT_SIZE;
				break;
			case 3:
				probtoadY += UNIT_SIZE;
				break;
			default:
				probtoadY -= UNIT_SIZE;
				break;
		}
	}
	
	/**
	 * check if random direction for toad is legal, call nextToadStep() function until it will fit
	 */
	public void moveToad()
	{
		if (toadmoverate == TOAD_SPEED)
		{
			toadmoverate = 1;
			boolean cantry = false;
			while(cantry != true)
			{
				nextToadStep();
				if ((probtoadX < 0) || (probtoadX > SCREEN_WIDTH-UNIT_SIZE) || (probtoadY < 0) || (probtoadY > SCREEN_HEIGHT-UNIT_SIZE))
					cantry = false;
				else cantry = true;
				if (cantry == true)
				{
					for (int i = 0; i < bodyParts; ++i)
					{
						if ((probtoadX != x[i]) || (probtoadY != y[i]))
							cantry = true;
						else 
						{
							cantry = false;
							i = bodyParts;
						}
					}
				}
				if (cantry == true)
				{
					for (int i = 0; i < AIbodyParts; ++i)
					{
						if ((probtoadX != AIx[i]) || (probtoadY != AIy[i]))
							cantry = true;
						else 
						{
							cantry = false;
							i = AIbodyParts;
						}
					}
				}
				if (cantry == true)
				{
					for (int j = 0; j < WALL_NUMBER; ++j)
					{
						if ((probtoadX != wallx[j]) || (probtoadY != wally[j]))
							cantry = true;
						else 
						{
							cantry = false;
							j = WALL_NUMBER;
						}
					}
				}
				
			}
			//nextToadStep();
			toadX = probtoadX;
			toadY = probtoadY;
		}
		else
			toadmoverate++;
	}
	
	
	/**
	 * check if any of snakes collided
	 */
	public void checkCollisions()
	{
		//if heads collides with body
		for(int i = bodyParts-1; i > 0; --i)
		{
			if((x[0] == x[i]) && (y[0] == y[i]))
			{
				running = false;
				AIeaten+=15;
			}
			if((AIx[0] == x[i]) && (AIy[0] == y[i]))
			{
				running = false;
				applesEaten += 15;
			}
		}
		//if head touches border
		if (x[0] < 0)
		{
			running = false;
			AIeaten+=15;
		}
		if (x[0] > SCREEN_WIDTH-UNIT_SIZE)
		{
			running = false;
			AIeaten+=15;
		}
		if (y[0] < 0)
		{
			running = false;
			AIeaten+=15;
		}
		if (y[0] > SCREEN_HEIGHT-UNIT_SIZE)
		{
			running = false;
			AIeaten+=15;
		}
		if ((AIx[0] == x[0]) && (AIy[0] == y[0]))
		{
			running = false;
		}
		
		if(!running)
			timer.stop();
		
		//if heads collides with AIbody
		for(int i = AIbodyParts; i > 0; --i)
		{
			if((AIx[0] == AIx[i]) && (AIy[0] == AIy[i]))
			{
				running = false;
				applesEaten += 15;
			}
			if((x[0] == AIx[i]) && (y[0] == AIy[i]))
			{
				running = false;
				AIeaten+=15;
			}
		}
		//if AIhead touches border
		if (AIx[0] < 0)
		{
			running = false;
			applesEaten += 15;
		}
		if (AIx[0] > SCREEN_WIDTH-UNIT_SIZE)
		{
			running = false;
			applesEaten += 15;
		}
		if (AIy[0] < 0)
		{
			running = false;
			applesEaten += 15;
		}
		if (AIy[0] > SCREEN_HEIGHT-UNIT_SIZE)
		{
			running = false;
			applesEaten += 15;
		}
		
		for (int j = 0; j < WALL_NUMBER; ++j)
		{
			if ((x[0] == wallx[j]) && (y[0] == wally[j]))
			{
				running = false;
				AIeaten+=15;
			}
			if ((AIx[0] == wallx[j]) && (AIy[0] == wally[j]))
			{
				running = false;
				applesEaten+=15;
			}
		}
			
		if(!running)
			timer.stop();
	}
	
	/**
	 * Game over function. Shows who is winner and player's and AI's scores
	 * @param g
	 */
	public void gameOver(Graphics g)
	{
		//Game Over text
		g.setColor(Color.green);
		
		g.setFont(new Font("SansSerif", Font.BOLD, 35));
		FontMetrics metrics2 = getFontMetrics(g.getFont());
		g.drawString("Your Score: "+applesEaten, (SCREEN_WIDTH - metrics2.stringWidth("Your Score: "+applesEaten))/2, SCREEN_HEIGHT/2 - 50);
		
		FontMetrics AImetrics = getFontMetrics(g.getFont());
		g.drawString("AI Score: "+AIeaten, (SCREEN_WIDTH - AImetrics.stringWidth("Your Score: "+applesEaten))/2, SCREEN_HEIGHT/2 - 5);
		
		g.setFont(new Font("SansSerif", Font.BOLD, 75));
		FontMetrics metrics1 = getFontMetrics(g.getFont());
		switch ((AIeaten == applesEaten) ? 0 :
            (AIeaten < applesEaten) ? 1 : 2)
		{
		case 0:
			g.drawString("Draw!", (SCREEN_WIDTH - metrics1.stringWidth("Draw!"))/2, SCREEN_HEIGHT/2 - 200);
			break;
		case 1:
			g.drawString("You Win!", (SCREEN_WIDTH - metrics1.stringWidth("You Win!"))/2, SCREEN_HEIGHT/2 - 200);
			break;
		case 2:
			g.drawString("AI Win!", (SCREEN_WIDTH - metrics1.stringWidth("AI Win!"))/2, SCREEN_HEIGHT/2 - 200);
			break;
		}
		
		g.setFont(new Font("SansSerif", Font.BOLD, 20));
		FontMetrics metrics3 = getFontMetrics(g.getFont());
		String playagain = "Press spacebar to play again";
		g.drawString(playagain, (SCREEN_WIDTH - metrics3.stringWidth(playagain))/2, SCREEN_HEIGHT/2 + 140);
		
		
	}
	
	/**
	 * Key Adapter for snake direction and game restart
	 * @author koons
	 *
	 */
	public class MyKeyAdapter extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyCode())
			{
			case KeyEvent.VK_LEFT:
				if(direction != 'R')
					direction = 'L';
				break;
			case KeyEvent.VK_RIGHT:
				if(direction != 'L')
					direction = 'R';
				break;
			case KeyEvent.VK_UP:
				if(direction != 'D')
					direction = 'U';
				break;
			case KeyEvent.VK_DOWN:
				if(direction != 'U')
					direction = 'D';
				break;
			case KeyEvent.VK_SPACE:
				startGame();
				break;
			}
		}
	}
	
	/**
	 * Function for AI snake to find the shortest way to apple. Currently set to find path to first apple in apple list
	 */
	private void pathFinder() {
		int hCostA = 0;
		int hCostB = 0;
		int hCostC = 0;
		int xDistance;
		int yDistance;
		boolean blocked = false;
		int fCostA = 999999999;
		int fCostB = 999999999;
		int fCostC = 999999999;
		
		switch(AIdirection) {
		case 'U':
			hCostA = 0;
			hCostB = 0;
			hCostC = 0;
			
			// If space to go up
			if (AIy[0] - UNIT_SIZE >= 0) {
				// If no AI body parts blocking
				for(int i = AIbodyParts; i>0; i--)
				{
					if((AIx[0] == AIx[i]) && (AIy[0] - UNIT_SIZE == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0] == x[i]) && (AIy[0] - UNIT_SIZE == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0]  == wallx[i]) && (AIy[0] - UNIT_SIZE == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				
				if (blocked != true) {
					// Going up
					xDistance = Math.abs((appleX[0] - AIx[0]) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - (AIy[0] - UNIT_SIZE)) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostA = 4;
					}
					hCostA+= (xDistance * 10) + (yDistance * 10);
					fCostA = hCostA + 10;
				}
				blocked = false;
			}
			
			// If space to go left
			if(AIx[0] - UNIT_SIZE >= 0)
			{
				// If no AI body parts blocking
				for(int i = AIbodyParts; i>0; i--)
				{
					if((AIx[0] - UNIT_SIZE == AIx[i]) && (AIy[0] == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0]  - UNIT_SIZE == x[i]) && (AIy[0] == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0]  - UNIT_SIZE == wallx[i]) && (AIy[0] == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				
				if (blocked != true) {
					// Going left
					xDistance = Math.abs((appleX[0] - (AIx[0] - UNIT_SIZE)) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - AIy[0]) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostB = 4;
					}
					hCostB+= (xDistance * 10) + (yDistance * 10);
					fCostB = hCostB + 14;
				}
				blocked = false;
			}
			
			// If space to go right
			if(AIx[0] + UNIT_SIZE < SCREEN_WIDTH)
			{
				// If no AI body parts blocking
				for(int i = AIbodyParts; i>0; i--)
				{
					if((AIx[0] + UNIT_SIZE == AIx[i]) && (AIy[0] == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0] + UNIT_SIZE == x[i]) && (AIy[0] == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0]  + UNIT_SIZE == wallx[i]) && (AIy[0] == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				if (blocked != true) {
					// Going right
					xDistance = Math.abs((appleX[0] - (AIx[0] + UNIT_SIZE)) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - AIy[0]) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostC = 4;
					}
					hCostC+= (xDistance * 10) + (yDistance * 10);
					fCostC = hCostC + 14;
				}
				blocked = false;
			}
			
			if(fCostA <= fCostB && fCostA <= fCostC) {
				AIdirection = 'U';
			} else if (fCostB < fCostA && fCostB <= fCostC) {
				AIdirection = 'L';
			} else if(fCostC < fCostB && fCostC < fCostA) {
				AIdirection = 'R';
			}
			fCostA = 999999999;
			fCostB = 999999999;
			fCostC = 999999999;
			
			break;
		
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		case 'D':
			hCostA = 0;
			hCostB = 0;
			hCostC = 0;
			
			// If space to go down
			if (AIy[0] + UNIT_SIZE < SCREEN_HEIGHT)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] == AIx[i]) && (AIy[0] + UNIT_SIZE == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0] == x[i]) && (AIy[0] + UNIT_SIZE == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0] == wallx[i]) && (AIy[0]  + UNIT_SIZE == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				if (blocked != true) {
					// Going down
					xDistance = Math.abs((appleX[0] - AIx[0]) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - (AIy[0] + UNIT_SIZE)) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostA = 4;
					}
					hCostA += (xDistance * 10) + (yDistance * 10);
					fCostA = hCostA + 10;
				}
				blocked = false;
			}
			
			// If space to go left
			if (AIx[0] - UNIT_SIZE >= 0)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] - UNIT_SIZE == AIx[i]) && (AIy[0] == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0] - UNIT_SIZE == x[i]) && (AIy[0] == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0]  - UNIT_SIZE == wallx[i]) && (AIy[0] == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				
				if (blocked != true) {
					// Going left
					xDistance = Math.abs((appleX[0] - (AIx[0] - UNIT_SIZE)) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - AIy[0]) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostB = 4;
					}
					hCostB += (xDistance * 10) + (yDistance * 10);
					fCostB = hCostB + 14;
				}
				blocked = false;
			}
			
			// If space to go right
			if (AIx[0] + UNIT_SIZE < SCREEN_WIDTH)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] + UNIT_SIZE == AIx[i]) && (AIy[0] == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0] + UNIT_SIZE == x[i]) && (AIy[0] == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0] + UNIT_SIZE  == wallx[i]) && (AIy[0] == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				if (blocked != true) {
					// Going right
					xDistance = Math.abs((appleX[0] - (AIx[0] + UNIT_SIZE)) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - AIy[0]) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostC = 4;
					}
					hCostC += (xDistance * 10) + (yDistance * 10);
					fCostC = hCostC + 14;
				}
				blocked = false;
			}
			
			if (fCostA <= fCostB && fCostA <= fCostC) {
				AIdirection = 'D';
			} else if (fCostB < fCostA && fCostB <= fCostC) {
				AIdirection = 'L';
			} else if (fCostC < fCostB && fCostC < fCostA) {
				AIdirection = 'R';
			}
			fCostA = 999999999;
			fCostB = 999999999;
			fCostC = 999999999;
			
			break;
		
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		case 'L':
			hCostA = 0;
			hCostB = 0;
			hCostC = 0;
			
			// If space to go left
			if (AIx[0] - UNIT_SIZE >= 0)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] - UNIT_SIZE == AIx[i]) && (AIy[0] == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0] - UNIT_SIZE == x[i]) && (AIy[0] == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0] - UNIT_SIZE  == wallx[i]) && (AIy[0] == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				if (blocked != true) {
					// Going left
					xDistance = Math.abs((appleX[0] - (AIx[0] - UNIT_SIZE)) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - AIy[0]) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostA = 4;
					}
					hCostA += (xDistance * 10) + (yDistance * 10);
					fCostA = hCostA + 10;
				}
				blocked = false;
			}
			
			// If space to go down
			if (AIy[0] + UNIT_SIZE < SCREEN_HEIGHT)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] == AIx[i]) && (AIy[0] + UNIT_SIZE == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0]  == x[i]) && (AIy[0] + UNIT_SIZE == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0]  == wallx[i]) && (AIy[0] + UNIT_SIZE == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				if (blocked != true) {
					// Going down
					xDistance = Math.abs((appleX[0] - AIx[0]) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - (AIy[0] + UNIT_SIZE)) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostB = 4;
					}
					hCostB += (xDistance * 10) + (yDistance * 10);
					fCostB = hCostB + 14;
				}
				blocked = false;
			}
			
			// If space to go up
			if (AIy[0] - UNIT_SIZE >= 0)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] == AIx[i]) && (AIy[0] - UNIT_SIZE == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0]  == x[i]) && (AIy[0] - UNIT_SIZE == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0]  == wallx[i]) && (AIy[0] - UNIT_SIZE == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				if (blocked != true) {
					// Going up
					xDistance = Math.abs((appleX[0] - AIx[0]) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - (AIy[0] - UNIT_SIZE)) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostC = 4;
					}
					hCostC += (xDistance * 10) + (yDistance * 10);
					fCostC = hCostC + 14;
				}
				blocked = false;
			}
			
			if (fCostA <= fCostB && fCostA <= fCostC) {
				AIdirection = 'L';
			} else if (fCostB < fCostA && fCostB <= fCostC) {
				AIdirection = 'D';
			} else if (fCostC < fCostB && fCostC < fCostA) {
				AIdirection = 'U';
			}

			fCostA = 999999999;
			fCostB = 999999999;
			fCostC = 999999999;
			
			break;
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		case 'R':
			hCostA = 0;
			hCostB = 0;
			hCostC = 0;
			
			// If space to go right
			if (AIx[0] + UNIT_SIZE < SCREEN_WIDTH)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] + UNIT_SIZE == AIx[i]) && (AIy[0] == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0] + UNIT_SIZE == x[i]) && (AIy[0] == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0] + UNIT_SIZE  == wallx[i]) && (AIy[0] == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				if (blocked != true) {
					// Going right
					xDistance = Math.abs((appleX[0] - (AIx[0] + UNIT_SIZE)) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - AIy[0]) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostA = 4;
					}
					hCostA += (xDistance * 10) + (yDistance * 10);
					fCostA = hCostA + 10;
				}
				blocked = false;
			}
			
			// If space to go down
			if (AIy[0] + UNIT_SIZE < SCREEN_HEIGHT)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] == AIx[i]) && (AIy[0] + UNIT_SIZE == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0]== x[i]) && (AIy[0] + UNIT_SIZE == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0]  == wallx[i]) && (AIy[0] + UNIT_SIZE == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				if (blocked != true) {
					// Going down
					xDistance = Math.abs((appleX[0] - AIx[0]) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - (AIy[0] + UNIT_SIZE)) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostB = 4;
					}
					hCostB += (xDistance * 10) + (yDistance * 10);
					fCostB = hCostB + 14;
				}
				blocked = false;
			}
			
			// If space to go up
			if (AIy[0] - UNIT_SIZE >= 0)
			{
				// If no AI body parts blocking
				for (int i = AIbodyParts; i > 0; i--)
				{
					if ((AIx[0] == AIx[i]) && (AIy[0] - UNIT_SIZE == AIy[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Player body parts blocking
				for(int i = bodyParts; i>0; i--)
				{
					if((AIx[0]== x[i]) && (AIy[0] - UNIT_SIZE == y[i]))
					{
						blocked = true;
						break;
					}
				}
				// If no Walls blocking
				for(int i = 0; i < WALL_NUMBER; ++i)
				{
					if((AIx[0]  == wallx[i]) && (AIy[0] - UNIT_SIZE == wally[i]))
					{
						blocked = true;
						break;
					}
				}
				
				if (blocked != true) {
					// Going up
					xDistance = Math.abs((appleX[0] - AIx[0]) / UNIT_SIZE);
					yDistance = Math.abs((appleY[0] - (AIy[0] - UNIT_SIZE)) / UNIT_SIZE);
					if (yDistance != 0) {
						hCostC = 4;
					}
					hCostC += (xDistance * 10) + (yDistance * 10);
					fCostC = hCostC + 14;
				}
				blocked = false;
			}
			
			if (fCostA <= fCostB && fCostA <= fCostC) {
				AIdirection = 'R';
			} else if (fCostB < fCostA && fCostB <= fCostC) {
				AIdirection = 'D';
			} else if (fCostC < fCostB && fCostC < fCostA) {
				AIdirection = 'U';
			}

			fCostA = 999999999;
			fCostB = 999999999;
			fCostC = 999999999;
			
			break;
		}
	}

	
	/**
	 * Action function from java.awt.event package
	 * check if game is running and then uses 4 threads to check apples status, move toad, calculate direction and move for AI snake and move players snake
	 * then check if no collisions on board and use repaint function to update board displayment
	 */
	public void actionPerformed(ActionEvent e) 
	{
		if(running)
		{
			/*move();
			moveToad();
			pathFinder();
			AImove();
			checkApple();	*/		
			
			Apple Apple = new Apple();
			Thread Appleth = new Thread(Apple);
			
			Toad Toad = new Toad();
			Thread Toadth = new Thread(Toad);
			
			AI AI = new AI();
			Thread AIth = new Thread(AI);
			
			Player Player = new Player();
			Thread Playerth = new Thread(Player);
			
			Appleth.start();
			Toadth.start();
			AIth.start();
			Playerth.start();	
			
			try {
				Appleth.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				Toadth.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				AIth.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				Playerth.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			checkToad();
			checkCollisions();
		}
		repaint();
		
	}

}
