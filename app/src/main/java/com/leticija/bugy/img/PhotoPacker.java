package com.leticija.bugy.img;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoPacker {

    List<Bitmap> photosBmpList;

    public PhotoPacker(List<Bitmap> photosBmpList) {
        this.photosBmpList = photosBmpList;
        System.out.println("TO ENCODE (IN CLASS): "+photosBmpList);
    }

    public List<String> getEncodedPhotos() {
        final List<String> encodedPhotos = new ArrayList<>();

        for (int i = 0; i < photosBmpList.size(); i++) {

            Bitmap imgBitmap= photosBmpList.get(i);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imgBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] b = baos.toByteArray();
            String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);
            //System.out.println("ENCODED IMAGE: "+imageEncoded);
            encodedPhotos.add(imageEncoded);
        }
        return encodedPhotos;
    }

    public void sendPhotosToServerInThread() {
        final List<String> encodedPhotosList = getEncodedPhotos();

        for (int i = 0; i < encodedPhotosList.size(); i++) {

            final String photo = encodedPhotosList.get(i);

            TaskQueue.prepare().backgroundTask(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    try {
                        //System.out.println("PHOTO TO UPLOAD: "+photo);
                        Map<String,String> headers = new HashMap<>();

                        headers.put("Body-length",String.valueOf(photo.length()));

                        System.out.println("SENDING IMAGE TO SERVER !");
                        String response = Requester.request("/home/uploadPhotos", headers, photo);
                        System.out.println(response);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).subscribeMe();
        }

    }
}
