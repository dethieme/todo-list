package de.thieme.viewmodel;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import de.thieme.model.ToDo;

public class OverviewActivityViewModel extends ViewModel {

    private boolean initialized;
    private List<ToDo> toDos = new ArrayList<>();

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
}
