/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ksoichiro.android.observablescrollview.samples;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;

/**
 * This is a sample of using RecyclerView that scrolls from the bottom.
 * It returns incorrect scrollY and commit 'a99a0de' fixed a part of this problem.
 * (Related to the issue #3)
 * It still returns incorrect scrollY and this is a known issue.
 * Please don't submit it as a new issue.
 * (Pull request to fix this is greatly appreciated!)
 */
public class ScrollFromBottomRecyclerViewActivity extends ActionBarActivity implements ObservableScrollViewCallbacks {

    private View mHeaderView;
    private View mToolbarView;
    private ObservableRecyclerView mRecyclerView;
    private int mBaseTranslationY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbarcontrolrecyclerview);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mHeaderView = findViewById(R.id.header);
        ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));
        mToolbarView = findViewById(R.id.toolbar);

        mRecyclerView = (ObservableRecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setScrollViewCallbacks(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(false);

        ArrayList<String> items = new ArrayList<String>();
        for (int i = 1; i <= 100; i++) {
            items.add("Item " + i);
        }
        View headerView = LayoutInflater.from(this).inflate(R.layout.recycler_header, null);
        mRecyclerView.setAdapter(new SimpleHeaderRecyclerAdapter(this, items, headerView));

        ViewTreeObserver vto = mRecyclerView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int count = mRecyclerView.getAdapter().getItemCount() - 1;
                int position = count == 0 ? 1 : count > 0 ? count : 0;
                mRecyclerView.scrollToPosition(position);
            }
        });
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (dragging) {
            int toolbarHeight = mToolbarView.getHeight();
            if (firstScroll) {
                float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
                if (-toolbarHeight < currentHeaderTranslationY && toolbarHeight < scrollY) {
                    mBaseTranslationY = scrollY;
                }
            }
            int headerTranslationY = Math.min(0, Math.max(-toolbarHeight, -(scrollY - mBaseTranslationY)));
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
        }
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        mBaseTranslationY = 0;

        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        int toolbarHeight = mToolbarView.getHeight();
        if (scrollState == ScrollState.UP) {
            if (toolbarHeight < mRecyclerView.getCurrentScrollY()) {
                if (headerTranslationY != -toolbarHeight) {
                    ViewPropertyAnimator.animate(mHeaderView).cancel();
                    ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
                }
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarHeight < mRecyclerView.getCurrentScrollY()) {
                if (headerTranslationY != 0) {
                    ViewPropertyAnimator.animate(mHeaderView).cancel();
                    ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
                }
            }
        }
    }
}