package image.http.connection;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.TypedValue;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Transfer extends Activity
{
    private static ProgressDialog pDialog = null;

    private static void setPDialog(String message) { pDialog.setMessage(message);}
    private static void closeDialog() { pDialog.dismiss(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        pDialog = new ProgressDialog(this);
        setPDialog("Loading Data...");
        pDialog.setCancelable(false);
        pDialog.show();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String arg1 = MainActivity.editText_Argument_1.getText().toString();
                String arg2 = MainActivity.editText_Argument_2.getText().toString();

                Config.SERVER_URL = "http://" + Config.Server_IP + ":8080/" + Config.UPLOAD;
                String[] paramSend = new String[]{Config.SERVER_URL, arg1, arg2};
                SendHttpRequestTask task = new SendHttpRequestTask();
                task.execute(paramSend);

                while(task.getData() == null)
                { }

                Config.SERVER_URL = "http://" + Config.Server_IP + ":8080/" + Config.DOWNLOAD;
                String[] paramReceive = {Config.SERVER_URL};
                ReceiveHttpRequestTask receiveTask = new ReceiveHttpRequestTask();
                receiveTask.execute(paramReceive);
            }
        }).start();
    }


    private class SendHttpRequestTask extends AsyncTask<String, Void, String>
    {
        private String data = null;
        private HttpClient client = null;

        String getData()
        {
            return data;
        }

        @Override
        protected String doInBackground(String... params)
        {
            //setPDialog("Se incarca imaginea pe server...");
            String url =    params[0];
            String param1 = params[1];
            String param2 = params[2];

            Bitmap b = BitmapFactory.decodeStream(MainActivity.inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, Integer.parseInt(MainActivity
                    .editText_QualityCompress.getText().toString()), baos);

            try
            {
                client = new HttpClient(url);
                client.connectForMultipart();
                client.addFormPart(Config.uploadPOSTArgs[0], param1);
                client.addFormPart(Config.uploadPOSTArgs[1], param2);
                client.addFilePart(Config.fileType, MainActivity.fileName, baos.toByteArray());
                client.finishMultipart();
                data = client.getResponse();
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }

            return null;
        }
    }

    private class ReceiveHttpRequestTask extends AsyncTask<String, Void, byte[]>
    {
        private InputStream data = null;
        private HttpClient client = null;

        @Override
        protected byte[] doInBackground(String... params)
        {
            //setPDialog("Se descarca imaginea de pe server...");
            String url = params[0];

            client = new HttpClient(url);
            byte[] data_byte = client.downloadImage();
            return data_byte;
        }

        @Override
        protected void onPostExecute(byte[] result)
        {
            ImageView imageView = findViewById(R.id.imageView_Transfer);
            Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
            SaveImage(bitmap);
            scaleImage(bitmap, imageView);

            //setPDialog("Imaginea a fost descarcata!");
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            closeDialog();
        }

        public int dpToPx(float dp, Context context)
        {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        }

        private void scaleImage(Bitmap bitmap, ImageView view)
        {
            // Get current dimensions AND the desired bounding box
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int bounding = dpToPx(350, MainActivity.getAppContext());

            // Determine how much to scale: the dimension requiring less scaling is
            // closer to the its side. This way the image always stays inside your
            // bounding box AND either x/y axis touches it.
            float xScale = ((float) bounding) / width;
            float yScale = ((float) bounding) / height;
            float scale = (xScale <= yScale) ? xScale : yScale;

            // Create a matrix for the scaling and add the scaling data
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            // Create a new bitmap and convert it to a format understood by the ImageView
            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

            // Apply the scaled bitmap
            view.setImageBitmap(scaledBitmap);
        }

        private void SaveImage(Bitmap finalBitmap)
        {
            String root = MainActivity.getAppContext().getExternalCacheDir().toString();
            File imageDir = new File(root + "/saved_images");
            File textImageDir = new File(root + "/text_images");
            imageDir.mkdirs();
            textImageDir.mkdir();

            String imageNameAndType = MainActivity.fileName;
            String textImage = getFileName(imageNameAndType) + ".txt";

            File fileImageDir = new File(imageDir, imageNameAndType);
            File fileTextImageDir = new File(textImageDir, textImage);

            try
            {
                FileOutputStream out1 = new FileOutputStream(fileImageDir);
                FileOutputStream out2 = new FileOutputStream(fileTextImageDir);

                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out1);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out2);

                out1.flush();
                out1.close();

                out2.flush();
                out2.close();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private String getFileName(String fileNameAndType)
    {
        StringBuilder sb = new StringBuilder(fileNameAndType);
        int index = sb.lastIndexOf(".");
        sb.delete(index, sb.length());
        return sb.toString();
    }
}
