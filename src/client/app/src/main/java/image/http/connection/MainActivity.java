package image.http.connection;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity
{
    private final int PICK_IMAGE = 1;

    static String LOG_TAG = "MainActivity";

    private String message = null;
    private Bitmap image = null;

    public static String fileName = null;

    static private TextView textViewMessage = null;
    private ImageView imageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editTextServerIP = findViewById(R.id.editText_ServerIP);
        textViewMessage = findViewById(R.id.viewText_Message);
        imageView = findViewById(R.id.imageView_Preview);
        Button buttonSend = findViewById(R.id.button_Send);
        Button buttonReceive = findViewById(R.id.button_Receive);
        Button buttonImageSelect = findViewById(R.id.button_ImageSelect);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image == null) {
                    Toast.makeText(getApplicationContext(), "Selecteaza o imagine!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    try {
                        String url = "http://" + editTextServerIP.getText().toString() + ":8080/LocalServer/OCR";
                        message = new ConnectionPostImage().execute(url).get();
                        DisplayMessage(message);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        buttonReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = "http://" + editTextServerIP.getText().toString() + ":8080/LocalServer/OCR";
                    message = new ConnectionGetMessage().execute(url).get();
//                    image = new ConnectionGetImage().execute(url).get();
//                    scaleImage(image, imageView);
                    DisplayMessage(message);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonImageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if ((requestCode == PICK_IMAGE) && (intent == null)) {
                Log.i(LOG_TAG,"Invalid data: OnActivityResult(int, int, Intent), MainActivity.java file");
                return;
            }
            fileName = getFileNameFromURI(intent.getData());
            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), intent.getData());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            if(image != null)
            {
                scaleImage(image, imageView);
            }
            DisplayMessage("Se trimite imaginea: " +fileName);
        }
    }

    private String getFileNameFromURI(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content"))
        {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try
            {
                if (cursor != null && cursor.moveToFirst())
                {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally
            {
                cursor.close();
            }
        }
        if (result == null)
        {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
            {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    static public void DisplayMessage(String message) {
        if(textViewMessage != null) {
            textViewMessage.setText(message);
            textViewMessage.setMovementMethod(new ScrollingMovementMethod());
        }
        else {
            Log.i(LOG_TAG, "DisplayMessage: textViewMessage este null.");
        }
    }

    private void scaleImage(Bitmap bitmap, ImageView view) {
        // Get current dimensions AND the desired bounding box
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = 350;

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

    private class ConnectionGetMessage extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DisplayMessage("Se descarca mesajul...");
        }

        @Override
        protected String doInBackground(String... arg) {
            InputStream is;
            String URL = arg[0];
            String message = "";

            is = DataTransmission.ByGetMethod(URL);
            if (is != null) {
                message = convertStreamToString(is);
                Log.i(LOG_TAG, "ConnectionGetMessage::doInBackground: Mesajul este preluat.");
            }
            else {
                Log.i(LOG_TAG, "ConnectionGetMessage::doInBackground: InputStream = null.");
            }

            return message;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    private class ConnectionGetImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DisplayMessage("Imaginea se descarca...");
            Log.i(LOG_TAG,"Imaginea se descarca...");
        }

        @Override
        protected Bitmap doInBackground(String... arg) {
            InputStream is;
            String URL = arg[0];
            Bitmap image = null;

            is = DataTransmission.ByGetMethod(URL);
            if (is != null) {
                image = convertStreamToBitmap(is);
                Log.i(LOG_TAG, "ConnectionGetImage::doInBackground: Imaginea este preluata.");
            }
            else {
                Log.i(LOG_TAG, "ConnectionGetImage::doInBackground: InputStream = null.");
            }
            return image;
        }

        protected void onPostExecute(Bitmap image) {
            super.onPostExecute(image);
        }
    }

    private class ConnectionPostImage extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DisplayMessage("Imaginea se trimite la server...");
            Log.i(LOG_TAG,"Imaginea se trimite la server...");
        }

        @Override
        protected String doInBackground(String... arg) {
            String URL = arg[0];
            String message = DataTransmission.ByPostMethod(URL, fileName, image);

            return message;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            DisplayMessage(result);
        }
    }
    
    private Bitmap convertStreamToBitmap(InputStream is) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

        int count;
        byte[] buffer = new byte[1024];
        try {
            while((count = is.read(buffer)) > 0) {
                arrayOutputStream.write(buffer, 0, count);
            }
            arrayOutputStream.flush();
            arrayOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return BitmapFactory.decodeByteArray(arrayOutputStream.toByteArray(),
                0, arrayOutputStream.toByteArray().length);
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}

