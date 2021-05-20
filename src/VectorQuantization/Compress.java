package VectorQuantization;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class Compress {
    private ArrayList<vector> blocks = new ArrayList<>();     // contains vectors
    private ArrayList<Group> averages = new ArrayList<>();    // contain averages and their nearest vectors
    private ArrayList<Integer> codes = new ArrayList<>();
    private int codeBookSize;
    private int h, w;
    private int height, width;


    Compress(String img_path) {
        ConstructVectors c = new ConstructVectors(4, 4, img_path);
        __init(c.blocks, 32, 4, 4, c.hei, c.wi);
    }

    void encode() throws IOException {
        System.out.println("Compressing...");
        split();
        getCodes();
        writeOnFile();
    }

    private void __init(ArrayList<vector> data, int k, int row, int col, int n, int m) {
        blocks.addAll(data);
        codeBookSize = k;
        h = row;
        w = col;
        height = n;
        width = m;
        vector v = new vector(h, w);
        Group g = new Group(v);
        g.setNeighbours(blocks);
        averages.add(g);
    }

    private void split() {

        // split til we get codeBookSize number of vectors
        while (averages.size() < codeBookSize) {
            ArrayList<Group> temp = new ArrayList<>();      // to hold new averages and their neighbours(nearest vectors)
            // Splitting every average into two averages
            for (Group curr : averages) {
                vector curr_vector = curr.v;
                ArrayList<vector> nei = curr.neighbours;    // takes nearest vectors of an average

                // calculate average of the nearest vectors
                vector new_avg = new vector(curr_vector.height, curr_vector.width);
                for (int n = 0; n < curr_vector.height; ++n) {
                    for (int m = 0; m < curr_vector.width; ++m) {
                        double d = 0.0;
                        for (vector v : nei) {
                            d += v.data[n][m];
                        }
                        new_avg.data[n][m] = Math.ceil(1.0 * (d / (1.0 * nei.size())));
                    }
                }

                // split the new average
                vector v1 = new vector(curr_vector.height, curr_vector.width);
                vector v2 = new vector(curr_vector.height, curr_vector.width);
                for (int j = 0; j < curr_vector.height; ++j) {
                    for (int k = 0; k < curr_vector.width; ++k) {
                        v1.data[j][k] = new_avg.data[j][k] - 1;
                    }
                }
                for (int j = 0; j < curr_vector.height; ++j) {
                    for (int k = 0; k < curr_vector.width; ++k) {
                        v2.data[j][k] = new_avg.data[j][k] + 1;
                    }
                }
                Group g1 = new Group(v1), g2 = new Group(v2);
                temp.add(g1);
                temp.add(g2);
            }

            // calculating nearest VectorQuantization.vector for every average
            __getNearestVector(temp);
            averages = temp;
        }

        // nearest vectors til the averages don't change
        int finish = 0;
        while (true) {
            for (Group curr : averages) {
                ArrayList<vector> nei = curr.neighbours;
                vector avg = new vector(curr.v.height, curr.v.width);
                for (int n = 0; n < avg.height; ++n) {
                    for (int m = 0; m < avg.width; ++m) {
                        Double d = 0.0;
                        for (vector aNei : nei) {
                            d += aNei.data[n][m];
                        }
                        if (nei.size() > 0) avg.data[n][m] = Math.ceil(1.0 * (d / (1.0 * nei.size())));
                        else avg.data[n][m] = curr.v.data[n][m];
                    }
                }
                if (equal(avg.data, curr.v.data)) {
                    finish++;
                }
                curr.v = avg;
                curr.neighbours.clear();
            }

            __getNearestVector(averages);
            if (finish == codeBookSize) break;
            else finish = 0;
        }
    }

    private void __getNearestVector(ArrayList<Group> groups) {
        for (vector curr : blocks) {
            int idx = -1;                               // index of average
            Double d = 10000000000000.0;                // to get min distance average
            for (int j = 0; j < groups.size(); ++j)     // loop through averages
            {
                Group g = groups.get(j);
                vector curr_avg = g.v;
                Double distance = 0.0;

                for (int k = 0; k < curr_avg.height; ++k) {
                    for (int l = 0; l < curr_avg.width; ++l) {
                        distance += Math.abs(curr.data[k][l] - curr_avg.data[k][l]);
                    }
                }
                if (distance < d) {
                    d = distance;
                    idx = j;
                }
            }
            groups.get(idx).add(curr);
        }
    }

    private void getCodes() {
        for (vector block : blocks) {
            boolean exit = false;
            for (int j = 0; j < averages.size(); ++j) {
                Group g = averages.get(j);
                for (int k = 0; k < g.neighbours.size(); ++k) {
                    if (equal(block.data, g.neighbours.get(k).data)) {
                        codes.add(j);
                        exit = true;
                        break;
                    }
                }
                if (exit) break;
            }
        }
    }

    private void writeOnFile() throws IOException {
        FileWriter f = new FileWriter("codeBook.txt");
        f.write(h + " " + w + " " + height + " " + width + " ");
        f.write("\n");
        for (Integer i : codes) f.write(i + " ");
        f.write("\n");
        for (Group average : averages) {
            vector v = average.v;
            for (int j = 0; j < v.height; ++j) {
                for (int k = 0; k < v.width; ++k) {
                    f.write(v.data[j][k] + " ");
                }
                f.write("\n");
            }
        }
        f.close();
    }

    private Boolean equal(double[][] a, double[][] b) {
        for (int i = 0; i < a.length; ++i) {
            for (int j = 0; j < a[0].length; ++j)
                if (a[i][j] != b[i][j]) return false;
        }
        return true;
    }
}
