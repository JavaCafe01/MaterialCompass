package com.gsnathan.compass;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class MainIntroActivity extends IntroActivity {

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addSlide(buildTitleSlide());
        addSlide(buildCalibSlide());
        addSlide(buildPermissionsSlide());
    }

    private SimpleSlide buildTitleSlide()
    {
        return new SimpleSlide.Builder()
                .title(R.string.app_name)
                .description(R.string.titleSlide_desc)
                .image(R.mipmap.ic_launcher)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build();
    }

    private SimpleSlide buildCalibSlide()
    {
        return new SimpleSlide.Builder()
                .title(R.string.calibSlide_title)
                .description(R.string.calib_mes)
                .image(R.drawable.calib_icon)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build();
    }

    private SimpleSlide buildPermissionsSlide()
    {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        return new SimpleSlide.Builder()
                .title(R.string.permissionSlide_title)
                .description(R.string.permissionSlide_desc)
                .image(R.drawable.permissions_pic)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .permissions(permissions)
                .build();
    }
}
