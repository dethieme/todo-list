package de.thieme.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.util.ArrayList;
import java.util.List;

import de.thieme.model.ToDoCRUDOperations;
import de.thieme.model.ToDo;

public class OverviewActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_CALL_DETAIL_VIEW_FOR_EDIT = 1;
    protected static final int REQUEST_CODE_CALL_DETAIL_VIEW_FOR_CREATE = 2;

    private ListView todoListView;
    private List<ToDo> todoList = new ArrayList<>();
    private ToDoAdapter todoListViewAdapter;

    private ToDoCRUDOperations crudOperations = new ToDoCRUDOperations();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        todoListView = findViewById(R.id.todoListView);
        todoListViewAdapter = new ToDoAdapter(this, todoList);
        todoListView.setAdapter(todoListViewAdapter);

        List<ToDo> todos = crudOperations.readAll();
        todoList.addAll(todos);
        todoListViewAdapter.notifyDataSetChanged();

        todoListView.setOnItemClickListener((adapterView, view, position, id) -> {
            ToDo selectedTodo = todoListViewAdapter.getItem(position);
            showDetailViewForToDo(selectedTodo);
        });

        FloatingActionButton saveTodoActionButton = findViewById(R.id.addTodoActionButton);
        saveTodoActionButton.setOnClickListener(view -> {
            Intent callDetailViewForCreateIntent = new Intent(this, DetailViewActivity.class);
            startActivityForResult(callDetailViewForCreateIntent, REQUEST_CODE_CALL_DETAIL_VIEW_FOR_CREATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CALL_DETAIL_VIEW_FOR_EDIT) {
            if (resultCode == OverviewActivity.RESULT_OK) {
                ToDo todoToBeEdited = (ToDo) data.getSerializableExtra(DetailViewActivity.ARG_TODO);
                boolean updated = crudOperations.update(todoToBeEdited);

                if (updated) {
                    int todoPosition = todoList.indexOf(todoToBeEdited);
                    ToDo existingToDo = todoList.get(todoPosition);
                    existingToDo.setName(todoToBeEdited.getName());
                    existingToDo.setDescription(todoToBeEdited.getDescription());
                    existingToDo.setExpiry(todoToBeEdited.getExpiry());
                    existingToDo.setIsFavourite(todoToBeEdited.isFavourite());
                    existingToDo.setContacts(todoToBeEdited.getContacts());

                    todoListViewAdapter.notifyDataSetChanged();
                }
            } else {
                showMessage("Oh nononono");
            }
        } else if (requestCode == REQUEST_CODE_CALL_DETAIL_VIEW_FOR_CREATE) {
            if (resultCode == OverviewActivity.RESULT_OK) {
                ToDo todoToBeCreated = (ToDo) data.getSerializableExtra(DetailViewActivity.ARG_TODO);
                ToDo createdTodo = crudOperations.create(todoToBeCreated);

                todoListViewAdapter.add(createdTodo);
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
