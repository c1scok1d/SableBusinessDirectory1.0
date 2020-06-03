package com.macinternetservices.sablebusinessdirectory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.macinternetservices.sablebusinessdirectory.model.Person;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import org.acra.*;

public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    CoreConfigurationBuilder builder;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        builder = new CoreConfigurationBuilder(this)
                .setBuildConfigClass(BuildConfig.class)
                .setReportFormat(StringFormat.JSON);

        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                .setReportAsFile(true)
                .setReportFileName("errorReport.txt")
                .setMailTo("admin@thesablebusinessdirectory.com")
                .setSubject("Sable Mobile App Error Report")
                .setEnabled(true);
        builder.getPluginConfigurationBuilder(DialogConfigurationBuilder.class)
                .setResText(R.string.acraErrorMesage)
                .setResCommentPrompt(R.string.acraAddComment)
                .setEnabled(true);
        builder.setReportContent(ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE);
    }

    /**
     * permissions request code
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    public static Double latitude, longitude;

    TextView tvMore, tvUserName, tvWpUserId, tvCity, tvCategories, tvLoading, noListingsTextView;
    Button login_button2, login_button3, login_button4, btnAdd, btnShowListings;
    RecyclerView verticalRecyclerView, featuredRecyclervView, recentListingsRecyclervView, recentReviewsRecyclervView;
    ProgressBar progressBar;
    LinearLayoutManager mLayoutManager, featuredRecyclerViewLayoutManager,
            recentListingsRecyclerViewLayoutManager, recentReviewsRecyclerViewLayoutManager;
    LinearLayout recentReviewsLayout, recentReviewsRecyclerLayout;
    RelativeLayout loadingLayout,  noListingsAnimationLayout;


    VerticalAdapter verticalAdapter;
    FeaturedListAdapter featuredListAdapter;
    RecentListingsAdapter recentListingsAdapter;
    RecentReviewListingsAdapter recentReviewListingsAdapter;


    public static String baseURL = "https://www.thesablebusinessdirectory.com", radius, address, state, country,
            zipcode, city, street, bldgno, todayRange, username = "android_app", isOpen, email,
            password = "mroK zH6o wOW7 X094 MTKy fwmY", userName, userEmail, userImage, userId, firstName, lastName;

    public static ArrayList<ListingsModel> verticalList = new ArrayList<>();
    ArrayList<String> listingName = new ArrayList<>();
    //ArrayList<ListReviewModel> reviewlList = new ArrayList<>();
    ArrayList<ListingsModel> featuredList = new ArrayList<>();
    ArrayList<ListingsModel> recentList = new ArrayList<>();
    ArrayList<RecentReviewListingsModel> recentReviewList = new ArrayList<>();
    ArrayList<ListingsModel> locationMatch = new ArrayList<>();
   // ArrayList<ListingsModel> locationReview = new ArrayList<>();
    public static ArrayList<Person> mapLocations = new ArrayList<>();



    //ArrayList<SearchListItems> category = new ArrayList<>();
    ArrayList<String> userActivityArray = new ArrayList<>();
    RadioGroup radioGroup;
    ImageView ivUserImage, spokesperson, ivLoading, noListingsImageView;
    //HorizontalScrollView category_radioButton_scroller;

    private static final int FRAME_TIME_MS = 8000;


    Cache cache;
    String date1, date2;


    AutoCompleteTextView searchView;
    public static LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();

    private GoogleMap mMap;
    private boolean isRestore;

    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    private FusedLocationProviderClient fusedLocationClient;

    LocationManager locationManager;

    CallbackManager fbLogincallbackManager;
    private AccessTokenTracker accessTokenTracker;


    AccessToken accessToken = AccessToken.getCurrentAccessToken();

    TextSwitcher textSwitcher, textSwitcher2, textSwitcher3;
    private SlidingUpPanelLayout mLayout;


    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    private static Retrofit retrofit;

    // check for Internet Connectivity
    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("thesablebusinessdirectory.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * cache JSON based on network connectivity
     */

    static Interceptor onlineInterceptor = chain -> {
        okhttp3.Response response = chain.proceed(chain.request());
        int maxAge = 300; // read from cache for 60 seconds even if there is internet connection
        return response.newBuilder()
                .header("Cache-Control", "public, max-age=" + maxAge)
                .removeHeader("Pragma")
                .build();
    };

    static Interceptor offlineInterceptor= chain -> {
        Request request = chain.request();
        if (!isInternetAvailable()) {
            int maxStale = 60 * 60 * 24 * 30; // Offline cache available for 30 days
            request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .removeHeader("Pragma")
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }
        return chain.proceed(request);
    };

    ImageSwitcher imageSwitcher, imageSwitcher2, imageSwitcher3;
    LinearLayout textSwitcherLayout, textSwitcher2Layout, textSwitcher3Layout,  sliderLayout;
    private Handler imageSwitchHandler;

    ListView searchList;
    SearchListViewAdapter searchAdapter;

   public static Context context;




    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setCache(getApplicationContext());
        //ACRA.init(getApplication(), builder);
        Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Animation imgAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        isRestore = savedInstanceState != null;
        setUpMap();
        loadingLayout = findViewById(R.id.loadingLayout);
        tvLoading = findViewById(R.id.tvLoading);
        tvLoading.setVisibility(View.GONE);
        tvMore = findViewById(R.id.tvMore);
        tvMore.setVisibility(View.GONE);
        sliderLayout = findViewById(R.id.sliderLayout);
        sliderLayout.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBar);
        recentReviewsRecyclerLayout = findViewById(R.id.recentReviewsRecyclerLayout);
        recentReviewsRecyclerLayout.setVisibility(View.GONE);
        recentReviewsLayout = findViewById(R.id.recentReviewsLayout);
        recentReviewsLayout.setVisibility(View.GONE);
        ivLoading = findViewById(R.id.ivLoading);
        ivLoading.setVisibility(View.GONE);
        noListingsImageView = findViewById(R.id.noListingsImageView);
        noListingsImageView.setVisibility(View.GONE);
        noListingsTextView = findViewById(R.id.noListingsTextView);
        noListingsTextView.setVisibility(View.GONE);


        /**
         * ABOUT US
         */
        textSwitcherLayout = findViewById(R.id.textSwitcherLayout);
        textSwitcher2Layout = findViewById(R.id.textSwitcher2Layout);
        textSwitcher3Layout = findViewById(R.id.textSwitcher3Layout);

        login_button2 = findViewById(R.id.login_button2);
        login_button2.setVisibility(View.GONE);

        login_button3 = findViewById(R.id.login_button3);
        login_button3.setVisibility(View.GONE);
        login_button4 = findViewById(R.id.login_button4);

        textSwitcher =  findViewById(R.id.textSwitcher);
        textSwitcher.setFactory(() -> {
            TextView textView = new TextView(getApplicationContext());
            textView.setLayoutParams(new TextSwitcher.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setTextSize(16);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent2));
            return textView;
        });

        textSwitcher.setInAnimation(imgAnimationIn);
        textSwitcher.setOutAnimation(imgAnimationOut);

        textSwitcher2 =  findViewById(R.id.textSwitcher2);
        textSwitcher2.setFactory(() -> {
            TextView textView = new TextView(getApplicationContext());
            textView.setLayoutParams(new TextSwitcher.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setTextSize(16);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            //textView.setGravity(Gravity.CENTER);
            return textView;
        });

        textSwitcher2.setInAnimation(imgAnimationIn);
        textSwitcher2.setOutAnimation(imgAnimationOut);

        textSwitcher3 =  findViewById(R.id.textSwitcher3);
        textSwitcher3.setFactory(() -> {
            TextView textView = new TextView(getApplicationContext());
            textView.setLayoutParams(new TextSwitcher.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setTextSize(16);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent2));
            return textView;
        });

        textSwitcher3.setInAnimation(imgAnimationIn);
        textSwitcher3.setOutAnimation(imgAnimationOut);

        imageSwitcher =  findViewById(R.id.imageSwitcher);

        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        ViewGroup.LayoutParams params = new ImageSwitcher.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        imageView.setLayoutParams(params);

        imageSwitcher2 =  findViewById(R.id.imageSwitcher2);

        ImageView imageView2 = new ImageView(getApplicationContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        ViewGroup.LayoutParams imageView2params = new ImageSwitcher.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        imageView2.setLayoutParams(imageView2params);

        imageSwitcher3 =  findViewById(R.id.imageSwitcher3);

        ImageView imageView3 = new ImageView(getApplicationContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        ViewGroup.LayoutParams imageView3params = new ImageSwitcher.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        imageView3.setLayoutParams(imageView3params);

        imageSwitchHandler = new Handler();
        imageSwitchHandler.post(runnableCode);

        /**
         *  strt fuckin' around with getting linearLayouts to fade in and out
         */
        textSwitcherLayout =  findViewById(R.id.textSwitcherLayout);

        LinearLayout textSwitcherLayout = new LinearLayout(getApplicationContext());

        ViewGroup.LayoutParams textSwitcherLayoutParams = new ImageSwitcher.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        textSwitcherLayout.setLayoutParams(textSwitcherLayoutParams);


        textSwitcherLayout.setAnimation(imgAnimationIn);
        textSwitcherLayout.setAnimation(imgAnimationOut);
        textSwitcherLayout.post(runnableCode);

        textSwitcher2Layout =  findViewById(R.id.textSwitcher2Layout);

        LinearLayout textSwitcher2Layout = new LinearLayout(getApplicationContext());


        textSwitcher2Layout.setLayoutParams(textSwitcherLayoutParams);

        textSwitcher2Layout.setAnimation(imgAnimationIn);
        textSwitcher2Layout.setAnimation(imgAnimationOut);
        textSwitcher2Layout.post(runnableCode);

        textSwitcher3Layout =  findViewById(R.id.textSwitcher3Layout);

        LinearLayout textSwitcher3Layout = new LinearLayout(getApplicationContext());


        textSwitcher3Layout.setLayoutParams(textSwitcherLayoutParams);

        textSwitcher3Layout.setAnimation(imgAnimationIn);
        textSwitcher3Layout.setAnimation(imgAnimationOut);
        textSwitcher3Layout.post(runnableCode);

        /**
         * end fuckin' around with getting lienarlayouts to fade in and out
         */

        imageSwitcher.setVisibility(View.GONE);
        imageSwitcher2.setVisibility(View.GONE);
        imageSwitcher3.setVisibility(View.GONE);

        textSwitcherLayout.setVisibility(View.GONE);
        textSwitcher2Layout.setVisibility(View.GONE);
        textSwitcher3Layout.setVisibility(View.GONE);

///END ABOUT US////

        /**
         * Featured Listings
         */
        featuredListAdapter = new FeaturedListAdapter(featuredList, getApplicationContext());
        featuredRecyclervView = findViewById(R.id.featuredListingsRecyclerView);
        featuredRecyclervView.setHasFixedSize(true);
        featuredRecyclervView.setAdapter(featuredListAdapter);
        featuredRecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        featuredRecyclervView.setLayoutManager(featuredRecyclerViewLayoutManager);

        /**
         * Recent Listings
         */
        recentListingsAdapter = new RecentListingsAdapter(recentList, getApplicationContext());
        recentListingsRecyclervView = findViewById(R.id.recentListingsRecyclerView);
        recentListingsRecyclervView.setHasFixedSize(true);
        recentListingsRecyclervView.setAdapter(recentListingsAdapter);
        recentListingsRecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recentListingsRecyclervView.setLayoutManager(recentListingsRecyclerViewLayoutManager);

        /**
         * Recent Reviews
         */

        recentReviewListingsAdapter = new RecentReviewListingsAdapter(recentReviewList, getApplicationContext());
        recentReviewsRecyclervView = findViewById(R.id.recentReviewsRecyclerView);
        recentReviewsRecyclervView.setHasFixedSize(true);
        recentReviewsRecyclervView.setAdapter(recentReviewListingsAdapter);
        recentReviewsRecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recentReviewsRecyclervView.setLayoutManager(recentReviewsRecyclerViewLayoutManager);


/**
 * login via facebook
 */
        fbLogincallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(fbLogincallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.e("Facebook Login Successful ", " response " + loginResult);
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e("Facebook Login Error ", " response " + exception);
                    }
                });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    accessToken = currentAccessToken;
                    useLoginInformation(currentAccessToken);
                } else {
                    restartActivity();
                }
            }
        };

        tvUserName = findViewById(R.id.tvUserName);
        ivUserImage = findViewById(R.id.ivUserImage);
        tvWpUserId = findViewById(R.id.tvWpUserId);
        textSwitcher = findViewById(R.id.textSwitcher);

        /*
            BEGIN vertical Recycler View
         */
        verticalRecyclerView = findViewById(R.id.verticalRecyclerView);
        mLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        verticalRecyclerView.setLayoutManager(mLayoutManager);
        verticalList = new ArrayList<>();
        locationMatch = new ArrayList<>();


        verticalAdapter = new VerticalAdapter(verticalList, userName, userEmail, userImage, userId, MainActivity.this);
        featuredListAdapter = new FeaturedListAdapter(featuredList, MainActivity.this);
        recentListingsAdapter = new RecentListingsAdapter(recentList, MainActivity.this);
        verticalRecyclerView.setAdapter(verticalAdapter);
        verticalRecyclerView.setNestedScrollingEnabled(false);

        featuredRecyclervView.setAdapter(featuredListAdapter);
        featuredRecyclervView.setNestedScrollingEnabled(false);

        recentListingsRecyclervView.setAdapter(recentListingsAdapter);
        recentListingsRecyclervView.setNestedScrollingEnabled(false);

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setVisibility(View.GONE);
        spokesperson = findViewById(R.id.spokesperson);
        tvCity = findViewById(R.id.tvCity);
        tvMore = findViewById(R.id.tvMore);

        btnShowListings = findViewById(R.id.btnShowListings);
        btnShowListings.setVisibility(View.GONE);




        btnShowListings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MarkerClusteringActivity.class));
            }
        });

        /**
         *  location add button
         */



        btnAdd.setOnClickListener(view -> {
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
            if (!isLoggedIn) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                //goto login activity get username and email via facebook create account, return here to check again and proceed

                Toast.makeText(getApplicationContext(), "User must be logged in to add a business listing.", Toast.LENGTH_LONG).show();
            } else {
                Intent addListingIntent = new Intent(MainActivity.this, AddListingActivity.class);
                startActivity(addListingIntent);
            }
        });

        /**
         *  directory search
         */

        searchView = findViewById(R.id.search);
        searchView.setVisibility(View.GONE);
        ArrayAdapter<String> searchViewAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, listingName);
        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                Map<String, String> query = new HashMap<>();
                query.put("per_page", "100");

                progressBar.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.VISIBLE);
                loadingLayout.setAnimation(imgAnimationIn);
                ivLoading.setImageResource(R.mipmap.online_reviews_foreground);
                String listingName = "<font color='#4FC1E9'>" +parent.getItemAtPosition(pos).toString()+"</font>";
                tvLoading.setText(Html.fromHtml(("Thank you for your waiting while we load reviews for " + listingName )));

                    retrofit = null;
                    retrofit = new Retrofit.Builder()
                            .baseUrl(baseURL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                    RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
                    // pass JSON data to BusinessListings class for filtering
                    Call<List<BusinessListings>> call = service.search(query);

                    // get filtered data from BusinessListings class and add to recyclerView adapter for display on screen
                    call.enqueue(new Callback<List<BusinessListings>>() {
                        @Override
                        public void onResponse(Call<List<BusinessListings>> call, Response<List<BusinessListings>> response) {

                            // loop through JSON response get parse and output to log
                            for (int i = 0; i < response.body().size(); i++) {

//                                String foo = parent.getItemAtPosition(pos).toString();
//                                String foo1 = response.body().get(i).getTitle().getRaw();
                                if (parent.getItemAtPosition(pos).toString().equals(response.body().get(i).getTitle().getRaw())) {
                                    ArrayList<ListingsModel> locationReview = new ArrayList<>();
                                    Intent showReviews = new Intent(getApplicationContext(), ListReviewActivity.class);
                                    locationReview.add((new ListingsModel(ListingsModel.IMAGE_TYPE,
                                            response.body().get(i).getId(),
                                            response.body().get(i).getTitle().getRaw(),
                                            response.body().get(i).getLink(),
                                            response.body().get(i).getStatus(),
                                            response.body().get(i).getPostCategory().get(0).getName(),
                                            response.body().get(i).getFeatured(),
                                            response.body().get(i).getFeaturedImage().getSrc(),
                                            response.body().get(i).getBldgNo(),
                                            response.body().get(i).getStreet(),
                                            response.body().get(i).getCity(),
                                            response.body().get(i).getRegion(),
                                            response.body().get(i).getCountry(),
                                            response.body().get(i).getZip(),
                                            response.body().get(i).getLatitude(),
                                            response.body().get(i).getLongitude(),
                                            response.body().get(i).getRating(),
                                            response.body().get(i).getRatingCount(),
                                            response.body().get(i).getPhone(),
                                            response.body().get(i).getEmail(),
                                            response.body().get(i).getWebsite(),
                                            response.body().get(i).getTwitter(),
                                            response.body().get(i).getFacebook(),
                                            response.body().get(i).getVideo(),
                                            todayRange,
                                            isOpen,
                                            response.body().get(i).getLogo(),
                                            response.body().get(i).getContent().getRaw(),
                                            response.body().get(i).getFeaturedImage().getThumbnail())));

                                    //progressBar.setVisibility(View.GONE);

                                    Bundle locationReviewBundle = new Bundle();
                                    locationReviewBundle.putParcelableArrayList("locationReviewBundle", locationReview);
                                    showReviews.putExtra("locationReview", locationReview);
                                    startActivity(showReviews);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<BusinessListings>> call, Throwable t) {
                            Log.e("CategoryNumber", " response: " + t);
                        }
                    });
            }
        });
        searchView.setThreshold(1);
        searchView.setAdapter(searchViewAdapter);

        if (!userActivityArray.isEmpty()) {
            userActivityArray = this.getIntent().getExtras().getStringArrayList("userActivityArray");
        }


        /***
         *  BEGIN SLIDE UP
         */


        mLayout =  findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
               // Log.i("onPanelSlide", "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (String.valueOf(newState).equals("COLLAPSED")) {
                    tvMore.setText("More");
                } else {
                    tvMore.setText("Less");
                }

               // Log.i("onPanelStateChanged", "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(view -> mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        Log.e("onCreate ", " END onCreate " );
    }

        //END ON CREATE
    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);
    }

    /**
     * Checks the dynamically-controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    } else {
                       startActivity(getIntent());
                    }
                }
                // all permissions were granted
                break;
        }
    }

    public void onStart() {
        super.onStart();
        verticalList.clear();
        listingName.clear();
        recentList.clear();
        featuredList.clear();
        mapLocations.clear();
        recentReviewList.clear();
        Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Log.e("onStart", "onStart Executed");

        /**
         *  location manager to get current location
         */
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,
                400, LocationListener);

        //This starts the access token tracking
        if (accessToken != null) {
            useLoginInformation(accessToken);
        //    loggedInLayout.setVisibility(View.VISIBLE);
            // startActivity(getIntent());
            Log.e("onStart Access Token Login Successful ", " accessToken " + accessToken);
        } else {
            // startActivity(getIntent());
            LinearLayout userImageLayout = findViewById(R.id.userImageLayout);
            //LinearLayout userNameLayout = findViewById(R.id.userNameLayout);
            userImageLayout.setVisibility(View.GONE);
            //userNameLayout.setVisibility(View.GONE);
        }
        accessTokenTracker.startTracking();
        Log.e("onStart", "onStart Ended");
        //throw new RuntimeException();
    }

    @Override
    protected void onStop() {
        Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Animation imgAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
       // tvLoading.setAnimation(imgAnimationIn);
        //tvLoading.setText("onSTOP");
        Log.e("onStop", "onStop Executed");
        imageSwitchHandler.removeCallbacks(runnableCode);
        super.onStop();
        Log.e("onStop", "onStop Ended");
    }

    public void onResume() {
        super.onResume();
        Log.e("onResume", "onResume Executed");
        //This starts the access token tracking
        accessTokenTracker.startTracking();
        Log.e("onResume", "onResume Ended");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "onDestroy Executed");
        // We stop the tracking before destroying the activity
        accessTokenTracker.stopTracking();
        deleteCache(getApplicationContext());
        Log.e("onDestroy", "onDestroy Ended");
    }

    public void restartActivity(){
        Intent mIntent = getIntent();
        finish();
        startActivity(mIntent);
    }

    public void useLoginInformation(final AccessToken accessToken) {

        /**
         Creating the GraphRequest to fetch user details
         1st Param - AccessToken
         2nd Param - Callback (which will be invoked once the request is successful)
         **/
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Picasso.Builder facebookImageBuilder = new Picasso.Builder(getApplicationContext());
                try {
                    AccessToken mAccessToken = accessToken;
                    userName = object.getString("name");
                    userEmail = object.getString("email");
                    userImage = object.getJSONObject("picture").getJSONObject("data").getString("url");

                    String[] parts = (object.getString("name").split(" "));
                    firstName = parts[0];
                    lastName = parts[1];
//                    tvUserName.setText(firstName);
//                    tvUserName2.setText(firstName);
                    //    tvUserEmail.setText(object.getString("email"));
                    facebookImageBuilder.build().load(object.getJSONObject("picture").getJSONObject("data").getString("url")).into(ivUserImage);

                    Map<String, String> query = new HashMap<>();
                    query.put("access_token", mAccessToken.getToken());
                    loginUser(query);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    // facebook login activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fbLogincallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Location listener to get device current lat/lng
     */
    LocationListener LocationListener = new LocationListener() {

        /**
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            Animation imgAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

            ivLoading.setVisibility(View.VISIBLE);
            ivLoading.setAnimation(imgAnimationIn);
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setAnimation(imgAnimationIn);
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
            if(isLoggedIn && firstName != null) {
                String name = "<font color='#4FC1E9'>" +firstName+"</font>";
                tvLoading.setText(Html.fromHtml(("Thanks for your patience " + name + "<br>We are searching our directory for black owned businesses near you.")));
            } else {
                tvLoading.setText("Thank you for waiting while we search our directory for black owned businesses near you.");
            }
            Log.e("onLocationChange", "onLocationChange Executed");
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Map<String, String> query = new HashMap<>();
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            query.put("latitude", String.valueOf(location.getLatitude()));
            query.put("longitude", String.valueOf(location.getLongitude()));
            //query.put("distance", "5");
            query.put("order", "asc");
            query.put("orderby", "distance");
            getRetrofit(query); //api call; pass current lat/lng to check if current location in database
            Log.e("Location Change", "Listings query executed by location change");
            getReviews();
            Log.e("Location Change", "Review query executed by location change");
            setAddress(location.getLatitude(), location.getLongitude());
            Log.e("onLocationChange", "onLocationChange Ended");
        }

        /**
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        /**
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
        }

        /**
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {
        }

    };
    /**
     * @param map
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationClickListener(this);
    }

    /**
     * @return
     */
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
        Map<String, String> query = new HashMap<>();

        query.put("latitude", String.valueOf(latitude));
        query.put("longitude", String.valueOf(longitude));
        //query.put("distance", "5");
        query.put("order", "asc");
        query.put("orderby", "distance");
        getRetrofit(query); //api call; pass current lat/lng to check if current location in database
        Log.e("onMyLocationButtonClick", "Listings query executed by onMyLocationButtonClick");
        getReviews();
        Log.e("onMyLocationButtonClick", "Review query executed by onMyLocationButtonClick");
        setAddress(latitude, longitude);
        return false;
    }

    /**
     * @param location
     */
    @Override
    public void onMyLocationClick(Location location) {
        Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
        Map<String, String> query = new HashMap<>();

        query.put("latitude", String.valueOf(latitude));
        query.put("longitude", String.valueOf(longitude));
        //query.put("distance", "5");
        query.put("order", "asc");
        query.put("orderby", "distance");
        getRetrofit(query); //api call; pass current lat/lng to check if current location in database
        Log.e("onMyLocationClick", "Listings query executed by onMyLocationClick");
        getReviews();
        Log.e("onMyLocationClick", "Review query executed by onMyLocationClick");
        setAddress(latitude, longitude);
    }

    /**
     * geocode location address using lat/lng
     *
     * @param latitude
     * @param longitude
     */
    public void setAddress(Double latitude, Double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses.size() > 0) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                for (int i = 0; i < fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append(" ");
                }
                 String address = strAddress.toString();
                } else {
                Toast.makeText(getApplicationContext(), "Searching current address...", Toast.LENGTH_LONG).show();
                }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not get address...", Toast.LENGTH_LONG).show();
        }

        if (addresses != null) {
            Log.d("max", " " + addresses.get(0).getMaxAddressLineIndex());

            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()/*String city = addresses.get(0).getLocality();
            bldgno = addresses.get(0).getSubThoroughfare(); // get bulding number
            street = addresses.get(0).getThoroughfare(); //get street name
            city = addresses.get(0).getLocality(); //get city name
            state = addresses.get(0).getAdminArea(); //get state name
            zipcode = addresses.get(0).getPostalCode(); //get zip code
            country = addresses.get(0).getCountryName(); //get country
            //tvAddress.setText(address);
            //tvCity.setText(addresses.get(0).getLocality());
            addresses.get(0).getAdminArea();
        } else {
           /*  Toast.makeText(this, "No GPS location available!  " +
                    "Please check your mobile device for possible service issues.", Toast.LENGTH_LONG).show(); */
        }
    }

    public void setCache(Context context) {
        int cacheSize = 10 * 1024 * 1024; // 10 MB
        File cacheFoo = context.getCacheDir();
        //  cache = new Cache(new File(context.getCacheDir(), "sable-cache"), cacheSize);
        cache = new Cache(cacheFoo,  cacheSize);
        Log.e("This is Cache:", "cacheFile: " +cache);

    }

    /**
     * http client set up for retrofit api call
     */

    int cacheSize = 10 * 1024 * 1024; // 10 MB
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new BasicAuthInterceptor(username, password))
            .addInterceptor(logging)
            .addInterceptor(offlineInterceptor)
            .addInterceptor(onlineInterceptor)
            .cache(cache)
            .build();


    /**
     * Query API for listings data
     * set URL and make call to API
     */

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
    /**
     * Retrofit API call to get listings
     *
     * @param query
     */
    public void getRetrofit(final Map<String, String> query) {

        Animation imgAnimationBlink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);

        //mapLocations.clear();

        retrofit = null;
        retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);

        // pass JSON data to BusinessListings class for filtering
        Call<List<BusinessListings>> call = service.getPostInfo(query);

        // get filtered data from BusinessListings class and add to recyclerView adapter for display on screen
        call.enqueue(new Callback<List<BusinessListings>>() {
            @Override
            public void onResponse(Call<List<BusinessListings>> call, Response<List<BusinessListings>> response) {
                if (response.isSuccessful()) {
                    Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    Animation imgAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                   // tvLoading.setAnimation(imgAnimationIn);
                    //tvLoading.setText("Loading nearby listings");
                    if (response.raw().cacheResponse() != null) {
                        Log.e("Network", "Listings response came from cache");
                    } else {
                        Log.e("Network", "Listings response came from server");
                    }
                    for (int i = 0; i < response.body().size(); i++) {
                        BusinessListings.BusinessHours businessHours = response.body().get(i).getBusinessHours();
                        if (businessHours == null) {
                            String today = "null";
                        } else {
                            todayRange = response.body().get(i).getBusinessHours().getRendered().getExtra().getTodayRange();
                            isOpen = response.body().get(i).getBusinessHours().getRendered().getExtra().getCurrentLabel();
                        }
                        /**
                         * onLocationMatch
                         * if device lat/lng equals stored listing lat/lng locationMatch = true
                         * add all matching data to array and launch Review Activity
                         *
                         */

                        Boolean onMatch = false;

                        if (String.valueOf(response.body().get(i).getLatitude()).equals(String.valueOf(latitude)) &&
                                String.valueOf(response.body().get(i).getLongitude()).equals(String.valueOf(longitude)) && userActivityArray.size() > 0 && onMatch) {
                            locationMatch.add(new ListingsModel(ListingsModel.IMAGE_TYPE,
                                    response.body().get(i).getId(),
                                    response.body().get(i).getTitle().getRaw(),
                                    response.body().get(i).getLink(),
                                    response.body().get(i).getStatus(),
                                    response.body().get(i).getPostCategory().get(0).getName(),
                                    response.body().get(i).getFeatured(),
                                    response.body().get(i).getFeaturedImage().getSrc(),
                                    response.body().get(i).getBldgNo(),
                                    response.body().get(i).getStreet(),
                                    response.body().get(i).getCity(),
                                    response.body().get(i).getRegion(),
                                    response.body().get(i).getCountry(),
                                    response.body().get(i).getZip(),
                                    response.body().get(i).getLatitude(),
                                    response.body().get(i).getLongitude(),
                                    response.body().get(i).getRating(),
                                    response.body().get(i).getRatingCount(),
                                    response.body().get(i).getPhone(),
                                    response.body().get(i).getEmail(),
                                    response.body().get(i).getWebsite(),
                                    response.body().get(i).getTwitter(),
                                    response.body().get(i).getFacebook(),
                                    response.body().get(i).getVideo(),
                                    todayRange,
                                    isOpen,
                                    response.body().get(i).getLogo(),
                                    response.body().get(i).getContent().getRaw(),
                                    response.body().get(i).getFeaturedImage().getSrc()));
                            Intent LocationMatch = new Intent(MainActivity.this, ReviewActivity.class);
                            Bundle locationMatchBundle = new Bundle();
                            locationMatchBundle.putParcelableArrayList("locationMatchBundle", locationMatch);
                            LocationMatch.putExtra("locationMatch", locationMatch);
                            startActivity(LocationMatch);
                            break;
                        } else {

                            /**
                             * populate vertical recycler in Main Activity
                             */
                            verticalList.add(new ListingsModel(ListingsModel.IMAGE_TYPE,
                                    response.body().get(i).getId(),
                                    response.body().get(i).getTitle().getRaw(),
                                    response.body().get(i).getLink(),
                                    response.body().get(i).getStatus(),
                                    response.body().get(i).getPostCategory().get(0).getName(),
                                    response.body().get(i).getFeatured(),
                                    response.body().get(i).getFeaturedImage().getSrc(),
                                    response.body().get(i).getBldgNo(),
                                    response.body().get(i).getStreet(),
                                    response.body().get(i).getCity(),
                                    response.body().get(i).getRegion(),
                                    response.body().get(i).getCountry(),
                                    response.body().get(i).getZip(),
                                    response.body().get(i).getLatitude(),
                                    response.body().get(i).getLongitude(),
                                    response.body().get(i).getRating(),
                                    response.body().get(i).getRatingCount(),
                                    response.body().get(i).getPhone(),
                                    response.body().get(i).getEmail(),
                                    response.body().get(i).getWebsite(),
                                    response.body().get(i).getTwitter(),
                                    response.body().get(i).getFacebook(),
                                    response.body().get(i).getVideo(),
                                    todayRange,
                                    isOpen,
                                    response.body().get(i).getLogo(),
                                    response.body().get(i).getContent().getRaw(),
                                    response.body().get(i).getFeaturedImage().getThumbnail()));
                            verticalAdapter.notifyDataSetChanged();

                            listingName.add(response.body().get(i).getTitle().getRaw());
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ss", Locale.US);
                                Date created = sdf.parse(response.body().get(i).getDateGmt());
                                Date currentTime = Calendar.getInstance().getTime();
                                date1 = String.valueOf(created);
                                date2 = String.valueOf(currentTime);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            boolean isRecent = date1 != null && date2 != null && date1.compareTo(date2) < 30;
                            if (isRecent) {
                                recentList.add(new ListingsModel(ListingsModel.IMAGE_TYPE,
                                        response.body().get(i).getId(),
                                        response.body().get(i).getTitle().getRaw(),
                                        response.body().get(i).getLink(),
                                        response.body().get(i).getStatus(),
                                        response.body().get(i).getPostCategory().get(0).getName(),
                                        response.body().get(i).getFeatured(),
                                        response.body().get(i).getFeaturedImage().getSrc(),
                                        response.body().get(i).getBldgNo(),
                                        response.body().get(i).getStreet(),
                                        response.body().get(i).getCity(),
                                        response.body().get(i).getRegion(),
                                        response.body().get(i).getCountry(),
                                        response.body().get(i).getZip(),
                                        response.body().get(i).getLatitude(),
                                        response.body().get(i).getLongitude(),
                                        response.body().get(i).getRating(),
                                        response.body().get(i).getRatingCount(),
                                        response.body().get(i).getPhone(),
                                        response.body().get(i).getEmail(),
                                        response.body().get(i).getWebsite(),
                                        response.body().get(i).getTwitter(),
                                        response.body().get(i).getFacebook(),
                                        response.body().get(i).getVideo(),
                                        todayRange,
                                        isOpen,
                                        response.body().get(i).getLogo(),
                                        response.body().get(i).getContent().getRaw(),
                                        response.body().get(i).getFeaturedImage().getThumbnail()));
                                recentListingsAdapter.notifyDataSetChanged();
                            }
                            boolean isFeatured = response.body().get(i).getFeatured();
                            if (isFeatured) {
                                featuredList.add(new ListingsModel(ListingsModel.IMAGE_TYPE,
                                        response.body().get(i).getId(),
                                        response.body().get(i).getTitle().getRaw(),
                                        response.body().get(i).getLink(),
                                        response.body().get(i).getStatus(),
                                        response.body().get(i).getPostCategory().get(0).getName(),
                                        response.body().get(i).getFeatured(),
                                        response.body().get(i).getFeaturedImage().getSrc(),
                                        response.body().get(i).getBldgNo(),
                                        response.body().get(i).getStreet(),
                                        response.body().get(i).getCity(),
                                        response.body().get(i).getRegion(),
                                        response.body().get(i).getCountry(),
                                        response.body().get(i).getZip(),
                                        response.body().get(i).getLatitude(),
                                        response.body().get(i).getLongitude(),
                                        response.body().get(i).getRating(),
                                        response.body().get(i).getRatingCount(),
                                        response.body().get(i).getPhone(),
                                        response.body().get(i).getEmail(),
                                        response.body().get(i).getWebsite(),
                                        response.body().get(i).getTwitter(),
                                        response.body().get(i).getFacebook(),
                                        response.body().get(i).getVideo(),
                                        todayRange,
                                        isOpen,
                                        response.body().get(i).getLogo(),
                                        response.body().get(i).getContent().getRaw(),
                                        response.body().get(i).getFeaturedImage().getThumbnail()));
                                featuredListAdapter.notifyDataSetChanged();
                            }

                            /**
                             * categories on top of the map
                             */

                            LatLng latlng = new LatLng(response.body().get(i).getLatitude(), response.body().get(i).getLongitude());
                            latLngBoundsBuilder.include(latlng);
                            mapLocations.add(new Person(latlng,
                                    response.body().get(i).getTitle().getRaw(),
                                    response.body().get(i).getFeaturedImage().getThumbnail(),
                                    response.body().get(i).getContent().getRaw(),
                                    response.body().get(i).getRating(),
                                    response.body().get(i).getRatingCount(),
                                    response.body().get(i).getCity(),
                                    response.body().get(i).getRegion(),
                                    response.body().get(i).getFeaturedImage().getThumbnail()));

                        }
                    }
                    // Pass results to ListViewAdapter Class
                   // searchAdapter = new SearchListViewAdapter(getApplicationContext(), category);
                    // Binds the Adapter to the ListView
//                    searchList.setAdapter(searchAdapter);
                   // tvLoading.setAnimation(imgAnimationIn);
                   // tvLoading.setText("...loading the map now.");
                    setMarkers();
                } else {
                    // do some stuff
                }
            }
            @Override
            public void onFailure(Call<List<BusinessListings>> call, Throwable t) {
                Log.e("getListingsFailure", " response: " + t);
                //OPTION TO RE-RUN QUERY OR ADD LISTING
                getRetrofit(query); //api call; pass current lat/lng to check if current location in database
                Log.e("getRetroFitListingsFailure", "Listings query executed by getRetroFitListingsFailure");
            }
        });
    }//END Retrofit API call to get listings

    /**
     * Retrofit API call to get reviews
     */

    public void getReviews() {
        retrofit = null;
        Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Animation imgAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);

        // pass JSON data to BusinessListings class for filtering
        Call<List<ListReviewPOJO>> call = service.getReviews();


        // get filtered data from BusinessListings class and add to recyclerView adapter for display on screen
        call.enqueue(new Callback<List<ListReviewPOJO>>() {
            @Override
            public void onResponse(Call<List<ListReviewPOJO>> call, Response<List<ListReviewPOJO>> response) {
                if (response.raw().cacheResponse() != null) {
                    Log.e("Network", "Reviews response came from cache");
                } else {
                    Log.e("Network", "Reviews response came from server");
                }
                if (response.isSuccessful() && response.body().size() > 0) {
                   // tvLoading.setAnimation(imgAnimationIn);
                   // tvLoading.setText("Loading recent reviews...");

                    for (int i = 0; i < response.body().size(); i++) {
                        /**
                         * populate vertical recycler in Main Activity
                         */
                        if(response.body().get(i).getImages().getRendered().size() != 0) {

                            recentReviewList.add(new RecentReviewListingsModel(RecentReviewListingsModel.IMAGE_TYPE,
                                    response.body().get(i).getId(),
                                    response.body().get(i).getLink(),
                                    response.body().get(i).getAuthorName(),
                                    response.body().get(i).getRating().getRating(),
                                    response.body().get(i).getDateGmt(),
                                    response.body().get(i).getImages().getRendered().get(0).getSrc(),
                                    response.body().get(i).getContent().getRendered(),
                                    response.body().get(i).getParent()));
                        }

                        recentReviewListingsAdapter.notifyDataSetChanged();
                    }
                } else {
                   // Log.e("getPostReview_METHOD_noResponse ", " SOMETHING'S FUBAR'd!!! :)");
                }
            }
            @Override
            public void onFailure(Call<List<ListReviewPOJO>> call, Throwable t) {
                Log.e("getReviewFailure", " response: " + t);
                //OPTION TO RE-RUN QUERY OR ADD LISTING
                getReviews();
                Log.e("getRetorFitReviewFailure", "Review query executed by getRetroFitReviewFailure");
                // setAddress(latitude, longitude);

            }
        });

    }

    //Retrofit retrofit = null;
    public void loginUser(final Map<String, String> query) {
        Retrofit retrofit;

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);

        // pass JSON data to BusinessListings class for filtering
        Call<UserAuthPOJO> call = service.getUserInfo(query);


        // get filtered data from BusinessListings class and add to recyclerView adapter for display on screen
        call.enqueue(new Callback<UserAuthPOJO>() {
            @Override
            public void onResponse(Call<UserAuthPOJO> call, Response<UserAuthPOJO> response) {
                Log.e("loginUser_METHOD_SUCCESS", " response " + response.body());
                if (response.isSuccessful()) {
                    userId = String.valueOf(response.body().getWpUserId());
//                    tvWpUserId.setText(String.valueOf(response.body().getWpUserId()));
                } else {
                    // do some stuff
                }
            }

            @Override
            public void onFailure(Call<UserAuthPOJO> call, Throwable t) {
                Log.e("UserLoginFailure", " response: " + t);
                Log.e("loginUser_METHOD_FAILURE ", " Re-running method...");
            }
        });
    }

    /**
     * more slider shit
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.demo, menu);
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (mLayout != null) {
            if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                item.setTitle(R.string.action_show);
            } else {
                item.setTitle(R.string.action_hide);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle: {
                if (mLayout != null) {
                    if (mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        item.setTitle(R.string.action_show);
                    } else {
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        item.setTitle(R.string.action_hide);
                    }
                }
                return true;
            }
            case R.id.action_anchor: {
                if (mLayout != null) {
                    if (mLayout.getAnchorPoint() == 1.0f) {
                        mLayout.setAnchorPoint(0.5f);
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        item.setTitle(R.string.action_anchor_disable);
                    } else {
                        mLayout.setAnchorPoint(1.0f);
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        item.setTitle(R.string.action_anchor_enable);
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    } //END OF SLIDER SHIT

    /**
     * @param context CLEAR CACHE on close
     */

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    /**
     * ABOUT US ANIMATIONS
     */



    private Runnable runnableCode = new Runnable() {
        int count = 0;


        // String image;
        @Override
        public void run() {
            Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            Animation imgAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);


            String[] text = {
                    "Welcome to The Sable Business Directory!\n",


                    "The Sable Business Directory is designed to help those wanting to support " +
                            "and frequent black owned businesses and service providers find black owned" +
                            "businesses and service providers.",

                    "We provide a one of a kind online platform that makes it easier to find, rate " +
                            "and review black owned businesses and service providers.",

                    "We have combined geo-search, social media and e-commerce technologies to create an online " +
                            "platform dedicated to the continued growth and support of black owned businesses.",

                    "To insure high quality services, customers maintain the directory by adding and " +
                            "reviewing the black owned businesses and service providers they frequent.",

                    "Our combined technologies then compile those listings, ratings and reviews to " +
                            "provide a directory listing of black owned business and service providers " +
                            "near your current location.",

                    "88% of people trust online reviews. Online reviews are an important way you can increase " +
                            "sales for your business. This is especially important for local businesses and service providers.",

                    "Adding and reviewing listings is easy. To protect the privacy of our users and insure high quality feedback " +
                            "we require users to login before adding or reviewing a listing.",

                    "Tap below to begin adding and reviewing black owned businesses using your Facebook account."

            };

            int[] images = {R.mipmap.hello_foreground, R.mipmap.showing_right_foreground,
                    R.mipmap.one_of_akind_foreground, R.mipmap.showing_tablet_foreground,R.mipmap.holding_phone_foreground, R.mipmap.making_thumbs_up_foreground,
                    R.mipmap.online_reviews_foreground, R.mipmap.showing_with_left_hand_foreground,R.mipmap.smiling_peace_foreground};

            if(count > text.length){
                count = 0;
            }
            switch (count)
            {
                case 1:
                case 3:
                case 5:
                case 7:

                    imageSwitcher3.setAnimation(imgAnimationOut);
                    //imageSwitcher3.setVisibility(View.GONE);

                    imageSwitcher.setImageResource(images[count]);
                    imageSwitcher.setAnimation(imgAnimationIn);
                    imageSwitcher.setVisibility(View.VISIBLE);

                    textSwitcher3Layout.setAnimation(imgAnimationOut);
                   // textSwitcher3Layout.setVisibility(View.GONE);

                    textSwitcher2.setText(text[count]);
                    textSwitcher2Layout.setAnimation(imgAnimationIn);
                    textSwitcher2Layout.setVisibility(View.VISIBLE);

                    imageSwitchHandler.postDelayed(this, FRAME_TIME_MS);
                   // i = randomGenerator.nextInt(100);
                    count ++;
                    break;
                case 2:
                case 4:
                case 6:
                //case 8:

                    imageSwitcher.setAnimation(imgAnimationOut);
                    imageSwitcher.setVisibility(View.GONE);

                    textSwitcher2Layout.setAnimation(imgAnimationOut);
                    textSwitcher2Layout.setVisibility(View.GONE);

                    imageSwitcher3.setImageResource(images[count]);
                    imageSwitcher3.setVisibility(View.VISIBLE);
                    imageSwitcher3.setAnimation(imgAnimationIn);
                    imageSwitcher.setAnimation(imgAnimationOut);


                    textSwitcher3.setText(text[count]);
                    textSwitcher3Layout.setVisibility(View.VISIBLE);
                    textSwitcher3Layout.setAnimation(imgAnimationIn);
                    //imageSwitcher.setVisibility(View.GONE);
                    //textSwitcher2Layout.setAnimation(imgAnimationOut);
                    //textSwitcher2Layout.setVisibility(View.GONE);
                    imageSwitchHandler.postDelayed(this, FRAME_TIME_MS);
                   // i = randomGenerator.nextInt(100);
                    count ++;
                    break;

                case 8:
                    imageSwitcher.setAnimation(imgAnimationOut);
                   // imageSwitcher.setVisibility(View.GONE);

                    imageSwitcher2.setImageResource(images[count]);
                    imageSwitcher2.setAnimation(imgAnimationIn);
                    imageSwitcher2.setVisibility(View.VISIBLE);


                    textSwitcher2Layout.setAnimation(imgAnimationOut);
                   // textSwitcher2Layout.setVisibility(View.GONE);

                    textSwitcher.setText(text[count]);
                    textSwitcherLayout.setAnimation(imgAnimationIn);
                    textSwitcherLayout.setVisibility(View.VISIBLE);

                    login_button2.setAnimation(imgAnimationIn);
                    login_button2.setVisibility(View.VISIBLE);
                    //btnDirectory.setAnimation(imgAnimationIn);
                    //btnLearnMore.setVisibility(View.VISIBLE);
                    //btnLearnMore.setAnimation(imgAnimationIn);
                    imageSwitchHandler.removeCallbacks(runnableCode);
                   // i = randomGenerator.nextInt(100);
                    count ++;
                    break;
                default:
                    imageSwitcher.setAnimation(imgAnimationOut);
                    //imageSwitcher.setVisibility(View.GONE);

                    textSwitcher2Layout.setAnimation(imgAnimationOut);
                    //textSwitcher2Layout.setVisibility(View.GONE);

                    imageSwitcher3.setImageResource(images[count]);
                    imageSwitcher3.setVisibility(View.VISIBLE);
                    imageSwitcher3.setAnimation(imgAnimationIn);
                    //imageSwitcher.setAnimation(imgAnimationOut);


                    textSwitcher3.setText(text[count]);
                    textSwitcher3Layout.setVisibility(View.VISIBLE);
                    textSwitcher3Layout.setAnimation(imgAnimationIn);
                    //imageSwitcher.setVisibility(View.GONE);
                    //textSwitcher2Layout.setAnimation(imgAnimationOut);
                    //textSwitcher2Layout.setVisibility(View.GONE);
                    imageSwitchHandler.postDelayed(this, FRAME_TIME_MS);
                    // i = randomGenerator.nextInt(100);
                    count ++;
                    break;

            }
        }
    };

    protected void setMarkers() {
        Log.e("setMarkersCall", "setMarkersCall Executed");
        Animation imgAnimationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Animation imgAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        Intent intent = new Intent(this, MarkerClusteringActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        //tvLoading.setAnimation(imgAnimationIn);
        //tvLoading.setText("Loading the map...");
        Log.e("setMarkersCall", "setMarkersCall Ended");


        //startActivity(new Intent(getApplicationContext(), MarkerClusteringActivity.class));
    }

    protected GoogleMap getMap() {
        return mMap;
    }
}