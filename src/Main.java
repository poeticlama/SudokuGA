import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        int[][] problem = input();
        Population population = new Population(problem, 150);
        do {
            population.update();
        } while (population.getAlpha().getDuplicates() != 0);
    }

    public static int[][] input() {
        Scanner scanner = new Scanner(System.in);
        int[][] matrix = new int[9][9];
        for (int i = 0; i < 9; i++) {
            String[] line = scanner.nextLine().split(" ");
            for (int j = 0; j < 9; j++) {
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

class Population {
    int[][] problem;
    List<Chromosome> population;
    int initialSize;

    public Population(int[][] problem, int initialSize) {
        this.problem = problem;
        this.initialSize = initialSize;
        this.population = new ArrayList<>();
        for (int i = 0; i < initialSize; i++) {
            population.add(new Chromosome(problem));
        }
    }

    void update() {
        doCrossOver();
        doMutation();
        doSpawn();
        doSelection();
        printAlpha();
    }

    private void printAlpha() {
        Chromosome alpha = getAlpha();
        alpha.printChromosome();
    }

    private void doCrossOver() {
        List<Chromosome> newPopulation = new ArrayList<>();
        for (Chromosome chromosome : this.population) {
            Chromosome partner = getCrossOverPartner(chromosome);
            newPopulation.addAll(Arrays.asList(chromosome.crossover(partner)));
        }
        this.population.addAll(newPopulation);
    }

    Chromosome getAlpha() {
        return this.population.getFirst();
    }

    private Chromosome getCrossOverPartner(Chromosome chromosome) {
        Chromosome partner = this.population.get(RandomGenerator.randomIndex(this.population.size())); //random index
        while(chromosome == partner) {
            partner = this.population.get(RandomGenerator.randomIndex(this.population.size())); //Random index
        }
        return partner;
    }

    private void doMutation() {
        List<Chromosome> newPopulation = new ArrayList<>();
        for(int i = 0; i < 50; i++) {
            Chromosome mutation = this.population.get(RandomGenerator.
                    randomIndex(this.population.size())).mutate(problem);
            newPopulation.add(mutation);
        }
        this.population.addAll(newPopulation);
    }

    private void doSpawn() {
        for (int i = 0; i < initialSize; i++) {
            this.population.add(new Chromosome(problem));
        }
    }

    private void doSelection() {
        this.population.sort(Comparator.comparingInt(Chromosome::fitness));
        population = population.subList(0, initialSize);
    }
}

class Chromosome {
    private List<Gene> matrix;
    private int duplicates;

    public Chromosome(int[][] problem) {
        List<Gene> newMatrix = new ArrayList<>();
        for (int i = 0; i < problem.length; i++) {
            Gene gene = new Gene(problem[i].clone());
            newMatrix.add(i, gene);
        }
        this.matrix = newMatrix;
    }

    public Chromosome(List<Gene> matrix) {
        this.matrix = matrix;
    }

    public int getDuplicates() {
        return duplicates;
    }

    public void printChromosome() {
        for (Gene gene : this.matrix) {
            gene.printGene();
        }
        System.out.println(duplicates);
    }

    public List<Gene>[] split() {
        List<Gene> first = new ArrayList<>();
        List<Gene> second = new ArrayList<>();
        int random = RandomGenerator.randomIndex(this.matrix.size() - 1) + 1;
        for (int i = 0; i < matrix.size(); i++) {
            if (i < random) {
                first.add(i, matrix.get(i));
            } else {
                second.add(i - random, matrix.get(i));
            }
        }
        List<Gene>[] result = new List[2];
        result[0] = first;
        result[1] = second;
        return result;
    }

    public Chromosome[] crossover(Chromosome other) {
        List<Gene>[] genesList = split();
        List<Gene> first = genesList[0];
        List<Gene> second = genesList[1];

        List<Gene>[] genesListOther = other.split();
        List<Gene> firstOther = genesListOther[0];
        List<Gene> secondOther = genesListOther[1];

        List<Gene> firstCrossover = new ArrayList<>(first);

        if (firstCrossover.size() + secondOther.size() == 9) {
            firstCrossover.addAll(secondOther);
        } else if (firstCrossover.size() + secondOther.size() < 9) {
            for (int i = 0; i < 9 - secondOther.size() - first.size(); i++) {
                firstCrossover.add(second.get(i));
            }
            firstCrossover.addAll(secondOther);
        } else {
            firstCrossover.addAll(second);
            for (int i = 9; i > first.size(); i--) {
                firstCrossover.set(i - 1, secondOther.get(i - 10 + secondOther.size()));
            }
        }

        List<Gene> secondCrossover = new ArrayList<>(firstOther);
        if (secondCrossover.size() + second.size() == 9) {
            secondCrossover.addAll(second);
        } else if (secondCrossover.size() + second.size() < 9) {
            for (int i = 0; i < 9 - second.size() - firstOther.size(); i++) {
                secondCrossover.add(secondOther.get(i));
            }
            secondCrossover.addAll(second);
        } else {
            secondCrossover.addAll(secondOther);
            for (int i = 9; i > firstOther.size(); i--) {
                secondCrossover.set(i - 1, second.get(i - 10 + second.size()));
            }
        }

        Chromosome[] result = new Chromosome[2];
        result[0] = new Chromosome(firstCrossover);
        result[1] = new Chromosome(secondCrossover);
        return result;
    }

    public Chromosome mutate(int[][] initialProblem) {
        int index1 = RandomGenerator.randomIndex(matrix.size());
        Gene mutated1 = matrix.get(index1);
        int[] row1 = initialProblem[index1];

        int index2 = RandomGenerator.randomIndex(matrix.size());
        while (index2 == index1) {
            index2 = RandomGenerator.randomIndex(matrix.size());
        }
        Gene mutated2 = matrix.get(index2);
        int[] row2 = initialProblem[index2];

        List<Integer> indexes1 = new ArrayList<>();
        List<Integer> indexes2 = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (initialProblem[index1][i] != 0) {
                indexes1.add(i);
            }
            if (initialProblem[index2][i] != 0) {
                indexes2.add(i);
            }
        }

        int mutInd1 = indexes1.get(RandomGenerator.randomIndex(indexes1.size()));
        int mutInd2 = indexes1.get(RandomGenerator.randomIndex(indexes1.size()));
        while (mutInd2 == mutInd1) {
            mutInd2 = indexes1.get(RandomGenerator.randomIndex(indexes1.size()));
        }
        mutated1.swap(mutInd1, mutInd2);

        mutInd1 = indexes2.get(RandomGenerator.randomIndex(indexes2.size()));
        mutInd2 = indexes2.get(RandomGenerator.randomIndex(indexes2.size()));
        while (mutInd2 == mutInd1) {
            mutInd2 = indexes2.get(RandomGenerator.randomIndex(indexes2.size()));
        }
        mutated2.swap(mutInd1, mutInd2);

        matrix.set(index1, mutated1);
        matrix.set(index2, mutated2);
        return this;
    }

    public int fitness() {
        int duplicatesNum = 0;
        for (int cube_row = 0; cube_row < 3; cube_row++)  {
            for (int cube_col = 0; cube_col < 3; cube_col++) {
                Set<Integer> cubeNums = new HashSet<>();
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        cubeNums.add(matrix.get(cube_row * 3 + i).getByIndex(cube_col * 3 + j));
                    }
                }
                duplicatesNum += cubeNums.size();
                cubeNums.clear();
            }
        }

        for (int col = 0; col < 9; col++) {
            Set<Integer> colNums = new HashSet<>();
            for (int row = 0; row < 9; row++) {
                colNums.add(matrix.get(row).getByIndex(col));
            }
            duplicatesNum += colNums.size();
            colNums.clear();
        }
        duplicates = duplicatesNum;
        return duplicatesNum;
    }
}

class Gene {
    private final int[] row;

    public void printGene() {
        for (int j : row) {
            System.out.print(j + " ");
        }
        System.out.println();
    }

    public Gene(int[] row) {
        List<Integer> sudokuNums = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < row.length; i++) {
            int index = RandomGenerator.randomIndex(sudokuNums.size());
            if (row[i] == 0) {
                row[i] = sudokuNums.get(index);
                sudokuNums.remove(index);
            } else {
                sudokuNums.remove((Integer) row[i]);
            }
        }
        this.row = row;
    }

    public int getByIndex(int index) {
        return row[index];
    }

    public void swap(int ind1, int ind2) {
        int temp = row[ind1];
        row[ind1] = row[ind2];
        row[ind2] = temp;
    }
}

class RandomGenerator {
    private final static Random R = new Random(System.currentTimeMillis());
    public static int randomIndex(int size) {
        return R.nextInt(size);
    }
}