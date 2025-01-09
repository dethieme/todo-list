package de.thieme.model;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class RetrofitToDoCRUDOperations implements IToDoCRUDOperations {

    public interface ToDoRESTWebAPI {
        @POST("todos")
        public Call<ToDo> createToDo(@Body ToDo toDo);

        @GET("todos")
        public Call<List<ToDo>> readAllToDos();

        @PUT("todos/{id}")
        public Call<ToDo> updateToDo(@Path("id") long id, @Body ToDo toDo);

        @DELETE("todos/{id}")
        public Call<Boolean> deleteToDo(@Path("id") long id);
    }

    private ToDoRESTWebAPI toDoRESTWebAPI;

    public RetrofitToDoCRUDOperations() {
        Retrofit apiBase = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        toDoRESTWebAPI = apiBase.create(ToDoRESTWebAPI.class);
    }

    @Override
    public ToDo create(ToDo toDo) {
        try {
            return toDoRESTWebAPI.createToDo(toDo).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<ToDo> readAll() {
        try {
            return toDoRESTWebAPI.readAllToDos().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ToDo read(long id) {
        return null;
    }

    @Override
    public boolean update(ToDo todo) {
        try {
            toDoRESTWebAPI.updateToDo(todo.getId(), todo).execute();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long id) {
        try {
            return Boolean.TRUE.equals(toDoRESTWebAPI.deleteToDo(id).execute().body());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void synchronize() {
       // not implementation
    }
}
