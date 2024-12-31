package de.thieme.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import de.thieme.model.IToDoCRUDOperations;
import de.thieme.model.ToDo;

public class OverviewViewModel extends ViewModel {

    public static enum ProcessingState {
        RUNNING_LONG,
        RUNNING,
        DONE
    }

    private boolean initialized;
    private List<ToDo> toDos = new ArrayList<>();
    private IToDoCRUDOperations crudOperations;
    private MutableLiveData<ProcessingState> processingState = new MutableLiveData<>();

    public List<ToDo> getToDos() {
        return toDos;
    }

    public void setToDos(List<ToDo> toDos) {
        this.toDos = toDos;
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
            getToDos().add(createdTodo);

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
            getToDos().addAll(todos);

            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void read(long id) {
        getToDos().stream().filter(toDo -> toDo.getId() == id).findFirst().get();
    }

    public void update(ToDo todo) {
        processingState.setValue(ProcessingState.RUNNING);

        new Thread(() -> {
            boolean updated = crudOperations.update(todo);

            if (updated) {
                int todoPosition = getToDos().indexOf(todo);
                ToDo existingToDo = getToDos().get(todoPosition);

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
        crudOperations.delete(id);
    }

    public void switchSortMode() {

    }
}
