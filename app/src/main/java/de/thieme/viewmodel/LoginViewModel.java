package de.thieme.viewmodel;

import android.view.inputmethod.EditorInfo;

import androidx.core.util.PatternsCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.regex.Pattern;

import de.thieme.ToDoApplication;

public class LoginViewModel extends ViewModel {

    private String email = "";
    private String password = "";

    private MutableLiveData<Boolean> loginButtonEnabled = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> loginSuccessful = new MutableLiveData<>();
    private MutableLiveData<Boolean> progressBarVisible = new MutableLiveData<>(false);

    private MutableLiveData<String> passwordErrorStatus = new MutableLiveData<>();
    private MutableLiveData<String> emailErrorStatus = new MutableLiveData<>();
    private MutableLiveData<String> credentialsErrorStatus = new MutableLiveData<>();

    public MutableLiveData<Boolean> getLoginButtonEnabled() {
        return loginButtonEnabled;
    }

    public MutableLiveData<Boolean> getLoginSuccessful() {
        return loginSuccessful;
    }

    public MutableLiveData<String> getCredentialsErrorStatus() {
        return credentialsErrorStatus;
    }

    public MutableLiveData<String> getPasswordErrorStatus() {
        return passwordErrorStatus;
    }

    public MutableLiveData<String> getEmailErrorStatus() {
        return emailErrorStatus;
    }

    public MutableLiveData<Boolean> getProgressBarVisible() {
        return progressBarVisible;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean checkEmailFieldInputInvalid(int keyId) {
        if (keyId == EditorInfo.IME_ACTION_NEXT || keyId == EditorInfo.IME_ACTION_DONE) {
            if (email.isBlank() || !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
                this.emailErrorStatus.setValue("Ungültige E-Mail.");
            }

            return true;
        }

        return false;
    }

    public boolean checkPasswordFieldInputInvalid(int keyId) {
        if (keyId == EditorInfo.IME_ACTION_DONE) {
            if (!Pattern.matches("^[0-9]{6}$", this.password)) {
                this.passwordErrorStatus.setValue("Ungültiges Passwort.");
            }

            return true;
        }

        return false;
    }

    public boolean onEmailFieldInputChanged() {
        emailErrorStatus.setValue(null);
        credentialsErrorStatus.setValue(null);
        return true;
    }

    public boolean onPasswordFieldInputChanged() {
        passwordErrorStatus.setValue(null);
        credentialsErrorStatus.setValue(null);
        return true;
    }

    public void validateLoginButtonState() {
        loginButtonEnabled.setValue(!email.trim().isEmpty() && !password.trim().isEmpty());
    }

    public void performLogin(ToDoApplication application) {
        progressBarVisible.setValue(true);
        loginButtonEnabled.setValue(false);

        new Thread(() -> {
            try {
                // Simulate network delay
                Thread.sleep(2000);
            } catch (Exception ignored) {
            }

            loginSuccessful.postValue(application.authenticateUser(this.email, this.password));
        }).start();
    }
}
