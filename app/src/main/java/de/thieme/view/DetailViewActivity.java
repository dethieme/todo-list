package de.thieme.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailViewBinding;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ItemContactBinding;

import java.util.List;

import de.thieme.model.Contact;
import de.thieme.model.ToDo;
import de.thieme.viewmodel.DetailViewViewModel;

public class DetailViewActivity extends AppCompatActivity {

    protected static final String ARG_TODO = "todo";
    protected static final int RESULT_CODE_EDITED_OR_CREATED = 200;
    protected static final int RESULT_CODE_DELETED = 400;

    private static final int REQUEST_CONTACT_PERMISSIONS = 42;
    private DetailViewViewModel viewModel;
    private ContactAdapter contactListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate or reuse the view model.
        viewModel = new ViewModelProvider(this).get(DetailViewViewModel.class);

        if (viewModel.getTodo() == null) {
            ToDo todo = (ToDo) getIntent().getSerializableExtra(ARG_TODO);

            if (todo == null) {
                todo = new ToDo();
            }

            viewModel.setTodo(todo);
        }

        // Register the activity as observer.
        viewModel.getTodoValidOnSave().observe(this, valid -> {
            if (valid) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(ARG_TODO, this.viewModel.getTodo());

                this.setResult(RESULT_CODE_EDITED_OR_CREATED, returnIntent);
                this.finish();
            }
        });

        // Instantiate the view and pass the view model to it.
        ActivityDetailViewBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_detail_view);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        binding.todoIsFavorite.setOnClickListener(view -> {
            viewModel.toggleFavourite();
        });
        binding.todoExpiry.setOnClickListener(view -> {
            showDatePickerDialog();
        });

        contactListViewAdapter = new ContactAdapter(this, viewModel.getTodo().getContacts());
        binding.contactListView.setAdapter(contactListViewAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addContact) {
            addContact();
            return true;
        } else if (item.getItemId() == R.id.deleteTodo) {
            deleteTodo();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showDatePickerDialog() {
        DetailViewViewModel.DateHelper dateHelper = viewModel.getDateHelper().getValue();

        new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {
                    new TimePickerDialog(
                            this,
                            (timePicker, hourOfDay, minute) -> {
                                dateHelper.setDate(year, month, day, hourOfDay, minute);
                            },
                            dateHelper.getHourOfDay(),
                            dateHelper.getMinute(),
                            true
                    ).show();
                },
                dateHelper.getYear(),
                dateHelper.getMonth(),
                dateHelper.getDayOfMonth()
        ).show();
    }

    public void deleteTodo() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Löschen");
        alert.setMessage("Willst du dieses To Do wirklich löschen?");

        alert.setPositiveButton("Löschen", (dialog, which) -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(ARG_TODO, this.viewModel.getTodo());

            this.setResult(RESULT_CODE_DELETED, returnIntent);
            this.finish();
        });
        alert.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());

        alert.show();
    }

    private void addContact() {
        Intent selectContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        selectContactLauncher.launch(selectContactIntent);
    }

    private void readContactDetails(Uri contactUri) {
        Cursor cursor = getContentResolver()
                .query(contactUri, null, null, null, null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String internalContactId = String.valueOf(cursor.getLong(columnIndex));
            int hasReadContactPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);

            if (hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSIONS);
            } else {
                if (!this.viewModel.getTodo().getContacts().contains(internalContactId)) {
                    this.viewModel.getTodo().getContacts().add(internalContactId);
                    contactListViewAdapter.notifyDataSetChanged();
                }
            }
        }

        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACT_PERMISSIONS
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addContact();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private final ActivityResultLauncher<Intent> selectContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == DetailViewActivity.RESULT_OK && result.getData() != null) {
                    readContactDetails(result.getData().getData());
                }
            }
    );

    protected class ContactAdapter extends ArrayAdapter<String> {

        public ContactAdapter(Context owner, List<String> contacts) {
            super(owner, R.layout.item_contact, contacts);
        }

        @NonNull
        @Override
        public View getView(int position, View recyclableContactView, @NonNull ViewGroup parent) {
            ItemContactBinding binding;
            View contactListView;

            // Reuse recyclableToDoView or inflate new view
            if (recyclableContactView == null) {
                binding = DataBindingUtil.inflate(
                        LayoutInflater.from(getContext()),
                        R.layout.item_contact,
                        null,
                        false
                );
                contactListView = binding.getRoot();
                contactListView.setTag(binding);
            } else {
                contactListView = recyclableContactView;
                binding = (ItemContactBinding) contactListView.getTag();
            }

            Contact contact = readContact(getItem(position));
            binding.setContact(contact);
            binding.setViewmodel(viewModel);

            binding.sendMail.setOnClickListener(view -> {
                Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
                selectorIntent.setData(Uri.parse("mailto:" + Uri.encode(contact.getMailAddress())));

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.getMailAddress()});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, viewModel.getTodo().getName());
                emailIntent.putExtra(Intent.EXTRA_TEXT, viewModel.getTodo().getDescription());
                emailIntent.setSelector(selectorIntent);
                startActivity(Intent.createChooser(emailIntent, "Mailversand: To Do"));
            });
            binding.sendSms.setOnClickListener(view -> {
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + contact.getMobileNumber()));
                smsIntent.putExtra(
                        "sms_body",
                        viewModel.getTodo().getName() + ":\n" + viewModel.getTodo().getDescription()
                );
                startActivity(smsIntent);
            });
            binding.deleteContact.setOnClickListener(view -> {
                viewModel.getTodo().getContacts().removeIf(c -> c.equals(contact.getId()));
                notifyDataSetChanged();
            });

            return contactListView;
        }

        public Contact readContact(String contactId) {
            Contact contact = new Contact(contactId);

            Cursor cursor = getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Email.ADDRESS
                    },
                    ContactsContract.Data.CONTACT_ID + " = ?",
                    new String[]{contactId},
                    null
            );

            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                if (contact.getName() == null) {
                    contact.setName(cursor.getString(index));
                }

                index = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
                String mimeType = cursor.getString(index);

                switch (mimeType) {
                    case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                        index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                        boolean isMobile = cursor.getInt(index) == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

                        if (isMobile) {
                            index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            contact.setMobileNumber(cursor.getString(index));
                        }
                        break;
                    case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                        index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                        contact.setMailAddress(cursor.getString(index));
                        break;
                    default:
                        break;
                }

                // Break early if all fields are populated
                if (contact.getName() != null
                        && contact.getMobileNumber() != null
                        && contact.getMailAddress() != null) {
                    break;
                }
            }

            cursor.close();

            return contact;
        }
    }
}
