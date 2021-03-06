package com.cse110team24.walkwalkrevolution.firebase.firestore.adapters;


import android.util.Log;

import com.cse110team24.walkwalkrevolution.firebase.firestore.observers.teams.TeamsDatabaseServiceObserver;
import com.cse110team24.walkwalkrevolution.firebase.firestore.observers.teams.TeamsRoutesObserver;
import com.cse110team24.walkwalkrevolution.firebase.firestore.observers.teams.TeamsTeamStatusesObserver;
import com.cse110team24.walkwalkrevolution.firebase.firestore.observers.teams.TeamsTeamWalksObserver;
import com.cse110team24.walkwalkrevolution.firebase.firestore.observers.teams.TeamsTeammatesObserver;
import com.cse110team24.walkwalkrevolution.firebase.firestore.services.TeamsDatabaseService;
import com.cse110team24.walkwalkrevolution.models.route.Route;
import com.cse110team24.walkwalkrevolution.models.route.RouteEnvironment;
import com.cse110team24.walkwalkrevolution.models.route.WalkStats;
import com.cse110team24.walkwalkrevolution.models.team.ITeam;
import com.cse110team24.walkwalkrevolution.models.team.TeamAdapter;
import com.cse110team24.walkwalkrevolution.models.team.walk.TeamWalk;
import com.cse110team24.walkwalkrevolution.models.team.walk.TeamWalkStatus;
import com.cse110team24.walkwalkrevolution.models.team.walk.TeammateStatus;
import com.cse110team24.walkwalkrevolution.models.user.FirebaseUserAdapter;
import com.cse110team24.walkwalkrevolution.models.user.IUser;
import com.cse110team24.walkwalkrevolution.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * {@inheritDoc}
 * This type's database provider is Cloud Firestore. The document path for a team is
 * teams/\{team\}. The document path for a teammate is teams/{\team\}/teammates/\{teammate}.
 * The document path for a teammate route is teams/{\team\}/routes/\{route}.
 */
public class FireBaseFireStoreAdapterTeams implements TeamsDatabaseService {
    private static final String TAG = "WWR_FirebaseFirestoreAdapterTeams";

    public static final String TEAMS_COLLECTION_KEY = "teams";
    public static final String TEAMMATES_SUB_COLLECTION = "teammates";
    public static final String TEAM_ROUTES_SUB_COLLECTION_KEY = "routes";

    private CollectionReference mTeamsCollection;

