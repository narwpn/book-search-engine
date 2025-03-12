package webir.booksearchengine.util;


public class CliProgressBar {
    private final String name;
    private final int totalWork;
    private int currentWork;
    private float currentRatio;

    public CliProgressBar(String name, int totalWork) {
        this.name = name;
        this.totalWork = totalWork;
        this.currentWork = 0;
        this.currentRatio = 0.0F;
    }

    public void incrementWorkDone(int workUnit) {
        currentWork += workUnit;
        currentRatio = currentWork / (float) totalWork;
        drawProgressBar();
    }

    private void drawProgressBar() {
        int barLength = 50;
        int completedBarLength = (int) (currentRatio * barLength);

        String bar = "=".repeat(completedBarLength) + " ".repeat(barLength - completedBarLength);
        String line = String.format("\r%s [%s] Work completed: %d / %d chunks ", name, bar, currentWork, totalWork);
        line += String.format("(%.2f%%)", currentRatio * 100);

        System.out.print(line);
        System.out.flush();
    }

}
