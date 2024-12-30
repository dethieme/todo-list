package de.thieme.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.util.ArrayList;

import de.thieme.model.ToDo;

public class OverviewActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_CALL_DETAIL_VIEW_FOR_EDIT = 1;

    private ListView todoListView;
    private ArrayList<ToDo> todoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        todoListView = findViewById(R.id.todoListView);

        todoList = new ArrayList<>();
        todoList.add(new ToDo("Buy groceries", "Get milk, eggs, and bread"));
        todoList.add(new ToDo("Call Mom", "Check in with her"));
        todoList.add(new ToDo("Finish Android project", "Complete RecyclerView to-do list"));

        ListAdapter todoAdapter = new ToDoAdapter(this, todoList);
        todoListView.setAdapter(todoAdapter);

        todoListView.setOnItemClickListener((parent, view, position, id) -> {
            ToDo todo = todoList.get(position);
            this.showDetailViewForToDo(todo);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CALL_DETAIL_VIEW_FOR_EDIT) {
            if (resultCode == OverviewActivity.RESULT_OK) {
                showMessage("Gespeichert");
            } else {
                showMessage("Oh nononono");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void showDetailViewForToDo(ToDo todo) {
        Intent callDetailViewIntent = new Intent(this, DetailViewActivity.class);

        callDetailViewIntent.putExtra(DetailViewActivity.ARG_TODO, todo);
        startActivityForResult(callDetailViewIntent, REQUEST_CODE_CALL_DETAIL_VIEW_FOR_EDIT);
    }

    protected void showMessage(String message) {
        Snackbar.make(findViewById(R.id.rootView), message, Snackbar.LENGTH_SHORT).show();
    }
}
