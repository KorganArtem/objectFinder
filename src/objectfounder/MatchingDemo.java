/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objectfounder;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author korgan
 */
public class MatchingDemo {
    public void run(Mat inFile, Mat templateFile, String outFile,
            int match_method) {
        System.out.println("\nRunning Template Matching");

        Mat img = inFile;
        Mat templ = templateFile;

        // / Create the result matrix
        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // / Do the Matching and Normalize
        Imgproc.matchTemplate(img, templ, result, match_method);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Imgcodecs.imwrite("out2.png", result);

        // / Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        System.out.println("inFile " + inFile.size());
        System.out.println("templateFile " + templateFile.size());
        System.out.println("Result " + result.size());
        Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF
                || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
            System.out.println(mmr.minVal);
        } else {
            matchLoc = mmr.maxLoc;
            System.out.println(mmr.maxVal);
        }
        // / Show me what you got

        // Save the visualized detection.
        System.out.println("Writing " + outFile);
        Imgcodecs.imwrite(outFile, img);

    }
}
