package com.rfw.hotkey.ui.gamepad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rfw.hotkey.R;
import com.rfw.hotkey.net.ConnectionManager;
import com.rfw.hotkey.util.misc.LoopedExecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity for gamepad layout and
 * editing gamepad keys
 *
 * @author Farhan Kabir
 */
public class GamepadActivity extends AppCompatActivity {

    private ArrayList<ImageButton> buttons;
    private ArrayList<String> actions;
    private ArrayList<String> pressedButtons;
    private boolean editLayout;
    private ImageButton editButton;
    private ImageButton saveButton;
    private ImageButton cancelButton;
    private ImageButton menuButton;
    private GridView keyBoardGrid;

    private int xDelta;
    private int yDelta;
    private int widthV;
    private int heightV;
    private int RSX;
    private int RSY;
    private int curIndex;
    private final int rightStickLimit = 150;
    private boolean setRightStick;

    /**
     * Keyboard layout for any keyboard keys
     * to simulate any key of keyboard.
     * Add new keys and map them in server accordingly.
     */


    static final String[] keyboardKeys = new String[]{
            "0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9",

            "ESC", "ALT", "CTRL", "SHIFT", "DEL",
            "INS", "HOME", "END", "PGUP", "PGDN",

            "A", "b", "c", "D", "E",
            "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o",
            "p", "q", "r", "S", "t",
            "u", "v", "W", "x", "y",
            "z","UP","DOWN","LEFT","RIGHT"

    };

    final List<String> gridKeys = new ArrayList<String>(Arrays.asList(keyboardKeys));

    private static final long BUTTON_PRESS_DELAY = 100; // milliseconds

