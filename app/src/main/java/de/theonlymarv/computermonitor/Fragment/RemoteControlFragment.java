package de.theonlymarv.computermonitor.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;

import com.google.zxing.integration.android.IntentIntegrator;

import de.theonlymarv.computermonitor.Database.ConnectionRepo;
import de.theonlymarv.computermonitor.Dialogs.ChooseConnectionDialog;
import de.theonlymarv.computermonitor.Interfaces.ChooseDialogEvents;
import de.theonlymarv.computermonitor.Interfaces.PreRemoteEvents;
import de.theonlymarv.computermonitor.Interfaces.WebSocketEvents;
import de.theonlymarv.computermonitor.Models.Connection;
import de.theonlymarv.computermonitor.R;
import de.theonlymarv.computermonitor.Remote.WebSocket.Action;
import de.theonlymarv.computermonitor.Remote.WebSocket.Remote;
import de.theonlymarv.computermonitor.Remote.WebSocket.RemoteResponse;
import de.theonlymarv.computermonitor.Utility;
import de.theonlymarv.computermonitor.WebSocket;

/**
 * Created by Marvin on 15.09.2016 for ComputerMonitor.
 */
public class RemoteControlFragment extends Fragment implements View.OnClickListener, PreRemoteEvents {
    private static final String TAG = RemoteControlFragment.class.getSimpleName();
    private View layoutView;

    private WebSocket webSocket;
    private SeekBar seekBar;
    private FloatingActionButton fab, fab1, fab2;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private Boolean isFabOpen = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.fragment_remote_control, container, false);

        setHasOptionsMenu(true);

        fab = (FloatingActionButton) layoutView.findViewById(R.id.fab);
        fab1 = (FloatingActionButton) layoutView.findViewById(R.id.fabCamera);
        fab2 = (FloatingActionButton) layoutView.findViewById(R.id.fabList);
        fab_open = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.rotate_backward);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        seekBar = (SeekBar) layoutView.findViewById(R.id.seekBar);
        assert seekBar != null;
        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (webSocket != null) {
                        progress = (int) (Math.round(progress / 5d) * 5);
                        sendSeekBarProgress(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return layoutView;
    }

    private void sendSeekBarProgress(int progress) {
        if (webSocket != null && webSocket.isConnected())
            webSocket.sendMessage(Action.Volumn, progress);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:

                animateFAB();
                break;
            case R.id.fabCamera:

                if (webSocket == null || !webSocket.isConnected()) {
                    onCameraClick();
                } else {
                    onCancelClick();
                }
                break;
            case R.id.fabList:

                showConnectionList();
                break;
        }
    }

    private void showConnectionList() {
        new ChooseConnectionDialog(getContext(), new ChooseDialogEvents<Connection>() {
            @Override
            public void OnChoose(Connection connection) {
                openWebSocketConnection(connection.getUrl());
            }

            @Override
            public void OnEmptyChooseList() {
                Utility.ShowSnackBarOnMainActivity(getActivity(), R.string.chooser_empty, Snackbar.LENGTH_LONG);
            }
        }).ShowDialog();
    }

    private void openWebSocketConnection(String url) {
        if (webSocket != null && webSocket.isConnected()) {
            webSocket.closeConnection();
        }

        String title = getResources().getString(R.string.dialog_please_wait);
        String message = getResources().getString(R.string.dialog_try_to_connect);
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), title, message, true, false);

        webSocket = new WebSocket(getActivity(), new WebSocketEvents() {
            @Override
            public void onMessage(Remote remote) {
                if (remote instanceof RemoteResponse) {
                    RemoteResponse rr = (RemoteResponse) remote;
                    if (rr.getStatus() == 100) {
                        ConnectionRepo repo = new ConnectionRepo(getActivity());
                        repo.insertConnection(new Connection(rr.getMessage(), webSocket.getUrl()));
                    }
                }
            }

            @Override
            public void onOpened() {
                fab1.setImageResource(R.drawable.disconnect);
                animateFAB();
                progressDialog.cancel();
                progressDialog.dismiss();
                seekBar.setEnabled(true);
            }

            @Override
            public void onClosed() {
                fab1.setImageResource(R.drawable.camera);
                seekBar.setEnabled(false);
            }

            @Override
            public void onError(String error) {
                Utility.ShowSnackBarOnMainActivity(getActivity(), error, Snackbar.LENGTH_LONG);
                progressDialog.cancel();
                progressDialog.dismiss();
            }
        }, url);
    }

    private void onCameraClick() {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(getResources().getString(R.string.scanner_qr));
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    private void onCancelClick() {
        if (webSocket != null)
            webSocket.closeConnection();
    }

    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;

        }
    }

    @Override
    public void OnQrCodeScanned(String url) {
        openWebSocketConnection(url);
    }

    @Override
    public boolean OnDownUpPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        int progress = (int) (Math.round(seekBar.getProgress() / 5d) * 5);
        if (webSocket == null || !webSocket.isConnected()) {
            return false;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    seekBar.setProgress(progress < 100 ? progress + 5 : progress);
                    sendSeekBarProgress(seekBar.getProgress());
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    seekBar.setProgress(progress > 0 ? progress - 5 : progress);
                    sendSeekBarProgress(seekBar.getProgress());
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                deleteRemoteHistory();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteRemoteHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_delete_history_title);
        builder.setMessage(R.string.dialog_delelte_history_message);
        builder.setPositiveButton(R.string.dialog_delete_history_positiv, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ConnectionRepo repo = new ConnectionRepo(getContext());
                repo.deleteAllConnections();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);

        builder.create().show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_remote, menu);
    }
}
