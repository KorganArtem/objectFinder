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
public class TableFieldFinderIter {
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
        MatOfPoint rightContour =  getRightContour(imageOrig, fileName);
        //Imgproc.drawContours(imageOrig, filteredContours, -1, new Scalar(255, 0 , 0), 3);
        
        
    }
    private MatOfPoint getRightContour(Mat imageOrig, String fileName){
        Double heigh = imageOrig.rows()/5.5;
        Double xStart = imageOrig.cols()/2.0;
        System.out.println("ImgWidth  " + imageOrig.cols() + " ImgHeigh " + imageOrig.rows()
                + " RectStart " + xStart + " imgWidth " + (heigh.intValue()));
        Rect rectCrop = new Rect(xStart.intValue(), 0, imageOrig.cols()-xStart.intValue(), heigh.intValue());
        System.out.println(rectCrop);
        Mat image = new Mat(imageOrig, rectCrop);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        int ddepth  = CvType.CV_32F;
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Sobel(image, gradX, ddepth, 1, 0);
        Imgproc.Sobel(image, gradY, ddepth, 0, 1);
        Mat gradientOrig = new Mat();
        Core.subtract(gradX, gradY, gradientOrig);
        Core.convertScaleAbs(gradientOrig, gradientOrig);
        Imgproc.blur(gradientOrig, gradientOrig, new Size(1, 1));
        int idRightBox = -1;
        int unknowParametr = 250;
        List<MatOfPoint> filteredContours = new ArrayList<>();
        while(idRightBox == -1 && unknowParametr > 0){
            Mat gradient = new Mat();
            unknowParametr = unknowParametr-10;
            Imgproc.threshold(gradientOrig, gradient, unknowParametr, 250, Imgproc.THRESH_BINARY);
            System.out.println("Now "+unknowParametr);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
            Imgproc.morphologyEx(gradient, gradient, Imgproc.MORPH_CLOSE, kernel);
            Point anchor = new Point();
            Imgproc.erode(gradient, gradient, kernel, anchor, 300);
            Imgproc.dilate(gradient, gradient, kernel);
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(gradient, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            filteredContours = findCotour(contours);
            idRightBox = searchRightBox(filteredContours); 
        }
        writeBox(gradientOrig, filteredContours.get(idRightBox), dstRoot+"\\rrr_"+fileName);
        Imgproc.drawContours(gradientOrig, filteredContours, idRightBox, new Scalar(255, 0 , 0), 30);
        Imgcodecs.imwrite(dstRoot+"\\"+fileName, gradientOrig);
        return null;
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
        //System.out.println("Te bigest contour "+id);
        return id;
    }
    public List<MatOfPoint> findCotour(List<MatOfPoint> contours){
        List<MatOfPoint> forRet = new ArrayList<>();
        for(int i =0; i < contours.size(); i++){
            if(Imgproc.contourArea(contours.get(i)) > 90000.00){
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
            if(soot<3.2){
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
    private void writeBox(Mat startImag, MatOfPoint contour, String fileName){
        MatOfPoint2f mop = new MatOfPoint2f(contour.toArray());
        RotatedRect rc = Imgproc.minAreaRect(mop);
        Rect box = rc.boundingRect();
        System.out.println(rc + "   "+box );
        if(box.x<0){
            box.x=0;
        }
        if(box.y<0){
            box.y=0;
        }
        Mat image;
        image = new Mat(startImag, box);
        Imgcodecs.imwrite(fileName, image);
    }
}
