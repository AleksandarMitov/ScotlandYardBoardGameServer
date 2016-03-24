package swing.algorithms;

import scotlandyard.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

/**
 * A class to convert an image to it's ASCII representation in the form of a string.
 */

public class ImgToASCII {

    private BufferedImage originalImage;
    private BufferedImage greyscaleImage;
    private Dimension panelSize;
    private final Dimension charSize = new Dimension(7, 13);
    private int x, y;
    private String output;

    /**
     * Constructs a new ImgToASCII object.
     *
     * @param image the image to be converted.
     * @param panelSize the size in pixels of the output image (string).
     */
    public ImgToASCII(BufferedImage image, Dimension panelSize) {
        this.panelSize = panelSize;
        output = new String();
        originalImage = image;
        greyscaleImage = new BufferedImage(originalImage.getWidth(),
                                            originalImage.getHeight(),
                                            BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = greyscaleImage.createGraphics();
        g.drawImage(originalImage, 0, 0, null);
        RescaleOp op = new RescaleOp(1.0f, 10.0f, null);
        greyscaleImage = op.filter(greyscaleImage, greyscaleImage);
        calcDimensions();
    }

    /**
     * Returns the ASCII representation of the image as a String.
     *
     * @return the ASCII representation of the image.
     */
    @Override
    public String toString() {
        return output;
    }

    /**
     * Returns the ASCII representation of the image as a BufferedImage.
     *
     * @return the ASCII representation of the image.
     */
    public BufferedImage toImage() {
        BufferedImage img = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        int y = 0;
        graphics.setFont(new Font("monospaced", Font.PLAIN, 12));
        graphics.setColor(Color.WHITE);
        for (String line : output.split("\n")) {
            graphics.drawString(line, 1, (y += 13));
        }
        return img;
    }

    /**
     * Coverts the image to ASCII. It is a separate method as
     * it can take a long time to convert large images.
     */
    public void convert() {
        for (int y = 0; y < greyscaleImage.getHeight(); y += this.y) {
            for (int x = 0; x < greyscaleImage.getWidth(); x += this.x) {
                int averageColour = getAverage(x, y);
                char asciiChar = getChar(averageColour);
                output += Character.toString(asciiChar);
            }
            output += "\n";
        }
    }

    // Calculates the size of the blocks in the image.
    private void calcDimensions() {
        x = (int) Math.floor((greyscaleImage.getWidth() * charSize.width) / panelSize.width);
        y = (int) Math.floor((greyscaleImage.getHeight() * charSize.height) / panelSize.height);
    }

    // Calculates the grey scale value of a single block.
    private int getAverage(int x, int y) {
        int runningTotal = 0;
        int count = 0;
        int yMax = y + this.y, xMax = x + this.x;
        for (; y < yMax && y < greyscaleImage.getHeight(); y++) {
            for (; x < xMax && x < greyscaleImage.getWidth(); x++) {
                int colour = greyscaleImage.getRGB(x, y) & 0xFF;
                runningTotal += colour;
                count++;
            }
        }
        return (int) runningTotal / count;
    }

    // Returns the correct character for a block with a
    // certain grey scale value.
    // @param averageColour the grey scale value of the block.
    // @return the ASCII character for the block.
    private char getChar(int averageColour) {
        if (0 <= averageColour && averageColour < 17) {
            return 'M';
        } else if (16 < averageColour && averageColour < 33) {
            return '#';
        } else if (32 < averageColour && averageColour < 49) {
            return 'A';
        } else if (48 < averageColour && averageColour < 65) {
            return '@';
        } else if (64 < averageColour && averageColour < 81) {
            return '$';
        } else if (80 < averageColour && averageColour < 97) {
            return '0';
        } else if (96 < averageColour && averageColour < 113) {
            return 'e';
        } else if (112 < averageColour && averageColour < 129) {
            return 'a';
        } else if (128 < averageColour && averageColour < 145) {
            return 'o';
        } else if (144 < averageColour && averageColour < 161) {
            return '=';
        } else if (160 < averageColour && averageColour < 177) {
            return '+';
        } else if (176 < averageColour && averageColour < 193) {
            return ';';
        } else if (192 < averageColour && averageColour < 209) {
            return ':';
        } else if (208 < averageColour && averageColour < 225) {
            return ',';
        } else if (224 < averageColour && averageColour < 241) {
            return '.';
        } else {
            return ' ';
        }
    }

}
