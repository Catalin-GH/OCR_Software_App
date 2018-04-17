package image.http.connection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends Activity
{
    private final int PICK_IMAGE = 1;

    public static InputStream inputStream = null;
    private Uri imageURI = null;
    public static String fileName = null;
    private EditText editText_ServerIP = null;
    public static EditText editText_Argument_1 = null;
    public static EditText editText_Argument_2 = null;
    public static EditText editText_QualityCompress = null;
    private Button button_ImageSelect = null;
    public static TextView textView_Details = null;
    private Button button_Transfer = null;

    private static Context context = null;

    public static Context getAppContext() { return MainActivity.context; }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();

        editText_ServerIP        = findViewById(R.id.editText_ServerIP);
        button_ImageSelect       = findViewById(R.id.button_ImageSelect);
        editText_Argument_1      = findViewById(R.id.editText_Argument_1);
        editText_Argument_2      = findViewById(R.id.editText_Argument_2);
        textView_Details         = findViewById(R.id.textView_Details);
        editText_QualityCompress = findViewById(R.id.editText_QualityCompress);
        button_Transfer          = findViewById(R.id.button_Transfer);

        editText_QualityCompress.setFilters(new InputFilter[]{ new MinMaxFilter("0", "100")});

        button_Transfer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(editText_ServerIP.getText().toString().matches(""))
                {
                    Toast.makeText(getApplicationContext(), "SERVER_IP este invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(editText_QualityCompress.getText().toString().matches(""))
                {
                    Toast.makeText(getApplicationContext(), "Quality Compress este invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(imageURI != null)
                {
                    try
                    {
                        inputStream = getContentResolver().openInputStream(imageURI);
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }

                    Config.Server_IP = editText_ServerIP.getText().toString();
                    Handler handler = new Handler();
                    Runnable run = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Intent i = new Intent(MainActivity.this, Transfer.class);
                            startActivity(i);
                        }
                    };
                    handler.post(run);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Selecteaza o imagine!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        button_ImageSelect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            switch(requestCode)
            {
                case PICK_IMAGE:
                    if (data == null)
                    {
                        System.out.println("Invalid data: OnActivityResult(int, int, Intent), MainActivity.java file");
                        return;
                    }
                    imageURI = data.getData();
                    fileName = getFileName(data.getData());
                    textView_Details.setText(fileName);
            }
        }
    }

    public void textViewClicked(View view)
    {
        Toast.makeText(this, "Introduceti adresa IP a serverului!", Toast.LENGTH_SHORT).show();
    }

    public String getFileName(Uri uri)
    {
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
            }
            finally
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
}

