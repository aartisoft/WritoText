package com.blank.writotext.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.blank.writotext.R;
import com.blank.writotext.utils.Fyle;
import com.blank.writotext.utils.PolygonView;
import com.blank.writotext.utils.RenameDialog;
import com.blank.writotext.utils.SQLiteDatabaseHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraActivity extends AppCompatActivity implements RenameDialog.RenameDialogListener{

    static {
        System.loadLibrary("opencv_java3");
    }

    public static final int[] fimgs = {R.drawable.doc,R.drawable.pdf};
    private FrameLayout sourceFrame;
    private PolygonView polygonView;
    private SQLiteDatabaseHandler db;
    Button binaB, pdfB, ocrB ,cropB;
    ImageView imageView;
    List<Point> points = new ArrayList<>();
    ArrayList<String> textlist = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_camera);


        binaB = findViewById(R.id.binBut);
        pdfB = findViewById(R.id.pdfBut);
        ocrB= findViewById(R.id.ocrBut);
        cropB = findViewById(R.id.cropBut);
        imageView = findViewById(R.id.editView);

        db = new SQLiteDatabaseHandler(this);

        String path= intent.getStringExtra("pic");

        Bitmap bitmap = null;
        try {
            bitmap = modifyOrientation(BitmapFactory.decodeFile(path),path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Bitmap[] finalBitmap = {bitmap};

        imageView.setImageBitmap(bitmap);
        sourceFrame = findViewById(R.id.sourceFrame);
        polygonView = findViewById(R.id.polygonView);
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                if (finalBitmap[0] != null) {
                    setBitmap(finalBitmap[0]);
                }
            }
        });



        binaB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalBitmap[0] = binarize(finalBitmap[0]);
                Toast.makeText(CameraActivity.this,"Image Saved",Toast.LENGTH_SHORT).show();
                imageView.setImageBitmap(finalBitmap[0]);
            }
        });

        cropB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int width = finalBitmap[0].getWidth();
                int height = finalBitmap[0].getHeight();
                float xRatio = (float) finalBitmap[0].getWidth() / imageView.getWidth();
                float yRatio = (float) finalBitmap[0].getHeight() / imageView.getHeight();
                Map<Integer, PointF> points = polygonView.getPoints();
                finalBitmap[0] = warp(finalBitmap[0], new Point(points.get(0).x* xRatio,points.get(0).y* yRatio), new Point(points.get(2).x* xRatio,points.get(2).y* yRatio)
                        , new Point(points.get(3).x* xRatio,points.get(3).y* yRatio), new Point(points.get(1).x* xRatio,points.get(1).y* yRatio));
                Toast.makeText(CameraActivity.this,"Image Saved",Toast.LENGTH_SHORT).show();
                polygonView.setVisibility(View.GONE);
                imageView.setImageBitmap(finalBitmap[0]);
            }
        });

        pdfB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(finalBitmap[0], null);

            }
        });


        ocrB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recogniseText(finalBitmap[0]);
                openDialog(null,textlist);
            }
        });
        final String[] temp = {null};
        ocrB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                recogniseText(finalBitmap[0]);
                if (textlist.size()<1){
                    Toast.makeText(CameraActivity.this,"Working On It!!!",Toast.LENGTH_SHORT).show();
                }else{
                    temp[0]="";
                    for(String str: textlist) {
                        temp[0] +=(str+" ");
                    }
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", temp[0]);
                    clipboard.setPrimaryClip(clip);
                    temp[0]="";
                    Toast.makeText(CameraActivity.this,"Text Copied",Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

    }

    private void setBitmap(Bitmap original) {
        Bitmap scaledBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
        imageView.setImageBitmap(scaledBitmap);
        Bitmap tempBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Map<Integer, PointF> pointFs = getEdgePoints(tempBitmap);
        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);
        int padding = (int) getResources().getDimension(R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);
    }

    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        Map<Integer, PointF> orderedPoints = orderedValidEdgePoints(tempBitmap, pointFs);
        return orderedPoints;
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }

    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(0, 0));
        pointFs.add(new PointF(tempBitmap.getWidth(), 0));
        pointFs.add(new PointF(0, tempBitmap.getHeight()));
        pointFs.add(new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return pointFs;
    }

    private void recogniseText(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        textRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processImage(firebaseVisionText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("eror",e.getMessage());
            }
        });
    }

    private void writeToFile(ArrayList<String> arr,String fname) {

        File directory = new File(Environment.getExternalStorageDirectory() + "/WritoText");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        ObjectOutput out = null;

        try {
            FileWriter writer = new FileWriter(directory
                    +  "/"+fname+".txt");
            for(String str: arr) {
                writer.write(str+" ");
            }
            writer.close();
            Fyle file = new Fyle();
            file.setFileName(fname+".txt");
            file.setFilePath(Environment.getExternalStorageDirectory() + "/WritoText/");
            file.setFileImg(fimgs[0]);
            db.addFile(file);
            db.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processImage(FirebaseVisionText firebaseVisionText){

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        if (blocks.size() == 0){
            Toast.makeText(this,"no text found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int l = 0; l < elements.size(); l++) {
                    textlist.add(elements.get(l).getText());
                }
            }
        }
    }
    public void openDialog(Bitmap bitmap, ArrayList<String> list) {
        RenameDialog dialog = new RenameDialog(bitmap, list);
        dialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void applyTexts(Bitmap bitmap,String fname) {
        convertPDF(bitmap,fname);
        Toast.makeText(this,"PDF Saved",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void applyTextocr(ArrayList<String> arr, String fname) {
        writeToFile(arr,fname);
        Toast.makeText(this,"Text Saved",Toast.LENGTH_SHORT).show();
    }

    public static Bitmap warp(Bitmap image, Point ocvPIn1, Point ocvPIn2, Point ocvPIn3, Point ocvPIn4) {
        int resultWidth = euclidean(ocvPIn2,ocvPIn3);
        int resultHeight = euclidean(ocvPIn1,ocvPIn2);

        Mat inputMat = new Mat(image.getHeight(), image.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, inputMat);
        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        List<Point> source = new ArrayList<Point>();
        source.add(ocvPIn1);
        source.add(ocvPIn2);
        source.add(ocvPIn3);
        source.add(ocvPIn4);
        Mat startM = Converters.vector_Point2f_to_Mat(source);

        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, 0);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        Bitmap output = Bitmap.createBitmap(euclidean(ocvPIn2,ocvPIn3), euclidean(ocvPIn1,ocvPIn2), Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);
        storeImage(output);
        return output;
    }


    public static int euclidean(Point p1, Point p2){
        return (int) Math.sqrt((p2.y - p1.y) * (p2.y - p1.y) + (p2.x - p1.x) * (p2.x - p1.x));
    }
    public Bitmap binarize(Bitmap bitmap){
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);


        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);

        Imgproc.adaptiveThreshold(imageMat,imageMat,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,31,10);

        Utils.matToBitmap(imageMat, bitmap);
        imageMat.release();
        storeImage(bitmap);
        return bitmap;
    }

    public void convertPDF(Bitmap bitmap, String fname)
    {
        Document document = new Document();
        try {

            File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/WritoText");

            PdfWriter.getInstance(document, new FileOutputStream(mediaStorageDir.getPath() + "/"+fname+".pdf")); //  Change pdf's name.

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            Image image = Image.getInstance(stream.toByteArray());

            document.open();
            float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
            image.scalePercent(scaler);
            image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

            document.add(image);

            Fyle file = new Fyle();
            file.setFileName(fname+".pdf");
            file.setFilePath(Environment.getExternalStorageDirectory() + "/WritoText/");
            file.setFileImg(fimgs[1]);
            db.addFile(file);
            db.close();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        finally {
            document.close();
        }
    }

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("co",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("a", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("a", "Error accessing file: " + e.getMessage());
        }
    }

    public static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Pictures"
                + "/ScannedImages");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


}
