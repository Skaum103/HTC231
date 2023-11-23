package com.example.htc23;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private TextView processingText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the views
        imageView = findViewById(R.id.imageView);
        Button button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        textView.setVisibility(View.INVISIBLE);
        processingText = findViewById(R.id.processingText);

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        // Set a click listener for submit the button
        button.setOnClickListener(view -> {
            textView.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent , 100);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            assert data != null;
            Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            imageView.setImageBitmap(bitmap);

            // Convert the bitmap to bytes array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // Send the image to the server
            sendImageToServer(byteArray);
            processingText.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Send the image to the server
     * @param imageBytes The image in bytes array
     */
    private void sendImageToServer(byte[] imageBytes) {
        OkHttpClient client = new OkHttpClient();

        // Create the request body, put username password and image bytes in it
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", "lionel")
                .addFormDataPart("password", "antonela240687")
                .addFormDataPart("image", "test.png",
                        RequestBody.create(MediaType.parse("image/png"), imageBytes))
                .build();

        // Create the request, send to server and get the response
        Request request = new Request.Builder()
                .url("http://24.224.142.115:3000/identify")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle the error
                e.printStackTrace();
            }

            // Extract the text from the response data, and set it to the TextView
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> textView.setText("Error: " + response));
                    throw new IOException("Unexpected code " + response);
                } else {
                    String responseData = response.body().string();
                    // Set the response data to the TextView on the main thread
                    runOnUiThread(() -> {
                        processingText.setVisibility(View.INVISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(responseData);
                    });
                }
            }
        });
    }
}