package de.thieme.viewmodel;

import android.view.inputmethod.EditorInfo;

import androidx.core.util.PatternsCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {

    private static final String LOG_TAG = LoginViewModel.class.getSimpleName();

    private String email = "";
    private String password = "";

    private MutableLiveData<Boolean> loginButtonEnabled = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> loginSuccessful = new MutableLiveData<>();

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

            validateLoginButtonState();
            return true;
        }

        return false;
    }

    public boolean checkPasswordFieldInputInvalid(int keyId) {
        if (keyId == EditorInfo.IME_ACTION_DONE) {
            if (password.length() != 6) {
                this.passwordErrorStatus.setValue("Passwort zu kurz.");
            }

            validateLoginButtonState();
            return true;
        }

        return false;
    }

    public boolean onEmailFieldInputChanged() {
        emailErrorStatus.setValue(null);
        credentialsErrorStatus.setValue(null);
        validateLoginButtonState();
        return true;
    }

    public boolean onPasswordFieldInputChanged() {
        passwordErrorStatus.setValue(null);
        credentialsErrorStatus.setValue(null);
        validateLoginButtonState();
        return true;
    }

    private void validateLoginButtonState() {
        loginButtonEnabled.setValue(!email.trim().isEmpty() && !password.isEmpty()
        );
    }

    public void performLogin() {
        // Simulate a login process
        if (email.equals("de.thieme@ostfalia.de") && password.equals("123456")) {
            loginSuccessful.setValue(true);
        } else {
            credentialsErrorStatus.setValue("Ungültige Anmeldedaten.");
            loginSuccessful.setValue(false);
        }
    }
}
