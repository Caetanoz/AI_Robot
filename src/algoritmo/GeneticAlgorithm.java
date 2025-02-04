package algoritmo;

import interf.IUIConfiguration;
import interf.IPoint;
import impl.Point;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
    private IUIConfiguration conf;
    private List<Rectangle> obstacles;
    private Random rand = new Random();

    public GeneticAlgorithm(IUIConfiguration conf, List<Rectangle> obstacles) {
        this.conf = conf;
        this.obstacles = obstacles;
    }

    public List<IPoint> findOptimalPath() {
        List<IPoint> bestSolution = null;
        double bestFitness = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 10000; i++) {
            List<IPoint> currentSolution = generateRandomPath(conf, rand);
            double fitness = calculateFitness(currentSolution, conf);

            if (fitness < bestFitness && isPathValid(currentSolution, conf)) {
                bestFitness = fitness;
                bestSolution = currentSolution;
            }
        }

        return bestSolution;
    }

    private List<IPoint> generateRandomPath(IUIConfiguration conf, Random rand) {
        List<IPoint> path = new ArrayList<>();
        path.add(conf.getStart());
        int size = rand.nextInt(5);
        for (int i = 0; i < size; i++) {
            path.add(new Point(rand.nextInt(conf.getWidth()-50)+25, rand.nextInt(conf.getHeight()-50)+25));
        }
        path.add(conf.getEnd());
        return path;
    }

    private boolean isPathValid(List<IPoint> path, IUIConfiguration conf) {
        return calculateCollisions(path, conf) == 0;
    }

    private int calculateCollisions(List<IPoint> path, IUIConfiguration conf) {
        int intersections = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Point2D.Double p1 = new Point2D.Double(path.get(i).getX(), path.get(i).getY());
            Point2D.Double p2 = new Point2D.Double(path.get(i + 1).getX(), path.get(i + 1).getY());
            Line2D.Double line = new Line2D.Double(p1, p2);

            for (Rectangle obstacle : conf.getObstacles()) {
                if (obstacle.intersectsLine(line)) {
                    intersections++;
                }
            }
        }
        return intersections;
    }

    public double calculateFitness(List<IPoint> path, IUIConfiguration conf) {
        int collisions = calculateCollisions(path, conf);
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            double dx = path.get(i + 1).getX() - path.get(i).getX();
            double dy = path.get(i + 1).getY() - path.get(i).getY();
            totalDistance += Math.sqrt(dx * dx + dy * dy);
        }
        return totalDistance + collisions * 100;
    }
}