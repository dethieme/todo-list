package de.thieme;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import de.thieme.model.IToDoCRUDOperations;
import de.thieme.model.RetrofitToDoCRUDOperations;
import de.thieme.model.RoomToDoCRUDOperations;
import de.thieme.model.SyncedToDoCRUDOperations;

public class ToDoApplication extends Application {

    private static final String LOG_TAG = ToDoApplication.class.getSimpleName();
    private static final String BACKEND_URL = "http://10.0.2.2:8080/api/todos";

    private IToDoCRUDOperations crudOperations;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private boolean isBackendAccessible() {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(BACKEND_URL).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
            connection.getInputStream();
            return true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Backend not available. Got error: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public Future<IToDoCRUDOperations> getCRUDOperations() {
        CompletableFuture<IToDoCRUDOperations> future = new CompletableFuture<>();

        new Thread(() -> {
            if (isBackendAccessible()) {
                RoomToDoCRUDOperations localCrud = new RoomToDoCRUDOperations(this);
                RetrofitToDoCRUDOperations remoteCrud = new RetrofitToDoCRUDOperations();

                this.crudOperations = new SyncedToDoCRUDOperations(localCrud, remoteCrud);
            } else {
                this.crudOperations = new RoomToDoCRUDOperations(this);
            }

            future.complete(this.crudOperations);
        }).start();

        return future;
    }

    public boolean isOffline() {
        return this.crudOperations instanceof RoomToDoCRUDOperations;
    }
}
