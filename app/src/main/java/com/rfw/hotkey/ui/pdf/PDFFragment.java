package com.rfw.hotkey.ui.pdf;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.rfw.hotkey.R;
import com.rfw.hotkey.net.ConnectionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class PDFFragment extends Fragment implements View.OnClickListener {
    private static final String KEY_PDF_READER_PLATFORM = "pdfPlatform";

    private LinearLayout pdfButtonLayout;
    private RelativeLayout pdfPlatformLayout;
    private ImageButton fullScreenButton;
    private ImageButton pdfMoreButton;
    private ImageButton upButton;
    private ImageButton zoomInButton;
    private ImageButton zoomOutButton;
    private ImageButton leftButton;
    private ImageButton downButton;
    private ImageButton rightButton;
    private ImageButton findPageButton;
    private ImageButton acrobatReaderButton;
    private ImageButton evinceButton;
    private int platform;
    private Button fitHeightButton, fitWidthButton;
    private boolean isFullScreen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pdf, container, false);
        initialization(rootView);
        // Inflate the layout for this fragment
        return rootView;
    }

    int getPlatform() {
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(KEY_PDF_READER_PLATFORM, 1);
        }

    void setPlatform(int platform) {
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_PDF_READER_PLATFORM, platform);
        editor.apply();
    }

    private void initialization(View rootView) {


        isFullScreen = false;
        pdfButtonLayout = rootView.findViewById(R.id.pdfButtonsLayoutID);
        pdfPlatformLayout = rootView.findViewById(R.id.pdfPlatformLayoutID);
        pdfMoreButton = rootView.findViewById(R.id.pdfMoreButtonID);
        findPageButton = rootView.findViewById(R.id.pdf_findPageButtonID);
        fitWidthButton = rootView.findViewById(R.id.pdf_fitWidthButtonID);
        fitHeightButton = rootView.findViewById(R.id.pdf_fitHeightButtonID);
        fullScreenButton = rootView.findViewById(R.id.pdf_fullScreenButtonID);
        upButton = rootView.findViewById(R.id.pdf_upButtonID);
        zoomInButton = rootView.findViewById(R.id.pdf_zoomInButtonID);
        zoomOutButton = rootView.findViewById(R.id.pdf_zoomOutButtonID);
        leftButton = rootView.findViewById(R.id.pdf_leftButtonID);
        downButton = rootView.findViewById(R.id.pdf_downButtonID);
        rightButton = rootView.findViewById(R.id.pdf_rightButtonID);
        acrobatReaderButton = rootView.findViewById(R.id.adobeAcrobatReaderID);
        evinceButton = rootView.findViewById(R.id.evinceID);
        acrobatReaderButton.setOnClickListener(this);
        evinceButton.setOnClickListener(this);
        pdfMoreButton.setOnClickListener(this);
        findPageButton.setOnClickListener(this);
        fitHeightButton.setOnClickListener(this);
        fitWidthButton.setOnClickListener(this);
        fullScreenButton.setOnClickListener(this);
        upButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        downButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        zoomInButton.setOnClickListener(this);
        zoomOutButton.setOnClickListener(this);

        pdfPlatformLayout.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.pdfMoreButtonID:
                pdfPlatformLayout.setVisibility(View.VISIBLE);
                pdfButtonLayout.setVisibility(View.INVISIBLE);
                findPageButton.setVisibility(View.INVISIBLE);
                break;
            case R.id.pdf_findPageButtonID:
                //TODO Make a dialog
                openDialog();
                break;
            case R.id.pdf_fullScreenButtonID:
                if (!isFullScreen) {
                    //sendMessageToServer("F5", "modifier");
                    //Log.d("onclick", "F5");
                    //fullScreenButton.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
                    Toast.makeText(getActivity(), "Full Screen Mode", Toast.LENGTH_SHORT).show();
                    sendMessageToServer("fullscreen", "modifier", String.valueOf(getPlatform()));
                    fullScreenButton.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
                    isFullScreen = true;
                } else {
                    sendMessageToServer("ESC", "modifier", String.valueOf(getPlatform()));
                    Log.d("onclick", "ESC");
                    fullScreenButton.setImageResource(R.drawable.ic_fullscreen_white_24dp);
                    Toast.makeText(getActivity(), "Normal Mode", Toast.LENGTH_SHORT).show();
                    isFullScreen = false;
                }
                break;
            case R.id.pdf_fitHeightButtonID:
                sendMessageToServer("fit_h", "modifier", String.valueOf(getPlatform()));
                Log.d("onclick", "FIT HEIGHT");
                Log.d("pdf m", String.valueOf(getPlatform()));
                break;
            case R.id.pdf_fitWidthButtonID:
                sendMessageToServer("fit_w", "modifier", String.valueOf(getPlatform()));
                Log.d("onclick", "FIT WIDTH");
                break;
            case R.id.pdf_zoomInButtonID:
                sendMessageToServer("zoom_in", "modifier", String.valueOf(getPlatform()));
                Log.d("onclick", "ZOOM IN");
                break;
            case R.id.pdf_zoomOutButtonID:
                sendMessageToServer("zoom_out", "modifier", String.valueOf(getPlatform()));
                Log.d("onclick", "ZOOM OUT");
                break;
            case R.id.pdf_upButtonID:
                // sendMessageToServer("modifier");
                sendMessageToServer("UP", "modifier", String.valueOf(getPlatform()));
                Log.d("onclick", "UP");
                break;
            case R.id.pdf_leftButtonID:
                //sendMessageToServer("modifier");
                sendMessageToServer("LEFT", "modifier", String.valueOf(getPlatform()));
                Log.d("onclick", "LEFT");
                break;
            case R.id.pdf_downButtonID:
                // sendMessageToServer("modifier");
                sendMessageToServer("DOWN", "modifier", String.valueOf(getPlatform()));
                Log.d("onclick", "DOWN");
                break;
            case R.id.pdf_rightButtonID:
                //sendMessageToServer("modifier");
                sendMessageToServer("RIGHT", "modifier", String.valueOf(getPlatform()));
                Log.d("onclick", "RIGHT");
                break;
            case R.id.adobeAcrobatReaderID:
                platform = 1;
                setPlatform(platform);
                Log.d("PDF More", String.valueOf(getPlatform()));
                pdfPlatformLayout.setVisibility(View.INVISIBLE);
                pdfButtonLayout.setVisibility(View.VISIBLE);
                findPageButton.setVisibility(View.VISIBLE);
                Log.d("pdf_more","adobe acrobat reader");
                break;
            case R.id.evinceID:
                platform = 2;
                setPlatform(platform);
                pdfPlatformLayout.setVisibility(View.INVISIBLE);
                pdfButtonLayout.setVisibility(View.VISIBLE);
                findPageButton.setVisibility(View.VISIBLE);
                break;
        }
        Log.d("PDF More", String.valueOf(getPlatform()));
    }

    /**
     * sends the message of specified action to Connection Manager
     *
     * @param message message (key press type)
     * @param action  type of the message
     */
    private void sendMessageToServer(String message, String action, String platform) {
        JSONObject packet = new JSONObject();

        try {
            packet.put("type", "pdf");
            packet.put("action", action);
            packet.put("platform", platform);
            packet.put("key", message);

            ConnectionManager.getInstance().sendPacket(packet);

        } catch (JSONException e) {
            Log.e("PDFFragment", "sendMessageToServer: error sending key-press", e);
        }
    }



    private void openDialog() {
        PDFFindPageDialog pdfFindPageDialog = new PDFFindPageDialog();
        assert getFragmentManager() != null;
        pdfFindPageDialog.show(getFragmentManager(), "Goto Page Dialog");
    }
}