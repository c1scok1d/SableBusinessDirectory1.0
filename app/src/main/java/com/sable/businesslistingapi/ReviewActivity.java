package com.sable.businesslistingapi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


public class ReviewActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

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
    EditText mFeedback;
    Button mSendFeedback;
    ImageButton btnPic, btnPicUpload;
    TextView tvPost_title, tvPost_status, tvState,
            tvStreet, tvCity, tvZip, tvCountry, tvRating, tvEmail, tvWebsite, tvTwitter, tvFacebook,
            tvVideo, tvHours, tvIsOpen, tvContent, tvPhone, tvBldgno, tvLatitude, tvLongitude, tvTimestamp, tvPostCategory, tvName;
    ImageView ivFeaturedImage, ivImage1, ivImage02;
    RatingBar simpleRatingBar;
    String title, content, city, state, zipcode, country, link, baseURL = "https://www.thesablebusinessdirectory.com", id = "12345", username = "android_app",
            password = "mroK zH6o wOW7 X094 MTKy fwmY", status = "approved", post_type = "business";
    Double latitude, longitude;
    MultipartBody.Part image00, image01, image02, image03, image04, image05;
    Integer category;
    Float rating;
    File image;
    /**
     * submit to api
     */
    MultipartBody.Part body;
    private ProgressBar progressBar;
    private ImagesAdapter imagesAdapter;
    private ArrayList<MediaFile> photos = new ArrayList<>();
    private EasyImage easyImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Picasso.Builder builder = new Picasso.Builder(this);

        ratingBar = findViewById(R.id.ratingBar);
        mRatingScale = findViewById(R.id.tvRatingScale);
        mFeedback = findViewById(R.id.etFeedback);
        mSendFeedback = findViewById(R.id.btnSubmit);
        simpleRatingBar = findViewById(R.id.simpleRatingBar);
        //tvPost_title = findViewById(R.id.tvName);
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
        //btnPic = findViewById(R.id.btnPic);
        tvPostCategory = findViewById(R.id.tvPostCategory);
        tvEmail = findViewById(R.id.tvEmail);
        tvWebsite = findViewById(R.id.tvWebsite);
        tvTwitter = findViewById(R.id.tvTwitter);
        tvFacebook = findViewById(R.id.tvFacebook);
        tvName = findViewById(R.id.tvName);
        ivFeaturedImage = findViewById(R.id.ivFeaturedImage);
        tvRating = findViewById(R.id.tvRating);
        tvIsOpen = findViewById(R.id.tvIsOpen);

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
                rating = ratingBar.getRating();
            }
        });

        /**
         *
         * gets Extras if stored lat/lng equals current lat/lng (onLocationMatch)
         *
         */
        ArrayList<ListingsModel> locationMatch;
        ArrayList<ListingsAddModel> locationAdd;
        ArrayList<ListingsModel> locationReview;

        locationMatch = this.getIntent().getExtras().getParcelableArrayList("locationMatch");
        locationAdd = this.getIntent().getExtras().getParcelableArrayList("locationAdd");
        locationReview = this.getIntent().getExtras().getParcelableArrayList("locationReview");
        if (locationReview != null){

            tvName.setText(locationReview.get(0).title);
            tvPostCategory.setText(locationReview.get(0).category);
            builder.build().load(getIntent().getStringExtra(locationReview.get(0).featured_image)).into(ivFeaturedImage);
            tvBldgno.setText(locationReview.get(0).bldgno);
            tvStreet.setText(locationReview.get(0).street);
            tvCity.setText(locationReview.get(0).city);
            tvState.setText(locationReview.get(0).state);
            tvCountry.setText(locationReview.get(0).country);
            tvZip.setText(locationReview.get(0).zipcode);
            tvRating.setText(String.valueOf(locationReview.get(0).rating));
            tvPhone.setText(locationReview.get(0).phone);
            tvEmail.setText(locationReview.get(0).email);
            tvWebsite.setText(locationReview.get(0).website);
            tvTwitter.setText(locationReview.get(0).twitter);
            tvFacebook.setText(locationReview.get(0).facebook);
//            tvVideo.setText(locationReview.get(0).video);
            tvHours.setText(locationReview.get(0).hours);
            tvIsOpen.setText(locationReview.get(0).isOpen);
            tvContent.setText(locationReview.get(0).content);
            link = locationReview.get(0).link;
            latitude = locationReview.get(0).latitude;
            longitude = locationReview.get(0).longitude;

            if(locationReview.get(0).isOpen.equals("Closed now")){
                tvIsOpen.setTextColor(Color.rgb(255, 0, 0 )); //red
            }

        } else if (locationMatch == null) {

            tvName.setText(locationAdd.get(0).name);
            tvPostCategory.setText(locationAdd.get(0).category);
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
            latitude = locationAdd.get(0).lat;
            longitude = locationAdd.get(0).lng;

        } else {

            tvName.setText(locationMatch.get(0).title);
            tvPostCategory.setText(locationMatch.get(0).category);
            builder.build().load(getIntent().getStringExtra(locationMatch.get(0).featured_image)).into(ivFeaturedImage);
            tvBldgno.setText(locationMatch.get(0).bldgno);
            tvStreet.setText(locationMatch.get(0).street);
            tvCity.setText(locationMatch.get(0).city);
            tvState.setText(locationMatch.get(0).state);
            tvCountry.setText(locationMatch.get(0).country);
            tvZip.setText(locationMatch.get(0).zipcode);
            tvRating.setText(locationMatch.get(0).rating);
            tvPhone.setText(locationMatch.get(0).phone);
            tvEmail.setText(locationMatch.get(0).email);
            tvWebsite.setText(locationMatch.get(0).website);
            tvTwitter.setText(locationMatch.get(0).twitter);
            tvFacebook.setText(locationMatch.get(0).facebook);
            tvVideo.setText(locationMatch.get(0).video);
            tvHours.setText(locationMatch.get(0).hours);
            tvIsOpen.setText(locationMatch.get(0).isOpen);
            tvContent.setText(locationMatch.get(0).content);
            link = locationMatch.get(0).link;
            latitude = locationMatch.get(0).latitude;
            longitude = locationAdd.get(0).lng;
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
                if (mFeedback.getText().toString().isEmpty()) {
                    Toast.makeText(ReviewActivity.this, "Please fill in feedback text box", Toast.LENGTH_LONG).show();
                } else {

                    mFeedback.setText("");
                    //rating = ratingBar.getRating();
                    submitData();
                }
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        //galleryButton = findViewById(R.id.gallery_button);

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


        findViewById(R.id.camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] necessaryPermissions = new String[]{Manifest.permission.CAMERA};
                if (arePermissionsGranted(necessaryPermissions)) {
                    easyImage.openCameraForImage(ReviewActivity.this);
                } else {
                    requestPermissionsCompat(necessaryPermissions, CAMERA_REQUEST_CODE);
                }
            }
        });

        findViewById(R.id.camera_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] necessaryPermissions = new String[]{Manifest.permission.CAMERA};
                if (arePermissionsGranted(necessaryPermissions)) {
                    easyImage.openCameraForVideo(ReviewActivity.this);
                } else {
                    requestPermissionsCompat(necessaryPermissions, CAMERA_VIDEO_REQUEST_CODE);
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
                    //body = imageFile.getFile();
                    image = imageFile.getFile();
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
    }

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

    private void submitData() {

        title = tvName.getText().toString();
        status = tvPost_status.getText().toString();
        state = tvState.getText().toString();
        city = tvCity.getText().toString();
        zipcode = tvZip.getText().toString();
        country = tvCity.getText().toString();
        rating = ratingBar.getRating();

        for (int i = 00; i <=06; i++) {
            String filename = "image"+i;

            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), image);
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part part  = MultipartBody.Part.createFormData(filename, image.getName(), requestFile);

             //MultipartBody.Part filename = body;
             //filename = body;

        }


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
        Call<List<BusinessListings>> call = service.postReview(/*id,*/
                link,
                title,
                status,
                rating,
                post_type,
                city,
                state,
                country,
                zipcode,
                latitude,
                longitude,
                image00,
                image01,
                image02,
                image03,
                image04,
                image05);


        call.enqueue(new Callback<List<BusinessListings>>() {
            @Override
            public void onResponse(Call<List<BusinessListings>> call, Response<List<BusinessListings>> response) {
                Log.e("add_listing", " response " + response.body());

//                progressBar.setVisibility(View.GONE); //hide progressBar
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            "Post Updated Title: " + response.body().get(0).getTitle() +
                                    " Body: " + response.body().get(0).getContent() +
                                    " PostId: " + response.body().get(0).getId(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<BusinessListings>> call, Throwable t) {
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