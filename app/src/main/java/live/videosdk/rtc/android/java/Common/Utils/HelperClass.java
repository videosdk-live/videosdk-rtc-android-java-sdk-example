package live.videosdk.rtc.android.java.Common.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.java.R;
import live.videosdk.rtc.android.java.Common.Roboto_font;

public class HelperClass {

    private static Dialog progressDialog;

    public static void setSnackBarStyle(View snackbarView, int textColor) {

        int snackbarTextId = com.google.android.material.R.id.snackbar_text;
        TextView textView = (TextView) snackbarView.findViewById(snackbarTextId);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
        params.height = 150;
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(params);

        if (textColor == 0) {
            textView.setTextColor(Color.BLACK);
        } else {
            textView.setTextColor(textColor);
        }
        textView.setTextSize(15);
        textView.setTypeface(Roboto_font.getTypeFace(snackbarView.getContext()));

    }

    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public static void showProgress(View view) {
        setViewAndChildrenEnabled(view, false);
        progressDialog = new Dialog(view.getContext(), R.style.ProgressDialogStyle);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.joinmeeting_progress_layout);
        WindowManager.LayoutParams wmlp = progressDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        wmlp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        progressDialog.getWindow().setWindowAnimations(R.style.DialogNoAnimation);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void hideProgress(View view) {
        progressDialog.dismiss();
        progressDialog.cancel();
        setViewAndChildrenEnabled(view, true);
    }

    public static void checkParticipantSize(View view, View layout) {
        setViewAndChildrenEnabled(view, false);
        Dialog leaveprogressDialog = new Dialog(view.getContext(), R.style.ProgressDialogStyle);
        leaveprogressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveprogressDialog.setContentView(layout);
        leaveprogressDialog.getWindow().setWindowAnimations(R.style.DialogNoAnimation);
        WindowManager.LayoutParams wmlp = leaveprogressDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        wmlp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        leaveprogressDialog.setCanceledOnTouchOutside(false);
        leaveprogressDialog.setCancelable(false);
        leaveprogressDialog.show();
    }

    public static boolean isNullOrEmpty(String str) {
        return "null".equals(str) || "".equals(str) || null == str;
    }

