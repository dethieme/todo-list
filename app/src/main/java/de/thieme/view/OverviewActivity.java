package de.thieme.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.util.ArrayList;
import java.util.List;

import de.thieme.model.LocalRoomToDoCRUDOperations;
import de.thieme.model.ToDoCRUDOperations;
import de.thieme.model.ToDo;

public class OverviewActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_CALL_DETAIL_VIEW_FOR_EDIT = 1;
    protected static final int REQUEST_CODE_CALL_DETAIL_VIEW_FOR_CREATE = 2;

    private ProgressBar progressBar;

    private ListView todoListView;
    private List<ToDo> todoList = new ArrayList<>();
    private ToDoAdapter todoListViewAdapter;

    private LocalRoomToDoCRUDOperations crudOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        crudOperations = new LocalRoomToDoCRUDOperations(this);

        todoListView = findViewById(R.id.todoListView);
        todoListViewAdapter = new ToDoAdapter(this, todoList);
        todoListView.setAdapter(todoListViewAdapter);

        // Handle the progress bar.
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ListView.VISIBLE);

        // Load to do list.
        new Thread(() -> {
            List<ToDo> todos = crudOperations.readAll();
            todoList.addAll(todos);

            runOnUiThread(() -> {
                progressBar.setVisibility(ListView.GONE);
                todoListViewAdapter.notifyDataSetChanged();
            });
        }).start();

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

                new Thread(() -> {
                    boolean updated = crudOperations.update(todoToBeEdited);

                    runOnUiThread(() -> {
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
                    });
                }).start();
            } else {
                showMessage("Oh nononono");
            }
        } else if (requestCode == REQUEST_CODE_CALL_DETAIL_VIEW_FOR_CREATE) {
            if (resultCode == OverviewActivity.RESULT_OK) {
                ToDo todoToBeCreated = (ToDo) data.getSerializableExtra(DetailViewActivity.ARG_TODO);

                new Thread(() -> {
                    ToDo createdTodo = crudOperations.create(todoToBeCreated);
                    runOnUiThread(() -> todoListViewAdapter.add(createdTodo));
                }).start();
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
