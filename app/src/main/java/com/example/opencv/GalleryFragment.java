package com.example.opencv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class GalleryFragment extends Fragment {
    private ActivityResultLauncher<Intent> mGetContent;
    private ImageAdapter adapter;
    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private GalleryViewModel viewModel;

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        Button uploadButton = view.findViewById(R.id.btn_upload_photos);
        Button processButton = view.findViewById(R.id.btn_process_photos);
        Button resetButton = view.findViewById(R.id.btn_reset_db);

        mGetContent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                viewModel.insertImage(imageUri.toString());
                            }
                        } else if (data.getData() != null) {
                            Uri imageUri = data.getData();
                            viewModel.insertImage(imageUri.toString());
                        }
                        updateImageUris();
                        Toast.makeText(getContext(), "Images selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        viewModel = new GalleryViewModel(((MainActivity) getActivity()).getDatabase());
        viewModel.deleteAllImages();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new ImageAdapter(imageUris);
        recyclerView.setAdapter(adapter);

        uploadButton.setOnClickListener(v -> uploadPhotos());
        processButton.setOnClickListener(v -> processPhotos());
        resetButton.setOnClickListener(v -> resetDbPCA());

        return view;
    }

    private void resetDbPCA() {
        if(viewModel != null){
            viewModel.deleteAllImages();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setTrained(false);
            }
            Toast.makeText(getContext(), "Database reset", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPhotos() {
        if (viewModel == null) {
            throw new IllegalStateException("ViewModel is not initialized");
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        mGetContent.launch(intent);
    }

    private boolean isFileDifferent(InputStream assetInputStream, File fileOnDisk) throws IOException {
        String assetHash = calculateHash(assetInputStream);
        String fileOnDiskHash = calculateHash(Files.newInputStream(fileOnDisk.toPath()));
        return !assetHash.equals(fileOnDiskHash);
    }

    private String calculateHash(InputStream inputStream) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        Formatter formatter = new Formatter();
        for (byte b : digest.digest()) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private void processPhotos() {
        new Thread(() -> {
            if(viewModel == null){
                throw new IllegalStateException("ViewModel is not initialized");
            }

            if(imageUris.size() == 0){
                showOnUiThread("No images selected");
                return;
            }

            List<Mat> imageMats = new ArrayList<>();
            for (Uri uri : imageUris) {
                try {
                    if (getContext() == null) {
                        throw new IllegalStateException("Context is null for processing photos");
                    }
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    Mat mat = new Mat();
                    Utils.bitmapToMat(bitmap, mat);
                    imageMats.add(mat);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            long[] imageMatsAddresses = new long[imageMats.size()];
            for (int i = 0; i < imageMats.size(); i++) {
                imageMatsAddresses[i] = imageMats.get(i).getNativeObjAddr();
            }

            try {
                if(getContext() == null){
                    throw new IllegalStateException("Context is null");
                }

                File cascadeDir = getContext().getDir("cascade", Context.MODE_PRIVATE);
                File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

                if (!cascadeFile.exists() || isFileDifferent(getContext().getAssets().open("haarcascade_frontalface_default.xml"), cascadeFile)) {
                    InputStream is = getContext().getAssets().open("haarcascade_frontalface_default.xml");
                    FileOutputStream os = new FileOutputStream(cascadeFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();
                }

                NativeClass.train(imageMatsAddresses, imageMats.size(), cascadeFile.getAbsolutePath());

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setTrained(true);
                }

                showOnUiThread("Training complete");

            } catch (IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    showOnUiThread("Error training");
                    getActivity().finish();
                }
            }
        }).start();
    }

    private void showOnUiThread(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
        }
    }

    private void updateImageUris() {
        viewModel.getAllImages().observe(getViewLifecycleOwner(), images -> {
            imageUris.clear();
            for (ImageEntity image : images) {
                imageUris.add(Uri.parse(image.getImagePath()));
            }
            adapter.notifyDataSetChanged();
            Log.d("GalleryFragment", "updateImageUris: " + imageUris.size());
        });
    }
}
