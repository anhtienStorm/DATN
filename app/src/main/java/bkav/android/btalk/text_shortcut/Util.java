// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package bkav.android.btalk.text_shortcut;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Util {
    private static final String TAG = "Bchrome";

    private static final String FOLDER = "/Bkav/ChimLac/";

    public static final boolean DEBUG = true;

    public static final boolean FORCE_WRITE_TO_FILE_DEBUG = true;

    private static final String[] END_DOMAIN = {
            ".vn", ".net", ".com", ".org", ".co", ".me"
    };

    private static final String[] START_DOMAIN = {
            "www.", "m.", "en.", "vi.", ".", "euro.", "mb.", "kinhdoanh.", "suckhoe.", "giadinh.",
            "mobile.", "touch.", "giaitri.", "tmo.", "news.", "thethao.", "thuthuat.", "sohoa."
    };

    private static final String[] START_URL = {
            "http://www.", "http://m.", "http://en.", "http://vi.", "http://", "https://www.",
            "https://m.", "https://en.", "https://vi.", "https://"
    };

    private static final String DOCS_GOOGLE_URL = "https://docs.google.com/gview?embedded=true&url=";

    public static final String YOUTUBE_URL_PARTTEN = "youtube.com/watch";

    public static final String GOOGLE_URL_PARTTEN = "google.com";

    public static final String[] LIST_URL_PARTTEN = {
            ".pdf", ".doc", ".docx", ".xls", ".xlsx"
    };

    public Util() {
    }

    public static boolean isDocumentUrl(String url) {
        url = url.toLowerCase();
        return ((url.endsWith(".pdf") || url.contains(".pdf?") || url.endsWith(".doc")
                || url.contains(".doc?") || url.endsWith(".docx") || url.contains(".docx?")
                || url.endsWith(".xls") || url.contains(".xls?") || url.endsWith(".xlsx") || url
                    .contains(".xlsx?")) && !url.contains("docs.google.com"));
    }

    private static long sTime = 0;

    public static void setStaticTime() {
        sTime = System.currentTimeMillis();
    }

    public static void showLogTime(String text) {
        long time = System.currentTimeMillis();
        long timedistance = time - sTime;
        showLog(text + " time = " + timedistance);
        sTime = time;
    }

    public static void showLog(String text) {
        showLog(text, false);
    }

    public static void showLog(String tag, String text) {
        showLog(tag, text, new Object[0]);
    }

    private static void showLog(String text, boolean tofile) {
        if (!DEBUG)
            return;
        Log.d(TAG, text);
        if (tofile || FORCE_WRITE_TO_FILE_DEBUG) {
            writeLog(text);
        }
    }

    public static void showLogStackTrace(String tag, String text, Object... arr) {
        final int N = arr.length;
        arr = java.util.Arrays.copyOf(arr, N + 1);
        arr[N] = Log.getStackTraceString(new Exception());
        showLog(false, tag, text, arr);
    }

    public static void showLog(String tag, String text, Object... arr) {
        showLog(false, tag, text, arr);
    }

    public static void showLog(boolean tofile, String tag, String text, Object... arr) {
        if (!DEBUG)
            return;

        StringBuilder s = new StringBuilder(tag).append(": ").append(text).append(" ");
        for (Object f : arr)
            s.append(f).append(" | ");
        System.out.println("QuangNHe:  " + s.toString());
        if (tofile || FORCE_WRITE_TO_FILE_DEBUG) {
            writeLog(text);
        }
    }

    public static void forceShowLog(String text) {
        System.out.println("QuangNHe: " + text);
        //        writeLog("QuangNHe: " + text);
    }

    public static boolean writeLog(String text) {
        String iconsStoragePath = Environment.getExternalStorageDirectory() + FOLDER;
        File sdIconStorageDir = new File(iconsStoragePath);
        sdIconStorageDir.mkdirs();
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy");
        String filename = ft.format(dNow) + ".txt";
        SimpleDateFormat time = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        text = time.format(dNow) + " " + text;
        try {
            String filePath = sdIconStorageDir.toString() + "/" + filename;
            File file = new File(filePath);
            BufferedWriter out = new BufferedWriter(new FileWriter(file, true), 1024);
            out.write(text);
            out.newLine();
            out.close();
            /*
             * FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             * fileOutputStream.write(text.getBytes()); fileOutputStream.close();
             */

        } catch (FileNotFoundException e) {
            // Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            // Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static String getDomainName(String url) {
        return getDomainName(url, false);
    }

    public static String getDomainName(String url, boolean subdomain) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            if (domain == null) {
                return url;
            }
            if (!subdomain) {
                for (String s : START_DOMAIN) {
                    if (domain.startsWith(s))
                        domain = domain.substring(s.length());
                }
            }
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {

        }
        return url;
    }

    /**
     * check xem có phải 2 url giống nhau không
     * 
     * @param url1
     * @param url2
     * @return
     */
    public static boolean checkSameUrl(String url1, String url2) {
        String domain1 = Util.getDomainName(url1);
        String domain2 = Util.getDomainName(url2);
        if (domain1.equals(domain2) || domain1.startsWith(domain2) || domain2.startsWith(domain1)
                || domain2.endsWith(domain1) || domain1.endsWith(domain2)) {
            return true;
        }
        String sitename1 = Util.getSiteName(domain1);
        String sitename2 = Util.getSiteName(domain2);
        if (sitename1.equals(sitename2)) {
            return true;
        }
        return false;
    }

    public static String getSiteName(String domain) {
        for (String s : START_DOMAIN) {
            if (domain.startsWith(s))
                domain = domain.substring(s.length());
        }
        for (String s : END_DOMAIN) {
            if (domain.endsWith(s)) {
                domain = domain.substring(0, domain.lastIndexOf(s));
            }
        }

        // QuangNHe: Rat nhieu sitename sau xu ly van con dau '/'. Doan nay check dieu kien day
        if (domain.contains("/")) {
            if (domain.startsWith("http://") || domain.startsWith("https://")) {
                domain = domain.substring(domain.indexOf("/") + 2);
            }
            if (domain.contains("/")) {
                domain = domain.substring(0, domain.indexOf("/"));
            }
        }

        return domain;
    }

    public static boolean saveImageFromBitmap(Bitmap imageData, String filename) {
        // get path to external storage (SD card)
        String iconsStoragePath = Environment.getExternalStorageDirectory() + FOLDER;
        File sdIconStorageDir = new File(iconsStoragePath);

        // create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();

        try {
            String filePath = sdIconStorageDir.toString() + "/" + filename + ".png";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            // choose another format if PNG doesn't suit you
            imageData.compress(CompressFormat.PNG, 100, bos);

            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            // Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            // Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean saveImageFromBitmap(Context context, Bitmap b, String name) {
        name = getSiteName(name);
        showLog("save : " + name);
        FileOutputStream out;
        try {
            out = context.openFileOutput(name + ".png", Context.MODE_PRIVATE);
            b.compress(CompressFormat.PNG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Bitmap getBitmapFromFile(String filename) {
        String iconsStoragePath = Environment.getExternalStorageDirectory() + FOLDER;
        File sdIconStorageDir = new File(iconsStoragePath);
        String filePath = sdIconStorageDir.toString() + "/" + filename + ".png";
        File file = new File(filePath);
        Bitmap bitmap = null;
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFile(filePath, options);
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromFile(Context context, String name) {
        name = getSiteName(name);
        try {
            FileInputStream fis = context.openFileInput(name + ".png");
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        } catch (Exception e) {
        }
        return null;
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * HoangNTb: Tính toán màu dựa vào favicon
     * 
     * @param bitmap
     * @return
     */
    public static int getColorFrombitmap(Bitmap bitmap) {
        int x = 0, y = 0;
        int w = bitmap.getWidth() - 2 * x;
        int h = bitmap.getHeight() - 2 * y;
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), x, y, w, h);
        //
        List<Mau> colors = new ArrayList<Mau>();
        for (int p : pixels) {
            // check mau trong list
            boolean check = false;
            for (Mau m : colors) {
                if (m.checkSameColor(p)) {
                    m.count++;
                    check = true;
                    break;
                }
            }
            if (!check) {
                Mau mau = new Mau(p);
                colors.add(0, mau);
            }
        }
        // dem so luong mau nhieu nhat
        int count = 0;
        int color = 0;
        for (Mau m : colors) {
            if (count < m.count) {
                color = m.color;
                count = m.count;
            }
        }
        // lấy màu khác trắng và đen
        if ((color == 0 && count == 0) || !checkBlackOrWhite(color))
            return color;
        int color2 = color;
        count = 0;
        for (Mau m : colors) {
            if (count < m.count && color != m.color) {
                color2 = m.color;
                count = m.count;
            }
        }
        return color2;
    }

    /**
     * HoangNTb: Tính màu chữ dựa vào màu background<br>
     * Màu chữ dùng 2 màu đen và trắng.
     * 
     * @return
     */
    public static int getColorTextFromBackgroundcolor(int color) {
        int[] textcolors = new int[3];
        textcolors[0] = 0xffffffff; // trắng
        textcolors[1] = 0xff666666;
        textcolors[2] = 0x6478E6;
        int distance = 0;
        int colorChose = textcolors[0];
        for (int i = 0; i < textcolors.length; i++) {
            if (distance < getDistanColor(color, textcolors[i])) {
                distance = getDistanColor(color, textcolors[i]);
                colorChose = textcolors[i];
            }
        }
        return colorChose;
    }

    private static boolean checkBlackOrWhite(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        // int[] lab = new int[3];
        // rgb2lab(r, g, b, lab);
        if ((r > 250 && g > 250 && b > 250) || (r < 5 && g < 5 && b < 5)) {
            return true;
        }
        return false;
    }

    public static boolean isSameColor(int colorOne, int colorTwo) {
        // int deltaAlpha = colorOne >> 24 - colorTwo >> 24;
        int deltaRed = ((colorOne & 0x00ffffff) >> 16) - ((colorTwo & 0x00ffffff) >> 16);
        int deltaGreen = ((colorOne & 0x0000ffff) >> 8) - ((colorTwo & 0x0000ffff) >> 8);
        int deltaBlue = (colorOne & 0x000000ff) - (colorTwo & 0x000000ff);
        return deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue * deltaBlue < 1200; // Số tự đưa vào
    }

    private static int getDistanColor(int colorOne, int colorTwo) {
        // int deltaAlpha = colorOne >> 24 - colorTwo >> 24;
        int deltaRed = ((colorOne & 0x00ffffff) >> 16) - ((colorTwo & 0x00ffffff) >> 16);
        int deltaGreen = ((colorOne & 0x0000ffff) >> 8) - ((colorTwo & 0x0000ffff) >> 8);
        int deltaBlue = (colorOne & 0x000000ff) - (colorTwo & 0x000000ff);
        return deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue * deltaBlue; // Số tự đưa vào
    }

    public static int getIntFromColor(int Red, int Green, int Blue) {
        Red = (Red << 16) & 0x00FF0000; // Shift red 16-bits and mask out other
                                        // stuff
        Green = (Green << 8) & 0x0000FF00; // Shift Green 8-bits and mask out
                                           // other stuff
        Blue = Blue & 0x000000FF; // Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; // 0xFF000000 for 100% Alpha.
                                                // Bitwise OR everything
                                                // together.
    }

    private static class Mau {
        int color;

        int maxR, maxG, maxB, minR, minG, minB;

        int count;

        public Mau(int c) {
            color = c;
            count = 1;
            maxR = (color >> 16) & 0xFF;
            minR = maxR;
            maxG = (color >> 8) & 0xFF;
            minG = maxG;
            minB = (color >> 0) & 0xFF;
            maxB = minB;
        }

        public boolean checkSameColor(int color2) {
            if (isSameColor(color, color2)) {

                int r = (color2 >> 16) & 0xFF;
                int g = (color2 >> 8) & 0xFF;
                int b = (color2 >> 0) & 0xFF;
                if (r >= minR && r <= maxR && g >= minG && g <= maxG && b >= minB && b <= maxB) {
                    return true;
                } else {
                    if (r < minR)
                        minR = r;
                    if (r > maxR)
                        maxR = r;
                    if (g < minG)
                        minG = g;
                    if (g > maxG)
                        maxG = g;
                    if (b < minB)
                        minB = b;
                    if (b > maxB)
                        maxB = b;
                    // tinh toan lai mau
                    color = getIntFromColor((int) (minR + maxR) / 2, (int) (minG + maxG) / 2,
                            (int) (minB + maxB) / 2);
                }
                return true;
            }
            return false;
        }
    }

    public static void rgb2lab(int R, int G, int B, int[] lab) {
        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f / 24389.f;
        float k = 24389.f / 27.f;

        float Xr = 0.964221f; // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = R / 255.f; // R 0..1
        g = G / 255.f; // G 0..1
        b = B / 255.f; // B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r / 12;
        else
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

        if (g <= 0.04045)
            g = g / 12;
        else
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

        if (b <= 0.04045)
            b = b / 12;
        else
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);

        X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
        Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
        Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

        // XYZ to Lab
        xr = X / Xr;
        yr = Y / Yr;
        zr = Z / Zr;

        if (xr > eps)
            fx = (float) Math.pow(xr, 1 / 3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if (yr > eps)
            fy = (float) Math.pow(yr, 1 / 3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if (zr > eps)
            fz = (float) Math.pow(zr, 1 / 3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = (116 * fy) - 16;
        as = 500 * (fx - fy);
        bs = 200 * (fy - fz);

        lab[0] = (int) (2.55 * Ls + .5);
        lab[1] = (int) (as + .5);
        lab[2] = (int) (bs + .5);
    }

    public static void writeFile(Context context, String filename, String data) {
        try {
            FileOutputStream fileout = context.openFileOutput(filename, context.MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(data);
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * HoangNTb: đọc file từ data/data 
     * @param context
     * @param filename
     * @return
     */
    public static String getJSFromFile(Context context, String filename) {
        String s = "";
        try {
            FileInputStream fileIn = context.openFileInput(filename);
            InputStreamReader InputRead = new InputStreamReader(fileIn);
            char[] inputBuffer = new char[1024];
            int charRead;
            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String getJSFromRawFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return byteArrayOutputStream.toString();
    }

    public static String getUrlSuggestions(String url) {
        if (url.indexOf(GOOGLE_URL_PARTTEN) == -1) {
            return getDomainName(url);
        }
        try {
            url = java.net.URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
        for (String s : START_URL) {
            if (url.startsWith(s))
                url = url.substring(s.length());
        }
        return url.startsWith("www.") ? url.substring(4) : url;
    }

    public static Bitmap getBitmapFromView(View v) {
        v.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        if (v.getMeasuredHeight() <= 0 || v.getMeasuredWidth() <= 0) {
            return null;
        }
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.draw(c);
        return b;
    }

    public static String getPackageName() {
        return "org.chromium.chrome.shell";

    }

    public static String getPackageNamePermission() {
        return "org.chromium.chrome.shell";

    }

    public static String addGoogleDocsToUrl(String url) {
        if (isPdfOrOfficeLink(url) && !url.contains(DOCS_GOOGLE_URL))
            url = DOCS_GOOGLE_URL + url;
        return url;
    }

    /**
     * Check xem link có phải là link tài liệu hay không
     * 
     * @param url
     * @return
     */
    public static boolean isPdfOrOfficeLink(String url) {
        if (url == null) {
            return false;
        }
        for (String partten : LIST_URL_PARTTEN) {
            if (url.contains(partten)) {
                if (url.endsWith(partten)) {
                    return true;
                } else {
                    String parttenTemp = partten + "&";
                    if (url.indexOf(parttenTemp) != -1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getIPAddressFromHost(String host) {
        String ip = "";
        try {
            InetAddress address = InetAddress.getByName(host);
            ip = address.toString();
        } catch (UnknownHostException e) {
            showLog("getIPAddressFromHost:  UnknownHostException" + e.toString());
        } catch (NetworkOnMainThreadException e) {
            showLog("getIPAddressFromHost:  NetworkOnMainThreadException" + e.toString());
        } /*
           * catch (MalformedURLException e) {
           * showLog("getIPAddressFromHost:  MalformedURLException" + e.toString()); }
           */
        if (ip.indexOf("/") > 0) {
            ip = ip.substring(ip.lastIndexOf("/") + 1);
        }
        return ip;
    }

    public static String fixUrl(String url) {
        if (url.indexOf("javascript:") != -1)
            return "";
        return url;
    }

    /**
     * HoangNTb: Lấy list string theo từng dòng từ raw file
     * 
     * @param ctx
     * @param resId
     * @return
     */
    public static List<String> getListLineFromRawFile(Context ctx, int resId) {
        List<String> lines = new ArrayList<String>();
        try {
            InputStream inputStream = ctx.getResources().openRawResource(resId);
            DataInputStream in = new DataInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            // Read File Line By Line

            while ((strLine = br.readLine()) != null) {
                lines.add(strLine.replace(" ", ""));
            }
            br.close();
        } catch (IOException e) {

        }
        return lines;
    }

    //Bkav QuanTHb: xay dung logic doc file rieng cho Adblock
    public static List<String> getListAdsFromRawFile(Context ctx, int resId) {
        List<String> list_ads = new ArrayList<String>();
        try {
            InputStream inputStream = ctx.getResources().openRawResource(resId);
            DataInputStream in = new DataInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String str_ads;
            // Read File Line By Line

            while ((str_ads = br.readLine()) != null) {
                list_ads.add(str_ads);
            }
            br.close();
        } catch (IOException e) {

        }
        return list_ads;
    }

    // QuanTHb add: hàm kiểm tra xem có phải link download file không
    public static final String[] DOCUMENT_TYPE = {
            ".csv", ".doc", ".docx", ".djvu", ".odp", ".ods", ".odt", ".pps", ".ppsx", ".ppt",
            ".pptx", ".pdf", ".ps", ".eps", ".rtf", ".txt", ".wks", ".wps", ".xls", ".xlsx",
            ".xps", ".7z", ".zip", ".rar", ".jar", ".tar", ".gz", ".cab"
    };

    public static final String[] SERVER_STORAGE = {
            "www.mediafire.com", "drive.google.com", "www.fshare.vn", "file.svit.vn", "upfile.vn"
    };

    public static boolean isFileDownload(String url) {
        boolean downloadThis = false;
        // QuanTHb add: check các đuôi file văn bản/tệp trong link
        for (int i = 0; i < DOCUMENT_TYPE.length; i++) {
            if (url != null && url.endsWith(DOCUMENT_TYPE[i])) {
                downloadThis = true;
                break;
            }
        }
        return downloadThis;
    }

    // QuanTHb add: hàm kiểm tra xem có phải email không?
    public static boolean isEmailLink(String url) {
        if (url.contains("@")) {
            return true;
        } else {
            return false;
        }
    }

    public static void scanFile(Context context, File fileToScan) {
        MediaScannerConnection.scanFile(context, new String[] {
            fileToScan.getAbsolutePath()
        }, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {

            }
        });
        Intent mediaScanIntent;
        mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(fileToScan);
        mediaScanIntent.setData(contentUri);
    }

    public static String encodeToBase64(Bitmap image, CompressFormat compressFormat,
            int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static boolean isInBounds(MotionEvent ev, View view) {
        if (ev == null || view == null)
            return false;
        int[] menuItemViewPos = new int[2];
        view.getLocationOnScreen(menuItemViewPos);

        return menuItemViewPos[0] < ev.getX() && ev.getX() < menuItemViewPos[0] + view.getWidth()
                && menuItemViewPos[1] < ev.getY()
                && ev.getY() < menuItemViewPos[1] + view.getHeight();
    }

}
