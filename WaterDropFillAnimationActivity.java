package com.example.aquatech;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WaterDropFillAnimationActivity extends AppCompatActivity {

    private WaterDropView waterWaveView;
    private CircularProgressIndicator circularProgress;
    private TextView tv_to_be_continued;
    private ImageView ivCheckIcon;

    private FirebaseAuth authManager;
    private DatabaseReference requestDatabase;
    private StorageReference documentStorage;

    private String requestCode;
    private String imagePath;
    private String notes;
    private String customerName;
    private String contactNumber;
    private String fullAddress;
    private String bookingReference;
    private String serviceDate;
    private String scheduleStart;
    private String scheduleEnd;
    private String serviceCategory;
    private String equipmentModel;
    
    private double totalAmount, latitude, longitude;
    
    private int itemAlpha;
    private int itemBravo;
    private int itemCharlie;
    private int itemDelta;
    private int itemEcho;
    private int itemFoxtrot;
    private int itemGolf;
    private int itemHotel;
    private int itemIndia;
    private int itemIndigo;

    private boolean uploadCompleted = false;
    private boolean animationCompleted = false;
    private String uploadedImageLink = "";

    private ValueAnimator loadingAnimator;

    private final String databaseUrl = BuildConfig.FIREBASE_DB_URL;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_water_drop_fill);

        authManager = FirebaseAuth.getInstance();
        requestDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference("ServiceRequests");
        documentStorage = FirebaseStorage.getInstance().getReference("CustomerIDs");

        loadIntentExtras();
        initializeViews();
        startAnimations();
        geocodeAddressAndUpload();
    }

    private void loadIntentExtras() {
        Intent intent = getIntent();
        
        requestCode = intent.getStringExtra("TICKET_ID");
        imagePath = intent.getStringExtra("SELECTED_ID_URI");
        notes = intent.getStringExtra("REMARKS");
        customerName = intent.getStringExtra("CUSTOMER_NAME");
        contactNumber = intent.getStringExtra("CONTACT_NUMBER");
        fullAddress = intent.getStringExtra("ADDRESS");
        bookingReference = intent.getStringExtra("REF_NO");
        serviceDate = intent.getStringExtra("DATE");
        scheduleStart = intent.getStringExtra("START_TIME");
        scheduleEnd = intent.getStringExtra("END_TIME");
        serviceCategory = intent.getStringExtra("PURCHASE_TYPE");
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0);
        equipmentModel = intent.getStringExtra("UNIT_MODEL");
        
        latitude = intent.getDoubleExtra("LATITUDE", 0.0);
        longitude = intent.getDoubleExtra("LONGITUDE", 0.0);

        itemAlpha = intent.getIntExtra("QTY_CBC", 0);
        itemBravo = intent.getIntExtra("QTY_SEDIMENT", 0);
        itemCharlie = intent.getIntExtra("QTY_WAY_VALVE", 0);
        itemDelta = intent.getIntExtra("QTY_AQUATAL", 0);
        itemEcho = intent.getIntExtra("QTY_INLINE", 0);
        itemFoxtrot = intent.getIntExtra("QTY_UV_LAMP", 0);
        itemGolf = intent.getIntExtra("QTY_TOUCH_PANEL", 0);
        itemHotel = intent.getIntExtra("QTY_PBC_BOARD", 0);
        itemIndia = intent.getIntExtra("QTY_SMSF_1_CBC", 0);
        itemIndigo = intent.getIntExtra("QTY_SMSF_10_SED", 0);
    }

    private void initializeViews() {
        waterWaveView = findViewById(R.id.waterWaveView);
        circularProgress = findViewById(R.id.circularProgress);
        tv_to_be_continued = findViewById(R.id.tv_to_be_continued);
        ivCheckIcon = findViewById(R.id.ivCheckIcon);
        tv_to_be_continued.setTextColor(Color.parseColor("#1C4E75"));
    }

    private void startAnimations() {
        pulseAnimator = ValueAnimator.ofFloat(1.0f, 1.1f);
        pulseAnimator.setDuration(500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        pulseAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            
            if(waterWaveView != null) { 
                waterWaveView.setScaleX(scale); 
                waterWaveView.setScaleY(scale); 
            }
            if(tv_to_be_continued != null) { 
                tv_to_be_continued.setScaleX(scale); 
                tv_to_be_continued.setScaleY(scale); 
            }
        });
        pulseAnimator.start();

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        
        animator.setDuration(4000);
        animator.setInterpolator(new LinearInterpolator());
        
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();    
            if(waterWaveView != null) waterWaveView.setWaveProgress(progress);
            if(circularProgress != null) circularProgress.setProgress(progress);
        });
        
        animator.addListener(new AnimatorListenerAdapter() {
            
            @Override
            public void onAnimationEnd(Animator animation) {
                showSuccessUI();
            }
        });
        animator.start();
    }

    private void showSuccessUI() {
        if (pulseAnimator != null) pulseAnimator.cancel();
        if (circularProgress != null) circularProgress.setIndicatorColor(Color.parseColor("#4CAF50"));

        if (waterWaveView != null) {
            waterWaveView.animate()
                    .scaleX(0f).scaleY(0f).alpha(0f).setDuration(400)
                    .withEndAction(() -> {
                        waterWaveView.setVisibility(View.GONE);
                        if (ivCheckIcon != null) {
                            ivCheckIcon.setVisibility(View.VISIBLE);
                            ivCheckIcon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).start();
                        }
                        if (tv_to_be_continued != null) {
                            tv_to_be_continued.setText("Request Sent!");
                            tv_to_be_continued.setTextColor(Color.parseColor("#4CAF50"));
                        }
                        isAnimationDone = true;
                        checkReadyToNavigate();
                    }).start();
        }
    }

    private void geocodeAddressAndUpload() {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("en", "PH"));
                String searchStr = "";

                if(address != null && !address.isEmpty()){
                    searchStr = address.replace("corner", "")
                            .replace("Corner", "")
                            + ", Philippines";
                }
                
                List<Address> addresses = geocoder.getFromLocationName(searchStr, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    latitude = addresses.get(0).getLatitude();
                    longitude = addresses.get(0).getLongitude();
                }
            } catch (Exception e) {}
            runOnUiThread(this::uploadRequestToFirebase);
        }).start();
    }

    private void uploadRequestToFirebase() {
        if (savedUri != null && !savedUri.isEmpty()) {
            try {
                StorageReference fileRef = storageRef.child(ticketId + ".jpg");
                fileRef.putFile(Uri.parse(savedUri))
                        .addOnSuccessListener(ts -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            firebaseImageUrl = uri.toString();
                            pushDataToDatabase();
                        }).addOnFailureListener(e -> { firebaseImageUrl = "Upload Failed"; pushDataToDatabase(); }))
                        .addOnFailureListener(e -> { firebaseImageUrl = "Upload Failed"; pushDataToDatabase(); });
            } catch (Exception e) {
                firebaseImageUrl = "Upload Failed";
                pushDataToDatabase();
            }
        } else {
            firebaseImageUrl = "None Provided";
            pushDataToDatabase();
        }
    }

    private void pushDataToDatabase() {
        Map<String, Object> data = new HashMap<>();
        data.put("ticketId", requestCode);
        data.put("customerName", customerName);
        data.put("contactNumber", contactNumber);
        data.put("address", fullAddress);

        data.put("referenceNo", bookingReference);
        data.put("date", serviceDate);
        data.put("startTime", scheduleStart);
        data.put("endTime", scheduleEnd);
        data.put("timeRange", scheduleStart + " - " + scheduleEnd);
        data.put("purchaseType", serviceCategory);
        data.put("validIdUrl", uploadedImageLink);
        data.put("remarks", notes);
        data.put("totalAmount", totalAmount);
        data.put("unitName", equipmentModel);
        data.put("status", "Open");
        data.put("userId", authManager.getUid());
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("timestamp", ServerValue.TIMESTAMP);

        data.put("qty_cbc", itemAlpha);
        data.put("qty_sediment", itemBravo);
        data.put("qty_wayvalve", itemCharlie);
        data.put("qty_aquatal", itemDelta);
        data.put("qty_inline", itemEcho);
        data.put("qty_uvlamp", itemFoxtrot);
        data.put("qty_touchpanel", itemGolf);
        data.put("qty_pbcboard", itemHotel);
        data.put("qty_smsf1", itemIndia);
        data.put("qty_smsf10", itemIndigo);

        requestDatabase.child(ticketId).setValue(data).addOnCompleteListener(task -> {
            AdminDashboardActivity.addAdminLog("New Service Request Form submitted by " + customerName);
            isFirebaseDone = true;
            checkReadyToNavigate();
        });
    }

    private void checkReadyToNavigate() {
        if (isAnimationDone && isFirebaseDone) {
            new Handler(Looper.getMainLooper()).postDelayed(this::navigateToSuccess, 500);
        }
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(this, ServiceRequestSuccessActivity.class);
        intent.putExtra("TICKET_ID", requestCode);
        intent.putExtra("CUSTOMER_NAME", customerName);
        intent.putExtra("CONTACT_NUMBER", contactNumber);
        intent.putExtra("ADDRESS", fullAddress);
        intent.putExtra("REF_NO", bookingReference);
        intent.putExtra("TOTAL_AMOUNT", totalAmount);
        intent.putExtra("REMARKS", notes);
        intent.putExtra("DATE", serviceDate);
        intent.putExtra("VALID_ID_URL", uploadedImageLink);    
        intent.putExtra("UNIT_MODEL", equipmentModel);

        intent.putExtra("qty_wayvalve", itemCharlie);
        intent.putExtra("qty_cbc", itemAlpha);
        intent.putExtra("qty_sediment", itemBravo);
        intent.putExtra("qty_aquatal", itemDelta);
        intent.putExtra("qty_inline", itemEcho);
        intent.putExtra("qty_uvlamp", itemFoxtrot);
        intent.putExtra("qty_touchpanel", itemGolf);
        intent.putExtra("qty_pbcboard", itemHotel);
        intent.putExtra("qty_smsf1", itemIndia);
        intent.putExtra("qty_smsf10", itemIndigo);

        if (savedUri != null && !savedUri.isEmpty()) {
            intent.putExtra("SELECTED_ID_URI", savedUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(intent);
        finish();
    }
}
