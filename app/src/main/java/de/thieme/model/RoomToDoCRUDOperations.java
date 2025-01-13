package de.thieme.model;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RoomToDoCRUDOperations implements IToDoCRUDOperations {

    public static class ListConverters {

        public static String SEPARATOR = ";;;";

        @TypeConverter
        public static ArrayList<String> fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return new ArrayList<>();
            }

            return new ArrayList<>(Arrays.asList(value.split(SEPARATOR)));
        }

        @TypeConverter
        public static String fromList(ArrayList<String> values) {
            if (values == null) {
                return "";
            }

            return values.stream().collect(Collectors.joining(SEPARATOR));
        }
    }

    @Dao
    public static interface SQLiteToDoCRUDOperations {
        @Insert
        public long createTodo(ToDo todo);

        @Query("SELECT * FROM to_dos")
        public List<ToDo> readAllTodos();

        @Query("SELECT * FROM to_dos WHERE id=(:id)")
        public ToDo readTodo(long id);

        @Update
        public void updateTodo(ToDo todo);

        @Delete
        public void deleteTodo(ToDo todo);
    }

    @Database(entities = {ToDo.class}, version = 1)
    public abstract static class ToDoDatabase extends RoomDatabase {
        public abstract SQLiteToDoCRUDOperations getDao();
    }

    private final ToDoDatabase database;

    public RoomToDoCRUDOperations(Context context) {
        database = Room.databaseBuilder(
                        context.getApplicationContext(),
                        ToDoDatabase.class,
                        "todos-db")
                .build();
    }

    @Override
    public ToDo create(ToDo todo) {
        long newId = database.getDao().createTodo(todo);
        todo.setId(newId);
        return todo;
    }

    @Override
    public List<ToDo> readAll() {
        return database.getDao().readAllTodos();
    }

    @Override
    public ToDo read(long id) {
        return database.getDao().readTodo(id);
    }

    @Override
    public boolean update(ToDo todo) {
        database.getDao().updateTodo(todo);
        return true;
    }

    @Override
    public boolean delete(long id) {
        readAll().stream()
                .filter(toDo -> toDo.getId() == id)
                .findFirst()
                .ifPresent(toDo -> database.getDao().deleteTodo(toDo));

        return true;
    }

    @Override
    public void synchronize() {
        // no implementation
    }

    @Override
    public void deleteAllLocalTodos() {
        readAll().forEach(todo -> delete(todo.getId()));
    }

    @Override
    public void deleteAllRemoteTodos() {
        // no implementation
    }
}
