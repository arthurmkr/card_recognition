package ru.mab.test.cardrecognition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CardRecognition {
    private static final String IMAGE_EXTENSION = ".png";
    private static final int WHITE_COLOR = 0xFFFFFFFF;
    private static final double MATCH_THRESHOLD = 0.6;
    private static final int BINARIZATION_THRESHOLD = 106;
    private static final ClassLoader classLoader = CardRecognition.class.getClassLoader();

    /**
     * args[0] = &lt;путь до папки с изображениями&gt;<br/>
     * args[1] = -s (не обязательный, включает вывод статистики выполнения)
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Image folder path is not specified");
            return;
        }

        Statistics statistics = new Statistics(args.length > 1 && args[1].equals("-s"));
        BufferedImage cardListMask = ImageIO.read(classLoader.getResourceAsStream("masks/cards.png"));
        Map<String, BufferedImage> suitMasks = loadMasks("suit", "s", "h", "c", "d");
        Map<String, BufferedImage> seniorityMasks = loadMasks("sen", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");
        File dir = new File(args[0]);

        for (File imageFile : dir.listFiles()) {
            statistics.startImageProcessing();
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
            statistics.stopImageProcessing(imageFile.getName().replace(IMAGE_EXTENSION, ""), result);
        }

        statistics.print();
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
            masks.put(suffix, ImageIO.read(classLoader.getResourceAsStream("masks/" + prefix + "_" + suffix + IMAGE_EXTENSION)));
        }
        return masks;
    }

    private static int binarize(int rgb) {
        Color color = new Color(rgb);
        return color.getRed() < BINARIZATION_THRESHOLD || color.getGreen() < BINARIZATION_THRESHOLD || color.getBlue() < BINARIZATION_THRESHOLD ? 0 : 1;
    }
}
