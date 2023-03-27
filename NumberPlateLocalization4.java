package project;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class NumberPlateLocalization4 {
  public static void main(String[] args) {
    try {
      // Load the image
      BufferedImage a = ImageIO.read(new File("C:\\Users\\TNJ\\Pictures\\FORMAL.jpg"));

      // Preprocessing
      // 1. Convert the image to grayscale
      BufferedImage grayScaleImage = avggrayscale(a);
      ImageIO.write(grayScaleImage, "jpg", new File("C:\\Users\\TNJ\\Pictures\\grayscale.jpg"));
      // 2. Apply histogram equalization
      BufferedImage histogramEqualization = histogramEqualization(grayScaleImage);
      
      BufferedImage c = cropUpperThird(histogramEqualization);
      ImageIO.write(c, "jpg", new File("C:\\Users\\TNJ\\Pictures\\half.jpg"));
      BufferedImage d = cropBottomTwoThirds(c);
      ImageIO.write(d, "jpg", new File("C:\\Users\\TNJ\\Pictures\\quarter.jpg"));
      //3. Edge Detection and Localization
      BufferedImage Localization = EdgeAndLocalization(d);
      ImageIO.write(Localization, "jpg", new File("C:\\Users\\TNJ\\Pictures\\detected_number_plate_001.jpg"));
      
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  
	public static BufferedImage convertToBinary(BufferedImage image) {
		BufferedImage binary_img = new BufferedImage(
			image.getWidth(),
			image.getHeight(),
			BufferedImage.TYPE_BYTE_BINARY);

		Graphics2D g = binary_img.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
	return binary_img;
}
  
	//luminosity
	 public static BufferedImage toGrayscale(BufferedImage image) {
	        BufferedImage grayscale = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

	        for (int i = 0; i < image.getWidth(); i++) {
	            for (int j = 0; j < image.getHeight(); j++) {
	                int color = image.getRGB(i, j);
	                int red = (color >> 16) & 0xff;
	                int green = (color >> 8) & 0xff;
	                int blue = color & 0xff;

	                int gray = (int)(0.21 * red + 0.72 * green + 0.07 * blue);

	                color = (gray << 16) | (gray << 8) | gray;
	                grayscale.setRGB(i, j, color);
	            }
	        }

	        return grayscale;
	    }
	

	    public static BufferedImage medianfilter(BufferedImage image) {
	        int width = image.getWidth();
	        int height = image.getHeight();
	        BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	        for (int y = 1; y < height - 1; y++) {
	            for (int x = 1; x < width - 1; x++) {
	                int[] neighborhood = new int[9];
	                int index = 0;
	                for (int k = -1; k <= 1; k++) {
	                    for (int j = -1; j <= 1; j++) {
	                        int pixel = image.getRGB(x + j, y + k);
	                        int r = (pixel >> 16) & 0xff;
	                        neighborhood[index] = r;
	                        index++;
	                    }
	                }
	                Arrays.sort(neighborhood);
	                int median = neighborhood[4];
	                int newRGB = (median << 16) | (median << 8) | median;
	                filteredImage.setRGB(x, y, newRGB);
	            }
	        }
	        return filteredImage;
	    }
	
	    public static BufferedImage avggrayscale(BufferedImage image) {
	        int width = image.getWidth();
	        int height = image.getHeight();
	        BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	                int pixel = image.getRGB(x, y);
	                int r = (pixel >> 16) & 0xff;
	                int g = (pixel >> 8) & 0xff;
	                int b = pixel & 0xff;
	                int average = (r + g + b) / 3;
	                int newPixel = (average << 16) | (average << 8) | average;
	                filteredImage.setRGB(x, y, newPixel);
	            }
	        }
	        return filteredImage;
	    }
	    
  public static BufferedImage convertToGrayscale(BufferedImage img) {
	    int width = img.getWidth();
        int height = img.getHeight();

        
        
       /// Convert image to grayscale average
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = img.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                int avg = (r + g + b) / 3;
                p = (a << 24) | (avg << 16) | (avg << 8) | avg;
                img.setRGB(x, y, p);
            }
        }

        // Apply Gaussian blur
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sum = 0;
                int count = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (x + i >= 0 && x + i < width && y + j >= 0 && y + j < height) {
                            int p = img.getRGB(x + i, y + j);
                            int a = (p >> 24) & 0xff;
                            int r = (p >> 16) & 0xff;
                            int g = (p >> 8) & 0xff;
                            int b = p & 0xff;

                            sum += (r + g + b) / 3;
                            count++;
                        }
                    }
                }
                int avg = sum / count;
                int p = (255 << 24) | (avg << 16) | (avg << 8) | avg;
                img.setRGB(x, y, p);
            }
            
        }
        

      return img;
  }
  
  public static BufferedImage histogramEqualization(BufferedImage image) {
	  int width = image.getWidth();
      int height = image.getHeight();
      
      // Get the image data in the form of an array of pixels
      int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
      
      // Create a histogram of the image
      int[] histogram = new int[256];
      for (int i = 0; i < pixels.length; i++) {
          int color = pixels[i] & 0xff;
          histogram[color]++;
      }
      
      // Create a cumulative histogram
      int[] cumulativeHistogram = new int[256];
      cumulativeHistogram[0] = histogram[0];
      for (int i = 1; i < 256; i++) {
          cumulativeHistogram[i] = cumulativeHistogram[i - 1] + histogram[i];
      }
      
      // Normalize the cumulative histogram
      float[] normalizedCumulativeHistogram = new float[256];
      for (int i = 0; i < 256; i++) {
          normalizedCumulativeHistogram[i] = (float) cumulativeHistogram[i] / (width * height);
      }
      
      // Create a new image with the equalized histogram
      BufferedImage histEqImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      for (int i = 0; i < pixels.length; i++) {
          int color = pixels[i] & 0xff;
          int newColor = (int) (normalizedCumulativeHistogram[color] * 255);
          pixels[i] = (pixels[i] & 0xff000000) | (newColor << 16) | (newColor << 8) | newColor;
      }
      histEqImage.setRGB(0, 0, width, height, pixels, 0, width);
      
      return histEqImage;
  }
 
  //down2/3
  public static BufferedImage cropUpperThird(BufferedImage originalImage) {
	// Calculate the height of the crop area
      int cropHeight = (int) (originalImage.getHeight() * (2.0/3.0));

      // Create a new BufferedImage with the cropped dimensions
      BufferedImage croppedImage = new BufferedImage(originalImage.getWidth(), cropHeight, originalImage.getType());

      // Copy the lower 2/3 of the original image to the cropped image
      for (int x = 0; x < originalImage.getWidth(); x++) {
          for (int y = 0; y < cropHeight; y++) {
              croppedImage.setRGB(x, y, originalImage.getRGB(x, y + (originalImage.getHeight() - cropHeight)));
          }
      }

      return croppedImage;
  }
  
  //width
  public static BufferedImage crop(BufferedImage originalImage) {
      int originalWidth = originalImage.getWidth();
      int originalHeight = originalImage.getHeight();
      int newWidth = (int) (originalWidth * (2.0/3.0));
      int newHeight = originalHeight;
      BufferedImage croppedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
      for (int x = 0; x < newWidth; x++) {
          for (int y = 0; y < newHeight; y++) {
              croppedImage.setRGB(x, y, originalImage.getRGB(x, y));
          }
      }
      return croppedImage;
  }
  

