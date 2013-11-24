import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Peter Abeles
 */
public class CreatePatternsFromSURF
{
   public static boolean paused = true;

   public static BufferedImage createPattern( SurfFeature feature ) {
      int square = 50;
      int imageWidth = square*8;

      BufferedImage image = new BufferedImage(imageWidth,imageWidth,BufferedImage.TYPE_3BYTE_BGR);

      double l[] = feature.value.clone();
      for( int i = 0; i < l.length; i++ ) {
         l[i] = Math.abs(l[i]);
      }

      Arrays.sort(l);

      double threshold = l[ l.length/2 ];

      Graphics2D g2 = image.createGraphics();
      g2.setColor(Color.WHITE);
      g2.fillRect(0,0,image.getWidth(), image.getHeight());
      int total = 0;
      for( int y = 0; y < 8; y++ ) {
         for( int x = 0; x < 8; x++ ) {
            int x0 = x*square;
            int y0 = y*square;

            double v = Math.abs(feature.value[y*8+x]);
            if( v <= threshold ) {
               total++;
               g2.setColor(Color.BLACK);
               g2.fillRect(x0,y0,square,square);
            }
         }
      }
      System.out.println("total squares "+total);

      return image;
   }

   public static class MyListener implements MouseListener
   {

      @Override
      public void mouseClicked(MouseEvent e){}

      @Override
      public void mousePressed(MouseEvent e)
      {
         System.out.println("prcessed");
         paused = !paused;
      }

      @Override
      public void mouseReleased(MouseEvent e){}

      @Override
      public void mouseEntered(MouseEvent e){}

      @Override
      public void mouseExited(MouseEvent e){}
   }

   public static void main( String args[] ) {
      BufferedImage image = UtilImageIO.loadImage("data/18_2013installathens10.jpg");

      ImageFloat32 gray = ConvertBufferedImage.convertFrom(image,(ImageFloat32)null);

      ConfigFastHessian configFH = new ConfigFastHessian(0,2,10,1,9,4,4);
      DetectDescribePoint<ImageFloat32,SurfFeature> dd = FactoryDetectDescribe.surfStable(configFH,null,null, ImageType.single(ImageFloat32.class));

      dd.detect(gray);

      ImagePanel gui = null;

      for( int i = 0; i < dd.getNumberOfFeatures(); i++ ) {
         System.out.println("Feature "+i);

         BufferedImage out = createPattern(dd.getDescription(i));

         if( gui == null ) {
            gui = ShowImages.showWindow(out,"Pattern");
            gui.addMouseListener(new MyListener());
            gui.grabFocus();
         } else
            gui.setBufferedImage(out);

         gui.repaint();

         UtilImageIO.saveImage(out,String.format("image%03d.png",i));

         while( paused ) {
            synchronized (Thread.currentThread()) {
            try {Thread.sleep(10); } catch (InterruptedException ignore) {}
            }
         }
         paused = true;
      }
   }
}
