package de.thieme.model;

import android.util.Log;

import java.util.List;

public class SyncedToDoCRUDOperations implements IToDoCRUDOperations {

    private static final String LOG_TAG = SyncedToDoCRUDOperations.class.getSimpleName();

    private final RoomToDoCRUDOperations localCRUD;
    private final RetrofitToDoCRUDOperations remoteCRUD;

    public SyncedToDoCRUDOperations(RoomToDoCRUDOperations roomToDoCRUDOperations, RetrofitToDoCRUDOperations retrofitToDoCRUDOperations) {
        this.localCRUD = roomToDoCRUDOperations;
        this.remoteCRUD = retrofitToDoCRUDOperations;
    }

    @Override
    public ToDo create(ToDo todo) {
        ToDo createdTodo = localCRUD.create(todo);
        remoteCRUD.create(createdTodo);

        return createdTodo;
    }

    @Override
    public List<ToDo> readAll() {
        return localCRUD.readAll();
    }

    @Override
    public boolean update(ToDo todo) {
        if (localCRUD.update(todo)) {
            remoteCRUD.update(todo);
        }

        return true;
    }

    @Override
    public boolean delete(long id) {
        if (localCRUD.delete(id)) {
            remoteCRUD.delete(id);
        }

        return true;
    }

    @Override
    public void synchronize() {
        try {
            List<ToDo> localTodos = localCRUD.readAll();

            if (!localTodos.isEmpty()) {
                deleteAllRemoteTodos();

                for (ToDo localTodo : localTodos) {
                    remoteCRUD.create(localTodo);
                }

                Log.d(LOG_TAG, "Synchronization succeeded: Local todos where transferred to remote database.");
            } else {
                List<ToDo> remoteTodos = remoteCRUD.readAll();

                for (ToDo remoteTodo : remoteTodos) {
                    localCRUD.create(remoteTodo);
                }

                Log.d(LOG_TAG, "Synchronization succeeded: Remote todos where transferred to local database.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Synchronization failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAllLocalTodos() {
        List<ToDo> localTodos = localCRUD.readAll();

        for (ToDo localTodo : localTodos) {
            localCRUD.delete(localTodo.getId());
        }
    }

    @Override
    public void deleteAllRemoteTodos() {
        List<ToDo> remoteTodos = remoteCRUD.readAll();

        for (ToDo remoteTodo : remoteTodos) {
            remoteCRUD.delete(remoteTodo.getId());
        }
    }
}
