package com.freelancing.zhenisapp;// MainActivity.java

import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ObjectApi objectApi;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Retrofit for API requests
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.example.com/")  // Replace with your API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        objectApi = retrofit.create(ObjectApi.class);

        // Set up CameraX
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Set up the Preview use case
                    Preview preview = new Preview.Builder().build();

                    // Set up the ImageAnalysis use case
                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(640, 480))  // Adjust resolution as needed

                            .build();

                    imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                    // Bind the use cases to the camera
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, getMainExecutor());
        }

    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        // Convert ImageProxy to byte array or File and send to API for measurement
        // For simplicity, the code below assumes you have a method to convert ImageProxy to byte array
        byte[] imageData = convertImageProxyToByteArray(imageProxy);

        // Create a MultipartBody.Part from the image data
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "image.jpg",
                RequestBody.create(MediaType.parse("image/*"), imageData));

        // Make the API request
        Call<MeasurementResponse> call = objectApi.measureObject(imagePart);
        call.enqueue(new Callback<MeasurementResponse>() {
            @Override
            public void onResponse(Call<MeasurementResponse> call, Response<MeasurementResponse> response) {
                if (response.isSuccessful()) {
                    MeasurementResponse measurementResponse = response.body();
                    updateUI(measurementResponse);
                } else {
                    // Handle API error
                }
            }

            @Override
            public void onFailure(Call<MeasurementResponse> call, Throwable t) {
                // Handle network failure
            }
        });

        // Close the imageProxy
        imageProxy.close();
    }

    private void updateUI(MeasurementResponse measurementResponse) {
        runOnUiThread(() -> {
            TextView heightTextView = findViewById(R.id.heightTextView);
            TextView widthTextView = findViewById(R.id.widthTextView);
            TextView weightTextView = findViewById(R.id.weightTextView);

            heightTextView.setText("Height: " + measurementResponse.getHeight() + " cm");
            widthTextView.setText("Width: " + measurementResponse.getWidth() + " cm");
            weightTextView.setText("Weight: " + measurementResponse.getWeight() + " kg");
        });
    }

    private byte[] convertImageProxyToByteArray(ImageProxy imageProxy) {
        // Implement the logic to convert ImageProxy to byte array
        // For simplicity, this example assumes you have a method for this purpose
        // You may need to use ImageProxy.getPlanes() to access pixel data
        return new byte[0];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
