package game.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.swing.JPanel;


import game.entities.Asteroid;
import game.entities.Craft;
import game.entities.Missile;
import game.entities.Obstacles;
import game.entities.Space;
import game.entities.Space.Star;
import game.listeners.ObstacleListener;
import game.listeners.ResizeListener;
import game.threads.ObstacleLauncher;



public class AnimationCanvas extends JPanel implements Runnable, ResizeListener, ObstacleListener {
	private static final long serialVersionUID = 1L;
	private final static int DELAY = 30;
	
	private Thread animationThread;
	private Thread asteroidLauncher; 
	
	private Craft craft;
	private Space space;
	private StateBar lifeBar;
	private boolean running;
	private Obstacles obstacles;
	private ObstacleLauncher obstacleLauncher;
	
	
	public AnimationCanvas() {
		this.addKeyListener(new TAdapter());
		this.setFocusable(true);
		this.setBackground(Color.black);
		this.setDoubleBuffered(true);
		
		this.craft = new Craft();
		this.space = new Space();
		this.obstacles = new Obstacles();
		this.lifeBar = new StateBar("life_bar");
		this.animationThread = new Thread(this);
		this.obstacleLauncher = new ObstacleLauncher();
		this.asteroidLauncher = new Thread(obstacleLauncher);
		this.obstacleLauncher.addObstacleListener(this);
		
		//Refactor
//		obstacles.addAsteroid();

	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		this.animationThread.start();
		this.asteroidLauncher.start();
		this.running = true;
	}
	
	
	
	@Override
	public void run() {
		while(running) {
			this.space.move();
			
			ArrayList<Missile> craftMissle = craft.getMissiles();
			for(Missile m : craftMissle) {
				if(m.isVisible()) {
					m.move();
				}
			}
			
			sleepThread();
			craft.move();
			obstacles.moveAsteroids();
			
			ArrayList<Asteroid> asteroids = obstacles.getAsteroids();
			for(Asteroid a : asteroids) {
				if(a.getBounds().intersects(craft.getBounds())) {
					craft.collision();
				}
			}
			
			craft.alive();
			this.repaint();
			
			
		}
	}

	private void sleepThread() {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		
		// Painting stars
		ArrayList<Star> stars = space.getStars();
		for(Star s : stars) {
			g2d.setColor(Color.white);
			g2d.fillOval(
					s.getX(), 
					s.getY(), 
					s.DIAMETER, 
					s.DIAMETER);
		}
		
		// Painting craft
		g2d.drawImage(
				craft.getCraftImage(), 
				craft.getX(), 
				craft.getY(), 
				this);

		
		// Painting missiles;
		ArrayList<Missile> craftMissiles = craft.getMissiles();
		for(Missile m : craftMissiles) {
			g2d.drawImage(
				m.getMissileImage(), 
				m.getX(), 
				m.getY() - m.getImageHeight(), 
				null);
		}

		// Painting Lifebar
		g2d.drawImage(
				lifeBar.getBarImg(), 
				MyFrame.FRAME_DIMENSION.width /2 - lifeBar.getBarImg().getWidth(null) / 2,
				0,
				null);
		
		// To do

		try {
			// Painting asteroids
			ArrayList<Asteroid> asteroids = obstacles.getAsteroids();
			for (Asteroid a : asteroids) {
//				debugCollisions(g2d, a); // Debug
				g2d.drawImage(
					a.getAsteroidImg(), 
					a.getPosX(), 
					a.getPosY(), 
					this);
			}
			
		} catch (ConcurrentModificationException e) {
			System.err.println(e.getMessage());
		}
	}

	private void debugCollisions(Graphics2D g2d, Asteroid a) {
		g2d.fillRect(a.getBounds().x, a.getBounds().y, a.getBounds().width, a.getBounds().height);
		g2d.fillRect(craft.getBounds().x, craft.getBounds().y, craft.getBounds().width, craft.getBounds().height);
	}

	private class TAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			craft.keyPressed(e);
		}
		
		
		@Override
		public void keyReleased(KeyEvent e) {
			craft.keyReleased(e);
		}
	}

	@Override
	public void onResize(Dimension newDimension) {
//		space.resize();
//		craft.resize();
	}

	@Override
	public void onLaunchAsteroid() {
		obstacles.addAsteroid();

	}

}


