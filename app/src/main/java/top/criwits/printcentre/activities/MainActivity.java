package top.criwits.printcentre.activities;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
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
    class GetDocumentMetaTask extends AsyncTask<String, String, Boolean> {
      private ProgressDialog dialog;
      private Activity activity;
      private int errorResId = 0;
      private String uuid;
      private int pages;

      public GetDocumentMetaTask(Activity activity) {
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
      protected Boolean doInBackground(String... strings) {
        // check meta
        publishProgress(getString(R.string.getting_document_info));
        // sleep for 0.7s, to prevent server from being overloaded (bushi
        try {
          Thread.sleep(700);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        uuid = strings[0];
        String documentMetaData = NetworkHelper.getString(Consts.SERVER_URI + "/" + uuid + ".meta");
        if (documentMetaData == null) {
          errorResId = R.string.document_not_exist;
          return false;
        }
        // Parse metadata to Int
        pages = Integer.parseInt(documentMetaData.split("\\s")[0]);

        // Download file
        publishProgress(getString(R.string.generate_document_preview));
        if (!NetworkHelper.downloadFile(
            Consts.SERVER_URI + "/" + uuid + "-preview.png",
            new File(Consts.getExternalPath("/print/" + uuid + "-preview.png"))
        )) {
          errorResId = R.string.document_not_exist;
          return false;
        }

        return true;
      }

      @Override
      protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        dialog.dismiss();
        if (status) {
          // Put pages and uuid to intent
          Intent intent = new Intent(activity, PrintSettingsActivity.class);
          intent.putExtra("pages", pages);
          intent.putExtra("uuid", uuid);
          startActivity(intent);
        } else {
          Toast.makeText(activity, errorResId, Toast.LENGTH_SHORT).show();
        }
      }
    }

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
            GetDocumentMetaTask task = new GetDocumentMetaTask(this);
            task.execute(uuid.toString());
            // ends
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