package com.cse110team24.walkwalkrevolution;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.cse110team24.walkwalkrevolution.models.Route;
import com.cse110team24.walkwalkrevolution.models.RouteEnvironment;
import com.cse110team24.walkwalkrevolution.models.WalkStats;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RoutesManagerTest {
    private static final String TEST_FILE = ".WWR_storage_test";

    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setup() {
        scenario = scenarioRule.getScenario();
    }

    @Test
    public void testRouteTitleOnly() {
        scenario.onActivity(activity -> {
            Context context = activity.getApplicationContext();
            List<Route> routes = new ArrayList<>();
            String title = "Test title";
            routes.add(new Route(title));
            try {
                RoutesManager.writeList(routes, TEST_FILE, context);
            } catch (IOException e) {
                fail();
            }

            try {
                List<Route> readRoutes = RoutesManager.readList(TEST_FILE, context);
                assertEquals(new Route(title), readRoutes.get(0));
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        });
    }

    @Test
    public void testAllFields() {
        scenario.onActivity(activity -> {
            RouteEnvironment env = getEnvironment();
            WalkStats stats = new WalkStats(500, 100_000, 1.2, new GregorianCalendar());
            Route route = new Route("title")
                    .setStartingLocation("Test World")
                    .setStats(stats)
                    .setEnvironment(env)
                    .setFavorite(true)
                    .setNotes("Testing reading a route");
            List<Route> routes = new ArrayList<>();

            Context context = activity.getApplicationContext();
            routes.add(route);
            try {
                RoutesManager.writeList(routes, TEST_FILE, context);
            } catch (IOException e) {
                fail();
            }

            try {
                List<Route> readRoutes = RoutesManager.readList(TEST_FILE, context);
                assertEquals(route, readRoutes.get(0));
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        });
    }

    private RouteEnvironment getEnvironment() {
        RouteEnvironment environment = new RouteEnvironment();
        environment.setDifficulty(RouteEnvironment.Difficulty.HARD);
        environment.setRouteType(RouteEnvironment.RouteType.LOOP);
        environment.setSurfaceType(RouteEnvironment.SurfaceType.EVEN);
        environment.setTerrainType(RouteEnvironment.TerrainType.FLAT);
        environment.setTrailType(RouteEnvironment.TrailType.TRAIL);
        return environment;
    }
}
