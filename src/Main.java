import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final int POPULATION_SIZE = 148;
        final int RESET = 350;
        int[][] problem = input();
        // Storing input as array of int arrays
        Population population = new Population(problem, POPULATION_SIZE);

        do {
            // Updating population until there is 500 updates without improving or
            // there is a solution found
            population.update();
            if (population.getAge() == RESET) {
                // Creating new population if no improvements in long time
                population = new Population(problem, RESET);
            }
        } while ((population.population.getFirst().fitness() != 0));
    }

    // Method for scanning input
    public static int[][] input() {
        Scanner scanner = new Scanner(System.in);
        int[][] matrix = new int[9][9];
        for (int i = 0; i < 9; i++) {
            String[] line = scanner.nextLine().split(" ");
            for (int j = 0; j < 9; j++) {

                // Storing 0 in cells where there is no data
                if (line[j].equals("-")) {
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = Integer.parseInt(line[j]);
                }
            }
        }
        return matrix;
    }
}

// Class for storing a population
class Population {
    int[][] problem;
    List<Chromosome> population;
    int initialSize;

    // Initial values for age and best fitness
    int age = 0;
    int bestFitness = 10000;

    public Population(int[][] problem, int initialSize) {
        this.problem = problem;
        this.initialSize = initialSize;
        this.population = new ArrayList<>();

        // Creating a new population
        for (int i = 0; i < initialSize; i++) {
            population.add(new Chromosome(problem));
        }
    }

    // Getter for age value
    public int getAge() {
        return age;
    }

    // Function for updating the population
    // creating new generation
    void update() {
        doCrossOver();
        doMutation();
        doSelection();

        // Taking fittest individual after selection
        int newBestFitness = population.getFirst().fitness();
        if (newBestFitness < bestFitness) {
            bestFitness = newBestFitness;
            age = 0;
            // Reset age if improvement is made
        } else {
            age++;
        }

        // If there is a solution found
        if (bestFitness == 0) {
            printAlpha();
        }
    }

    // Printing the best solution found
    public void printAlpha() {
        Chromosome alpha = this.population.getFirst();;

        // Calling print method for chromosome
        alpha.printChromosome();
    }


    // Doing crossover for every chromosome in population
    private void doCrossOver() {
        List<Chromosome> newPopulation = new ArrayList<>();
        for (Chromosome chromosome : this.population) {
            // Taking partner and adding new chromosomes
            Chromosome partner = getCrossOverPartner(chromosome);
            newPopulation.addAll(Arrays.asList(chromosome.crossover(partner)));
        }

        this.population.addAll(newPopulation);
    }

    // Method for finding partner for chromosome to make a crossover
    private Chromosome getCrossOverPartner(Chromosome chromosome) {
        // Taking partner randomly until it is not the same chromosome
        Chromosome partner = this.population.get(RandomGenerator.randomIndex(this.population.size()));
        while(chromosome == partner) {
            partner = this.population.get(RandomGenerator.randomIndex(this.population.size())); //Random index
        }
        return partner;
    }

    // Function for generating mutations
    private void doMutation() {
        List<Chromosome> newPopulation = new ArrayList<>();
        for(int i = 0; i < population.size(); i++) {
            // Taking chromosome randomly for mutating
            Chromosome mutation = this.population.get(RandomGenerator.
                    randomIndex(this.population.size())).mutate(problem);
            newPopulation.add(mutation);
            mutation.fitness();
        }

        this.population.addAll(newPopulation);
    }

    // Function to select best chromosomes in population
    private void doSelection() {
        population = population.stream()
                .sorted(Comparator.comparingInt(Chromosome::fitness))
                .limit(initialSize)
                .collect(Collectors.toList());
    }


}

// Class representing possible solution for sudoku
class Chromosome {
    private List<Gene> matrix;

    // Initializing fitness as -1
    private int fitness = -1;

    // First constructor
    public Chromosome(int[][] problem) {
        List<Gene> newMatrix = new ArrayList<>();
        for (int i = 0; i < problem.length; i++) {
            Gene gene = new Gene(problem[i].clone());
            newMatrix.add(i, gene);
        }
        this.matrix = newMatrix;
    }

