package de.thieme.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailViewBinding;

import de.thieme.model.ToDo;
import de.thieme.util.ImageViewUtil;

public class DetailViewActivity extends AppCompatActivity {

    protected final static String ARG_TODO = "ToDo";

    protected static final int RESULT_CODE_EDITED_OR_CREATED = 200;
    protected static final int RESULT_CODE_DELETED = 400;

    TextView expiryTextView;
    ImageView favoriteImageView;
    ListView contactsListView;

    private ToDo todo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        todo = (ToDo) getIntent().getSerializableExtra(ARG_TODO);

        if (todo == null) {
            todo = new ToDo();
        }

        ActivityDetailViewBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_detail_view);
        binding.setController(this);

        populateViews(todo);
        populateContacts(todo);
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

    public ToDo getTodo() {
        return todo;
    }

    public void saveTodo() {
        Intent returnIntent = new Intent();

        // todo.setExpiry(expiryTextView.getText());
        // todo.setIsFavourite(favoriteImageView.get);
        // todo.setContacts(contactsListView.ch;

        returnIntent.putExtra(ARG_TODO, todo);
        this.setResult(RESULT_CODE_EDITED_OR_CREATED, returnIntent);
        this.finish();
    }
}
