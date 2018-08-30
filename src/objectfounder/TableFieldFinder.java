/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objectfounder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import ru.akor.barcodeDetecter.BarcodeDetector;


/**
 *
 * @author korgan
 */
public class TableFieldFinder {
    boolean moved = false;
    String dstPath = null;
    String dstRoot = "C:\\smartdoc\\SORTED\\";
    public void work() throws IOException{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        String rootDirectiryPath = "C:\\smartdoc\\TRASH";
        for(File lowEntry : getListInFolder(new File(rootDirectiryPath))){
            System.out.println(lowEntry.getAbsolutePath());
            barCodeSearcher(rootDirectiryPath, lowEntry.getName(), "C:\\smartdoc\\CROPED");
         }
    }
    private File[] getListInFolder(File folder){
        File[] fileList = folder.listFiles();
        return fileList;
    }
    public void barCodeSearcher(String srcPath, String fileName, String dstPath) throws IOException {
        Mat imageOrig;
        imageOrig = Imgcodecs.imread(srcPath+"\\"+fileName);
        if(imageOrig.empty()){
            System.err.println("Файл не загружен");
            return;
        }
        //Mat image = imageOrig.clone();
        
        Rect rectCrop = new Rect(0, 0, imageOrig.cols(), imageOrig.rows()/3);
        Mat image = new Mat(imageOrig, rectCrop);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        int ddepth  = CvType.CV_32F;
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Sobel(image, gradX, ddepth, 1, 0);
        Imgproc.Sobel(image, gradY, ddepth, 0, 1);
        Mat gradient = new Mat();
        Core.subtract(gradX, gradY, gradient);
        Core.convertScaleAbs(gradient, gradient);
        Imgproc.blur(gradient, gradient, new Size(1, 1));
        Imgproc.threshold(gradient, gradient, 200, 255, Imgproc.THRESH_BINARY);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
        Imgproc.morphologyEx(gradient, gradient, Imgproc.MORPH_CLOSE, kernel);
        Point anchor = new Point();
        Imgproc.erode(gradient, gradient, kernel, anchor, 50);
        Imgproc.dilate(gradient, gradient, kernel);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(gradient, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<MatOfPoint> filteredContours =findCotour(contours);
        System.out.println(contours.size()+ "   " + filteredContours.size());
        Imgproc.drawContours(imageOrig, filteredContours, searchRightBox(filteredContours), new Scalar(255, 0 , 0), 3); //searchRightBox(contours)
        //Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(bgestContour).toArray()), center, radius);
        Imgproc.drawContours(imageOrig, filteredContours, -1, new Scalar(255, 0 , 0), 3);
        Imgcodecs.imwrite(dstRoot+"\\"+fileName, imageOrig);
    }
    public int getIdBigestCountour(List<MatOfPoint> contours){
        double maxSize = 0;
        int id = 0;
        for(int contourId = 0; contourId<contours.size(); contourId++){
            if(maxSize<Imgproc.contourArea(contours.get(contourId))){
                maxSize=Imgproc.contourArea(contours.get(contourId));
                id = contourId;
            }
        }
        System.out.println("Te bigest contour "+id);
        return id;
    }
    public List<MatOfPoint> findCotour(List<MatOfPoint> contours){
        List<MatOfPoint> forRet = new ArrayList<>();
        for(int i =0; i < contours.size(); i++){
            if(Imgproc.contourArea(contours.get(i)) > 100000.00){
                forRet.add(contours.get(i));
            }
        }
        return forRet;
    }
    private int searchRightBox(List<MatOfPoint> contours){
        int id = -1;
        for(int contourId = 0; contourId<contours.size(); contourId++){
            MatOfPoint2f mop = new MatOfPoint2f(contours.get(contourId).toArray());
            RotatedRect rc = Imgproc.minAreaRect(mop);
            double soot = rc.size.width / rc.size.height;
            System.out.println(contourId + "  " + soot);
            if(soot<2.8){
                id = contourId;
            } else {
            }
        }
        System.out.println("Right  "+id);
        return id;
    }
    private int searchMainBox(List<MatOfPoint> contours){
        int id = -1;
        for(int contourId = 0; contourId<contours.size(); contourId++){
            MatOfPoint2f mop = new MatOfPoint2f(contours.get(contourId).toArray());
            RotatedRect rc = Imgproc.minAreaRect(mop);
            double soot = rc.size.width / rc.size.height;
            System.out.println(contourId + "  " + soot);
            if(soot<2.8){
                id = contourId;
            } else {
            }
        }
        System.out.println("Right  "+id);
        return id;
    }
}
