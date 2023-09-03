package com.example.whatsappshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 123; // You can choose any value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if permissions are granted
        if (checkPermissions()) {
            // Permissions are already granted, you can proceed with your app's logic
        } else {
            // Request permissions
            requestPermissions();
        }

        Button pickupImage = findViewById(R.id.pickImageButton);
        pickupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(v);
            }
        });

        Button whatsappShare = findViewById(R.id.washare);
        whatsappShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Update the authorities string with your app's package name
                String authorities = getPackageName() + ".fileprovider";




                // File sourceImageFile = new File("/data/user/0/com.example.whatsappshare/files/your_image.png");
                List<File> files = getFilesInDirectory("/data/user/0/com.example.whatsappshare/files");

//                // Add URIs of the files you want to share
//                File externalImageFile1 = new File(getExternalFilesDir(null), "1.png");
//                File externalImageFile2 = new File(getExternalFilesDir(null), "2.png");
//
//
//                try{
//                    copyFile(files.get(1), externalImageFile1);
//                    copyFile(files.get(2), externalImageFile2);
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }

                // Create a content URI for the files using FileProvider
                Uri uri1 = FileProvider.getUriForFile(getApplicationContext(), authorities, files.get(1));
                Uri uri2 = FileProvider.getUriForFile(getApplicationContext(), authorities, files.get(2));

                // Create an ArrayList to store the URIs of the files to share
                ArrayList<Uri> imageUris = new ArrayList<>();
                imageUris.add(uri1);
                imageUris.add(uri2);

                // Create an Intent to share multiple files via WhatsApp
                String phoneNumber = "972526297003";

                // Create an Intent to open WhatsApp with the specific contact
                Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber);

                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putExtra("jid", phoneNumber + "@s.whatsapp.net");

                shareIntent.setType("image/*");
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                shareIntent.setPackage("com.whatsapp"); // Specify WhatsApp package name
//                shareIntent.setPackage("com.whatsapp.w4b");

                // Grant temporary permission to read the content URI
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, "Share images with"));
            }
        });


        TextView countLabel = findViewById(R.id.textView);
        List<File> files = getFilesInDirectory("/data/user/0/com.example.whatsappshare/files");
        int files_cnt = files.size();
        countLabel.setText(String.valueOf(files_cnt));
    }

        public static void copyFile(File sourceFile, File destFile) throws IOException {
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }

            FileChannel sourceChannel = null;
            FileChannel destChannel = null;

            try {
                sourceChannel = new FileInputStream(sourceFile).getChannel();
                destChannel = new FileOutputStream(destFile).getChannel();
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            } finally {
                if (sourceChannel != null) {
                    sourceChannel.close();
                }
                if (destChannel != null) {
                    destChannel.close();
                }
            }
        }

        public void pickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            // Save the selected image to app's private storage
            saveImageToPrivateStorage(selectedImageUri);

            // Do something with the selected image URI, e.g., display it in an ImageView
            // ImageView imageView = findViewById(R.id.imageView);
            // imageView.setImageURI(selectedImageUri);
        }
    }

    public static List<File> getFilesInDirectory(String directoryPath) {
        List<File> fileList = new ArrayList<>();

        File directory = new File(directoryPath);

        // Check if the directory exists
        if (directory.exists() && directory.isDirectory()) {
            // List all files in the directory
            File[] files = directory.listFiles();

            // Add each file to the list
            if (files != null) {
                for (File file : files) {
                    fileList.add(file);
                }
            }
        }

        return fileList;
    }

    private void saveImageToPrivateStorage(Uri imageUri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);

            // Create a unique filename for the saved image
            String filename = "image_" + System.currentTimeMillis() + ".jpg";

            // Get the directory for app's private storage
            File directory = getFilesDir();

            // Create a file for the image
            File imageFile = new File(directory, filename);

            // Save the bitmap to the file
            OutputStream os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
            os.flush();
            os.close();

            // Now, 'imageFile' contains the saved image

            // If you want to display the saved image in an ImageView:
            // ImageView imageView = findViewById(R.id.imageView);
            // imageView.setImageURI(Uri.fromFile(imageFile));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Permissions handling
    private boolean checkPermissions() {
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    // Handle the result of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, you can now proceed with your app's logic
            } else {
                // Permissions denied, handle accordingly (e.g., show an explanation or disable functionality)
            }
        }
    }

}