package com.team1.easyhelp.home.fragment;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.Event;

import java.util.List;


public class QuestionListFragment extends Fragment {

    private static final String QUESTION_FRAGMENT = "QUESTION_FRAGMENT";
    private Context context;
    private View rootView;
    private SwipeRefreshLayout mSwipeLayout;

    private int user_id;
    private List<Event> events;
    private ListView QuesListView;


    public static QuestionListFragment newInstance(String text) {
        QuestionListFragment fragment = new QuestionListFragment();
        Bundle args = new Bundle();
        args.putString(QUESTION_FRAGMENT, text);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_question_list, container, false);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        context = rootView.getContext();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
