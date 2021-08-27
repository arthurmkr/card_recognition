package ru.mab.test.cardrecognition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class CardRecognition {
    private static final String IMAGE_EXTENSION = ".png";
    private static final int WHITE_COLOR = 0xFFFFFFFF;
    private static final double MATCH_THRESHOLD = 0.6;
    private static final int BINARIZATION_THRESHOLD = 120;

    public static void main(String[] args) throws IOException {
        long totalTime = System.currentTimeMillis();

        BufferedImage cardListMask = getImage("masks/cards.png");
        Map<String, BufferedImage> suitMasks = loadMasks("suit", "s", "h", "c", "d");
        Map<String, BufferedImage> seniorityMasks = loadMasks("sen", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");

        int errors = 0;
        TreeSet<Long> timesPerImage = new TreeSet<>();
        File directory = new File("D:\\dev\\projects\\testCodestyle\\src\\main\\resources\\imgs_marked");
        File[] files = directory.listFiles();

        for (File imageFile : files) {
            long timePerImage = System.currentTimeMillis();
            BufferedImage testImage = ImageIO.read(imageFile);
            String result = "";
            for (int y = 0; y < cardListMask.getHeight() && result.length() == 0; y++) {
                for (int x = 0; x < cardListMask.getWidth(); x++) {
                    if (cardListMask.getRGB(x, y) == WHITE_COLOR) {
                        result += matchByMask(x, y, testImage, seniorityMasks) + matchByMask(x, y, testImage, suitMasks);
                        x += suitMasks.get("s").getWidth();
                    }
                }
            }

            System.out.println(imageFile.getName() + " - " + result);

            timesPerImage.add(System.currentTimeMillis() - timePerImage);
            errors += imageFile.getName().replace(IMAGE_EXTENSION, "").equals(result) ? 0 : 1;
        }

        System.out.println("\n\ntotal time: " + (System.currentTimeMillis() - totalTime) + " millis" +
                "\nMAX time taken to image:  " + timesPerImage.last() + " millis" +
                "\nMIN time taken to image: " + timesPerImage.first() + " millis" +
                "\npercentage of errors: " + ((double) errors / files.length) + "%");
    }

    private static String matchByMask(int shiftX, int shiftY, BufferedImage cardImage, Map<String, BufferedImage> masks) {
        TreeMap<Double, String> probabilities = new TreeMap<>();
        for (Map.Entry<String, BufferedImage> entry : masks.entrySet()) {
            BufferedImage mask = entry.getValue();
            int countInImage = 0;
            for (int y = 0; y < mask.getHeight(); y++) {
                for (int x = 0; x < mask.getWidth(); x++) {
                    countInImage += binarize(mask.getRGB(x, y)) ^ binarize(cardImage.getRGB(x + shiftX, y + shiftY));
                }
            }

            probabilities.put((double) countInImage / (mask.getHeight() * mask.getWidth()), entry.getKey());
        }

        return probabilities.lastKey() > MATCH_THRESHOLD ? probabilities.lastEntry().getValue() : "";
    }

    private static Map<String, BufferedImage> loadMasks(String prefix, String... suffixes) throws IOException {
        Map<String, BufferedImage> masks = new HashMap<>();
        for (String suffix : suffixes) {
            masks.put(suffix, getImage("masks/" + prefix + "_" + suffix + ".png"));
        }
        return masks;
    }

    private static BufferedImage getImage(String s) throws IOException {
        return ImageIO.read(CardRecognition.class.getClassLoader().getResourceAsStream(s));
    }

    private static int binarize(int rgb) {
        Color color = new Color(rgb);
        return color.getRed() < BINARIZATION_THRESHOLD || color.getGreen() < BINARIZATION_THRESHOLD || color.getBlue() < BINARIZATION_THRESHOLD ? 0 : 1;
    }
}
