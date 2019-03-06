package com.gsnathan.compass;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kobakei.ratethisapp.RateThisApp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    GPSTracker gps;
    private Compass compass;
    private ImageView compassView;
    private TextView degreeView;
    private TextView coordView;
    private AlertDialog dialogBuilder;
    private boolean isFirstRun;
    private float currentAzimuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        checkPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupCompass();
        getLocation();

        // Custom condition: 5 days and 5 launches
        RateThisApp.Config config = new RateThisApp.Config(5, 5);
        RateThisApp.init(config);
        // Monitor launch times and interval from installation
        RateThisApp.onCreate(this);
        // If the condition is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    private void checkPermissions() {
        int permission1 = PermissionChecker.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission2 = PermissionChecker.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission1 == PermissionChecker.PERMISSION_GRANTED
                || permission2 == PermissionChecker.PERMISSION_GRANTED) {
            //good to go
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1);
        }
    }

    private void getLocation() {
        gps = new GPSTracker(this, coordView);
        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            coordView.setText(doubToDMS(latitude, true) + "  " + doubToDMS(longitude, false));
        } else {
            coordView.setText(R.string.perm_notgranted);
            /*
            Utils.showToast(
                    "Go to Material Compass settings to change location permission",
                    Toast.LENGTH_SHORT, getApplicationContext());
            */
        }
    }

    private void changeTheme() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useDarkTheme = sp.getBoolean("theme_pref", false);
        if (useDarkTheme) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private void initViews() {
        setSupportActionBar(findViewById(R.id.toolbar));
        compassView = findViewById(R.id.image_dial);
        degreeView = findViewById(R.id.degree_view);
        coordView = findViewById(R.id.coord_view);
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.start();
        getLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        compass.stop();
    }

    private void setupCompass() {
        compass = new Compass(this);
        Compass.CompassListener cl = this::adjustArrow;
        compass.setListener(cl);
    }

    private void adjustArrow(float azimuth) {
        Log.d(TAG, "will set rotation from " + currentAzimuth + " to "
                + azimuth);

        Animation animator = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;
        String display = (int) currentAzimuth + "";
        String cardDirect;

        if (currentAzimuth == 0 || currentAzimuth == 360)
            cardDirect = "N";
        else if (currentAzimuth > 0 && currentAzimuth < 90)
            cardDirect = "NE";
        else if (currentAzimuth == 90)
            cardDirect = "E";
        else if (currentAzimuth > 90 && currentAzimuth < 180)
            cardDirect = "SE";
        else if (currentAzimuth == 180)
            cardDirect = "S";
        else if (currentAzimuth > 180 && currentAzimuth < 270)
            cardDirect = "SW";
        else if (currentAzimuth == 270)
            cardDirect = "W";
        else if (currentAzimuth > 270 && currentAzimuth < 360)
            cardDirect = "NW";
        else
            cardDirect = "Unknown";

        degreeView.setText(display + "°" + " " + cardDirect);

        animator.setDuration(500);
        animator.setRepeatCount(0);
        animator.setFillAfter(true);

        compassView.startAnimation(animator);
    }

    private String doubToDMS(double value, boolean isLat) {
        String direction;
        int degrees, minutes, seconds;
        double val = Math.abs(value), min;
        degrees = (int) val;
        min = (val - (double) degrees) * 60;
        minutes = (int) min;
        seconds = (int) ((min - (double) minutes) * 60);

        if (isLat) {
            if (value < 0) {
                direction = "S";
            } else {
                direction = "N";
            }
        } else {
            if (value < 0) {
                direction = "W";
            } else {
                direction = "E";
            }
        }

        return degrees + "° " + minutes + "' " + seconds + "\" " + direction;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                startActivity(Utils.navIntent(this, AboutActivity.class));
                return true;
            case R.id.action_calib:
                calibrateDialog();
                return true;
            case R.id.action_settings:
                startActivity(Utils.navIntent(this, MyPreferencesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void calibrateDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View calibrateView = layoutInflater.inflate(R.layout.calibrate_dialog_layout, null);

        dialogBuilder = new AlertDialog.Builder(this).create();
        dialogBuilder.setTitle("Calibration");
        dialogBuilder.setIcon(R.mipmap.ic_launcher);
        dialogBuilder.setView(calibrateView);

        Button btnClose = calibrateView.findViewById(R.id.close);
        btnClose.setOnClickListener(v -> dialogBuilder.dismiss());

        dialogBuilder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Utils.showToast(
                            "Permission denied to access your location",
                            Toast.LENGTH_LONG, this);
                }
            }
        }
    }
}
