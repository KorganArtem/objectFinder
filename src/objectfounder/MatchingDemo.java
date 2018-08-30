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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import ru.akor.barcodeDetecter.BarcodeDetector;


/**
 *
 * @author korgan
 */
public class MatchingDemo {
    boolean moved = false;
    String dstPath = null;
    String dstRoot = "C:\\smartdoc\\SORTED\\";
    public void work() throws IOException{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        String rootDirectiryPath = "C:\\smartdoc\\TRASH";
        for(File lowEntry : getListInFolder(new File(rootDirectiryPath))){
            //System.out.println(lowEntry.getAbsolutePath());
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
        Double width = imageOrig.cols() / 1.6;
        Rect rectCrop = new Rect(0, 0, width.intValue(), imageOrig.rows()/4);
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
        Imgproc.blur(gradient, gradient, new Size(2, 5));
        Imgproc.threshold(gradient, gradient, 225, 255, Imgproc.THRESH_BINARY);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(21, 7));
        Imgproc.morphologyEx(gradient, gradient, Imgproc.MORPH_CLOSE, kernel);
        Point anchor = new Point();
        Imgproc.erode(gradient, gradient, kernel, anchor, 5);
        Imgproc.dilate(gradient, gradient, kernel);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(gradient, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        if(contours.size()<1){
            if(moved==true){
                File flSrs = new File(srcPath+"\\"+fileName);
                File flDst = new File(this.dstPath+"\\"+fileName);
                Files.copy(flSrs.toPath(), flDst.toPath());
                Files.delete(flSrs.toPath());
            }
            return;
        }
        
        int bgestContour = getIdBigestCountour(contours);
        Double sizeArea = Imgproc.contourArea(contours.get(bgestContour));
        if( sizeArea < 11000 || sizeArea > 84606){
            if(moved==true){
                File flSrs = new File(srcPath+"\\"+fileName);
                File flDst = new File(this.dstPath+"\\"+fileName);
                Files.copy(flSrs.toPath(), flDst.toPath());
                Files.delete(flSrs.toPath());
            }
            return;
        }
        
        Imgcodecs.imwrite("C:\\smartdoc\\FIRST"+"\\"+fileName, gradient);
        Point center = new Point();
        float[] radius = new float[1];
        Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(bgestContour).toArray()), center, radius);
       
        int point = (imageOrig.width()/21 + imageOrig.height()/29)/2;
        Point leftCorner = new Point((center.x-point*2.6), (center.y-point*1.4) );
        Point rightCorner = new Point((center.x+point*3.7), (center.y+point*1.7) );
        System.out.println(fileName + " \t " + sizeArea.intValue()+"  Center: "+center+ " Point size: "+point+"  LeftCorner: "+leftCorner+"  RightCorner: "+rightCorner);
        Imgproc.rectangle(imageOrig, leftCorner, rightCorner, new Scalar(255,0,0), 3);
        
        Rect roiRect = getRectBarCode(leftCorner, rightCorner);
        Imgcodecs.imwrite(dstPath+"\\"+fileName, imageOrig.submat(roiRect));
        BarcodeDetector bcd = new BarcodeDetector();
        String barCode = bcd.readCode(imageOrig.submat(roiRect));
        if(barCode == null){
            if(moved==true){
                File flSrs = new File(srcPath+"\\"+fileName);
                File flDst = new File(this.dstPath+"\\"+fileName);
                System.out.println(flSrs.getAbsolutePath() + " --> " + flDst.getAbsolutePath());
                Files.copy(flSrs.toPath(), flDst.toPath());
                Files.delete(flSrs.toPath());
            }
            return;
        }
        if(createFolder(barCode)){
            this.dstPath=dstRoot+barCode;
            File flSrs = new File(srcPath+"\\"+fileName);
            File flDst = new File(this.dstPath+"\\"+fileName);
            Files.copy(flSrs.toPath(), flDst.toPath());
            Files.delete(flSrs.toPath());
            moved = true;
        }
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
        return id;
    }
    public Rect getRectBarCode(Point leftCorner, Point rightCorner){
        if(leftCorner.y<0)
            leftCorner.y=0;
        if(leftCorner.x<0)
            leftCorner.x=0;
        if(rightCorner.y<0)
            rightCorner.y=0;
        if(rightCorner.x<0)
            rightCorner.x=0;
        Rect roiRect = new Rect(leftCorner, rightCorner);
        return roiRect;
    }
    private boolean  createFolder(String folderName){
        File folder = new File(dstRoot+folderName);
        return folder.mkdir();
    }
}
