package ru.mab.test.cardrecognition;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CardRecognition {
    private static final int WHITE = 0xFFFFFFFF;

    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        BufferedImage testImage = getImage("imgs_marked/2sQs10h10c.png");
        BufferedImage cardListMask = getImage("masks/cards.png");
        Map<String, BufferedImage> suitMasks = loadMasks("suit", "s", "h", "c", "d");
        Map<String, BufferedImage> seniorityMasks =
                loadMasks("sen", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");

        boolean isCardFound = false;
        StringBuilder result = new StringBuilder();
        for (int y = 0; y < cardListMask.getHeight() && !isCardFound; y++) {
            for (int x = 0; x < cardListMask.getWidth(); x++) {
                if (cardListMask.getRGB(x, y) == WHITE) {
                    result.append(matchByMask(x, y, testImage, seniorityMasks)).append(matchByMask(x, y, testImage, suitMasks));
                    x += seniorityMasks.get("2").getWidth();
                    isCardFound = true;
                }
            }
        }

        System.out.println("\nresult: " + result);
        System.out.println("total time: " + (System.currentTimeMillis() - time));
    }

    private static String matchByMask(int shiftX, int shiftY, BufferedImage cardImage, Map<String, BufferedImage> masks) {
        double maxProbability = 0;
        String maxKey = "";
        for (Map.Entry<String, BufferedImage> entry : masks.entrySet()) {
            BufferedImage mask = entry.getValue();
            int countInMask = 0;
            int countInImage = 0;
            for (int y = 0; y < mask.getHeight(); y++) {
                for (int x = 0; x < mask.getWidth(); x++) {
                    int bitInMask = binarize(mask.getRGB(x, y));
                    int bitInImage = binarize(cardImage.getRGB(x + shiftX, y + shiftY));
                    countInMask++;
                    countInImage += bitInMask ^ bitInImage;
                }
            }

            if ((double) countInImage / countInMask > maxProbability) {
                maxKey = entry.getKey();
                maxProbability = (double) countInImage / countInMask;
            }
        }

        System.out.println("maxKey: " + maxKey + " prob: " + maxProbability);
        return maxProbability > 0.6 ? maxKey : " ";
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
        int edge = 220;
        int shift = 100;

        return (((rgb & 0xFF0000) >> 16) + shift) < edge ||
                (((rgb & 0x00FF00) >> 8) + shift) < edge ||
                ((rgb & 0x0000FF) + shift) < edge ? 0 : 1;
    }
}
