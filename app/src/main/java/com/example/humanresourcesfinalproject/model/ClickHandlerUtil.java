package com.example.humanresourcesfinalproject.model;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class ClickHandlerUtil {
    public interface ClickCallbacks {
        void onShortClick(int position);
        void onLongClick(int position);
    }

    public static void setupListViewClicks(ListView listView, ClickCallbacks callbacks) {
        final Handler handler = new Handler();
        final int LONG_PRESS_DELAY = 500;

        // Create a container class to hold our mutable state
        class ClickState {
            Runnable longPressRunnable = null;
            boolean isLongPressHandled = false;
            int pressedPosition = AdapterView.INVALID_POSITION;
        }

        final ClickState state = new ClickState();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (!state.isLongPressHandled) {
                callbacks.onShortClick(position);
            }
            state.isLongPressHandled = false;
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int position = listView.pointToPosition((int) event.getX(), (int) event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        state.pressedPosition = position;
                        state.isLongPressHandled = false;
                        state.longPressRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (state.pressedPosition != AdapterView.INVALID_POSITION) {
                                    state.isLongPressHandled = true;
                                    callbacks.onLongClick(state.pressedPosition);
                                }
                            }
                        };
                        handler.postDelayed(state.longPressRunnable, LONG_PRESS_DELAY);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (state.longPressRunnable != null) {
                            handler.removeCallbacks(state.longPressRunnable);
                        }
                        state.pressedPosition = AdapterView.INVALID_POSITION;
                        break;
                }
                return false;
            }
        });
    }
}
