package com.example.amplenoteclone.ocr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.Manifest;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanImageToNoteActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageView imageView;
    private ImageButton btnGallery;
    private Button btnScan;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean cameraGranted = result.get(Manifest.permission.CAMERA);
                        Boolean storageGranted;

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            storageGranted = result.get(Manifest.permission.READ_MEDIA_IMAGES);
                        } else {
                            storageGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }

                        if (cameraGranted != null && cameraGranted) {
                            startCamera();
                        } else if (storageGranted != null && storageGranted) {
                            openGallery();
                        }
                    });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imageView.setVisibility(ImageView.VISIBLE);
                        previewView.setVisibility(PreviewView.GONE);
                        imageView.setImageBitmap(bitmap);
                        processImageToText(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_image_to_note);

        cameraExecutor = Executors.newSingleThreadExecutor();
        initializeViews();
        setupClickListeners();
        checkCameraPermission();
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        imageView = findViewById(R.id.imageView);
        btnGallery = findViewById(R.id.btnGallery);
        btnScan = findViewById(R.id.btnScan);
    }

    private void setupClickListeners() {
        btnGallery.setOnClickListener(v -> checkStoragePermission());
        btnScan.setOnClickListener(v -> takePicture());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }
    }

    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                permissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                permissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                previewView.setVisibility(PreviewView.VISIBLE);
                imageView.setVisibility(ImageView.GONE);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePicture() {
        if (imageCapture == null) return;

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        Bitmap bitmap = imageProxyToBitmap(image);
                        imageView.setVisibility(ImageView.VISIBLE);
                        previewView.setVisibility(PreviewView.GONE);
                        imageView.setImageBitmap(bitmap);
                        processImageToText(bitmap);
                        image.close();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        Toast.makeText(ScanImageToNoteActivity.this,
                                "Error capturing image: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return rotateBitmap(bitmap, image.getImageInfo().getRotationDegrees());
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int rotation) {
        if (rotation == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            return bitmap;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void processImageToText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    String recognizedText = text.getText();
                    if (recognizedText.isEmpty()) {
                        Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show();
                    }
                    showOCRResultDialog(recognizedText);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showOCRResultDialog(String recognizedText) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("OCR Result")
                .setMessage(recognizedText)
                .setPositiveButton("Create Note", (dialog, which) -> {
                    showCreateNoteDialog(recognizedText);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCreateNoteDialog(String content) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 30, 60, 30);

        android.widget.TextView titleText = new android.widget.TextView(this);
        titleText.setText("Enter note title");
        titleText.setTextSize(20);
        titleText.setPadding(0, 0, 0, 20);
        layout.addView(titleText);

        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Title");
        input.setBackgroundResource(android.R.drawable.edit_text);
        input.setPadding(40, 30, 40, 30);
        input.setSingleLine(true);

        input.setBackground(getEditTextBackground());

        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Create", null); // We'll override this below
        builder.setNegativeButton("Cancel", null);

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) {
                input.setError("Title cannot be empty");
            } else {
                dialog.dismiss();
                saveNoteToFirestore(title, content);
            }
        });
    }

    private android.graphics.drawable.GradientDrawable getEditTextBackground() {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(android.graphics.Color.TRANSPARENT);
        drawable.setStroke(2, getResources().getColor(android.R.color.darker_gray));
        drawable.setCornerRadius(8);
        return drawable;
    }

    private void saveNoteToFirestore(String title, String content) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setUserId(userId);
        note.setCreatedAt(new Date());
        note.setUpdatedAt(new Date());
        note.setProtected(false);
        note.setTags(new ArrayList<>());
        note.setTasks(new ArrayList<>());

        db.collection("notes")
                .add(note)
                .addOnSuccessListener(documentReference -> {
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating note: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}