package de.thieme.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailViewBinding;

import de.thieme.model.ToDo;
import de.thieme.util.ImageViewUtil;
import de.thieme.viewmodel.DetailViewViewModel;

public class DetailViewActivity extends AppCompatActivity {

    protected final static String ARG_TODO = "ToDo";

    protected static final int RESULT_CODE_EDITED_OR_CREATED = 200;
    protected static final int RESULT_CODE_DELETED = 400;

    private DetailViewViewModel viewModel;

    TextView expiryTextView;
    ImageView favoriteImageView;
    ListView contactsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        // Instantiate or reuse the view model.
        viewModel = new ViewModelProvider(this).get(DetailViewViewModel.class);

        if (viewModel.getToDo() == null) {
            ToDo todo = (ToDo) getIntent().getSerializableExtra(ARG_TODO);

            if (todo == null) {
                todo = new ToDo();
            }

            viewModel.setToDo(todo);
        }

        // Register the activity as observer.
        viewModel.getToDoValidOnSave().observe(this, valid -> {
            if (valid) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(ARG_TODO, this.viewModel.getToDo());

                this.setResult(RESULT_CODE_EDITED_OR_CREATED, returnIntent);
                this.finish();
            }
        });

        // Instantiate the view and pass the view model to it.
        ActivityDetailViewBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_detail_view);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        populateViews(this.viewModel.getToDo());
        populateContacts(this.viewModel.getToDo());
    }

    private void populateViews(ToDo todo) {
        expiryTextView = findViewById(R.id.todoExpiry);
        expiryTextView.setText(String.valueOf(todo.getExpiry()));

        favoriteImageView = findViewById(R.id.todoIsFavorite);
        favoriteImageView.setOnClickListener(view -> {
            todo.setIsFavourite(!todo.isFavourite());
            ImageViewUtil.setFavoriteIcon(favoriteImageView, todo);
        });
        ImageViewUtil.setFavoriteIcon(favoriteImageView, todo);
    }

    private void populateContacts(ToDo todo) {
        contactsListView = findViewById(R.id.contactsListView);

        for (String contact : todo.getContacts()) {
            TextView contactTextView = new TextView(this);
            contactTextView.setText(contact);
            contactTextView.setTextSize(16);
            contactsListView.addView(contactTextView);
        }
    }

    public void deleteTodo() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(ARG_TODO, this.viewModel.getToDo());

        this.setResult(RESULT_CODE_DELETED, returnIntent);
        this.finish();
    }
}
