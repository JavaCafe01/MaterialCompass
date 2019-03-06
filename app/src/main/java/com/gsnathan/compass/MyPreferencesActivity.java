package com.gsnathan.compass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.graphics.drawable.DrawableCompat;

public class MyPreferencesActivity extends AppCompatPreferenceActivity {

    private boolean useDarkTheme;
    private GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        useDarkTheme = sp.getBoolean("theme_pref", false);

        setupActionBar();
        addPreferencesFromResource(R.xml.preferences);
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        if (useDarkTheme) {
            setTheme(R.style.AppThemeDark);
            changeStatusBarColor();
            getListView().setBackgroundColor(getResources().getColor(R.color.colorGrey));
        }

        int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                2, getResources().getDisplayMetrics());

        int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                2, getResources().getDisplayMetrics());

        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                10,
                getResources().getDisplayMetrics());

        getListView().setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);

        SwitchPreference toggle = (SwitchPreference) findPreference("theme_pref");
        toggle.setOnPreferenceClickListener(preference -> {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            return false;
        });

        Preference button = findPreference("per_pref");

        if (Utils.permissionsGranted(this)) {
            button.setSummary("Location granted");
        } else {
            button.setSummary("Grant location");
        }

        button.setOnPreferenceClickListener(preference -> {
            if (Utils.permissionsGranted(getApplicationContext())) {
                Utils.showToast("Permission is granted", Toast.LENGTH_SHORT, getApplicationContext());
            } else {
                Utils.openPermissionSettings(getApplicationContext());
            }
            return true;
        });
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorBlack));
    }

    private void setupActionBar() {
        getLayoutInflater().inflate(R.layout.toolbar, findViewById(android.R.id.content));
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setElevation(4);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        if (useDarkTheme) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(getColoredArrow());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private Drawable getColoredArrow() {
        Drawable arrowDrawable = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        Drawable wrapped = DrawableCompat.wrap(arrowDrawable);

        // This should avoid tinting all the arrows
        arrowDrawable.mutate();
        DrawableCompat.setTint(wrapped, Color.WHITE);

        return wrapped;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        startActivity(Utils.navIntent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
