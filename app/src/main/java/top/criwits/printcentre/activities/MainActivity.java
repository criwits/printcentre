package top.criwits.printcentre.activities;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import top.criwits.printcentre.R;
import top.criwits.printcentre.misc.Consts;
import top.criwits.printcentre.network.NetworkHelper;
import top.criwits.printcentre.printer.PrinterHelper;

public class MainActivity extends KioskActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final Button startPrintingButton = findViewById(R.id.startPrintingButton);
    final Button getTicketButton = findViewById(R.id.getTicketButton);

    startPrintingButton.setOnClickListener(v -> {
      try {
        onStartPrint();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }




  /**
   * Called when 'Start printing' button is pressed.
   * This function will first check printer status, then server status.
   * If both are OK, it will start scanning activity.
   * @throws InterruptedException
   * @throws ExecutionException
   */
  private void onStartPrint() throws InterruptedException, ExecutionException {
    class PreparePrintingTask extends AsyncTask<Void, String, Boolean> {
      private ProgressDialog dialog;
      private Activity activity;
      private int errorResId = 0;

      public PreparePrintingTask(Activity activity) {
        this.activity = activity;
        dialog = new ProgressDialog(activity);
        dialog.setCancelable(false);
      }

      @Override
      protected void onPreExecute() {
        super.onPreExecute();
        dialog.show();
      }

      @Override
      protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        dialog.setMessage(values[0]);
      }

      @Override
      protected Boolean doInBackground(Void... voids) {
        // Check printer status
        publishProgress(getString(R.string.checking_printer));

        PrinterHelper.PrinterStatus status =  PrinterHelper.checkPrinterStatus();
        if (status != PrinterHelper.PrinterStatus.CONNECTED) {
          errorResId  = R.string.printer_disconnected;
          return false;
        }

        // Check server status
        publishProgress(getString(R.string.checking_server));

        String serverStatus = NetworkHelper.getString(Consts.SERVER_URI + "/server-status");
        if (serverStatus == null || !serverStatus.contains("OK")) {
          errorResId = R.string.server_unavailable;
          return false;
        }

        return true;
      }

      @Override
      protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        dialog.dismiss();
        if (status) {
          new IntentIntegrator(activity)
              .setCaptureActivity(ScanningActivity.class)
              .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
              .setPrompt("")
              .setCameraId(1)
              .setBeepEnabled(false)
              .setBarcodeImageEnabled(false)
              .initiateScan();
        } else {
          Toast.makeText(activity, errorResId, Toast.LENGTH_SHORT).show();
        }

      }
    }


    // Check printer status
    PreparePrintingTask task = new PreparePrintingTask(this);
    task.execute();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    if (result != null) {
      if (result.getContents() == null) {
        // cancelled
      } else {
        // scanned
        String str = result.getContents();
        // Check if str is a UUID
        if (str.length() == 36) {
          // Check if str is a valid UUID
          try {
            UUID uuid = UUID.fromString(str);
            // Check if document is valid
            ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.checking_ticket), true);
            dialog.dismiss();
            // Download document
            ProgressDialog dialog2 = ProgressDialog.show(this, "", getString(R.string.downloading_doc), true);
            // download document at http://172.16.0.100:20480/document/uuid as PDF


          } catch (IllegalArgumentException e) {
            // Show error
            Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show();
          }
        } else {
          // Show error
          Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show();
        }

      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

}