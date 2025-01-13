package de.thieme.viewmodel;

import android.view.inputmethod.EditorInfo;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.GregorianCalendar;

import de.thieme.model.ToDo;

public class DetailViewViewModel extends ViewModel {

    public class DateHelper {

        private GregorianCalendar calendar = new GregorianCalendar();

        public DateHelper(long dateAsLong) {
            calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateAsLong);
        }

        public void setDate(int year, int month, int dayOfMonth) {
            calendar.set(year, month, dayOfMonth);
            toDo.setExpiry(calendar.getTimeInMillis());
            dateHelper.setValue(this);
        }

        public int getDayOfMonth() {
            return calendar.get(GregorianCalendar.DAY_OF_MONTH);
        }

        public int getMonth() {
            return calendar.get(GregorianCalendar.MONTH);
        }

        public int getYear() {
            return calendar.get(GregorianCalendar.YEAR);
        }
    }

    private MutableLiveData<Boolean> toDoValidOnSave = new MutableLiveData<>(false);
    private MutableLiveData<String> errorStatus = new MutableLiveData<>();

    private ToDo toDo;
    private MutableLiveData<DateHelper> dateHelper = new MutableLiveData<>(new DateHelper(System.currentTimeMillis()));

    public void setToDo(ToDo toDo) {
        this.toDo = toDo;
    }

    public ToDo getToDo() {
        return toDo;
    }

    public MutableLiveData<DateHelper> getDateHelper() {
        return dateHelper;
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
