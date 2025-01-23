package de.thieme.viewmodel;

import android.icu.text.SimpleDateFormat;
import android.view.inputmethod.EditorInfo;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.thieme.model.ToDo;

public class DetailViewViewModel extends ViewModel {

    private final MutableLiveData<Boolean> todoValidOnSave = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorStatus = new MutableLiveData<>();
    private final MutableLiveData<DateHelper> dateHelper = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFavourite = new MutableLiveData<>();

    private ToDo todo;

    public void setTodo(ToDo todo) {
        this.todo = todo;
        this.dateHelper.setValue(new DateHelper(todo.getExpiry()));
        this.isFavourite.setValue(todo.isFavourite());
    }

    public ToDo getTodo() {
        return todo;
    }

    public MutableLiveData<DateHelper> getDateHelper() {
        return dateHelper;
    }

    public MutableLiveData<Boolean> getTodoValidOnSave() {
        return todoValidOnSave;
    }

    public MutableLiveData<String> getErrorStatus() {
        return errorStatus;
    }

    public MutableLiveData<Boolean> getIsFavourite() {
        return isFavourite;
    }

    public void toggleFavourite() {
        if (todo != null) {
            boolean newValue = !todo.isFavourite();
            todo.setIsFavourite(newValue);
            isFavourite.setValue(newValue);
        }
    }

    public void saveToDo() {
        todoValidOnSave.setValue(true);
    }

    public boolean checkNameFieldInputInvalid(int keyId) {
        if (keyId == EditorInfo.IME_ACTION_NEXT || keyId == EditorInfo.IME_ACTION_DONE) {
            if (todo.getName().length() < 4) {
                this.errorStatus.setValue("Name zu kurz.");
                return true;
            }
        }

        return false;
    }

    public boolean onNameFieldInputChanged() {
        this.errorStatus.setValue(null);
        return false;
    }

    public class DateHelper {

        private final GregorianCalendar calendar;

        public DateHelper(long dateAsLong) {
            calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateAsLong);
        }

        public void setDate(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
            calendar.set(year, month, dayOfMonth, hourOfDay, minute);
            todo.setExpiry(calendar.getTimeInMillis());
            dateHelper.setValue(this);
        }

        public String getDateTimeString() {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault());
            return format.format(new Date(calendar.getTimeInMillis())) + " Uhr";
        }

        public int getYear() {
            return calendar.get(GregorianCalendar.YEAR);
        }

        public int getMonth() {
            return calendar.get(GregorianCalendar.MONTH);
        }

        public int getDayOfMonth() {
            return calendar.get(GregorianCalendar.DAY_OF_MONTH);
        }

        public int getHourOfDay() {
            return calendar.get(GregorianCalendar.HOUR_OF_DAY);
        }

        public int getMinute() {
            return calendar.get(GregorianCalendar.MINUTE);
        }
    }
}
