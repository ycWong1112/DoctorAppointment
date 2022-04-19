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
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterDoctorActivity extends AppCompatActivity {
    private ImageView userImage;
    private TextView haveAcc, getLocation;
    private EditText name, email, phoneNo, address, password, confirmPassword;
    private Button register;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

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
    private Uri UriImage;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_doctor);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        userImage = findViewById(R.id.imageUserIV);
        name = findViewById(R.id.etName);
        email = findViewById(R.id.etEmail);
        phoneNo = findViewById(R.id.etPhoneNum);
        getLocation = findViewById(R.id.userGetLocationTV);
        address = findViewById(R.id.etAddress);
        password = findViewById(R.id.etPassword);
        confirmPassword = findViewById(R.id.etConfirmPw);
        haveAcc = findViewById(R.id.tvHaveAcc);
        register = findViewById(R.id.btnRegister);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        resultReceiver = new AddressResultReceiver(new Handler());

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //detect current location
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegisterDoctorActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    getCurrentLocation();
                }
            }
        });

        haveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(RegisterDoctorActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                overridePendingTransition(0,0);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkUserValidation()){
                    //Upload data to the database
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();

                    String emailAddress = email.getText().toString().trim();
                    String passwordAcc = password.getText().toString().trim();

                    firebaseAuth.createUserWithEmailAndPassword(emailAddress,passwordAcc).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendUserEmailVerification();
                                saveUserData();
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterDoctorActivity.this, "Registration Failed! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showImagePickDialog() {
        //option to display in dialog
        String [] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if (which == 0){
                            //camera clicked
                            if (checkCameraPermission()){
                                //permission granted
                                pickFromCamera();
                            }
                            else {
                                //permission not granted, request
                                requestCameraPermission();
                            }
                        }
                        else {
                            //gallery clicked
                            if (checkStoragePermission()){
                                pickFromGallery();
                            }
                            else {
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        // using media store to pick high/original quality image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "ImageDescription");

        UriImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, UriImage);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result; // return true or false
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        // both permission granted
                        pickFromCamera();
                    }
                    else {
                        //both or one of permission denied
                        Toast.makeText(this, "Camera permissions are required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Storage permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case REQUEST_CODE_LOCATION_PERMISSION:{
                if (grantResults.length>0){
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
        LocationServices.getFusedLocationProviderClient(RegisterDoctorActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(RegisterDoctorActivity.this)
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

    private void fetchAddressFromLatLong(Location location){
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
                address.setText(resultData.getString(FetchAddressConstants.RESULT_DATA_KEY));
            }
            else {
                Toast.makeText(RegisterDoctorActivity.this, resultData.getString(FetchAddressConstants.RESULT_DATA_KEY), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //handle image pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image picked from gallery

                //save picked image Uri
                UriImage = data.getData();

                //set image
                userImage.setImageURI(UriImage);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                // image picked from camera
                userImage.setImageURI(UriImage);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String userName, userEmail, userPhoneNo, userAddress, userPassword, userConfirmPw;
    private Boolean checkUserValidation() {
        Boolean result = false;

        userName = name.getText().toString();
        userEmail = email.getText().toString();
        userPhoneNo = phoneNo.getText().toString();
        userAddress = address.getText().toString();
        userPassword = password.getText().toString();
        userConfirmPw = confirmPassword.getText().toString();

        //validate data
        if(userName.isEmpty() && userEmail.isEmpty() && userPhoneNo.isEmpty() && userAddress.isEmpty() && userPassword.isEmpty() && userConfirmPw.isEmpty()){
            Toast.makeText(this,"Please enter all the details.", Toast.LENGTH_SHORT).show();
        } else if(userName.isEmpty() || userEmail.isEmpty() || userPhoneNo.isEmpty() || userAddress.isEmpty() || userPassword.isEmpty() || userConfirmPw.isEmpty()){
            Toast.makeText(this,"Please enter all the details.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(userName)){
            Toast.makeText(this,"Please enter your full name.", Toast.LENGTH_SHORT).show();
            //name.setError("Please enter your full name.");
        }
        else if (TextUtils.isEmpty(userEmail)){
            Toast.makeText(this,"Please enter your Email address.", Toast.LENGTH_SHORT).show();
            //address.setError("Please enter your Email address.");
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            Toast.makeText(this,"Please enter Valid Email address.", Toast.LENGTH_SHORT).show();
            //address.setError("Invalid Email address.");
        }
        else if (TextUtils.isEmpty(userPhoneNo)){
            Toast.makeText(this,"Please enter your phone number.", Toast.LENGTH_SHORT).show();
            //phoneNo.setError("Please enter your phone number.");
        }
        else if (TextUtils.isEmpty(userAddress)){
            Toast.makeText(this,"Please complete your hospital address.", Toast.LENGTH_SHORT).show();
            //address.setError( "Please complete your address.");
        }
        else if (!validPassword(userPassword)) {
            Toast.makeText(RegisterDoctorActivity.this, "Password must contain special characters, upper case, lower case, number and at least 8 characters long.", Toast.LENGTH_LONG).show();
        }
        else if (!userPassword.equals(userConfirmPw)){
            Toast.makeText(this, "Password does not match.", Toast.LENGTH_SHORT).show();
        }
        else {
            result = true;
        }
        return result;
    }

    private boolean validPassword(final String password) {
        Pattern pattern;
        Matcher matcher;

        // ^ -> starting of the string
        // [0-9] -> at least 1 digit
        // [a-z] -> at least 1 lower case letter
        // [A-Z] -> at least 1 upper case letter
        //[@#$%^&+=] -> at least 1 special character
        // .{8,} -> //at least 8 characters
        // $ -> end of the string
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    private void sendUserEmailVerification() {
        progressDialog.setMessage("Sending email verification...");
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        saveUserData();
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(RegisterDoctorActivity.this, "Failed to send verification.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void saveUserData() {
        progressDialog.setMessage("Saving info...");

        StorageReference imageReference = storageReference.child(firebaseAuth.getUid()).child("User Image");
        imageReference.putFile(UriImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded, get uri of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadImageUri = uriTask.getResult();

                        if (uriTask.isSuccessful()) {

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("accountID", "" + firebaseAuth.getUid());
                            hashMap.put("accountType", "Doctor");
                            hashMap.put("online", "false");
                            hashMap.put("userName", "" + userName);
                            hashMap.put("userEmail", "" + userEmail);
                            hashMap.put("userPhone", "" + userPhoneNo);
                            hashMap.put("userAddress", "" + userAddress);
                            hashMap.put("userImage", "" + downloadImageUri);
                            hashMap.put("latitude", "" + latitude);
                            hashMap.put("longitude", "" + longitude);

                            //update to firebase
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User Type");
                            databaseReference.child(firebaseAuth.getUid()).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //updated
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterDoctorActivity.this, "Registered. Please verify your account via Email.", Toast.LENGTH_SHORT).show();
                                            firebaseAuth.signOut();
                                            startActivity(new Intent(RegisterDoctorActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //failed to update
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterDoctorActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterDoctorActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }
}