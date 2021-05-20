package VectorQuantization;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class ConstructVectors {

    ArrayList<vector> blocks = new ArrayList<>();  // to hold blocks
    int hei, wi;
    private int row, col;                                  // VectorQuantization.vector size
    private File file;                                     // image to be loaded

    ConstructVectors(int n, int m, String path) {
        row = n;
        col = m;
        file = new File(path);
        LoadImage();
    }

    private void LoadImage() {
        try {
            BufferedImage img = ImageIO.read(file);
            int width = img.getWidth();                   //width of the image
            int height = img.getHeight();                 //height of the image
            int[][] intImg = new int[height][width];
            hei = height;
            wi = width;
            System.out.println("Reading complete.");

            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); ++j) {

                    int p = img.getRGB(j, i);
                    int blue = (p & 0xff);
                    intImg[i][j] = blue;
                }
            }
            if (img.getWidth() % col == 0 && img.getHeight() % row == 0) {
                for (int i = 0; i < img.getHeight(); i += row) {
                    for (int j = 0; j < img.getWidth(); j += col) {
                        vector v = new vector(row, col);
                        int r = i, c = j;
                        for (int k = 0; k < row; ++k) {
                            for (int l = 0; l < col; ++l) {
                                v.data[k][l] = intImg[r][c++];
                            }
                            c = j;
                            r++;
                        }
                        blocks.add(v);
                    }
                }
            } else {
                System.out.print("Invalid VectorQuantization.vector size");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
}