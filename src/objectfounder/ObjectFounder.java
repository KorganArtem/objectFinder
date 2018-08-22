/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objectfounder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
/**
 *
 * @author korgan
 */
public class ObjectFounder {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        String rootDirectiryPath = "C:\\IWTRT\\learn\\good";
        for(File lowEntry : getListInFolder(new File(rootDirectiryPath))){
            System.out.println(lowEntry.getAbsolutePath());
            //moveFile(lowEntry, folderIndex+"_"+lowEntry.getName());
            barCodeSearcher(rootDirectiryPath, lowEntry.getName(), "C:\\IWTRT\\learn\\croped");
         }
        
        //new MatchingDemo().run(image, tmp, "/IWTRT/learn/res.jpg", 0);
    }
    private static File[] getListInFolder(File folder){
        File[] fileList = folder.listFiles();
        return fileList;
    }
    public static void barCodeSearcher(String srcPath, String fileName, String dstPath){
        Mat image;
        image = Imgcodecs.imread(srcPath+"\\"+fileName);
        //Mat tmp = Imgcodecs.imread("C:\\IWTRT\\learn\\templ.jpg");
        if(image.empty()){
            System.err.println("Файл не загружен");
            return;
        }
        Double width = image.cols() / 1.8;
        Rect rectCrop = new Rect(0, 0, width.intValue(), image.rows()/4);
        image = new Mat(image, rectCrop);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        int ddepth  = CvType.CV_32F;
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Sobel(image, gradX, ddepth, 1, 0);
        Imgproc.Sobel(image, gradY, ddepth, 0, 1);
        Mat gradient = new Mat();
        Core.subtract(gradX, gradY, gradient);
        Core.convertScaleAbs(gradient, gradient);
        Imgproc.blur(gradient, gradient, new Size(2, 5));
        Imgproc.threshold(gradient, gradient, 225, 255, Imgproc.THRESH_BINARY);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(21, 7));
        Imgproc.morphologyEx(gradient, gradient, Imgproc.MORPH_CLOSE, kernel);
        Point anchor = new Point();
        Imgproc.erode(gradient, gradient, kernel, anchor, 4);
        Imgproc.dilate(gradient, gradient, kernel);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(gradient, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println(contours.size());
        Imgproc.drawContours(gradient, contours, -1, new Scalar(255,0,0));
        //Imgproc.boxPoints(Imgproc.minAreaRect(points), kernel);
        Imgcodecs.imwrite(dstPath+"\\"+fileName, gradient);
    }
}