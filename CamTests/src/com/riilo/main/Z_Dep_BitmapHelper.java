package com.riilo.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.camtests.R;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;

public class Z_Dep_BitmapHelper {

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        } else {
            return BitmapFactory
                    .decodeByteArray(byteArray, 0, byteArray.length);
        }
    }

    public static Bitmap shrinkBitmap(Bitmap bm, int maxLengthOfEdge) {
        return shrinkBitmap(bm, maxLengthOfEdge, 0);
    }

    public static Bitmap shrinkBitmap(Bitmap bm, int maxLengthOfEdge,
            int rotateXDegree) {
        if (maxLengthOfEdge > bm.getWidth() && maxLengthOfEdge > bm.getHeight()) {
            return bm;
        } else {
            // shrink image
            float scale = (float) 1.0;
            if (bm.getHeight() > bm.getWidth()) {
                scale = ((float) maxLengthOfEdge) / bm.getHeight();
            } else {
                scale = ((float) maxLengthOfEdge) / bm.getWidth();
            }
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scale, scale);
            matrix.postRotate(rotateXDegree);

            // RECREATE THE NEW BITMAP
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
                    matrix, false);

            matrix = null;
            System.gc();

            return bm;
        }
    }
    
    public static Bitmap rotateBitmap(Bitmap bm,
            int rotateXDegree) {
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postRotate(rotateXDegree);

            // RECREATE THE NEW BITMAP
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
                    matrix, false);

            matrix = null;
            System.gc();

            return bm;
    }

    public static Bitmap readBitmap(Context context, Uri selectedImage) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inScaled = false;
//      options.inSampleSize = 3;
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(selectedImage, "r");
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            try {
                bm = BitmapFactory.decodeFileDescriptor(
                        fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                return null;
            }
        }
        return bm;
    }
    
    @SuppressWarnings("unused")
	private static Uri resizeAndSaveImage(Context context, Uri uri){
    	Bitmap bm = readBitmap(context, uri);
    	Bitmap resizedBm = shrinkBitmap(bm, 1920);
    	Uri retVal = saveBitmap(context.getResources(), resizedBm);
    	
    	bm.recycle();
    	resizedBm.recycle();
    	System.gc();
    	
    	return retVal;
    }
    
    public static Uri resizeAndSaveImage(Resources resources, String uriOrFilePath){
    	
    	Bitmap bm = BitmapFactory.decodeFile(Helpers.uriToFilePath(uriOrFilePath));
    	Bitmap resizedBm = shrinkBitmap(bm, 1920);
    	Uri retVal = saveBitmap(resources, resizedBm);
    	
    	bm.recycle();
    	resizedBm.recycle();
    	System.gc();
    	
    	return retVal;
    }
    
    public static Uri saveBitmap(Resources resources, Bitmap bitmap){
    	String envPath = Environment.getExternalStorageDirectory().getPath();
        if (!envPath.endsWith("/")){
        	envPath+="/";
        }
        envPath += resources.getString(R.string.photos_folder_name)+"/";
        File directory = new File(envPath);
        if (!directory.exists())
        	directory.mkdir();
        File file = new File(envPath+resources.getString(R.string.photo_name_base)+System.currentTimeMillis()+".jpg");
        FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
		return Uri.fromFile(file);
    }

    public static void clearBitmap(Bitmap bm) {
        bm.recycle();
        System.gc();
    }
}