/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.akor.barcodeDetecter;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author korgan
 */
public class BarcodeDetector {
    public String readCode(Mat findedBarcode) throws IOException{
        try{
            Result result = null;
            BufferedImage barCodeBufferedImage = matToBuff(findedBarcode);
        

            LuminanceSource source = new BufferedImageLuminanceSource(barCodeBufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Reader reader = new MultiFormatReader();
            result = reader.decode(bitmap);
            return result.getText();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    private BufferedImage matToBuff(Mat matImg) throws IOException{

        MatOfByte bytemat = new MatOfByte();

        Imgcodecs.imencode(".jpg", matImg, bytemat);

        byte[] bytes = bytemat.toArray();

        InputStream in = new ByteArrayInputStream(bytes);

        BufferedImage img = ImageIO.read(in);
        return img;
    }
}