    public FireBaseFireStoreAdapterTeams() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        mTeamsCollection = firebaseFirestore.collection(TEAMS_COLLECTION_KEY);
    }

    @Override
    public String createTeamInDatabase(IUser user) {
        Log.d(TAG, "createTeamInDatabase: creating team");
        // create new team document and update user's teamUid
        DocumentReference teamDocument = mTeamsCollection.document();
        String teamUid = teamDocument.getId();

        // create the teammates collection and the individual member document
        CollectionReference teamSubCollection = teamDocument.collection(TEAMMATES_SUB_COLLECTION);
        DocumentReference memberDocument = teamSubCollection.document(user.documentKey());
        memberDocument.set(user.userData()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "createTeamInDatabase: successfully created team document");
            } else {
                Log.e(TAG, "createTeamInDatabase: error creating team document", task.getException());
            }
        });

        return teamUid;
    }

    @Override
    public void addUserToTeam(IUser user, String teamUid) {
        // teamsCollection/teamDocument/teammatesCollection/userDocument
        DocumentReference teamDocument = mTeamsCollection.document(teamUid);
        CollectionReference teammatesCollection = teamDocument.collection(TEAMMATES_SUB_COLLECTION);
        teammatesCollection.document(user.documentKey()).set(user.userData()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "addUserToTeam: successfully updated team");
            } else {
                Log.e(TAG, "addUserToTeam: error updating team", task.getException());
            }
        });
    }

    @Override
    public void getUserTeam(String teamUid, String currentUserDisplayName) {
        DocumentReference documentReference = mTeamsCollection.document(teamUid);
        CollectionReference teammatesCollection = documentReference.collection(TEAMMATES_SUB_COLLECTION);
        teammatesCollection.orderBy("displayName").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.i(TAG, "getUserTeam: team successfully retrieved");
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                ITeam team = getTeamList(documents, currentUserDisplayName);
                notifyObserversTeamRetrieved(team);
            } else {
                Log.e(TAG, "getUserTeam: error getting user team", task.getException());
            }
        });
    }

    private ITeam getTeamList(List<DocumentSnapshot> documents, String currentUserDisplayName) {
        ITeam team = new TeamAdapter(new ArrayList<>());
        for (DocumentSnapshot member : documents) {
            String displayName = (String) member.get("displayName");
            // skip the current user
            if (currentUserDisplayName.equals(displayName)) continue;
            IUser user = FirebaseUserAdapter.builder()
                    .addDisplayName(displayName)
                    .addLatestWalkStatus(TeammateStatus.get(member.getString("status")))
                    .build();
            team.addMember(user);
        }
        return team;
    }

    @Override
    public void getUserTeamRoutes(String teamUid, String currentUserDisplayName, int routeLimitCount, DocumentSnapshot lastRoute) {
        Log.d(TAG, "getUserTeamRoutes: teamUid " + teamUid + " currentDisplayName " + currentUserDisplayName);
        // return routes ordered by name, skipping routes that current user owns
        Query routesQuery = getRoutesQuery(teamUid, currentUserDisplayName, routeLimitCount, lastRoute, 0);

        routesQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot resultDocs = task.getResult();
                Log.i(TAG, "getUserTeamRoutes: " + resultDocs.size() + " routes retrieved with names < current user");
                List<Route> routes = getRoutes(resultDocs);

                // try to grab more routes if not enough grabbed
                if (resultDocs.size() < routeLimitCount) {
                    getUserTeamRoutesGreaterThan(routes, teamUid, currentUserDisplayName, routeLimitCount - resultDocs.size(), lastRoute);
                    return;
                }

                getLastVisibleDoc(resultDocs, routes);

            } else {
                Log.e(TAG, "getUserTeamRoutes: could not retrieve team routes", task.getException());
            }
        });
    }

    private void getLastVisibleDoc(QuerySnapshot resultDocs, List<Route> routes) {
        DocumentSnapshot lastVisible = null;
        if (resultDocs.size() > 0) {
            lastVisible = resultDocs.getDocuments().get(resultDocs.size() - 1);
        }

        notifyObserversTeamRoutesRetrieved(routes, lastVisible);
    }

    private List<Route> getRoutes(QuerySnapshot resultDocs) {

        List<Route> routes = new ArrayList<>(resultDocs.size());
        resultDocs.getDocuments().forEach(documentSnapshot -> routes.add(buildRoute(documentSnapshot)));
        return routes;
    }

    // build the query as ordered by teammate name, limited by routeLimitCount. Skips current user via < and > clauses
    private Query getRoutesQuery(String teamUid, String currentUserDisplayName, int routeLimitCount, DocumentSnapshot lastRoute, int order) {
        Query routesQuery = mTeamsCollection
                .document(teamUid)
                .collection(TEAM_ROUTES_SUB_COLLECTION_KEY);

        if (order == 0) {
            routesQuery = routesQuery.whereLessThan("createdBy", currentUserDisplayName);
        } else {
            routesQuery = routesQuery.whereGreaterThan("createdBy", currentUserDisplayName);
        }
        routesQuery = routesQuery.orderBy("createdBy").limit(routeLimitCount);
        if (lastRoute != null) {
            routesQuery = routesQuery.startAfter(lastRoute);
        }
        return routesQuery;
    }

    // get the team routes for user's with names greater than currentUserDisplayName
    private void getUserTeamRoutesGreaterThan(List<Route> routes, String teamUid, String currentUserDisplayName, int routeLimitCount, DocumentSnapshot lastRoute) {
        Query routesQuery = getRoutesQuery(teamUid, currentUserDisplayName, routeLimitCount, lastRoute, 1);

        routesQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot resultDocs = task.getResult();
                Log.i(TAG, "getUserTeamRoutes: " + resultDocs.size() + " routes retrieved with names > current user");
                routes.addAll(getRoutes(resultDocs));
                getLastVisibleDoc(resultDocs, routes);
            } else {
                Log.e(TAG, "getUserTeamRoutes: could not retrieve team routes", task.getException());
            }
        });
    }

    private Route buildRoute(DocumentSnapshot routeDoc) {
        WalkStats stats = null;
        Object data = routeDoc.get("stats");
        if (data != null) {
            stats = buildWalkStats((Map<String, Object>) data);
        }
        return new Route.Builder(routeDoc.getString("title"))
                .addCreatorDisplayName(routeDoc.getString("createdBy"))
                .addWalkStats(stats)
                .addRouteEnvironment(buildRouteEnvironment((Map<String, Object>) routeDoc.get("environment")))
                .addNotes(routeDoc.getString("notes"))
                .addStartingLocation(routeDoc.getString("startingLocation"))
                .addRouteUid(routeDoc.getId())
                .build();
    }

    private WalkStats buildWalkStats(Map<String, Object> data) {
        Long steps = Utils.getValueOrNull("steps", data);
        Double distance = Utils.getValueOrNull("distance", data);
        Long timeElapsed = Utils.getValueOrNull("elapsedTimeMillis", data);
        Timestamp time = Utils.getValueOrNull("date", data);
        Calendar calendarInstance = Calendar.getInstance();
        calendarInstance.setTime(time.toDate());
        return new WalkStats(steps, timeElapsed, distance, calendarInstance);
    }

    private RouteEnvironment buildRouteEnvironment(Map<String, Object> data) {
        RouteEnvironment.Difficulty difficulty = data.get("difficulty") == null ? null : RouteEnvironment.Difficulty.valueOf((String) data.get("difficulty"));
        RouteEnvironment.RouteType routeType = data.get("routeType") == null ? null : RouteEnvironment.RouteType.valueOf((String) data.get("routeType"));
        RouteEnvironment.SurfaceType surfaceType = data.get("surfaceType") == null ? null : RouteEnvironment.SurfaceType.valueOf((String) data.get("surfaceType"));
        RouteEnvironment.TerrainType terrainType = data.get("terrainType") == null ? null : RouteEnvironment.TerrainType.valueOf((String) data.get("terrainType"));
        RouteEnvironment.TrailType trailType = data.get("trailType") == null ? null : RouteEnvironment.TrailType.valueOf((String) data.get("trailType"));
        return RouteEnvironment.builder()
                .addDifficulty(difficulty)
                .addRouteType(routeType)
                .addSurfaceType(surfaceType)
                .addTerrainType(terrainType)
                .addTrailType(trailType)
                .build();
    }

    @Override
    public void uploadRoute(String teamUid, Route route) {
        // upload to teams/{team}/routes/{route}
        DocumentReference routeDoc = mTeamsCollection
                .document(teamUid)
                .collection(TEAM_ROUTES_SUB_COLLECTION_KEY)
                .document();
        route.setRouteUid(routeDoc.getId());
        routeDoc.set(route.routeData()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "uploadRoute: route uploaded successfully");
            } else {
                Log.e(TAG, "uploadRoute: route failed to upload", task.getException());
            }
        });
    }

    @Override
    public void updateRoute(String teamUid, Route route) {
        // update in teams/{team}/routes/{route}
        DocumentReference routeDoc = mTeamsCollection
                .document(teamUid)
                .collection(TEAM_ROUTES_SUB_COLLECTION_KEY)
                .document(route.getRouteUid());
        routeDoc.set(route.routeData()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "uploadRoute: success updated route " + route);
            } else {
                Log.e(TAG, "uploadRoute: error updating route.", task.getException());
            }
        });
    }

    // if walk doesn't exist, creates it first
    @Override
    public String updateCurrentTeamWalk(TeamWalk teamWalk) {
        // update in teams/{team}/teamWalks/{teamWalk}
        if (Utils.checkNotNull(teamWalk.getWalkUid())) {
            mTeamsCollection.document(teamWalk.getTeamUid())
                    .collection("teamWalks")
                    .document(teamWalk.getWalkUid())
                    .update(teamWalk.dataInMapForm())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "updateCurrentTeamWalk: Success updating team walk");
                        } else {
                            Log.e(TAG, "updateCurrentTeamWalk: error updating team walk, will attempt creation", task.getException());
                            // try creating it if failed
                        }
                    });
            return teamWalk.getWalkUid();
        } else {
            return tryToCreateTeamWalkDoc(teamWalk);
        }
    }

    private String tryToCreateTeamWalkDoc(TeamWalk teamWalk) {
        DocumentReference docRef = mTeamsCollection.document(teamWalk.getTeamUid())
                .collection("teamWalks")
                .document();
        docRef.set(teamWalk.dataInMapForm()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "tryToCreateTeamWalkDoc: team walk document created");
            } else {
                Log.e(TAG, "tryToCreateTeamWalkDoc: error creating team walk document", task.getException());
            }
        });
        return docRef.getId();
    }

    private TeamWalk buildTeamWalk(DocumentSnapshot documentSnapshot) {
        Route proposedRoute = new Route.Builder("").addFieldsFromMap((Map<String, Object>) documentSnapshot.get("proposedRoute")).build();
        TeamWalk teamWalk = TeamWalk.builder()
                .addTeamUid(documentSnapshot.getString("teamUid"))
                .addProposedBy(documentSnapshot.getString("proposedBy"))
                .addProposedDateAndTime(documentSnapshot.getTimestamp("proposedDateAndTime"))
                .addProposedRoute(proposedRoute)
                .addWalkUid(documentSnapshot.getId())
                .addStatus(TeamWalkStatus.valueOf(documentSnapshot.getString("status")))
                .build();

        return teamWalk;
    }

    @Override
    public void getLatestTeamWalksDescendingOrder(String teamUid, int teamWalkLimitCt) {
        Query query = mTeamsCollection.document(teamUid).collection("teamWalks")
                .orderBy("proposedOn", Query.Direction.DESCENDING)
                .limit(teamWalkLimitCt);

        query.get().addOnCompleteListener(task -> {

            List<TeamWalk> teamWalks = new ArrayList<>();
            if (task.isSuccessful() && Utils.checkNotNull(task.getResult())) {
                Log.i(TAG, "getLatestTeamWalksDescendingOrder: success getting team walks");
                QuerySnapshot result = task.getResult();
                result.getDocuments().forEach(documentSnapshot -> {
                    teamWalks.add(buildTeamWalk(documentSnapshot));
                });
            } else {
                Log.e(TAG, "getLatestTeamWalksDescendingOrder: error getting team walks", task.getException());
            }
            notifyObserversTeamWalksRetrieved(teamWalks);

        });
    }

    @Override
    public void changeTeammateStatusForLatestWalk(IUser user, TeamWalk teamWalk, TeammateStatus changedStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put("displayName", user.getDisplayName());
        data.put("status", changedStatus.getReason());
        // teams/{team}/teamWalks/{teamWalk}/teammateStatuses/{teammate}
        DocumentReference teammateStatusDocument = mTeamsCollection
                .document(user.teamUid())
                .collection("teamWalks")
                .document(teamWalk.getWalkUid())
                .collection("teammateStatuses")
                .document(user.documentKey());
        teammateStatusDocument
                .update(data).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Log.i(TAG, "changeTeammateStatus: success updating teammate status");
                    } else {
                        tryToSetTeammateStatus(data, teammateStatusDocument);
                    }
        });

    }

    @Override
    public void getTeammateStatusesForTeamWalk(TeamWalk teamWalk, String teamUid) {
        // first get all teammate documents to know who are teammates
        mTeamsCollection.document(teamUid)
                .collection("teammates")
                .get().addOnCompleteListener(task -> {
                    SortedMap<String, String> data = new TreeMap<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "getTeammateStatusesForTeamWalk: success");
                        addToMap(task.getResult().getDocuments(), data, teamWalk, teamUid, 0);
                    } else {
                        Log.e(TAG, "getTeammateStatusesForTeamWalk: error", task.getException());
                        notifyObserversTeamWalkStatusesRetrieved(data);
                    }
        });
    }

    private void addToMap(List<DocumentSnapshot> documentSnapshots, SortedMap<String, String> data, TeamWalk teamWalk, String teamUid, int position) {
        Set<String> teammateNames = new HashSet<>();
        documentSnapshots.forEach(documentSnapshot -> teammateNames.add(documentSnapshot.getString("displayName")));
        // get all status documents
        mTeamsCollection.document(teamUid)
                .collection("teamWalks")
                .document(teamWalk.getWalkUid())
                .collection("teammateStatuses")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "addToMap: success");
                        // for each teammate with a status update it to the data map; remove them from set to know who is missing
                        task.getResult().getDocuments().forEach(documentSnapshot -> {
                            getUserNameAndStatus(data, documentSnapshot);
                            teammateNames.remove(documentSnapshot.getString("displayName"));
                        });
                        addPendingStatusForRemainingTeammates(teammateNames, data);
                        notifyObserversTeamWalkStatusesRetrieved(data);
                    } else {
                        Log.e(TAG, "addToMap: error", task.getException());
                        notifyObserversTeamWalkStatusesRetrieved(data);
                    }
        });
    }

    private void getUserNameAndStatus(SortedMap<String, String> data, DocumentSnapshot documentSnapshot) {
        String status = documentSnapshot.getString("status");
        String displayName = documentSnapshot.getString("displayName");
        data.put(displayName, status);
    }

    private void addPendingStatusForRemainingTeammates(Set<String> teammateNames, SortedMap<String, String> data) {
        String status = TeammateStatus.PENDING.getReason();
        teammateNames.forEach(teammateName -> data.put(teammateName, status));
    }

    private void tryToSetTeammateStatus(Map<String, Object> data, DocumentReference statusDocument) {
        statusDocument
                .set(data, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "tryToSetTeammateStatus: success setting teammate status for first time");
                    } else {
                        Log.e(TAG, "tryToSetTeammateStatus: error updating and setting teammate status", task.getException());
                    }
                });
    }

    private List<TeamsDatabaseServiceObserver> observers = new ArrayList<>();
    @Override
    public void notifyObserversTeamRetrieved(ITeam team) {
        observers.forEach(observer -> {
            if (observer instanceof TeamsTeammatesObserver) {
                ((TeamsTeammatesObserver) observer).onTeamRetrieved(team);
            }
        });
    }

    @Override
    public void notifyObserversTeamRoutesRetrieved(List<Route> routes, DocumentSnapshot lastRoute) {
        observers.forEach(observer -> {
            if (observer instanceof TeamsRoutesObserver) {
                ((TeamsRoutesObserver) observer).onRoutesRetrieved(routes, lastRoute);
            }
        });
    }

    @Override
    public void notifyObserversTeamWalksRetrieved(List<TeamWalk> walks) {
        observers.forEach(observer -> {
            if (observer instanceof TeamsTeamWalksObserver) {
                ((TeamsTeamWalksObserver) observer).onTeamWalksRetrieved(walks);
            }
        });
    }

    @Override
    public void notifyObserversTeamWalkStatusesRetrieved(SortedMap<String, String> statusData) {
        observers.forEach(observer -> {
            if (observer instanceof TeamsTeamStatusesObserver) {
                ((TeamsTeamStatusesObserver) observer).onTeamWalkStatusesRetrieved(statusData);
            }
        });
    }

    @Override
    public void register(TeamsDatabaseServiceObserver observer) {
        observers.add(observer);
    }


    @Override
    public void deregister(TeamsDatabaseServiceObserver observer) {
        observers.remove(observer);
    }
}
