package com.gsnathan.compass;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Compass compass;
    private ImageView compassView;
    private TextView degreeView;
    private TextView coordView;
    private boolean useDarkTheme;
    private AlertDialog dialogBuilder;
    GPSTracker gps;
    private boolean isFirstRun;
    private float currentAzimuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        onFirstInstall();
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

    private void onFirstInstall() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isFirstRun = prefs.getBoolean("FIRSTINSTALL", true);
        if (isFirstRun) {
            startActivity(Utils.navIntent(this, MainIntroActivity.class));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("FIRSTINSTALL", false);
            editor.commit();
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
            Utils.showToast("Go to Material Compass settings to change location permission", Toast.LENGTH_SHORT, getApplicationContext());
        }
    }

    private void changeTheme() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        useDarkTheme = sp.getBoolean("theme_pref", false);

        if (useDarkTheme) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private void initViews() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        compassView = (ImageView) findViewById(R.id.image_dial);
        degreeView = (TextView) findViewById(R.id.degree_view);
        coordView = (TextView) findViewById(R.id.coord_view);
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
        Compass.CompassListener cl = new Compass.CompassListener() {

            @Override
            public void onNewAzimuth(float azimuth) {
                adjustArrow(azimuth);
            }
        };
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

        if(currentAzimuth == 0 || currentAzimuth == 360)
            cardDirect = "N";
        else if(currentAzimuth > 0 && currentAzimuth < 90)
            cardDirect = "NE";
        else if(currentAzimuth == 90)
            cardDirect = "E";
        else if(currentAzimuth > 90 && currentAzimuth < 180)
            cardDirect = "SE";
        else if(currentAzimuth == 180)
            cardDirect = "S";
        else if(currentAzimuth > 180 && currentAzimuth < 270)
            cardDirect = "SW";
        else if(currentAzimuth == 270)
            cardDirect = "W";
        else if(currentAzimuth > 270 && currentAzimuth < 360)
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
        int degrees = 0, minutes = 0, seconds = 0;
        double val = Math.abs(value), min = 0;
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

        Button btnClose = (Button) calibrateView.findViewById(R.id.close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.show();
    }
}
