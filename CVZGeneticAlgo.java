import java.util.*;
import java.io.*;

/**
 * I had heard mention that a genetic algorith might be the way to go with this problem.
 * after looking into how they worked I created the following code to implement it
 * due to time limitations within the problem, simulating more than one move was untenable for larger games
 * this solution did not pass all test cases but passed enough that it was able to give me a significant score boost
 * 
 * comments in this code were generated by ChatGPT based on my existing code
 * 
 **/

class Player {
    private static final int POPULATION_SIZE = 50;
    private static final int GENERATIONS = 30;
    // Since we output only one move, our candidate is a single move (2 integers)
    private static final int GAME_WIDTH = 16000;
    private static final int GAME_HEIGHT = 9000;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            int ashX = scanner.nextInt();
            int ashY = scanner.nextInt();
            int humanCount = scanner.nextInt();
            List<int[]> humans = new ArrayList<>();
            for (int i = 0; i < humanCount; i++) {
                // Each human: id, x, y
                int id = scanner.nextInt();
                int x = scanner.nextInt();
                int y = scanner.nextInt();
                humans.add(new int[]{id, x, y});
            }

            int zombieCount = scanner.nextInt();
            List<int[]> zombies = new ArrayList<>();
            for (int i = 0; i < zombieCount; i++) {
                // Each zombie: id, x, y, nextX, nextY
                int id = scanner.nextInt();
                int x = scanner.nextInt();
                int y = scanner.nextInt();
                int nextX = scanner.nextInt();
                int nextY = scanner.nextInt();
                zombies.add(new int[]{id, x, y, nextX, nextY});
            }

            int[] bestMove = geneticAlgorithm(ashX, ashY, humans, zombies);
            System.out.println(bestMove[0] + " " + bestMove[1] + " Using GA");
        }
    }

    private static int[] geneticAlgorithm(int ashX, int ashY, List<int[]> humans, List<int[]> zombies) {
        List<int[]> population = initializePopulation(ashX, ashY, humans, zombies);
        Random random = new Random();

        for (int gen = 0; gen < GENERATIONS; gen++) {
            // Evaluate candidates and sort by descending score
            population.sort((move1, move2) -> Double.compare(
                    evaluateCandidate(move2, ashX, ashY, humans, zombies),
                    evaluateCandidate(move1, ashX, ashY, humans, zombies)
            ));
            // Create new population via crossover and mutation, keeping top elites
            population = crossoverAndMutate(population, random);
            if (population.isEmpty()) {
                population = initializePopulation(ashX, ashY, humans, zombies);
            }
        }
        return population.get(0);
    }

    private static List<int[]> initializePopulation(int ashX, int ashY, List<int[]> humans, List<int[]> zombies) {
        List<int[]> population = new ArrayList<>();
        // For diversity, half the population is initialized using a heuristic and half randomly
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int[] candidate = new int[2];
            if (i < POPULATION_SIZE / 2) {
                int[] target = findBestTarget(ashX, ashY, humans, zombies);
                candidate[0] = target[0];
                candidate[1] = target[1];
            } else {
                candidate[0] = (int)(Math.random() * GAME_WIDTH);
                candidate[1] = (int)(Math.random() * GAME_HEIGHT);
            }
            population.add(candidate);
        }
        return population;
    }

    private static int[] findBestTarget(int ashX, int ashY, List<int[]> humans, List<int[]> zombies) {
        int[] bestTarget = new int[]{ashX, ashY};
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int[] zombie : zombies) {
            double score = evaluateMove(zombie[1], zombie[2], humans, zombies);
            if (score > bestScore) {
                bestScore = score;
                bestTarget = new int[]{zombie[1], zombie[2]};
            }
        }
        return bestTarget;
    }

    private static List<int[]> crossoverAndMutate(List<int[]> population, Random random) {
        List<int[]> newPopulation = new ArrayList<>();
        // Elitism: carry over the top 10% directly
        int elites = Math.max(1, POPULATION_SIZE / 10);
        for (int i = 0; i < elites; i++) {
            newPopulation.add(population.get(i));
        }
        // Generate children until we fill the population
        while (newPopulation.size() < POPULATION_SIZE) {
            int[] parent1 = population.get(random.nextInt(population.size()));
            int[] parent2 = population.get(random.nextInt(population.size()));
            int[] child = new int[2];
            child[0] = (parent1[0] + parent2[0]) / 2;
            child[1] = (parent1[1] + parent2[1]) / 2;
            // Mutation chance of 10%
            if (random.nextDouble() < 0.1) {
                child[0] += random.nextInt(2000) - 1000;
                child[1] += random.nextInt(2000) - 1000;
            }
            // Ensure the move stays within the game zone
            child[0] = clamp(child[0], 0, GAME_WIDTH);
            child[1] = clamp(child[1], 0, GAME_HEIGHT);
            newPopulation.add(child);
        }
        return newPopulation;
    }

    private static double evaluateCandidate(int[] candidate, int ashX, int ashY, List<int[]> humans, List<int[]> zombies) {
        // Simulate one turn:
        // 1. Copy zombie positions
        List<int[]> simulatedZombies = deepCopy(zombies);
        // 2. Move zombies
        simulateZombieMovement(simulatedZombies);
        // 3. Ash moves to candidate position
        int newAshX = candidate[0];
        int newAshY = candidate[1];
        // 4. Evaluate the move
        return evaluateMove(newAshX, newAshY, humans, simulatedZombies);
    }

    private static void simulateZombieMovement(List<int[]> zombies) {
        for (int[] zombie : zombies) {
            int targetX = zombie[3];
            int targetY = zombie[4];
            double dist = distance(zombie[1], zombie[2], targetX, targetY);
            if (dist > 0) {
                // Move the zombie 400 units toward its target
                zombie[1] += (int)((targetX - zombie[1]) * 400 / dist);
                zombie[2] += (int)((targetY - zombie[2]) * 400 / dist);
            }
        }
    }

    private static double evaluateMove(int x, int y, List<int[]> humans, List<int[]> zombies) {
        double score = 0;
        // Penalize if a zombie can reach a human before Ash
        for (int[] human : humans) {
            for (int[] zombie : zombies) {
                double distHuman = distance(zombie[1], zombie[2], human[1], human[2]);
                double distAsh = distance(x, y, zombie[1], zombie[2]);
                // Compare turns: zombie turns = dist / 400, Ash turns = dist / 1000
                if (distHuman / 400 < distAsh / 1000) {
                    score -= 5000;
                }
            }
        }
        // Reward if any zombie is within Ash's shooting range (2000 units)
        for (int[] zombie : zombies) {
            double dist = distance(x, y, zombie[1], zombie[2]);
            if (dist <= 2000) {
                score += humans.size() * humans.size() * 10;
            }
        }
        return score;
    }

    private static List<int[]> deepCopy(List<int[]> list) {
        List<int[]> copy = new ArrayList<>();
        for (int[] arr : list) {
            copy.add(Arrays.copyOf(arr, arr.length));
        }
        return copy;
    }

    private static double distance(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private static int clamp(int value, int min, int max) {
        if(value < min) return min;
        if(value > max) return max;
        return value;
    }
}
