package sampleRobots;

import robocode.*;
import java.util.List;
import java.awt.geom.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import algoritmo.GeneticAlgorithm;
import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.*;
import impl.UIConfiguration;
import interf.IPoint;

/**
 * This Robot uses the model provided to guess whether it will hit or miss an enemy.
 * This is a very basic model, trained specifically on the following enemies: Corners, Crazy, SittingDuck, Walls.
 * It is not expected to do great...
 */
public class NengueRobot extends AdvancedRobot {

    EasyPredictModelWrapper model;

    double lastShotTime = 0;
    double bulletsInAir = 0;

    private List<Rectangle> obstacles;
    public static UIConfiguration conf;
    private List<IPoint> points;
    private HashMap<String, Rectangle> inimigos;
    private class Dados{
        Double distancia; //distancia a que o robot se encontra
        Double velocidade; //velocidade a que o robot inimigo se desloca

        public Dados(Double distancia, Double velocidade) {
            this.distancia = distancia;
            this.velocidade = velocidade;
        }

        double gunHeading = getGunHeading(); // Direção da arma

        double gunHeat = getGunHeat(); // Calor da arma
        double gunCoolingRate = getGunCoolingRate(); // Taxa de arrefecimento da arma
        double timeUntilCooled = gunHeat / gunCoolingRate; // Tempo até a arma arrefecer

        double shooterHeading = getHeading(); // Direção do robô
        double shooterVelocity = getVelocity(); // Velocidade do robô
        double radarHeading = getRadarHeading(); // Direção do radar

    }
    HashMap<Bullet, Dados> balasNoAr = new HashMap<>();


    @Override
    public void run() {
        super.run();

        obstacles = new ArrayList<>();
        inimigos = new HashMap<>();
        conf = new UIConfiguration((int) getBattleFieldWidth(), (int) getBattleFieldHeight(), obstacles);

        System.out.println("Reading model from folder: " + getDataDirectory());
        try {
            // Load the model
            // TODO: be sure to change the path to the model!
            // You will need to create the corresponding .data folder in the package of your robot's class, and copy the model there
            model = new EasyPredictModelWrapper(MojoModel.load(this.getDataFile("gbm_573ec046_f31c_4771_a661_c7fe34d9e44c.zip").getAbsolutePath()));
            model = new EasyPredictModelWrapper(MojoModel.load(this.getDataFile("gbm_3e6f4c5c_aba3_4925_a55a_9708f487349e.zip").getAbsolutePath()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        while (true) {
            setAhead(100);
            setTurnLeft(100);
            Random rand = new Random();
            setBodyColor(Color.BLACK); // Define a cor do corpo como vermelho
            setGunColor(Color.BLACK); // Define a cor da arma como preto
            setRadarColor(Color.BLACK); // Define a cor do radar como amarelo
            setBulletColor(Color.BLACK); // Define a cor das balas como verde
            setScanColor(Color.BLACK); // Define a cor do scan como branco

            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        Point2D.Double enemyCoordinates = utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        System.out.println("Enemy " + event.getName() + " spotted at " + enemyCoordinates.x + "," + enemyCoordinates.y + "\n");

        Bullet b = fireBullet(3);
        if (b!=null){
            System.out.println("Firing at "+event.getName());
            //guardar os dados do inimigo temporariamente, até que a bala chegue ao destino, para depois os escrever em ficheiro
            balasNoAr.put(b, new Dados(event.getDistance(), event.getVelocity()));
        }
        else
            System.out.println("Cannot fire right now...");
        double enemyBearing = event.getBearing();
        double gunTurn = getHeading() + enemyBearing - getGunHeading();
        setTurnGunRight(normalizeBearing(gunTurn));

        double timeSinceLastShot = getTime() - lastShotTime;
        bulletsInAir++;
        double distanceToCenter = Point2D.distance(getX(), getY(), getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);

        RowData row = new RowData();
        row.put("name", event.getName());
        row.put("distance", event.getDistance());
        row.put("velocity", event.getVelocity());
        row.put("GunHeading", getGunHeading());
        row.put("GunHeat", getGunHeat());
        row.put("GunCoolingRate", getGunCoolingRate());
        row.put("shooterHeading", getHeading());
        row.put("shooterVelocity", getVelocity());
        row.put("radarHeading", getRadarHeading());
        row.put("timeUntilCooled", getGunHeat() / getGunCoolingRate());
        row.put("distancia", event.getDistance());
        row.put("enemyX", enemyCoordinates.x);
        row.put("enemyY", enemyCoordinates.y);
        row.put("myX", getX());
        row.put("myY", getY());
        row.put("myEnergy", getEnergy());
        row.put("enemyEnergy", event.getEnergy());
        row.put("myHeading", getHeading());
        row.put("myVelocity", getVelocity());
        row.put("enemyHeading", event.getHeading());
        row.put("enemyVelocity", event.getVelocity());
        row.put("timeSinceLastShot", timeSinceLastShot);
        row.put("bulletsInAir", bulletsInAir);
        row.put("distanceToCenter", distanceToCenter);

        try {
            BinomialModelPrediction p = model.predictBinomial(row);
            System.out.println("Will I hit? -> " + p.classProbabilities[1]);

            attemptFire(p.classProbabilities[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        execute();
    }

    private void attemptFire(double hitProbability) {
        System.out.println("Hit probability: " + hitProbability);
        System.out.println("Gun heat: " + getGunHeat());
        System.out.println("Energy: " + getEnergy());
        if (hitProbability > 0.7 && getGunHeat() == 0 && getEnergy() > 1) {
            System.out.println("Firing!");
            fire(3);
            lastShotTime = getTime(); // Update the lastShotTime when firing
        } else {
            System.out.println("Not firing");
        }
    }

    private double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        super.onHitByBullet(event);
        // Evade by changing direction and speed
        double turnDegrees = 45 + new Random().nextDouble() * 45; // Turn between 45 and 90 degrees
        if (new Random().nextBoolean()) {
            setTurnRight(turnDegrees);
        } else {
            setTurnLeft(turnDegrees);
        }
        setAhead(150); // Move ahead to evade
        execute();
    }

    public void executeGeneticAlgorithm() {
        GeneticAlgorithm ga = new GeneticAlgorithm(conf, obstacles);
        List<IPoint> optimalPath = ga.findOptimalPath();

        // Print the optimal path
        System.out.println("Optimal Path:");
        for (IPoint point : optimalPath) {
            System.out.println("(" + point.getX() + ", " + point.getY() + ")");
        }
    }
}