    private LoopedExecutor buttonPresser = null;
    private LoopedExecutor rightStickHandler = null;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamepad);

        // make full screen and hide status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );

        editLayout = false;
        setRightStick = true;


        buttons = new ArrayList<ImageButton>();
        actions = new ArrayList<>();
        pressedButtons = new ArrayList<>();

        buttons.add(findViewById(R.id.leftLeftID));
        buttons.add(findViewById(R.id.leftBotID));
        buttons.add(findViewById(R.id.leftRightID));
        buttons.add(findViewById(R.id.leftTopID));
        buttons.add(findViewById(R.id.rightLeftID));
        buttons.add(findViewById(R.id.rightBotID));
        buttons.add(findViewById(R.id.rightRightID));
        buttons.add(findViewById(R.id.rightTopID));
        buttons.add(findViewById(R.id.LBID));
        buttons.add(findViewById(R.id.LTID));
        buttons.add(findViewById(R.id.LSID));
        buttons.add(findViewById(R.id.RBID));
        buttons.add(findViewById(R.id.RTID));
        buttons.add(findViewById(R.id.RSID));
        buttons.add(findViewById(R.id.menuID));
        buttons.add(findViewById(R.id.startID));
        buttons.add(findViewById(R.id.leftStickTopID));
        buttons.add(findViewById(R.id.leftStickLeftID));
        buttons.add(findViewById(R.id.leftStickBotID));
        buttons.add(findViewById(R.id.leftStickRightID));
        buttons.add(findViewById(R.id.rightStickID));
        cancelButton = findViewById(R.id.cancelButtonID);
        keyBoardGrid = findViewById(R.id.keyGridID);
        editButton = findViewById(R.id.editButtonID);
        saveButton = findViewById(R.id.saveButtonID);
        menuButton = findViewById(R.id.gamepadMenuID);

        initialization();

        for (int i = 0; i < buttons.size(); i++)
            buttons.get(i).setOnTouchListener(onTouchListener());

        Intent intent = getIntent();
        if (intent.getBooleanExtra("gamepadEditLayout", false)) {
            editLayout = true;
        }

        if (editLayout) {
            editButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            menuButton.setVisibility(View.INVISIBLE);

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            cancelButton.setOnClickListener(view -> {
                finish();
                Intent intnt = new Intent(this, GamepadActivity.class);
                intnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intnt);
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (curIndex != -1) {
                        keyBoardGrid.setVisibility(View.VISIBLE);

                        keyBoardGrid.setAdapter(new ArrayAdapter<String>(
                                getApplicationContext(), android.R.layout.simple_list_item_1, gridKeys) {
                            public View getView(int position, View convertView, ViewGroup parent) {

                                View view = super.getView(position, convertView, parent);

                                TextView tv = (TextView) view;


                                tv.setTextColor(Color.WHITE);

                                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
                                );
                                tv.setLayoutParams(lp);

                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();

                                tv.setLayoutParams(params);

                                tv.setGravity(Gravity.CENTER);

                                tv.setText(gridKeys.get(position));

                                if (tv.getText().toString().equals(actions.get(curIndex)))
                                    tv.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                else tv.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

                                return tv;
                            }
                        });

                        keyBoardGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String buttonName = parent.getItemAtPosition(position).toString();
                                actions.set(curIndex, buttonName);
                                Toast.makeText(getApplicationContext(),
                                        "Altered with " + buttonName, Toast.LENGTH_SHORT).show();
                                keyBoardGrid.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else Toast.makeText(getApplicationContext(),
                            "Invalid key selected", Toast.LENGTH_SHORT).show();
                }
            });

        }

        menuButton.setOnClickListener(view -> {
            finish();
            Intent intnt = new Intent(this, GamepadActivity.class);
            intnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            intnt.putExtra("gamepadEditLayout", true);
            startActivity(intnt);
        });
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {

            @SuppressLint({"ClickableViewAccessibility", "ResourceAsColor"})
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (editLayout) {
                    if (!view.getTag().equals("rightStick")) {
                        if (curIndex != -1)
                            buttons.get(curIndex).setColorFilter(getResources().getColor(R.color.colorAccent));
                        curIndex = Integer.parseInt((String) view.getTag());
                        buttons.get(curIndex).clearColorFilter();
                    }
                } else {
                    if (!view.getTag().equals("rightStick")) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (!actions.get(Integer.parseInt((String) view.getTag())).equals("") && !pressedButtons.contains(actions.get(Integer.parseInt((String) view.getTag())))) {
                                    pressedButtons.add(actions.get(Integer.parseInt((String) view.getTag())));
                                }
                                if (buttonPresser == null && !actions.get(Integer.parseInt((String) view.getTag())).equals("")) {
                                    buttonPresser = new LoopedExecutor(BUTTON_PRESS_DELAY) {
                                        @Override
                                        public void task() {
                                            JSONObject gamepad = new JSONObject();
                                            try {
                                                gamepad.put("type", "macro");
                                                gamepad.put("size", Integer.toString(pressedButtons.size()));
                                                for (int i = 0; i < pressedButtons.size(); i++) {
                                                    gamepad.put(Integer.toString(i), pressedButtons.get(i));
                                                }
                                                ConnectionManager.getInstance().sendPacket(gamepad);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    buttonPresser.start();
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                if (pressedButtons.contains(actions.get(Integer.parseInt((String) view.getTag()))))
                                    pressedButtons.remove(actions.get(Integer.parseInt((String) view.getTag())));
                                if (pressedButtons.size() == 0 && buttonPresser != null) {
                                    buttonPresser.end();
                                    buttonPresser = null;
                                }
                                break;
                        }
                    } else {
                        final int x = (int) event.getRawX();
                        final int y = (int) event.getRawY();
                        if (setRightStick) {
                            RSX = view.getLeft();
                            RSY = view.getTop();
                            setRightStick = false;

                        }

                        switch (event.getAction() & MotionEvent.ACTION_MASK) {

                            case MotionEvent.ACTION_DOWN:
                                xDelta = x - view.getLeft();
                                yDelta = y - view.getTop();
                                widthV = view.getWidth();
                                heightV = view.getHeight();
                               // Log.d("RS error",RSX+" "+RSY+" "+view.getLeft()+" "+view.getTop());
                                if (rightStickHandler == null) {
                                    rightStickHandler = new LoopedExecutor(BUTTON_PRESS_DELAY) {
                                        @Override
                                        public void task() {
                                            try {

                                                Log.d("RSerror",RSX+" "+RSY+" "+view.getLeft()+" "+view.getTop()+" "+( view.getLeft() -RSX) +" "+ ( view.getTop()-RSY) );
                                                JSONObject packet = new JSONObject();
                                                packet.put("type", "mouse");
                                                packet.put("action", "TouchpadMove");
                                                packet.put("deltaX", (view.getLeft() - RSX));
                                                packet.put("deltaY",  (view.getTop() -RSY));
                                                ConnectionManager.getInstance().sendPacket(packet);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    rightStickHandler.start();
                                }
                                break;

                            case MotionEvent.ACTION_UP:
                                view.setLeft(RSX);
                                view.setTop(RSY);
                                view.setRight(view.getLeft() + widthV);
                                view.setBottom(view.getTop() + heightV);
                                if (rightStickHandler != null) {
                                    rightStickHandler.end();
                                    rightStickHandler = null;
                                }
                                break;

                            case MotionEvent.ACTION_MOVE:
                                if ((x - xDelta > RSX - rightStickLimit) && (x - xDelta < RSX + rightStickLimit) && (y - yDelta > RSY - rightStickLimit) && (y - yDelta < RSY + rightStickLimit)) {
                                    view.setLeft(x - xDelta);
                                    view.setTop(y - yDelta);
                                    view.setRight(view.getLeft() + widthV);
                                    view.setBottom(view.getTop() + heightV);
                                    //Log.d("RS error",RSX+" "+RSY+" "+view.getLeft()+" "+view.getTop());
                                }

                                if (rightStickHandler == null) {
                                    rightStickHandler = new LoopedExecutor(BUTTON_PRESS_DELAY) {
                                        @Override
                                        public void task() {
                                            try {
                                                JSONObject packet = new JSONObject();
                                                packet.put("type", "mouse");
                                                packet.put("action", "TouchpadMove");
                                                packet.put("deltaX", (view.getLeft() -RSX));
                                                packet.put("deltaY", (view.getTop() - RSY));
                                                ConnectionManager.getInstance().sendPacket(packet);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    rightStickHandler.start();
                                }
                                break;
                        }
                    }

                }
                return true;
            }
        };
    }

    void initialization() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("HotkeyGamepadData", MODE_PRIVATE);
        keyBoardGrid.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.INVISIBLE);
        saveButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);
        curIndex = -1;
        String data;
        menuButton.setColorFilter(getResources().getColor(R.color.colorAccent));
        editButton.setColorFilter(getResources().getColor(R.color.colorAccent));
        saveButton.setColorFilter(getResources().getColor(R.color.colorAccent));
        cancelButton.setColorFilter(getResources().getColor(R.color.colorAccent));
        //rightStickBase.setColorFilter(getResources().getColor(R.color.colorAccent));
        try {
            JSONObject buttonData;
            for (int i = 0; i < buttons.size() - 1; i++) {
                buttons.get(i).setColorFilter(getResources().getColor(R.color.colorAccent));
                actions.add("");
                data = sharedPref.getString("buttonData" + Integer.toString(i), null);
                if (data != null) {
                    buttonData = new JSONObject(data);
                    actions.set(i, buttonData.getString("action"));
                }
                buttons.get(i).setTag(Integer.toString(i));
            }
            buttons.get(buttons.size() - 1).setTag("rightStick");
            buttons.get(buttons.size() - 1).setColorFilter(getResources().getColor(R.color.colorAccent));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void saveData() throws JSONException {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("HotkeyGamepadData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        JSONObject buttonData;
        for (int i = 0; i < buttons.size() - 1; i++) {
            buttonData = new JSONObject();
            buttonData.put("action", actions.get(i));
            editor.putString("buttonData" + Integer.toString(i), buttonData.toString());
        }
        editor.apply();
        Toast.makeText(getApplicationContext(),
                "Data Saved", Toast.LENGTH_SHORT).show();
    }
}
