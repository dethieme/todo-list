package de.thieme.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.dieschnittstelle.mobile.android.skeleton.R;
import java.util.List;
import de.thieme.model.ToDo;

public class DetailViewActivity extends AppCompatActivity {

    protected final static String ARG_TODO = "ToDo";

    TextView nameTextView;
    TextView descriptionTextView;
    TextView expiryTextView;
    CheckBox doneCheckBox;
    ImageView favoriteImageView;
    ListView contactsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        ToDo todo = (ToDo) getIntent().getSerializableExtra(ARG_TODO);
        assert todo != null : "'" + ARG_TODO + "' is null.";

        populateViews(todo);
        populateContacts(todo);

        findViewById(R.id.saveTodoActionButton).setOnClickListener(view -> {
           this.saveItem(todo);
        });
    }

    private void populateViews(ToDo todo) {
        nameTextView = findViewById(R.id.todoName);
        nameTextView.setText(todo.getName());

        descriptionTextView = findViewById(R.id.toDoDescription);
        descriptionTextView.setText(todo.getDescription());

        expiryTextView = findViewById(R.id.todoExpiry);
        expiryTextView.setText(String.valueOf(todo.getExpiry()));

        doneCheckBox = findViewById(R.id.todoIsDone);
        doneCheckBox.setChecked(todo.isDone());

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

    protected void saveItem(ToDo todo) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(ARG_TODO, todo);

        this.setResult(DetailViewActivity.RESULT_OK, returnIntent);
        this.finish();
    }
}
