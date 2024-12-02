import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final int POPULATION_SIZE = 1000;
        final int RESET = 250;
        int[][] problem = input();
        // Storing input as array of int arrays
        Population population = new Population(problem, POPULATION_SIZE);

        do {
            // Updating population until there is 250 updates without improving or
            // there is a solution found
            population.update();
            if (population.getAge() == RESET) {
                // Creating new population if no improvements in long time
                population = new Population(problem, POPULATION_SIZE);
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
        for (int i = 0; i < initialSize * 2; i++) {
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
        for (int i = 0; i < initialSize; i++) {
            // Taking partner and adding new chromosomes
            Chromosome chromosome = population.get(i);
            Chromosome partner = getCrossOverPartner(population.get(i));
            newPopulation.add(chromosome.crossover(partner));
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
        for(int i = 0; i < population.size() / 2; i++) {
            // Taking chromosome randomly for mutating
            Chromosome mutation = this.population.get(RandomGenerator.
                    randomIndex(this.population.size())).mutate(problem);
            newPopulation.add(mutation);
        }

        this.population.addAll(newPopulation);
    }

    // Function to select best chromosomes in population
    private void doSelection() {
        population = population.parallelStream()
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

    // Constructor for mutation function
    public Chromosome(List<Gene> matrix) {
        this.matrix = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            this.matrix.add(new Gene(matrix.get(i).getRow().clone()));
        }
    }


    // Constructor to spawn new solution
    public Chromosome(int[][] problem) {
        List<Gene> newMatrix = new ArrayList<>();
        for (int i = 0; i < problem.length; i++) {
            Gene gene = new Gene(problem[i].clone());
            newMatrix.add(i, gene);
        }
        this.matrix = newMatrix;
    }

    // Constructor for cross-over functions
    public Chromosome() {
        this.matrix = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            this.matrix.add(new Gene());
        }
        fitness = -1;
    }

    // Method for printing possible solution
    public void printChromosome() {
        for (Gene gene : this.matrix) {
            gene.printGene();
        }
    }

    // Cross-over function
    public Chromosome crossover(Chromosome chromosome) {
        Chromosome crossover = new Chromosome();
        for (int i = 0; i < chromosome.matrix.size(); i++) {
            // Setting random genes from old chromosomes to create
            // new chromosome
            if (RandomGenerator.randomIndex(2) == 0) {
                crossover.matrix.set(i, this.matrix.get(i));
            } else {
                crossover.matrix.set(i, chromosome.matrix.get(i));
            }
        }
        // Returning a chromosome created
        return crossover;
    }

    // Method for mutation
    public Chromosome mutate(int[][] initialProblem) {
        int geneIndex1 = RandomGenerator.randomIndex(matrix.size());
        // Creating mutated chromosome
        Chromosome mutatedChromosome = new Chromosome(this.matrix);
        Gene mutated1 = mutatedChromosome.matrix.get(geneIndex1);

        // Checking valid indexes for mutation
        List<Integer> indexes1 = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (initialProblem[geneIndex1][i] == 0) {
                indexes1.add(i);
            }
        }

        // Swapping random indexes in a row
        int mutInd1 = indexes1.get(RandomGenerator.randomIndex(indexes1.size()));
        int mutInd2 = indexes1.get(RandomGenerator.randomIndex(indexes1.size()));
        while (mutInd2 == mutInd1) {
            mutInd2 = indexes1.get(RandomGenerator.randomIndex(indexes1.size()));
        }
        mutated1.swap(mutInd1, mutInd2);

        mutatedChromosome.matrix.set(geneIndex1, mutated1);
        return mutatedChromosome;
    }

    // Fitness function
    public int fitness() {
        if (fitness == -1) { // Calculate only if not updated
            int duplicates = 0;
            int[] counts = new int[10]; // use fixed-size array for counts

            // Columns check
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

    // Method for printing a row
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

    // Constructor to initialize an empty row
    public Gene() {
        this.row = new int[9];
        Arrays.fill(row, 0);
    }

    // Constructor to initialize row as a gene
    public Gene(int[] row) {
        List<Integer> sudokuNums = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .collect(Collectors.toCollection(ArrayList::new));
        for (int j : row) {
            if (j != 0) {
                sudokuNums.remove((Integer) j);
            }
        }

        // Creating a row without conflicts
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

    // Getting an element from a row by index
    public int getByIndex(int index) {
        return row[index];
    }

    // Swapping 2 elements in a row for mutations
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
// =ТЕРПИМ=