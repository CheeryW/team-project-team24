package com.cse110team24.walkwalkrevolution;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.cse110team24.walkwalkrevolution.models.Route;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoutesActivity extends AppCompatActivity {
    public static final String TAG = "RoutesActivity";

    public static final String LIST_SAVE_FILE = ".WWR_route_list_data";
    public static final String SAVE_FILE_KEY = "save_file";
    public static final int REQUEST_CODE = 11;

    private RouteAdapter adapter;
    private RecyclerView rvRoutes;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigationView;

    private List<Route> routes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getUIElements();
        setListeners();

        checkForExistingSavedRoutes();
        configureRecyclerViewAdapter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RouteDetailsActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            returnToHomeForWalk(data);
        } else if (requestCode == SaveRouteActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            handleNewRoute(data);
        }

    }

    // TODO: 2020-02-16 make sure this doesn;t break anything. Now save route doesn't save. Just passes back the route
    private void handleNewRoute(@Nullable Intent data) {
        Route newRoute = (Route) data.getSerializableExtra(SaveRouteActivity.NEW_ROUTE_KEY);
        routes.add(newRoute);
        Collections.sort(routes);
        adapter.notifyDataSetChanged();
    }

    private void returnToHomeForWalk(Intent data) {
        setResult(Activity.RESULT_OK, data);
        transitionWithAnimation();
    }

    public void launchGoToHomeActivity() {
        setResult(Activity.RESULT_CANCELED);
        transitionWithAnimation();
    }

    private void transitionWithAnimation() {
        RoutesManager.AsyncTaskSaveRoutes saver = new RoutesManager.AsyncTaskSaveRoutes();
        saver.execute(routes, this);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void getUIElements() {
        fab = findViewById(R.id.fab);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvRoutes = findViewById(R.id.recycler_view);

    }

    private void setListeners() {
        setFabOnClickListener();
        setBottomNavItemSelectedListener();
    }

    private void setFabOnClickListener() {
        fab.setOnClickListener(view -> {
            launchSaveActivity();
        });
    }

    private void launchSaveActivity() {
        Intent intent = new Intent(this, SaveRouteActivity.class);
        startActivityForResult(intent, SaveRouteActivity.REQUEST_CODE);
    }

    private void setBottomNavItemSelectedListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch(menuItem.getItemId()) {
                case R.id.action_home:
                    launchGoToHomeActivity();
                    break;

                case R.id.action_routes_list:
                    break;
            }
            return true;
        });
    }

    private void checkForExistingSavedRoutes() {
        String filename = getSaveFilename();
        try {
            List<Route> tempList = RoutesManager.readList(filename, this);
            routes.addAll(tempList);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "checkForExistingSavedRoutes: no routes found ", e);
        }
        Log.i(TAG, "checkForExistingSavedRoutes: list of saved routes found with size " + routes.size());
    }

    private String getSaveFilename() {
        String filename = getIntent().getStringExtra(SAVE_FILE_KEY);
        return (filename == null) ? LIST_SAVE_FILE : filename;
    }


    private void configureRecyclerViewAdapter() {
        Collections.sort(routes);
        adapter = new RouteAdapter(routes, this);
        rvRoutes.setAdapter(adapter);
        rvRoutes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }
}
