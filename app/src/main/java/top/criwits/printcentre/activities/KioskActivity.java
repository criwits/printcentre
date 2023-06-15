package top.criwits.printcentre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NavUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.Objects;


abstract public class KioskActivity extends AppCompatActivity {
    public static void preventStatusBarExpansion(Context context) {
    WindowManager manager = ((WindowManager) context.getApplicationContext()
        .getSystemService(Context.WINDOW_SERVICE));

    Activity activity = (Activity) context;
    WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
    localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
    localLayoutParams.gravity = Gravity.TOP;
    localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
        // this is to enable the notification to recieve touch events
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
        // Draws over status bar
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

    localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;

    int resId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
    int result = 0;
    if (resId > 0) {
      result = activity.getResources().getDimensionPixelSize(resId);
    }
    localLayoutParams.height = result;
    localLayoutParams.format = PixelFormat.TRANSPARENT;
    customViewGroup view = new customViewGroup(context);
    manager.addView(view, localLayoutParams);
  }

  public static class customViewGroup extends ViewGroup {

    public customViewGroup(Context context) {
      super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
      Log.v("customViewGroup", "**********Intercepted");
      return true;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this); // blinks, should fix
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected void enableBackButton() {
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    super.onCreate(savedInstanceState);
    preventStatusBarExpansion(this);
  }
}
