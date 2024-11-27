import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        int[][] problem = input();
        Population population = new Population(problem, 1000);
    }

    public static int[][] input() throws FileNotFoundException {
        PrintStream fout = new PrintStream(new FileOutputStream("output.txt"));
        System.setOut(fout);
        FileInputStream fin = new FileInputStream("input.txt");
        System.setIn(fin);
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
    }

    void update() {
        doCrossOver();
        doMutation();
        doSpawn();
        doSelection();
    }

    private void doCrossOver() {
        List<Chromosome> newPopulation = new ArrayList<>();
        for(Chromosome chromosome : this.population) {
            Chromosome partner = getCrossOverPartner(chromosome);
            newPopulation.addAll(Arrays.asList(chromosome.crossover(partner)));
        }
        this.population.addAll(newPopulation);
    }

    private Chromosome getCrossOverPartner(Chromosome chromosome) {
        Chromosome partner = this.population.get(RandomGenerator.randomIndex(this.population.size())); //random index
        while(chromosome == partner) {
            partner = this.population.get(RandomGenerator.randomIndex(this.population.size())); //Random index
        }
        return partner;
    }

    private void doMutation() {

    }

    private void doSpawn() {
        for (int i = 0; i < initialSize; i++) {
            this.population.add(Chromosome.create(problem));
        }
    }

    private void doSelection() {

    }
}

class Chromosome {
    private Gene[] matrix;
    private int duplicates;

    private Chromosome(Gene[] matrix) {
        this.matrix = matrix;
    }

    public static Chromosome create(int[][] matrix) {

    }

    public List<Gene[]> split() {
        Gene[] first = new Gene[matrix.length / 2];
        Gene[] second = new Gene[matrix.length - matrix.length / 2];
        for (int i = 0; i < matrix.length; i++) {
            if (i < matrix.length / 2) {
                first[i] = matrix[i];
            } else {
                second[matrix.length - i] = matrix[i];
            }
        }
        List<Gene[]> result = new ArrayList<>();
        result.add(first);
        result.add(second);
        return result;
    }

    public Chromosome[] crossover(Chromosome other) {
        for (Gene gene : matrix) {

        }
    }

    public Chromosome mutate() {

    }

    public int fitness() {
        int duplicatesNum = 0;
        for (int cube_row = 0; cube_row < 3; cube_row++)  {
            for (int cube_col = 0; cube_col < 3; cube_col++) {
                Set<Integer> cubeNums = new HashSet<>();
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        cubeNums.add(matrix[cube_row * 3 + i].getByIndex(cube_col * 3 + j));
                    }
                }
                duplicatesNum += cubeNums.size();
                cubeNums.clear();
            }
        }

        for (int col = 0; col < 9; col++) {
            Set<Integer> colNums = new HashSet<>();
            for (int row = 0; row < 9; row++) {
                colNums.add(matrix[row].getByIndex(col));
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

    public Gene(int[] row) {
        this.row = row;
    }

    public int getByIndex(int index) {
        return row[index];
    }
}

class RandomGenerator {
    public static int randomIndex(int size) {
        return (int) (Math.random() * size);
    }
}