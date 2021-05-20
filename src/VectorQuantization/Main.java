package VectorQuantization;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String img_path = "in.jpg";
        Compress compress = new Compress(img_path);
        compress.encode();
        Decompress d = new Decompress("codeBook.txt");
        d.decode();
        d.makeImage();
    }
}
