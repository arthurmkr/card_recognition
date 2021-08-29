package ru.mab.test.cardrecognition;

public class Statistics {
    private final long startTime = System.currentTimeMillis();
    private final boolean enabled;
    private long maxTime;
    private long minTime = Long.MAX_VALUE;
    private long timePerImage;
    private int errors;
    private int totalCount;

    public Statistics(boolean enabled) {
        this.enabled = enabled;
    }

    public void startImageProcessing() {
        timePerImage = System.currentTimeMillis();
    }

    public void stopImageProcessing(String expected, String actual) {
        long curTime = System.currentTimeMillis() - timePerImage;

        maxTime = Math.max(curTime, maxTime);
        minTime = Math.min(curTime, minTime);

        timePerImage = 0;

        totalCount++;
        errors += actual.equals(expected) ? 0 : 1;
    }

    public void print() {
        if (enabled) {
            System.out.println("\n\ntotal time (" + totalCount + "images): " + (System.currentTimeMillis() - startTime) + " millis" +
                    "\nMAX time taken to image: " + maxTime + " millis" +
                    "\nMIN time taken to image: " + minTime + " millis" +
                    "\npercentage of errors: " + ((double) errors / totalCount) + "%");
        }
    }
}
