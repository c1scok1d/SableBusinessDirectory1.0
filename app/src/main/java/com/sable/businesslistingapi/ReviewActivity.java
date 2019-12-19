package com.sable.businesslistingapi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ReviewActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String PHOTOS_KEY = "easy_image_photos_list";
    private static final int CHOOSER_PERMISSIONS_REQUEST_CODE = 7459;
    private static final int CAMERA_REQUEST_CODE = 7500;
    private static final int CAMERA_VIDEO_REQUEST_CODE = 7501;
    private static final int GALLERY_REQUEST_CODE = 7502;
    private static final int DOCUMENTS_REQUEST_CODE = 7503;
    protected RecyclerView recyclerView;
    protected View galleryButton;
    RatingBar ratingBar;
    TextView mRatingScale;
    EditText etFeedBack;
    Button mSendFeedback;
    TextView tvFeatured, tvStatus, tvState,
            tvStreet, tvCity, tvZip, tvCountry, tvRating, tvId, tvEmail, tvWebsite, tvTwitter, tvFacebook,
            tvVideo, tvHours, tvIsOpen, tvLink, tvContent, tvPhone, tvBldgno, tvLatitude, tvLongitude, tvRatingCount, tvCategory, tvName, tvFirstRate, tvDistance;
    ImageView logo, ivFeaturedImage;
    RatingBar simpleRatingBar;
    String title, content, city, state, zipcode, country, link, baseURL = "https://www.thesablebusinessdirectory.com", username = "android_app",
            password = "mroK zH6o wOW7 X094 MTKy fwmY", status = "approved", post_type = "business", todayRange, isOpen;
    Double latitude, longitude;
    Integer category, id, rating;
    float newRating;
    //public static List<BusinessListings> mListPost;
    //private ProgressDialog pDialog;


    private ProgressBar pDialog;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private ImagesAdapter imagesAdapter;
    private ArrayList<MediaFile> photos = new ArrayList<>();
    //Map<String, RequestBody> parts = new HashMap<>();
    ArrayList<ListingsModel> locationReview = new ArrayList<>();
    ArrayList<ListingsModel> locationMatch = new ArrayList<>();
    ArrayList<ListingsAddModel> locationAdd = new ArrayList<>();


    private EasyImage easyImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Picasso.Builder builder = new Picasso.Builder(this);
        pDialog = new ProgressBar(this);

        ratingBar = findViewById(R.id.ratingBar);
        mRatingScale = findViewById(R.id.tvRatingScale);
        etFeedBack = findViewById(R.id.etFeedBack);
        mSendFeedback = findViewById(R.id.btnSubmit);
        simpleRatingBar = findViewById(R.id.simpleRatingBar);
        tvId = findViewById(R.id.tvId);
        tvBldgno = findViewById(R.id.tvBldgNo);
        tvState = findViewById(R.id.tvState);
        tvStreet = findViewById(R.id.tvStreet);
        tvCity = findViewById(R.id.tvCity);
        tvZip = findViewById(R.id.tvZip);
        tvCountry = findViewById(R.id.tvCountry);
        tvHours = findViewById(R.id.tvHours);
        tvIsOpen = findViewById(R.id.tvIsOpen);
        tvContent = findViewById(R.id.tvContent);
        tvPhone = findViewById(R.id.tvPhone);
        tvFirstRate = findViewById(R.id.tvFirstRate);
        tvCategory = findViewById(R.id.tvCategory);
        tvEmail = findViewById(R.id.tvEmail);
        tvWebsite = findViewById(R.id.tvWebsite);
        tvTwitter = findViewById(R.id.tvTwitter);
        tvFacebook = findViewById(R.id.tvFacebook);
        tvName = findViewById(R.id.tvName);
        ivFeaturedImage = findViewById(R.id.ivFeaturedImage);
        //rating = findViewById(R.id.simpleRatingBar);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvIsOpen = findViewById(R.id.tvIsOpen);
        tvDistance = findViewById(R.id.tvDistance);
        tvFeatured = findViewById(R.id.tvFeatured);
        tvLink = findViewById(R.id.tvLink);
        tvStatus = findViewById(R.id.tvStatus);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        pDialog = findViewById(R.id.progressBar);
        tvProgressStatus = findViewById(R.id.tvProgressStatus);

        pDialog.setVisibility(View.GONE);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                mRatingScale.setText(String.valueOf(v));
                switch ((int) ratingBar.getRating()) {
                    case 1:
                        mRatingScale.setText("Very bad");
                        break;
                    case 2:
                        mRatingScale.setText("Need some improvement");
                        break;
                    case 3:
                        mRatingScale.setText("Good");
                        break;
                    case 4:
                        mRatingScale.setText("Great");
                        break;
                    case 5:
                        mRatingScale.setText("Awesome. I love it");
                        break;
                    default:
                        mRatingScale.setText("");
                }
                rating = ((int) ratingBar.getRating());
            }
        });


        /**
         *
         * gets Extras if stored lat/lng equals current lat/lng (onLocationMatch)
         *
         */
        locationMatch = this.getIntent().getExtras().getParcelableArrayList("locationMatch");
        locationAdd = this.getIntent().getExtras().getParcelableArrayList("locationAdd");
        locationReview= this.getIntent().getExtras().getParcelableArrayList("locationReview");

        if (locationMatch != null) {

            tvName.setText(locationMatch.get(0).title);
            tvCategory.setText(locationMatch.get(0).category);
            builder.build().load(locationMatch.get(0).featured_image).into(ivFeaturedImage);
            tvBldgno.setText(locationMatch.get(0).bldgno);
            tvStreet.setText(locationMatch.get(0).street);
            tvCity.setText(locationMatch.get(0).city);
            tvState.setText(locationMatch.get(0).state);
            tvCountry.setText(locationMatch.get(0).country);
            tvZip.setText(locationMatch.get(0).zipcode);
            simpleRatingBar.setNumStars(locationMatch.get(0).rating);
            tvRatingCount.setText(String.valueOf(locationMatch.get(0).ratingCount));
            tvPhone.setText(locationMatch.get(0).phone);
            tvEmail.setText(locationMatch.get(0).email);
            tvWebsite.setText(locationMatch.get(0).website);
            tvTwitter.setText(locationMatch.get(0).twitter);
            tvFacebook.setText(locationMatch.get(0).facebook);
            //tvVideo.setText(locationMatch.get(0).video);
            tvHours.setText(locationMatch.get(0).hours);
            tvIsOpen.setText(locationMatch.get(0).isOpen);
            tvContent.setText(locationMatch.get(0).content);
            tvLink.setText(locationMatch.get(0).link);
            tvLatitude.setText(String.valueOf(locationMatch.get(0).latitude));
            tvLongitude.setText(String.valueOf(locationMatch.get(0).longitude));
            tvId.setText(String.valueOf(locationMatch.get(0).id));
            tvStatus.setText(locationMatch.get(0).status);


            Location locationA = new Location("point A");
            locationA.setLatitude(locationMatch.get(0).latitude); //listing lat
            locationA.setLongitude(locationMatch.get(0).longitude); //listing lng


            Location locationB = new Location("point B");
            locationB.setLatitude(MainActivity.latitude); //device lat
            locationB.setLongitude(MainActivity.longitude); //device lng

            double distance = (locationA.distanceTo(locationB) * 0.000621371192); //convert meters to miles
            tvDistance.setText(String.format(Locale.US, "%10.2f", distance));
            if(locationMatch.get(0).isOpen == "Closed now"){
                tvIsOpen.setTextColor(Color.rgb(255, 0, 0 )); //red
            }
            if(locationMatch.get(0).featured.equals(true)){
                String featured = "FEATURED";
                tvFeatured.setText(featured);
                tvFeatured.setTextColor(Color.rgb(255, 128, 0 )); //red
            }

            if (locationMatch.get(0).ratingCount == 0){
                String FirstRate = "Be the first to rate";
                tvFirstRate.setText(FirstRate);
                //tvFirstRate.setTextColor(Color.rgb(0, 255, 0)); //orange
            }

        } else if (locationAdd!= null) {
            tvName.setText(locationAdd.get(0).name);
            tvCategory.setText(locationAdd.get(0).category);
            tvBldgno.setText(locationAdd.get(0).bldgNo);
            tvStreet.setText(locationAdd.get(0).street);
            tvCity.setText(locationAdd.get(0).city);
            tvState.setText(locationAdd.get(0).state);
            tvCountry.setText(locationAdd.get(0).country);
            tvZip.setText(locationAdd.get(0).zipcode);
            tvPhone.setText(locationAdd.get(0).phone);
            tvEmail.setText(locationAdd.get(0).email);
            tvWebsite.setText(locationAdd.get(0).website);
            tvTwitter.setText(locationAdd.get(0).twitter);
            tvFacebook.setText(locationAdd.get(0).facebook);
            tvContent.setText(locationAdd.get(0).description);
            category = locationAdd.get(0).addCategory;
            link = locationAdd.get(0).link;
            latitude = locationAdd.get(0).latitude;
            longitude = locationAdd.get(0).longitude;
        } else {
            tvName.setText(locationReview.get(0).title);
            tvCategory.setText(locationReview.get(0).category);
            builder.build().load(locationReview.get(0).featured_image).into(ivFeaturedImage);
            tvBldgno.setText(locationReview.get(0).bldgno);
            tvStreet.setText(locationReview.get(0).street);
            tvCity.setText(locationReview.get(0).city);
            tvState.setText(locationReview.get(0).state);
            tvCountry.setText(locationReview.get(0).country);
            tvZip.setText(locationReview.get(0).zipcode);
            simpleRatingBar.setNumStars(locationReview.get(0).rating);
            tvRatingCount.setText(String.valueOf(locationReview.get(0).ratingCount));
            tvPhone.setText(locationReview.get(0).phone);
            tvEmail.setText(locationReview.get(0).email);
            tvWebsite.setText(locationReview.get(0).website);
            tvTwitter.setText(locationReview.get(0).twitter);
            tvFacebook.setText(locationReview.get(0).facebook);
//            tvVideo.setText(locationReview.get(0).video);
            tvHours.setText(locationReview.get(0).hours);
            tvIsOpen.setText(locationReview.get(0).isOpen);
            tvContent.setText(locationReview.get(0).content);
            tvLink.setText(locationReview.get(0).link);
            tvLatitude.setText(String.valueOf(locationReview.get(0).latitude));
            tvLongitude.setText(String.valueOf(locationReview.get(0).longitude));
            tvId.setText(String.valueOf(locationReview.get(0).id));
            tvStatus.setText(locationReview.get(0).status);

            Location locationA = new Location("point A");
            locationA.setLatitude(locationReview.get(0).latitude); //listing lat
            locationA.setLongitude(locationReview.get(0).longitude); //listing lng


            Location locationB = new Location("point B");
            locationB.setLatitude(MainActivity.latitude); //device lat
            locationB.setLongitude(MainActivity.longitude); //device lng

            double distance = (locationA.distanceTo(locationB) * 0.000621371192); //convert meters to miles
            tvDistance.setText(String.format(Locale.US, "%10.2f", distance));
            if(locationReview.get(0).isOpen =="Closed now"){
                tvIsOpen.setTextColor(Color.rgb(255, 0, 0 )); //red
            }
            if(locationReview.get(0).featured.equals(true)){
                String featured = "FEATURED";
                tvFeatured.setText(featured);
                tvFeatured.setTextColor(Color.rgb(255, 128, 0 )); //orange
            }

            if (locationReview.get(0).ratingCount == 0){
                String FirstRate = "Be the first to rate";
                tvFirstRate.setText(FirstRate);
                //tvFirstRate.setTextColor(Color.rgb(22, 53, 64)); //green
            }
        }

        findViewById(R.id.tvName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvName.setCursorVisible(true);
                tvName.setFocusableInTouchMode(true);
                tvName.setInputType(InputType.TYPE_CLASS_TEXT);
                tvName.requestFocus(); //to trigger the soft input
            }
        });

        findViewById(R.id.tvPhone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvPhone.setCursorVisible(true);
                tvPhone.setFocusableInTouchMode(true);
                tvPhone.setInputType(InputType.TYPE_CLASS_TEXT);
                tvPhone.requestFocus(); //to trigger the soft input
            }
        });

        findViewById(R.id.tvWebsite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvWebsite.setCursorVisible(true);
                tvWebsite.setFocusableInTouchMode(true);
                tvWebsite.setInputType(InputType.TYPE_CLASS_TEXT);
                tvWebsite.requestFocus(); //to trigger the soft input
            }
        });

        findViewById(R.id.tvEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvEmail.setCursorVisible(true);
                tvEmail.setFocusableInTouchMode(true);
                tvEmail.setInputType(InputType.TYPE_CLASS_TEXT);
                tvEmail.requestFocus(); //to trigger the soft input
            }
        });

        findViewById(R.id.tvTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvTwitter.setCursorVisible(true);
                tvTwitter.setFocusableInTouchMode(true);
                tvTwitter.setInputType(InputType.TYPE_CLASS_TEXT);
                tvTwitter.requestFocus(); //to trigger the soft input
            }
        });

        findViewById(R.id.tvFacebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvFacebook.setCursorVisible(true);
                tvFacebook.setFocusableInTouchMode(true);
                tvFacebook.setInputType(InputType.TYPE_CLASS_TEXT);
                tvFacebook.requestFocus(); //to trigger the soft input
            }
        });

        findViewById(R.id.tvContent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvContent.setCursorVisible(true);
                tvContent.setFocusableInTouchMode(true);
                tvContent.setInputType(InputType.TYPE_CLASS_TEXT);
                tvContent.requestFocus(); //to trigger the soft input
            }
        });


        mSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etFeedBack.getText().toString().isEmpty()) {
                    Toast.makeText(ReviewActivity.this, "Please fill in feedback text box", Toast.LENGTH_LONG).show();
                } else {

                    submitData();
                    Intent mainActivity = new Intent(view.getContext(), MainActivity.class);
                    startActivity(mainActivity);
                }
            }
        });

        recyclerView = findViewById(R.id.imageAddRecycler);

        if (savedInstanceState != null) {
            photos = savedInstanceState.getParcelableArrayList(PHOTOS_KEY);
        }

        imagesAdapter = new ImagesAdapter(this, photos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(imagesAdapter);

        easyImage = new EasyImage.Builder(this)
                .setChooserTitle("Pick media")
                .setCopyImagesToPublicGalleryFolder(false)
//                .setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setFolderName("EasyImage sample")
                .allowMultiple(true)
                .build();

        checkGalleryAppAvailability();


        findViewById(R.id.gallery_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */
                String[] necessaryPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (arePermissionsGranted(necessaryPermissions)) {
                    easyImage.openGallery(ReviewActivity.this);
                } else {
                    requestPermissionsCompat(necessaryPermissions, GALLERY_REQUEST_CODE);
                }
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PHOTOS_KEY, photos);
    }

    private void checkGalleryAppAvailability() {
        if (!easyImage.canDeviceHandleGallery()) {
            //Device has no app that handles gallery intent
            galleryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CHOOSER_PERMISSIONS_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openChooser(ReviewActivity.this);
        } else if (requestCode == CAMERA_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openCameraForImage(ReviewActivity.this);
        } else if (requestCode == CAMERA_VIDEO_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openCameraForVideo(ReviewActivity.this);
        } else if (requestCode == GALLERY_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openGallery(ReviewActivity.this);
        } else if (requestCode == DOCUMENTS_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openDocuments(ReviewActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                    @Override
                    public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                        for (MediaFile imageFile : imageFiles) {
                            Log.d("EasyImage", "Image file returned: " + imageFile.getFile().toString());
                        }
                        onPhotosReturned(imageFiles);
                    }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });
    }

    private void onPhotosReturned(@NonNull MediaFile[] returnedPhotos) {
        photos.addAll(Arrays.asList(returnedPhotos));
        imagesAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(photos.size() - 1);
        uploadFiles(returnedPhotos);


    }

    File[] filesToUpload;

    public void uploadFiles(@NonNull MediaFile[] returnedPhotos){
        photos.addAll(Arrays.asList(returnedPhotos));

        filesToUpload = new File[photos.size()];

        for(int i=0; i< photos.size(); i++){
            filesToUpload[i] = new File(photos.get(i).getFile().toString());
        }
        pDialog.setVisibility(View.VISIBLE);
        FileUploader fileUploader = new FileUploader();
        fileUploader.uploadFiles("wp-json/wp/v2/media", "file", filesToUpload, new FileUploader.FileUploaderCallback() {
            @Override
            public void onError() {

                pDialog.setVisibility(View.GONE);
            }
            String foo;

            @Override
            public void onFinish(String[] responses) {
                pDialog.setVisibility(View.GONE);
                tvProgressStatus.setText("Media upload complete!");
                for(int i=0; i< responses.length; i++){
                    try {
                        final JSONObject obj = new JSONObject(responses[i]);
                        final JSONObject geodata = obj.getJSONObject("guid");
                        foo = geodata.getString("rendered");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    filesToUploadfoo.add(foo);
                    Log.e("RESPONSE "+i, responses[i]);
                }
            }

            @Override
            public void onProgressUpdate(int currentpercent, int totalpercent, int filenumber) {
                updateProgress(totalpercent);
                Log.e("Progress Status", currentpercent+" "+totalpercent+" "+filenumber);
            }
        });
    }

    public void updateProgress(int val){
        pDialog.setProgress(val);
        tvProgressStatus.setText("Uploading media ..."+ val+"%");
    }

    private TextView tvProgressStatus;


    private boolean arePermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        }
        return true;
    }

    private void requestPermissionsCompat(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(ReviewActivity.this, permissions, requestCode);
    }

    StringBuilder sb = new StringBuilder();
    String main;

    ArrayList<String> filesToUploadfoo = new ArrayList<>();
    private void submitData() {

        int nestingDepth = 0;

        for (int i = 0; i < filesToUploadfoo.size(); i++) {
            sb.append(filesToUploadfoo.get(i)).append("::");
            nestingDepth ++;
        }
        // Append last element due to special casing
        sb.append(filesToUploadfoo.get(0));
        for (int i = 0; i < nestingDepth; i++) {
            sb.append(')');
        }

        main = sb.toString();

        //Add the interceptor to the client builder.
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(username, password))
                .addInterceptor(logging)
                .build();

        //Defining retrofit api service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
        Call<List<ListReviewActivity>> call = service.postReview(
                Integer.valueOf(tvId.getText().toString()),
               rating,
                /*2,*/ etFeedBack.getText().toString(), main);

        call.enqueue(new Callback<List<ListReviewActivity>>() {
            @Override
            public void onResponse(Call<List<ListReviewActivity>> call, Response<List<ListReviewActivity>> response) {
                Log.e("ReviewActivity", " response " + response.body().get(0).getId());
                Log.e("ReviewActivity", " response " + response.body().get(0).getCity());
                Log.e("ReviewActivity", " response " + response.body().get(0).getRegion());
                Log.e("ReviewActivity", " response " + response.body().get(0).getRating().getLabel());
                Log.e("ReviewActivity", " response " + response.body().get(0).getRating().getRating());
                Log.e("ReviewActivity", " response " + response.body().get(0).getImages().getRendered().get(0).getThumbnail());
                Log.e("ReviewActivity", " response " + response.body().get(0).getContent());



//                progressBar.setVisibility(View.GONE); //hide progressBar
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            "Post Updated Title: " + response.body().get(0).getId() +
                                    " Body: " + response.body().get(0).getContent() +
                                    " PostId: " + response.body().get(0).getId(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<List<ListReviewActivity>> call, Throwable t) {
//                progressBar.setVisibility(View.GONE); //hide progressBar
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }

        });

        Toast.makeText(ReviewActivity.this, "Thank you for sharing your feedback", Toast.LENGTH_SHORT).show();


    }

    public class BasicAuthInterceptor implements Interceptor {

        private String credentials;

        public BasicAuthInterceptor(String user, String password) {
            this.credentials = Credentials.basic(user, password);
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder()
                    .header("Authorization", credentials).build();
            return chain.proceed(authenticatedRequest);
        }

    }
}