package sampleRobots;

import robocode.*;

import java.awt.geom.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class LoggerRobot extends AdvancedRobot {

    private class Dados {
        Double distancia;
        Double enemyX;
        Double enemyY;
        Double myX;
        Double myY;
        Double myEnergy;
        Double enemyEnergy;
        Double myHeading;
        Double myVelocity;
        Double enemyHeading;
        Double enemyVelocity;
        Double timeSinceLastShot;
        Double bulletsInAir;
        Double distanceToCenter;

        public Dados(Double distancia, Double enemyX, Double enemyY, Double myX, Double myY, Double myEnergy,
                     Double enemyEnergy, Double myHeading, Double myVelocity, Double enemyHeading,
                     Double enemyVelocity, Double timeSinceLastShot, Double bulletsInAir, Double distanceToCenter) {
            this.distancia = distancia;
            this.enemyX = enemyX;
            this.enemyY = enemyY;
            this.myX = myX;
            this.myY = myY;
            this.myEnergy = myEnergy;
            this.enemyEnergy = enemyEnergy;
            this.myHeading = myHeading;
            this.myVelocity = myVelocity;
            this.enemyHeading = enemyHeading;
            this.enemyVelocity = enemyVelocity;
            this.timeSinceLastShot = timeSinceLastShot;
            this.bulletsInAir = bulletsInAir;
            this.distanceToCenter = distanceToCenter;
        }
    }

    double cellWidth = 0;
    double cellHeight = 0;
    int[][] grid;

    RobocodeFileOutputStream fw;

    HashMap<Bullet, Dados> balasNoAr = new HashMap<>();
    double lastShotTime = 0;
    double bulletsInAir = 0;

    @Override
    public void run() {
        double fieldWidth = getBattleFieldWidth();
        double fieldHeight = getBattleFieldHeight();
        int numRows = 10;
        int numCols = 10;
        cellWidth = fieldWidth / numCols;
        cellHeight = fieldHeight / numRows;
        grid = new int[numRows][numCols];

        try {
            fw = new RobocodeFileOutputStream(this.getDataFile("log_robocode.txt").getAbsolutePath(), true);
            System.out.println("Writing to: " + this.getDataFile("log_robocode.txt").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            setAhead(100);
            setTurnLeft(100);
            Random rand = new Random();
            setAllColors(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        Point2D.Double enemyCoordinates = utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        double enemyHeading = event.getHeading();
        double enemyVelocity = event.getVelocity();
        double timeSinceLastShot = getTime() - lastShotTime;
        double distanceToCenter = Point2D.distance(getX(), getY(), getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);

        System.out.println(
                "Enemy " + event.getName() + " spotted at " + enemyCoordinates.x + "," + enemyCoordinates.y + "\n");

        Bullet b = fireBullet(3);
        if (b != null) {
            System.out.println("Firing at " + event.getName());
            lastShotTime = getTime();
            bulletsInAir++;
            balasNoAr.put(b, new Dados(event.getDistance(), enemyCoordinates.x, enemyCoordinates.y, getX(), getY(),
                    getEnergy(), event.getEnergy(), getHeading(), getVelocity(), enemyHeading, enemyVelocity,
                    timeSinceLastShot, bulletsInAir, distanceToCenter));
        } else {
            System.out.println("Cannot fire right now...");
        }

        // Map the enemy position to the grid
        int enemyRow = (int) (enemyCoordinates.y / cellHeight);
        int enemyCol = (int) (enemyCoordinates.x / cellWidth);

        if (enemyRow >= 0 && enemyRow < grid.length && enemyCol >= 0 && enemyCol < grid[0].length) {
            grid[enemyRow][enemyCol] = 1;
            System.out.println("Enemy is in cell: (" + enemyRow + ", " + enemyCol + ")");
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        Dados d = balasNoAr.get(event.getBullet());
        if (d != null) {
            try {
                fw.write((d.distancia + "," + d.enemyX + "," + d.enemyY + "," + d.myX + "," + d.myY + "," + d.myEnergy
                        + "," + d.enemyEnergy + "," + d.myHeading + "," + d.myVelocity + "," + d.enemyHeading + ","
                        + d.enemyVelocity + "," + d.timeSinceLastShot + "," + d.bulletsInAir + "," + d.distanceToCenter
                        + ",hit\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            balasNoAr.remove(event.getBullet());
            bulletsInAir--;
        }
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        Dados d = balasNoAr.get(event.getBullet());
        if (d != null) {
            try {
                fw.write((d.distancia + "," + d.enemyX + "," + d.enemyY + "," + d.myX + "," + d.myY + "," + d.myEnergy + ","
                        + d.enemyEnergy + "," + d.myHeading + "," + d.myVelocity + "," + d.enemyHeading + ","
                        + d.enemyVelocity + "," + d.timeSinceLastShot + "," + d.bulletsInAir + "," + d.distanceToCenter
                        + ",no_hit\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            balasNoAr.remove(event.getBullet());
            bulletsInAir--;
        }
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        Dados d = balasNoAr.get(event.getBullet());
        if (d != null) {
            try {
                fw.write((d.distancia + "," + d.enemyX + "," + d.enemyY + "," + d.myX + "," + d.myY + "," + d.myEnergy + ","
                        + d.enemyEnergy + "," + d.myHeading + "," + d.myVelocity + "," + d.enemyHeading + ","
                        + d.enemyVelocity + "," + d.timeSinceLastShot + "," + d.bulletsInAir + "," + d.distanceToCenter
                        + ",no_hit\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            balasNoAr.remove(event.getBullet());
            bulletsInAir--;
        }
    }

    @Override
    public void onDeath(DeathEvent event) {
        try {
            if (fw != null) {
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        try {
            if (fw != null) {
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}