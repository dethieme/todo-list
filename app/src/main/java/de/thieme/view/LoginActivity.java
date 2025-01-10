package de.thieme.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityLoginBinding;

import de.thieme.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate or reuse the view model.
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Instantiate the view and pass the view model to it.
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        // Observe login success
        viewModel.getLoginSuccessful().observe(this, isSuccessful -> {
            if (isSuccessful) {
                 Intent overviewIntent = new Intent(this, OverviewActivity.class);
                startActivity(overviewIntent);
                finish();
            }
        });
    }
}
