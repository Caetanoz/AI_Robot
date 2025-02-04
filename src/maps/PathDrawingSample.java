package maps;

import interf.IUIConfiguration;
import viewer.PathViewer;
import interf.IPoint;
import algoritmo.GeneticAlgorithm;

import java.util.List;

/**
 * Exemplo que mostra como desenhar um caminho no visualizador.
 */
public class PathDrawingSample {
    public static IUIConfiguration conf;

    public static void main(String args[]) throws InterruptedException, Exception {
        // O ID do mapa a usar (ver Maps.java)
        int map_id = 1;

        conf = Maps.getMap(map_id);
        
        // Instantiate the GeneticAlgorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(conf, conf.getObstacles());

        // Find the optimal path using the genetic algorithm
        List<IPoint> bestSolution = ga.findOptimalPath();

        if (bestSolution != null) {
            double bestFitness = ga.calculateFitness(bestSolution, conf);
            System.out.println("Melhor solução encontrada com fitness: " + bestFitness);
            
            // Visualize the best solution found
            PathViewer pv = new PathViewer(conf);
            pv.setFitness(bestFitness); // Substitui pelo valor de fitness calculado
            pv.setStringPath(pathToString(bestSolution)); // Substitui pelo caminho encontrado
            pv.paintPath(bestSolution);
        } else {
            System.out.println("Nenhuma solução válida encontrada.");
        }
    }

    // Função para converter uma lista de pontos em uma string
    private static String pathToString(List<IPoint> path) {
        StringBuilder solutionString = new StringBuilder();
        for (IPoint point : path) {
            solutionString.append("(").append(point.getX()).append(",").append(point.getY()).append(") ");
        }
        return solutionString.toString();
    }
}
