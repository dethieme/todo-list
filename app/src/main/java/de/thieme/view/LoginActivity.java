package de.thieme.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityLoginBinding;

import de.thieme.ToDoApplication;
import de.thieme.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        LoginViewModel viewModel = new LoginViewModel();

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.getLoginSuccessful().observe(this, isSuccessful -> {
            if (isSuccessful) {
                this.setResult(RESULT_OK);
                this.finish();
            } else {
                viewModel.getCredentialsErrorStatus().setValue("Ungültige Anmeldedaten.");
                viewModel.getProgressBarVisible().setValue(false);
                viewModel.getLoginButtonEnabled().setValue(true);
            }
        });

        binding.loginButton.setOnClickListener(view -> {
            viewModel.performLogin((ToDoApplication) getApplication());
        });
    }
}
