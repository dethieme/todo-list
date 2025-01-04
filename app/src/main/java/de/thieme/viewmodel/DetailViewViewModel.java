package de.thieme.viewmodel;

import android.view.inputmethod.EditorInfo;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import de.thieme.model.ToDo;

public class DetailViewViewModel extends ViewModel {

    private MutableLiveData<Boolean> toDoValidOnSave = new MutableLiveData<>(false);
    private MutableLiveData<String> errorStatus = new MutableLiveData<>();

    private ToDo toDo;

    public void setToDo(ToDo toDo) {
        this.toDo = toDo;
    }

    public ToDo getToDo() {
        return toDo;
    }

    public MutableLiveData<Boolean> getToDoValidOnSave() {
        return toDoValidOnSave;
    }

    public MutableLiveData<String> getErrorStatus() {
        return errorStatus;
    }

    public void saveToDo() {
        toDoValidOnSave.setValue(true);
    }

    public boolean checkFieldInputInvalid(int keyId) {

        if (keyId == EditorInfo.IME_ACTION_NEXT || keyId == EditorInfo.IME_ACTION_DONE) {
            String itemName = toDo.getName();

            if (itemName.length() < 4) {
                this.errorStatus.setValue("Name too short.");
                return true;
            }
        }

        return false;
    }

    public boolean onNameFieldInputChanged() {
        this.errorStatus.setValue(null);
        return false;
    }
}
