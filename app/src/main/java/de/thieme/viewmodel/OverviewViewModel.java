package de.thieme.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.thieme.model.IToDoCRUDOperations;
import de.thieme.model.ToDo;

public class OverviewViewModel extends ViewModel {

    private static final String LOG_TAG = OverviewViewModel.class.getSimpleName();

    public enum ProcessingState {
        RUNNING_LONG,
        RUNNING,
        DONE
    }

    private final Comparator<ToDo> SORT_BY_EXPIRY_AND_FAVOURITE =
            Comparator.comparing(ToDo::isDone)
                    .thenComparing(ToDo::getExpiry)
                    .thenComparing(ToDo::isFavourite);

    private final Comparator<ToDo> SORT_BY_FAVOURITE_AND_EXPIRY =
            Comparator.comparing(ToDo::isDone)
                    .thenComparing(ToDo::isFavourite, Comparator.reverseOrder())
                    .thenComparing(ToDo::getExpiry);

    private boolean initialized;
    private List<ToDo> todos = new ArrayList<>();
    private IToDoCRUDOperations crudOperations;
    private MutableLiveData<ProcessingState> processingState = new MutableLiveData<>();
    private Comparator<ToDo> currentSortMode = SORT_BY_EXPIRY_AND_FAVOURITE;

    public List<ToDo> getTodos() {
        return todos;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public void setCrudOperations(IToDoCRUDOperations crudOperations) {
        this.crudOperations = crudOperations;
    }

    public MutableLiveData<ProcessingState> getProcessingState() {
        return processingState;
    }

    public void create(ToDo todo) {
        processingState.setValue(ProcessingState.RUNNING);

        new Thread(() -> {
            ToDo createdTodo = crudOperations.create(todo);
            getTodos().add(createdTodo);

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void readAll() {
        processingState.postValue(ProcessingState.RUNNING_LONG);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (Exception ignored) {
            }

            List<ToDo> todos = crudOperations.readAll();
            getTodos().addAll(todos);

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void update(ToDo todo) {
        processingState.setValue(ProcessingState.RUNNING);

        new Thread(() -> {
            if (crudOperations.update(todo)) {
                int todoPosition = getTodos().indexOf(todo);
                ToDo existingToDo = getTodos().get(todoPosition);

                existingToDo.setName(todo.getName());
                existingToDo.setDescription(todo.getDescription());
                existingToDo.setExpiry(todo.getExpiry());
                existingToDo.setIsDone(todo.isDone());
                existingToDo.setIsFavourite(todo.isFavourite());
                existingToDo.setContacts(todo.getContacts());
            }

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void delete(long id) {
        processingState.setValue(ProcessingState.RUNNING);

        new Thread(() -> {
            if (crudOperations.delete(id)) {
                getTodos().removeIf(todo -> todo.getId() == id);
            }

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void sortTodos() {
        getTodos().sort(currentSortMode);
    }

    public void switchSortMode() {
        if (currentSortMode == SORT_BY_EXPIRY_AND_FAVOURITE) {
            currentSortMode = SORT_BY_FAVOURITE_AND_EXPIRY;
            Log.i(LOG_TAG, "Switched sort mode to favourite and then expiry.");
        } else {
            currentSortMode = SORT_BY_EXPIRY_AND_FAVOURITE;
            Log.i(LOG_TAG, "Switched sort mode to expiry and then favourite.");
        }

        sortTodos();
    }

    public void synchronizeTodos() {
        processingState.setValue(ProcessingState.RUNNING_LONG);

        new Thread(() -> {
            crudOperations.synchronize();
            getTodos().clear();
            getTodos().addAll(crudOperations.readAll());

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void deleteAllLocalTodos() {
        processingState.setValue(ProcessingState.RUNNING);

        new Thread(() -> {
            crudOperations.deleteAllLocalTodos();
            getTodos().clear();

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void deleteAllRemoteTodos() {
        processingState.setValue(ProcessingState.RUNNING);

        new Thread(() -> {
            crudOperations.deleteAllRemoteTodos();
            processingState.postValue(ProcessingState.DONE);
        }).start();
    }
}
