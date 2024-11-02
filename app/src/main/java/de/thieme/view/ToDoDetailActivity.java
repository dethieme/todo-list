package de.thieme.view;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.util.List;

import de.thieme.model.ToDo;

public class ToDoDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        // Assume To Do object is passed as a Serializable or Parcelable extra
        ToDo toDo = (ToDo) getIntent().getSerializableExtra("ToDo");

        // Find views by ID
        TextView nameTextView = findViewById(R.id.toDoName);
        TextView descriptionTextView = findViewById(R.id.toDoDescription);
        TextView expiryTextView = findViewById(R.id.toDoExpiry);
        CheckBox doneCheckBox = findViewById(R.id.doneCheckbox);
        ImageView favoriteImageView = findViewById(R.id.todoFavouriteIcon);
        LinearLayout contactsContainer = findViewById(R.id.contactsContainer);

        favoriteImageView.setOnClickListener(view -> {
            toDo.setFavourite(!toDo.isFavourite());

            if (toDo.isFavourite()) {
                favoriteImageView.setImageResource(R.drawable.baseline_star_24);
            } else {
                favoriteImageView.setImageResource(R.drawable.baseline_star_border_24);
            }
        });

        // Populate views
        nameTextView.setText(toDo.getName());
        descriptionTextView.setText(toDo.getDescription());
        expiryTextView.setText(String.valueOf(toDo.getExpiry()));
        doneCheckBox.setChecked(toDo.isDone());

        // Set favourite icon
        if (toDo.isFavourite()) {
            favoriteImageView.setImageResource(R.drawable.baseline_star_24);
        } else {
            favoriteImageView.setImageResource(R.drawable.baseline_star_border_24);
        }

        // Add contacts dynamically
        List<String> contacts = toDo.getContacts();

        if (contacts != null) {
            for (String contact : contacts) {
                TextView contactTextView = new TextView(this);
                contactTextView.setText(contact);
                contactTextView.setTextSize(14);
                contactsContainer.addView(contactTextView);
            }
        }
    }
}
