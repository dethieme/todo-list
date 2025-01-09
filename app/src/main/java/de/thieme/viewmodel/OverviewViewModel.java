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

            sortTodos();

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void readAll() {
        processingState.setValue(ProcessingState.RUNNING_LONG);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (Exception ignored) {
            }

            List<ToDo> todos = crudOperations.readAll();
            Log.i("mama", String.valueOf(todos.size()));
            getTodos().addAll(todos);
            sortTodos();

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void update(ToDo todo) {
        processingState.setValue(ProcessingState.RUNNING);

        new Thread(() -> {
            boolean updated = crudOperations.update(todo);

            if (updated) {
                int todoPosition = getTodos().indexOf(todo);
                ToDo existingToDo = getTodos().get(todoPosition);

                existingToDo.setName(todo.getName());
                existingToDo.setDescription(todo.getDescription());
                existingToDo.setExpiry(todo.getExpiry());
                existingToDo.setIsDone(todo.isDone());
                existingToDo.setIsFavourite(todo.isFavourite());
                existingToDo.setContacts(todo.getContacts());
            }

            sortTodos();

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void delete(long id) {
        processingState.setValue(ProcessingState.RUNNING);

        new Thread(() -> {
            crudOperations.delete(id);
            getTodos().removeIf(todo -> todo.getId() == id);
            sortTodos();

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    private void sortTodos() {
        getTodos().sort(currentSortMode);
    }

    public void switchSortMode() {
        if (currentSortMode == SORT_BY_EXPIRY_AND_FAVOURITE) {
            currentSortMode = SORT_BY_FAVOURITE_AND_EXPIRY;
        } else {
            currentSortMode = SORT_BY_EXPIRY_AND_FAVOURITE;
        }

        sortTodos();
    }
}