    // Second constructor (used for cross over functions)
    public Chromosome(List<Gene> matrix) {
        this.matrix = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            this.matrix.add(new Gene(matrix.get(i).getRow().clone()));
        }
    }

    // Method for printing possible solution
    public void printChromosome() {
        for (Gene gene : this.matrix) {
            gene.printGene();
        }
    }

    // Cross over function
    public Chromosome[] crossover(Chromosome chromosome) {
        // Initializing 2 new chromosomes
        Chromosome[] crossover = new Chromosome[2];
        crossover[0] = new Chromosome(this.matrix);
        crossover[1] = new Chromosome(this.matrix);
        for (int i = 0; i < chromosome.matrix.size(); i++) {
            // Setting random genes from old chromosomes to create
            // 2 new chromosomes
            if (RandomGenerator.randomIndex(2) == 0) {
                crossover[0].matrix.set(i, this.matrix.get(i));
                crossover[1].matrix.set(i, chromosome.matrix.get(i));
            } else {
                crossover[1].matrix.set(i, this.matrix.get(i));
                crossover[0].matrix.set(i, chromosome.matrix.get(i));
            }
        }
        // Returning an array of 2 chromosomes created
        return crossover;
    }

    // Method for mutation
    public Chromosome mutate(int[][] initialProblem) {
        // Select three distinct rows
        Set<Integer> distinctRows = new HashSet<>();
        while (distinctRows.size() < 3) {
            distinctRows.add(RandomGenerator.randomIndex(matrix.size()));
        }

        List<Integer> rows = new ArrayList<>(distinctRows); // Convert set to list
        Chromosome mutatedChromosome = new Chromosome(this.matrix);

        // Perform a swap in each selected row
        for (int geneIndex : rows) {
            List<Integer> indices = IntStream.range(0, 9)
                    .filter(i -> initialProblem[geneIndex][i] == 0)
                    .boxed()
                    .collect(Collectors.toList());

            // Skip mutation if fewer than 2 mutable indices are available
            if (indices.size() < 2) {
                continue;
            }

            // Selecting random indices
            Collections.shuffle(indices);
            int ind1 = indices.get(0);
            int ind2 = indices.get(1);

            Gene gene = mutatedChromosome.matrix.get(geneIndex);
            gene.swap(ind1, ind2); // Swap two mutable values in the selected row
        }

        mutatedChromosome.fitness = -1; // Reset fitness
        return mutatedChromosome;
    }

    // fitness function
    public int fitness() {
        if (fitness == -1) { // Calculate only if not updated
            int duplicates = 0;
            int[] counts = new int[10]; // use fixed-size array for counts

            // Column check
            for (int col = 0; col < 9; col++) {
                Arrays.fill(counts, 0);
                for (int row = 0; row < 9; row++) {
                    counts[matrix.get(row).getByIndex(col)]++;
                }
                for (int count : counts) {
                    if (count > 1) duplicates += count - 1;
                }
            }

            // sub grid check
            for (int gridRow = 0; gridRow < 3; gridRow++) {
                for (int gridCol = 0; gridCol < 3; gridCol++) {
                    Arrays.fill(counts, 0);
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            counts[matrix.get(gridRow * 3 + i).getByIndex(gridCol * 3 + j)]++;
                        }
                    }
                    for (int count : counts) {
                        if (count > 1) duplicates += count - 1;
                    }
                }
            }
            fitness = duplicates;
        }
        return fitness;
    }

}

// Class gene representing each row of sudoku matrix
class Gene {
    private final int[] row;

    // Getter for row
    public int[] getRow() {
        return row;
    }

    // Method for printing
    public void printGene() {
        for (int i = 0; i < 9; i++) {
            if (i == 8) {
                System.out.print(row[i]);
                break;
            }
            System.out.print(row[i] + " ");
        }
        System.out.println();
    }

    // Constructor to initialize row
    public Gene(int[] row) {
        List<Integer> sudokuNums = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .collect(Collectors.toCollection(ArrayList::new));
        for (int j : row) {
            if (j != 0) {
                sudokuNums.remove((Integer) j);
            }
        }

        // Creating row without conflicts
        for (int i = 0; i < row.length; i++) {
            if (!sudokuNums.isEmpty()) {
                int index = RandomGenerator.randomIndex(sudokuNums.size());
                if (row[i] == 0) {
                    row[i] = sudokuNums.get(index);
                    sudokuNums.remove(index);
                }
            } else {
                break;
            }
        }
        this.row = row;
    }

    // Getting element from row by index
    public int getByIndex(int index) {
        return row[index];
    }

    // Swapping for mutations
    public void swap(int ind1, int ind2) {
        int temp = row[ind1];
        row[ind1] = row[ind2];
        row[ind2] = temp;
    }
}

// Random generator
class RandomGenerator {
    private final static Random R = new Random(System.currentTimeMillis());
    public static int randomIndex(int size) {
        return R.nextInt(size);
    }
}

// =ОБНЯЛ=