    public static PopupWindow callStatsPopupDisplay(Participant participant, ImageView ivNetwork, Context context,boolean isScreenShare) {

        PopupWindow popupWindow = new PopupWindow(context);

        // inflate your layout or dynamically add view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.call_stats, null);
        View linearView = ((CardView) view).getChildAt(0);

        LinearLayout linearLayoutView = (LinearLayout) ((LinearLayout) linearView).getChildAt(0);
        View childLinearLayoutView = linearLayoutView.getChildAt(0);

        linearLayoutView.findViewById(R.id.btnDismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        View tableLayoutView = ((LinearLayout) linearView).getChildAt(1);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                JSONObject audio_stats = null;
                JSONObject video_stats;
                if(isScreenShare)
                {
                    video_stats = participant.getShareStats();
                }else {
                    audio_stats = participant.getAudioStats();
                    video_stats = participant.getVideoStats();
                }

                int score = 0;
                if (video_stats != null)
                    score = getQualityScore(video_stats);
                else if (audio_stats != null)
                    score = getQualityScore(audio_stats);

                if (score >= 7) {
                    setText(childLinearLayoutView.findViewById(R.id.txtScore), "Good",context);
                    linearLayoutView.setBackgroundColor(Color.parseColor("#3BA55D"));
                    ivNetwork.setImageResource(R.drawable.green_signal);
                } else if (score >= 4) {
                    setText(childLinearLayoutView.findViewById(R.id.txtScore), "Average",context);
                    linearLayoutView.setBackgroundColor(Color.parseColor("#F1CC4A"));
                    ivNetwork.setImageResource(R.drawable.orange_signal);
                } else if (score > 0) {
                    setText(childLinearLayoutView.findViewById(R.id.txtScore), "Poor",context);
                    linearLayoutView.setBackgroundColor(Color.parseColor("#FF5D5D"));
                    ivNetwork.setImageResource(R.drawable.red_signal);
                }

                String audio_latency = "-", video_latency = "-";
                String audio_jitter = "-", video_jitter = "-";
                String audio_packetLoss = "-", video_packetLoss = "-";
                String audio_bitrate = "-", video_bitrate = "-";
                String video_frameRate = "-";
                String video_resolution = "-";
                String audio_codec = "-", video_codec = "-";
                try {
                    if (audio_stats != null) {
                        audio_latency = audio_stats.has("rtt") ? String.valueOf(audio_stats.getInt("rtt")).concat(" ms ") : "-";
                        audio_jitter = audio_stats.has("jitter") ? String.format("%.2f", audio_stats.getDouble("jitter")).concat(" ms ") : "-";
                        audio_packetLoss = audio_stats.has("packetsLost") && audio_stats.has("totalPackets") && audio_stats.getInt("packetsLost") > 0 && audio_stats.getInt("totalPackets") > 0 ? String.format("%.2f", audio_stats.getDouble("packetsLost") * 100 / audio_stats.getDouble("totalPackets")).concat("% ") : "-";
                        audio_bitrate = audio_stats.has("bitrate") ? String.format("%.2f", audio_stats.getDouble("bitrate")).concat(" kb/s ") : "-";
                        audio_codec = audio_stats.has("codec") ? audio_stats.getString("codec") : "-";
                    }
                    if (video_stats != null) {
                        video_latency = video_stats.has("rtt") ? String.valueOf(video_stats.getInt("rtt")).concat(" ms ") : "-";
                        video_jitter = video_stats.has("jitter") ? String.format("%.2f", video_stats.getDouble("jitter")).concat(" ms ") : "-";
                        video_packetLoss = video_stats.has("packetsLost") && video_stats.has("totalPackets") && video_stats.getInt("packetsLost") > 0 && video_stats.getInt("totalPackets") > 0 ? String.format("%.2f", video_stats.getDouble("packetsLost") * 100 / video_stats.getDouble("totalPackets")).concat("% ") : "-";
                        video_bitrate = video_stats.has("bitrate") ? String.format("%.2f", video_stats.getDouble("bitrate")).concat(" kb/s ") : "-";
                        video_frameRate = video_stats.has("size") ? video_stats.getJSONObject("size").has("framerate") ? String.valueOf(video_stats.getJSONObject("size").getInt("framerate")) : "-" : "-";
                        video_resolution = video_stats.has("size") ? video_stats.getJSONObject("size").has("width") && video_stats.getJSONObject("size").has("height") ? String.valueOf(video_stats.getJSONObject("size").getInt("width")).concat("x").concat(String.valueOf(video_stats.getJSONObject("size").getInt("height"))) : "-" : "-";
                        video_codec = video_stats.has("codec") ? video_stats.getString("codec") : "-";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                TableLayout tableLayout = (TableLayout) tableLayoutView;
                try {
                    for (int i = 1; i < tableLayout.getChildCount(); i++) {
                        View rowView = tableLayout.getChildAt(i);
                        if (rowView instanceof TableRow) {
                            setText(rowView.findViewById(R.id.audio_latency), audio_latency,context);
                            setText(rowView.findViewById(R.id.audio_jitter), audio_jitter,context);
                            setText(rowView.findViewById(R.id.audio_packetLoss), audio_packetLoss,context);
                            setText(rowView.findViewById(R.id.audio_bitrate), audio_bitrate,context);
                            setText(rowView.findViewById(R.id.audio_codec), audio_codec,context);

                            setText(rowView.findViewById(R.id.video_latency), video_latency,context);
                            setText(rowView.findViewById(R.id.video_jitter), video_jitter,context);
                            setText(rowView.findViewById(R.id.video_packetLoss), video_packetLoss,context);
                            setText(rowView.findViewById(R.id.video_bitrate), video_bitrate,context);
                            setText(rowView.findViewById(R.id.video_frameRate), video_frameRate,context);
                            setText(rowView.findViewById(R.id.video_resolution), video_resolution,context);
                            setText(rowView.findViewById(R.id.video_codec), video_codec,context);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);



        popupWindow.setContentView(view);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(dpToPx(270,context));
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.linearlayout_style));

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                timer.cancel();
            }
        });

        return popupWindow;
    }

    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private static void setText(final TextView textView, final String value, Context context) {
        ((Activity) context ).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textView != null) {
                    textView.setText(value);
                }
            }
        });
    }


    private static int getQualityScore(JSONObject stats) {
        Double packetLossPercent = 0.0, jitter = 0.0;
        int rtt = 0;
        int score = 100;
        try {
            packetLossPercent = stats.has("packetsLost") && stats.has("totalPackets") && stats.getInt("packetsLost") != 0 && stats.getInt("totalPackets") != 0 ? stats.getDouble("packetsLost") / stats.getDouble("totalPackets") : 0.0;
            rtt = stats.has("rtt") ? stats.getInt("rtt") : 0;
            jitter = stats.has("jitter") ? stats.getDouble("jitter") : 0.0;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        score -= packetLossPercent * 50 > 50 ? 50 : packetLossPercent * 50;
        score -= ((jitter / 30) * 25 > 25 ? 25 : (jitter / 30) * 25);
        score -= ((rtt / 300) * 25 > 25 ? 25 : (rtt / 300) * 25);
        return score / 10;
    }

}
