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

    public class DateHelper {

        private final GregorianCalendar calendar;

        public DateHelper(long dateAsLong) {
            calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateAsLong);
        }

        public void setDate(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
            calendar.set(year, month, dayOfMonth, hourOfDay, minute);
            toDo.setExpiry(calendar.getTimeInMillis());
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

    private MutableLiveData<Boolean> toDoValidOnSave = new MutableLiveData<>(false);
    private MutableLiveData<String> errorStatus = new MutableLiveData<>();
    private MutableLiveData<DateHelper> dateHelper = new MutableLiveData<>();

    private ToDo toDo;

    public void setToDo(ToDo toDo) {
        this.toDo = toDo;
        this.dateHelper.setValue(new DateHelper(toDo.getExpiry()));
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
