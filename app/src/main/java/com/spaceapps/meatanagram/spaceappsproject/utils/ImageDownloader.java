package com.spaceapps.meatanagram.spaceappsproject.utils;

import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Federica on 11/04/15.
 */
public class ImageDownloader {

    private static final String TAG = "ImageDownloader";

    public static int [] getTiles(final double lat, final double lon, final int zoom) {
        int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        if (xtile < 0)
            xtile=0;
        if (xtile >= (1<<zoom))
            xtile=((1<<zoom)-1);
        if (ytile < 0)
            ytile=0;
        if (ytile >= (1<<zoom))
            ytile=((1<<zoom)-1);
        int[] tiles = new int[2];
        tiles[0] = xtile;
        tiles[1] = ytile;
        return tiles;
    }

    public static String saveImageDefaultInDate(double lat, double lon, Date date) throws IOException {

        return saveImage(lat, lon, 7, date);
    }

    public static String saveImageDefaultToday(double lat, double lon) throws IOException {

        Date date = new Date();

        return saveImage(lat, lon, 7, date);
    }

    public static String saveImage(double lat, double lon, int zoom, Date date) throws IOException {
        int [] tiles = getTiles(lat, lon, zoom);
        int d, m ,y;
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
//        String day, month, year;
//        d = Calendar.DAY_OF_MONTH;
//        m = Calendar.MONTH;
//        y = Calendar.YEAR;
//        if(d < 10)
//            day = "0"+String.valueOf(d);
//        else day = String.valueOf(d);
//        if(m < 10)
//            month = "0"+String.valueOf(m);
//        else month = String.valueOf(m);
//        year = String.valueOf(y);

        String imageUrl = "http://map1.vis.earthdata.nasa.gov/wmts-geo/MODIS_Terra_CorrectedReflectance_TrueColor/default/"+dateString+"/EPSG4326_250m/"+String.valueOf(zoom)+"/"+ String.valueOf(tiles[0])+"/"+String.valueOf(tiles[1])+".jpg";
        Log.d(TAG, imageUrl);
//        imageUrl = "http://map1.vis.earthdata.nasa.gov/wmts-geo/MODIS_Terra_CorrectedReflectance_TrueColor/default/2014-02-05/EPSG4326_250m/6/39/24.jpg";
        return imageUrl;
//        String destinationFile = "image.jpg";
//
//        URL url = new URL(imageUrl);
//        InputStream is = url.openStream();
//        OutputStream os = new FileOutputStream(destinationFile);
//
//        byte[] b = new byte[2048];
//        int length;
//
//        if (is == null){
//            while (is == null){
//                day = imageUrl.substring(102,105);
//                month = imageUrl.substring(99,102);
//                year = imageUrl.substring(94,99);
//
//                if (day.equals("01")){
//                    if(month.equals("01")){
//                        day ="31";
//                        month ="12";
//                        year = String.valueOf(Integer.parseInt(month)-1);
//                    }
//                    else if (month.equals("03")){
//                        day = "28";
//                        month = "02";
//                    }
//                    else if (month.equals("02") || month.equals("04") || month.equals("06") || month.equals("09") || month.equals("11")){
//                        day = "31";
//                        if (Integer.parseInt(month) <= 10)
//                            month = "0"+String.valueOf(Integer.parseInt(month)-1);
//                        else month = String.valueOf(Integer.parseInt(month)-1);
//                    }
//                    else{
//                        day = "30";
//                        if (month.equals("08"))
//                            day = "31";
//                        if (Integer.parseInt(month) <= 10)
//                            month = "0"+String.valueOf(Integer.parseInt(month)-1);
//                        else month = String.valueOf(Integer.parseInt(month)-1);
//                    }
//                }
//
//                else{
//                    if(Integer.parseInt(day)<=10)
//                        day = "0"+String.valueOf(Integer.parseInt(day)-1);
//                    else day = String.valueOf(Integer.parseInt(month)-1);
//                }
//
//                if(Integer.parseInt(year) < 2015) //fixme no idea 8)
//                    break;
//
//                imageUrl = "http://map1.vis.earthdata.nasa.gov/wmts-geo/MODIS_Terra_CorrectedReflectance_TrueColor/default/"+year+"-"+month+"-"+day+"/EPSG4326_250m/"+String.valueOf(zoom)+"/"+ String.valueOf(tiles[0])+"/"+String.valueOf(tiles[1])+".jpg";
//                url = new URL(imageUrl);
//                is = url.openStream();
//            }
//        }
//
//        while ((length = is.read(b)) != -1)
//            os.write(b, 0, length);
//
//        is.close();
//        os.close();
    }

 }

