package com.liberty.apps.studio.libertyvpn.view.activites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.florent37.expansionpanel.ExpansionLayout;
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection;
import com.liberty.apps.studio.libertyvpn.R;

import java.util.ArrayList;
import java.util.List;

public class faq_activity extends AppCompatActivity {

    private static String[] faq_questions, faq_answers;
    RecyclerView recyclerView;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        backButton = findViewById(R.id.faq_back_button);
        recyclerView = findViewById(R.id.recyclerView);
        faq_questions = getResources().getStringArray(R.array.faq_questions);
        faq_answers = getResources().getStringArray(R.array.faq_answers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final RecyclerAdapter adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(view -> finish());

        //fill with empty objects
        final List<Object> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(new Object());
        }
        adapter.setItems(list);
    }


    public final static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder> {

        private final List<Object> list = new ArrayList<>();

        private final ExpansionLayoutCollection expansionsCollection = new ExpansionLayoutCollection();

        public RecyclerAdapter() {
            expansionsCollection.openOnlyOne(true);
        }

        @Override
        public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return RecyclerHolder.buildFor(parent);
        }

        //Set the Faq details to the corresponding Question and answer Fields
        @Override
        public void onBindViewHolder(RecyclerHolder holder, int position) {
            holder.bind(list.get(position));
            holder.setTextViews(faq_questions[position], faq_answers[position]);

            expansionsCollection.add(holder.getExpansionLayout());
        }

        //Get the lenght pf the array of the Faq details...
        @Override
        public int getItemCount() {
            return faq_questions.length;
        }

        public void setItems(List<Object> items) {
            this.list.addAll(items);
            notifyDataSetChanged();
        }

        public final static class RecyclerHolder extends RecyclerView.ViewHolder {

            private static final int LAYOUT = R.layout.expansion_panel_recycler_cell;

            ExpansionLayout expansionLayout;
            TextView question_text, answer_text;

            //Initialization
            public RecyclerHolder(View itemView) {
                super(itemView);
                expansionLayout = itemView.findViewById(R.id.expansionLayout);

                question_text = (TextView) itemView.findViewById(R.id.question_text);
                answer_text = (TextView) itemView.findViewById(R.id.answer_text);
            }

            public static RecyclerHolder buildFor(ViewGroup viewGroup) {
                return new RecyclerHolder(LayoutInflater.from(viewGroup.getContext()).inflate(LAYOUT, viewGroup, false));
            }

            public void bind(Object object) {
                expansionLayout.collapse(false);
            }

            public ExpansionLayout getExpansionLayout() {
                return expansionLayout;
            }

            public void setTextViews(String ques, String ans) {
                question_text.setText(ques);
                answer_text.setText(ans);
            }
        }
    }
}
