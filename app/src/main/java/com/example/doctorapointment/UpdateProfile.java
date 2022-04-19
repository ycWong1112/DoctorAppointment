package com.example.doctorapointment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class UpdateProfile extends AppCompatActivity {

    private ImageView backEditProfileUser, editImageUser;
    private TextView updateUserTV, userNameTV, userEmailTV, userEditGetLocationTV;
    private EditText editUserUserName, editUserPhone, editUserAddress;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    //DatePickerDialog.OnDateSetListener userBirthDatesSetListerner;

    private ArrayList<String> arrayList = new ArrayList<>();

    //permission category
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 500;

    private ResultReceiver resultReceiver;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked URL
    private Uri imageUserUri;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        backEditProfileUser = findViewById(R.id.backEditProfileUser);
        editImageUser = findViewById(R.id.editImageUser);
        updateUserTV = findViewById(R.id.updateUserTV);
        userNameTV = findViewById(R.id.userNameTV);
        userEmailTV = findViewById(R.id.userEmailTV);
        editUserUserName = findViewById(R.id.editUserUserNameET);
        editUserPhone = findViewById(R.id.editUserPhoneET);
        editUserAddress = findViewById(R.id.editUserAddressET);
        userEditGetLocationTV = findViewById(R.id.userEditGetLocationTV);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        resultReceiver = new AddressResultReceiver(new Handler());

        checkUsers();

        backEditProfileUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editImageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        updateUserTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

        userEditGetLocationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UpdateProfile.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    getCurrentLocation();
                }
            }
        });
    }

    private void checkUsers() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(UpdateProfile.this, MainActivity.class));
            finish();
        } else {
            userInfo();
        }
    }

    private void userInfo() {
        DatabaseReference databaseReference = firebaseDatabase.getReference("User Type");
        databaseReference.orderByChild("accountID").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String accountID = "" + dataSnapshot.child("accountID").getValue();
                            String accountType = "" + dataSnapshot.child("accountType").getValue();
                            String online = "" + dataSnapshot.child("online").getValue();
                            String profileImage = "" + dataSnapshot.child("userImage").getValue();
                            String userName = "" + dataSnapshot.child("userName").getValue();
                            String userEmail = "" + dataSnapshot.child("userEmail").getValue();
                            String phoneNo = "" + dataSnapshot.child("userPhone").getValue();
                            String userAddress = "" + dataSnapshot.child("userAddress").getValue();

                            userNameTV.setText(userName);
                            userEmailTV.setText(userEmail);
                            editUserUserName.setText(userName);
                            editUserPhone.setText(phoneNo);
                            editUserAddress.setText(userAddress);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person).into(editImageUser);
                            } catch (Exception e) {
                                editImageUser.setImageResource(R.drawable.ic_baseline_person);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showImagePickDialog() {
        //option to display in dialog
        String[] options = {"Camera", "Gallery"};

        //dialog
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                //permission granted
                                pickFromCamera();
                            } else {
                                //permission not granted, request
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (checkStoragePermission()) {
                                pickFromGallery();
                            } else {
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        // using media store to pick high/original quality image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "ImageDescription");

        imageUserUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUserUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result; // return true or false
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        // both permission granted
                        pickFromCamera();
                    } else {
                        //both or one of permission denied
                        Toast.makeText(this, "Camera & Storage permissions are required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission granted
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case REQUEST_CODE_LOCATION_PERMISSION: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getCurrentLocation();
                    } else {
                        Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getCurrentLocation() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(UpdateProfile.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(UpdateProfile.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                            Location location = new Location("providerNA");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            fetchAddressFromLatLong(location);
                        }

                    }
                }, Looper.getMainLooper());
    }

    private void fetchAddressFromLatLong(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressConstants.RECEIVER, resultReceiver);
        intent.putExtra(FetchAddressConstants.LOCATION_DATA_EXTRA, location);
        startService(intent);

    }

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == FetchAddressConstants.SUCCESS_RESULT) {
                editUserAddress.setText(resultData.getString(FetchAddressConstants.RESULT_DATA_KEY));
            } else {
                Toast.makeText(UpdateProfile.this, resultData.getString(FetchAddressConstants.RESULT_DATA_KEY), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //handle image pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image picked from gallery

                //save picked image Uri
                imageUserUri = data.getData();

                //set image
                editImageUser.setImageURI(imageUserUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // image picked from camera
                editImageUser.setImageURI(imageUserUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String userName, phoneNo, userAddress;

    private void inputData() {
        userName = editUserUserName.getText().toString();
        phoneNo = editUserPhone.getText().toString();
        userAddress = editUserAddress.getText().toString();

        if (userName.isEmpty() && phoneNo.isEmpty() && userAddress.isEmpty() && imageUserUri == null) {
            Toast.makeText(this, "Please enter all the details.", Toast.LENGTH_SHORT).show();
            return; // don't need to proceed further
        }
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Please enter your full name.", Toast.LENGTH_SHORT).show();
            return; // don't need to proceed further
        }
        if (TextUtils.isEmpty(phoneNo)) {
            Toast.makeText(this, "Phone number is required.", Toast.LENGTH_SHORT).show();
            return; // don't need to proceed further
        }
        if (TextUtils.isEmpty(userAddress)) {
            Toast.makeText(this, "Address is required.", Toast.LENGTH_SHORT).show();
            return; // don't need to proceed further
        }

        updateData();
    }

    private void updateData() {
        progressDialog.setMessage("Updating Data...");
        progressDialog.show();

        if (imageUserUri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userName", "" + userName);
            hashMap.put("userPhone", "" + phoneNo);
            hashMap.put("userAddress", "" + userAddress);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);

            //update to firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User Type");
            databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //updated
                            progressDialog.dismiss();
                            Toast.makeText(UpdateProfile.this, "Profile updated.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to update
                            progressDialog.dismiss();
                            Toast.makeText(UpdateProfile.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            StorageReference imageReference = storageReference.child(firebaseAuth.getUid()).child("User Image");
            imageReference.putFile(imageUserUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get uri of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                //image uri received, setup data to firebase
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("userImage", "" + downloadImageUri);
                                hashMap.put("userName", "" + userName);
                                hashMap.put("userPhone", "" + phoneNo);
                                hashMap.put("userAddress", "" + userAddress);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);

                                //update to firebase
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User Type");
                                databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //updated
                                                progressDialog.dismiss();
                                                Toast.makeText(UpdateProfile.this, "Data updated.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to update
                                                progressDialog.dismiss();
                                                Toast.makeText(UpdateProfile.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UpdateProfile.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}