//downcut
  public static BufferedImage cropBottomTwoThirds(BufferedImage input) {
    int height = input.getHeight();
    int newHeight = (4 * height) / 5;
    return input.getSubimage(0, 0, input.getWidth(), newHeight);
  }
  
  
  //half 
  public static BufferedImage cropBottomHalf(BufferedImage originalImage) {
      int height = originalImage.getHeight();
      int width = originalImage.getWidth();

      BufferedImage bottomHalf = new BufferedImage(width, height/2, originalImage.getType());

      for (int y = height/2; y < height; y++) {
          for (int x = 0; x < width; x++) {
              bottomHalf.setRGB(x, y - (height/2), originalImage.getRGB(x, y));
          }
      }

      return bottomHalf;
  }
  
  
  public static BufferedImage EdgeAndLocalization(BufferedImage image) {
	  //histogram
      int[] histogram = new int[256];
      for (int x = 0; x < image.getWidth(); x++) {
          for (int y = 0; y < image.getHeight(); y++) {
              int color = image.getRGB(x, y);
              int red = (color >> 16) & 0xff;
              histogram[red]++;
          }
      }

      // Find the peak value in the histogram
      int peak = 0;
      for (int i = 0; i < histogram.length; i++) {
          if (histogram[i] > peak) {
              peak = histogram[i];
          }
      }

      // Find the edges of the peak
      int[] edges = new int[2];
      boolean foundLeftEdge = false;
      for (int i = 0; i < histogram.length; i++) {
          if (!foundLeftEdge && histogram[i] == peak) {
              edges[0] = i;
              foundLeftEdge = true;
          } else if (foundLeftEdge && histogram[i] != peak) {
              edges[1] = i - 1;
              break;
          }
      }
      
      int width = image.getWidth();
      int height = image.getHeight();

      
      int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

      
      // Sobel Edge Detection
       edges = new int[pixels.length];
      for (int y = 1; y < height - 1; y++) {
          for (int x = 1; x < width - 1; x++) {
              int p1 = pixels[(y - 1) * width + x - 1] & 0xff;
              int p2 = pixels[(y - 1) * width + x] & 0xff;
              int p3 = pixels[(y - 1) * width + x + 1] & 0xff;
              int p4 = pixels[y * width + x - 1] & 0xff;
              int p5 = pixels[y * width + x] & 0xff;
              int p6 = pixels[y * width + x + 1] & 0xff;
              int p7 = pixels[(y + 1) * width + x - 1] & 0xff;
              int p8 = pixels[(y + 1) * width + x] & 0xff;
              int p9 = pixels[(y + 1) * width + x + 1] & 0xff;
              int gx = (p1 + 2 * p4 + p7) - (p3 + 2 * p6 + p9);
              int gy = (p1 + 2 * p2 + p3) - (p7 + 2 * p8 + p9);
              edges[y * width + x] = (int) Math.sqrt(gx * gx + gy * gy);
          }
      }
 
      // Horizontal Scanning
      int[] sumsh = new int[height];
      for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
              sumsh[y] += edges[y * width + x];
          }
      }

      
      // Find the y coordinate of the upper and lower bounds of the license plate
      //UP
      int upperBoundH = 0;
    
      for (int y = 0; y < height; y++) {
          if (sumsh[y] > sumsh[upperBoundH]) {
              upperBoundH = y;
          }
       
      }
      //DOWN
      int lowerBoundH =  height - 1;
  
      for (int y = 0; y < height; y++) {
          if (sumsh[y] > sumsh[lowerBoundH]) {
        	  lowerBoundH = y;
          }
        
      }
      
      
      // vertical Scanning
      int[] sumsv = new int[width];
      for (int y = 0; y < width; y++) {
          for (int x = 0; x < height; x++) {
              sumsv[y] += edges[y * height + x];
          }
      }

      
      // Find the y coordinate of the upper and lower bounds of the license plate
      //LEFT
      int upperBoundV = 0;
      for (int y = 0; y < width; y++) {
          if (sumsv[y] > sumsv[upperBoundV]) {
              upperBoundV = y;
          }
      }
      //RIGHT
      int lowerBoundV = width - 1;
      for (int y = 0; y < width; y++) {
          if (sumsv[y] > sumsv[lowerBoundV]) {
        	  lowerBoundV = y;
          }
 
      }
      
      // Crop the image to the region of the license plate
      BufferedImage cropped = image.getSubimage(upperBoundV, upperBoundH,lowerBoundV , lowerBoundH);
	  return cropped;
  }
	  
  }