package de.thieme.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailViewBinding;

import de.thieme.model.ToDo;
import de.thieme.util.ImageViewUtil;
import de.thieme.viewmodel.DetailViewViewModel;

public class DetailViewActivity extends AppCompatActivity {

    protected final static String ARG_TODO = "todo";

    protected static final int RESULT_CODE_EDITED_OR_CREATED = 200;
    protected static final int RESULT_CODE_DELETED = 400;

    private DetailViewViewModel viewModel;
    private ListView contactsListView;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.selectContact) {
            selectContact();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void selectContact() {
        Intent selectContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        selectContactLauncher.launch(selectContactIntent);
    }

    private void populateViews(ToDo todo) {
        TextView expiryTextView = findViewById(R.id.todoExpiry);
        expiryTextView.setText(String.valueOf(todo.getExpiry()));

        ImageView favoriteImageView = findViewById(R.id.todoIsFavorite);
        favoriteImageView.setOnClickListener(view -> {
            todo.setIsFavourite(!todo.isFavourite());
            ImageViewUtil.setFavouriteIcon(favoriteImageView, todo);
        });
        ImageViewUtil.setFavouriteIcon(favoriteImageView, todo);
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

    protected ActivityResultLauncher<Intent> selectContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    ToDo newTodo = (ToDo) result.getData().getSerializableExtra(DetailViewActivity.ARG_TODO);
                    selectContact();
                } else {

                }
            }
    );
}
