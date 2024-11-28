import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private List<Gene> matrix;
    private int duplicates;

    private Chromosome(List<Gene> matrix) {
        this.matrix = matrix;
    }

    public static Chromosome create(int[][] problem) {
        List<Gene> matrix = new ArrayList<Gene>();
        for (int i = 0; i < problem.length; i++) {
            Gene gene = new Gene(problem[i]);
            matrix.set(i, gene);
        }
        return new Chromosome(matrix);
    }

    public List<Gene>[] split() {
        List<Gene> first = new ArrayList<Gene>();
        List<Gene> second = new ArrayList<Gene>();
        for (int i = 0; i < matrix.size(); i++) {
            if (i < matrix.size() / 2) {
                first.set(i, matrix.get(i));
            } else {
                second.set(i - matrix.size() / 2, matrix.get(i));
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
            if (firstCrossover.size() <= 4) {
                for (int i = 0; i < 9 - secondOther.size() - first.size(); i++) {
                    firstCrossover.add(second.get(i));
                }
            } else {
                for (int i = first.size(); i < 9 - secondOther.size(); i++) {
                    firstCrossover.add(firstOther.get(i));
                }
            }
        } else {
            for (int i = first.size(); i < 9; i++) {
                firstCrossover.add(first.get(i - first.size()));
            }
            for (int i = 9; i > first.size(); i--) {
                firstCrossover.set(i, secondOther.get(i - 10 + secondOther.size()));
            }
        }

        List<Gene> secondCrossover = new ArrayList<>(firstOther);
        if (secondCrossover.size() + second.size() == 9) {
            secondCrossover.addAll(secondOther);
        } else if (secondCrossover.size() + second.size() < 9) {
            if (secondCrossover.size() <= 4) {
                for (int i = 0; i < 9 - second.size() - firstOther.size(); i++) {
                    secondCrossover.add(secondOther.get(i));
                }
            } else {
                for (int i = firstOther.size(); i < 9 - second.size(); i++) {
                    secondCrossover.add(first.get(i));
                }
            }
        } else {
            for (int i = firstOther.size(); i < 9; i++) {
                secondCrossover.add(firstOther.get(i - firstOther.size()));
            }
            for (int i = 9; i > firstOther.size(); i--) {
                secondCrossover.set(i, second.get(i - 10 + second.size()));
            }
        }

        Chromosome[] result = new Chromosome[2];
        result[0] = new Chromosome(firstCrossover);
        result[1] = new Chromosome(secondCrossover);
        return result;
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

    public Gene(int[] row) {
        List<Integer> sudokuNums = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < row.length; i++) {
            if (row[i] == 0) {
                row[i] = sudokuNums.get(RandomGenerator.randomIndex(sudokuNums.size()));
            }
            sudokuNums.remove(row[i]);
        }
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