package top.criwits.printcentre.activities;

import android.os.Bundle;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import top.criwits.printcentre.R;

public class ScanningActivity extends KioskActivity{
  private CaptureManager capture;
  private DecoratedBarcodeView decoratedBarcodeView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    super.enableBackButton();
    setContentView(R.layout.activity_scanning);
    decoratedBarcodeView = findViewById(R.id.decoratedBarcodeView);
    capture = new CaptureManager(this, decoratedBarcodeView);
    capture.initializeFromIntent(getIntent(), savedInstanceState);
    capture.decode();
  }

  @Override
  protected void onResume() {
    super.onResume();
    capture.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    capture.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    capture.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    capture.onSaveInstanceState(outState);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }
}
