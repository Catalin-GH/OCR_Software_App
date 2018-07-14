package image.http.connection;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataTransmission {

    static private String delimiter = "--";
    static private String boundary =  "SwA"+Long.toString(System.currentTimeMillis())+"SwA";

    static private String LOG_TAG = "DataTransmssion";

    static public String ByPostMethod(String ServerURL, String imageName, Bitmap image) {
        String message = "";
        OutputStream os;
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS);
        byte[] byteArray = byteArrayOS.toByteArray();
        image.recycle();

        try {
            URL url = new URL(ServerURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            os = connectForMultipart(connection);
            writeParamData(os);
            addFilePart(os, Config.fileType, imageName, byteArray);
            finishMultipart(os);

            message = getResponse(connection);

            connection.disconnect();
        }
        catch (Exception e) {
            Log.i(LOG_TAG, LOG_TAG + "::ByPostMethod: exceptie aruncata", e);
        }
        return message;
    }

    static public InputStream ByGetMethod(String ServerURL) {
        InputStream DataInputStream = null;
        try {
            URL url = new URL(ServerURL);
            HttpURLConnection cc = (HttpURLConnection) url.openConnection();
            cc.setReadTimeout(5000);
            cc.setConnectTimeout(5000);
            cc.setRequestMethod("GET");
            cc.setDoInput(true);
            int response = cc.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                DataInputStream = cc.getInputStream();
            }
        }
        catch (Exception e) {
            Log.i(LOG_TAG, LOG_TAG + "::ByGetMethod: exceptie aruncata", e);
        }
        return DataInputStream;
    }

    static private OutputStream connectForMultipart(HttpURLConnection connection) throws Exception {
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.connect();

        return connection.getOutputStream();
    }

    static private void addFilePart(OutputStream os, String paramName,
                            String fileName, byte[] data) throws Exception {
        os.write( (delimiter + boundary + "\r\n").getBytes());
        os.write( ("Content-Disposition: form-data; name=\"" + paramName +  "\"; filename=\"" + fileName + "\"\r\n"  ).getBytes());
        os.write( ("Content-Type: application/octet-stream\r\n"  ).getBytes());
        os.write( ("Content-Transfer-Encoding: binary\r\n"  ).getBytes());
        os.write("\r\n".getBytes());
        os.write(data);
        os.write("\r\n".getBytes());
    }

    static private void finishMultipart(OutputStream os) throws Exception {
        os.write( (delimiter + boundary + delimiter + "\r\n").getBytes());
    }

    static private String getResponse(HttpURLConnection connection) throws Exception {
        InputStream is = connection.getInputStream();
        byte[] b1 = new byte[1024];
        StringBuffer buffer = new StringBuffer();

        while ( is.read(b1) != -1)
            buffer.append(new String(b1));

        return buffer.toString();
    }

    static private void writeParamData(OutputStream os) throws Exception {
        os.write( (delimiter + boundary + "\r\n").getBytes());
        os.write( "Content-Type: text/plain\r\n".getBytes());
        os.write( ("Content-Disposition: form-data; name=\"" + "arg1" + "\"\r\n").getBytes());
        os.write( ("\r\n" + "" + "\r\n").getBytes());
    }
}
