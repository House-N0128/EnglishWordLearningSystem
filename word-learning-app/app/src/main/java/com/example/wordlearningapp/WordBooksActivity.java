package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class WordBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TextView tvBack;
    private Spinner spinnerDifficulty;
    private EditText etSearchBook;
    private boolean isAdmin;
    private JsonArray allBooks;
    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_books);

        isAdmin = "admin".equals(AuthManager.get().getRole());

        tvBack = findViewById(R.id.tv_back);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        etSearchBook = findViewById(R.id.et_search_book);

        tvBack.setOnClickListener(v -> finish());

        setupDifficultySpinner();
        setupSearchListener();
        setupSpinnerListener();

        if (isAdmin) {
            addAdminButton();
        }

        loadBooks();
    }

    private void setupDifficultySpinner() {
        List<String> difficultyLevels = new ArrayList<>();
        difficultyLevels.add("难度等级");
        difficultyLevels.add("初级");
        difficultyLevels.add("中级");
        difficultyLevels.add("高级");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                difficultyLevels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
    }

    private void setupSearchListener() {
        etSearchBook.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSpinnerListener() {
        spinnerDifficulty.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterBooks();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void addAdminButton() {
        ViewGroup root = (ViewGroup) recyclerView.getParent();
        Button addBtn = new Button(this);
        addBtn.setText("+ 新增词书");
        addBtn.setTextColor(0xFFFFFFFF);
        addBtn.setBackgroundColor(0xFF52e8bc);
        addBtn.setTextSize(15);
        addBtn.setPadding(14, 12, 14, 12);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addBtn.setLayoutParams(params);
        addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditBookActivity.class)));

        if (root instanceof ViewGroup) {
            ((ViewGroup) root).addView(addBtn, 2);
        }
    }

    private void loadBooks() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/wordbooks");
                if (res.get("code").getAsInt() == 200) {
                    allBooks = res.getAsJsonArray("data");
                    runOnUiThread(() -> filterBooks());
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvEmpty.setText("加载失败");
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void filterBooks() {
        if (allBooks == null) return;

        String searchText = etSearchBook.getText().toString().trim().toLowerCase();
        String selectedDifficulty = spinnerDifficulty.getSelectedItem().toString();

        JsonArray filteredBooks = new JsonArray();

        for (int i = 0; i < allBooks.size(); i++) {
            JsonObject book = allBooks.get(i).getAsJsonObject();
            String bookName = book.has("wordBookName") ? book.get("wordBookName").getAsString() : "";
            String difficulty = book.has("difficultyLevel") ? book.get("difficultyLevel").getAsString() : "";

            boolean matchSearch = searchText.isEmpty() || bookName.toLowerCase().contains(searchText);
            boolean matchDifficulty = "难度等级".equals(selectedDifficulty) || difficulty.equals(selectedDifficulty);

            if (matchSearch && matchDifficulty) {
                filteredBooks.add(book);
            }
        }

        if (filteredBooks.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new BookAdapter(filteredBooks);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(filteredBooks);
            }
        }
    }

    private class BookAdapter extends RecyclerView.Adapter<BookAdapter.VH> {
        private JsonArray data;

        BookAdapter(JsonArray data) {
            this.data = data;
        }

        void updateData(JsonArray newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_word_book, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH holder, int pos) {
            JsonObject b = data.get(pos).getAsJsonObject();

            holder.tvBookName.setText(b.has("wordBookName") ? b.get("wordBookName").getAsString() : "");
            holder.tvDifficulty.setText((b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "")
                    + " | " + (b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词");
            holder.tvCreateTime.setText("创建时间: " + (b.has("createTime") && !b.get("createTime").isJsonNull()
                    ? trimDate(b.get("createTime").getAsString()) : "未知"));

            String bookId = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";

            holder.btnViewDetail.setOnClickListener(v -> {
                Intent intent = new Intent(WordBooksActivity.this, BookDetailActivity.class);
                intent.putExtra("bookId", bookId);
                startActivity(intent);
            });

            if (isAdmin) {
                holder.btnViewDetail.setText("编辑");
                holder.btnViewDetail.setBackgroundColor(0xFF52e8bc);

                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(v.getContext())
                            .setTitle("确认下架")
                            .setMessage("确定下架词书\"" + holder.tvBookName.getText() + "\"？")
                            .setPositiveButton("确定", (d, w) -> {
                                new Thread(() -> {
                                    try {
                                        JsonObject res = ApiClient.get().delete("/api/wordbooks/" + bookId, null);
                                        runOnUiThread(() -> {
                                            Toast.makeText(WordBooksActivity.this,
                                                    res.has("message") ? res.get("message").getAsString() : "已下架",
                                                    Toast.LENGTH_SHORT).show();
                                            loadBooks();
                                        });
                                    } catch (Exception e) {
                                        runOnUiThread(() -> Toast.makeText(WordBooksActivity.this, "网络错误", Toast.LENGTH_SHORT).show());
                                    }
                                }).start();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                });
            } else {
                holder.btnViewDetail.setText("查看详情");
                holder.btnViewDetail.setBackgroundColor(0xFF1E90FF);
                holder.btnDelete.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvBookName;
            TextView tvDifficulty;
            TextView tvCreateTime;
            Button btnViewDetail;
            Button btnDelete;

            VH(View v) {
                super(v);
                tvBookName = v.findViewById(R.id.tv_book_name);
                tvDifficulty = v.findViewById(R.id.tv_difficulty);
                tvCreateTime = v.findViewById(R.id.tv_create_time);
                btnViewDetail = v.findViewById(R.id.btn_view_detail);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }

    private String trimDate(String dt) {
        return dt != null && dt.length() >= 10 ? dt.substring(0, 10) : dt;
    }
}
