package top.criwits.printcentre.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;

import top.criwits.printcentre.R;
import top.criwits.printcentre.misc.Consts;
import top.criwits.printcentre.network.NetworkHelper;

public class PrintSettingsActivity extends KioskActivity {
  private String uuid;
  private int pages;

  private ImageView imageView;
  private Switch duplexSwitch;
  private NumberPicker copiesNumberPicker;
  private Button button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    super.enableBackButton();
    setContentView(R.layout.activity_print_settings);
    uuid = getIntent().getStringExtra("uuid");
    pages = getIntent().getIntExtra("pages", 1);

    imageView = findViewById(R.id.imageView);
    duplexSwitch = findViewById(R.id.duplexSwitch);
    copiesNumberPicker = findViewById(R.id.numberPicker);
    button = findViewById(R.id.button);

    File imgFile = new File(Consts.getExternalPath("/print/" + uuid + "-preview.png"));
    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    imageView.setImageBitmap(bitmap);

    duplexSwitch.setChecked(true);
    if (pages == 1) {
      duplexSwitch.setChecked(false);
      duplexSwitch.setEnabled(false);
    }

    copiesNumberPicker.setMinValue(1);
    copiesNumberPicker.setMaxValue(10);
    copiesNumberPicker.setValue(1);

    button.setOnClickListener(v -> {
      try {
        onStartPrint();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  private void onStartPrint() throws Exception {
    class FetchPrintResourceTask extends AsyncTask<Void, String, Boolean> {
      private ProgressDialog dialog;
      private Activity activity;
      private int errorResId = 0;

      public FetchPrintResourceTask(Activity activity) {
        this.activity = activity;
      }

      @Override
      protected void onPreExecute() {
        dialog = new ProgressDialog(activity);
        dialog.setMessage(getString(R.string.downloading_document));
        dialog.setCancelable(false);
        dialog.show();
      }

      @Override
      protected Boolean doInBackground(Void... voids) {
        if (pages > 1 && duplexSwitch.isChecked()) {
          // Duplex printing
          if (NetworkHelper.downloadFile(Consts.SERVER_URI + "/" + uuid + "-odd.urf", new File(Consts.getExternalPath("/print/" + uuid + "-odd.urf"))) &&
          NetworkHelper.downloadFile(Consts.SERVER_URI + "/" + uuid + "-even.urf", new File(Consts.getExternalPath("/print/" + uuid + "-even.urf")))) {
            return true;
          } else {
            errorResId = R.string.download_failed;
            return false;
          }
        } else {
          // Simple printing
          if (NetworkHelper.downloadFile(Consts.SERVER_URI + "/" + uuid + ".urf", new File(Consts.getExternalPath("/print/" + uuid + ".urf")))) {
            return true;
          } else {
            errorResId = R.string.download_failed;
            return false;
          }
        }
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (dialog.isShowing()) {
          dialog.dismiss();
        }
        if (success) {
          // put uuid, duplex, copies to intent
          Intent intent = new Intent(activity, SimplePrintActivity.class);
          intent.putExtra("uuid", uuid);
          intent.putExtra("duplex", duplexSwitch.isChecked());
          intent.putExtra("copies", copiesNumberPicker.getValue());
          startActivity(intent);
          finish();
        } else {
          Toast.makeText(activity, errorResId, Toast.LENGTH_SHORT).show();
        }
      }
    }
    new FetchPrintResourceTask(this).execute();
  }
}