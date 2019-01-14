package com.nomi.merger.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Locale;

import androidx.annotation.ArrayRes;
import androidx.annotation.BoolRes;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import lombok.NonNull;


//*********************************************************************
public class AndroidUtil
//*********************************************************************
{
    public static final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Currently active context.
     */
    @SuppressLint("StaticFieldLeak")
    private static Context sContext = null;

    /**
     * Method used by {@link #getSystemProperty}.
     */
    private static final
    @Nullable
    Method sGetSystemProperty;

    /**
     * Static initializer.
     */
    //******************************************************************
    static
    //******************************************************************
    {
        Method m = null;
        try
        {
            Class<?> c = Class.forName("android.os.SystemProperties");
            m = c.getMethod("get", String.class);
        }
        catch (ClassNotFoundException e)
        {

        }
        catch (NoSuchMethodException e)
        {

        }
        sGetSystemProperty = m;
    }


    /**
     * Methods for API 16.
     */
    //******************************************************************
    @SuppressWarnings("deprecation")
    private static class Api16
            //******************************************************************
    {
        /**
         * Method for obtaining drawables from AppCompat.
         */
        private Method mAppCompatGetDrawable = null;

        /**
         * Constructor.
         */
        //******************************************************************
        Api16()
        //******************************************************************
        {
            try
            {
                final Class<?> appCompat = Class.forName(
                        "android.support.v7.content.res.AppCompatResources");
                mAppCompatGetDrawable = appCompat.getMethod("getDrawable", Context.class,
                                                            int.class);
            }
            catch (Exception e)
            {
            }
        }

        /**
         * Check whether the application has a permission.
         *
         * @param permission The permission to check.
         * @return Whether the application has the specified permission.
         */
        //******************************************************************
        @CheckResult
        public boolean checkPermission(@NonNull String permission)
        //******************************************************************
        {
            return sContext.getPackageManager()
                           .checkPermission(permission, sContext.getPackageName())
                    == PackageManager.PERMISSION_GRANTED;
        }

        /**
         * Get a color from the resources.
         *
         * @param resId The resource ID.
         * @return The color.
         * @throws Resources.NotFoundException Invalid resource ID.
         */
        //******************************************************************
        @CheckResult
        public
        @ColorInt
        int getColor(@ColorRes int resId)
                throws Resources.NotFoundException
        //******************************************************************
        {
            return sContext.getResources()
                           .getColor(resId);
        }

        /**
         * Get a drawable.
         *
         * @param resId The resource ID.
         * @return The drawable, or <code>null</code> if the resource could
         * not be resolved.
         * @throws Resources.NotFoundException Invalid resource ID.
         */
        //******************************************************************
        @CheckResult
        public final
        @Nullable
        Drawable getDrawable(@DrawableRes int resId)
                throws Resources.NotFoundException
        //******************************************************************
        {
            if (mAppCompatGetDrawable != null)
                try
                {
                    return (Drawable)mAppCompatGetDrawable.invoke(null, sContext, resId);
                }
                catch (RuntimeException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    mAppCompatGetDrawable = null;
                }
            return getDrawableDirect(resId);
        }

        /**
         * Get a drawable from the resources.
         *
         * @param resId The resource ID.
         * @return The drawable, or <code>null</code> if the resource could
         * not be resolved.
         * @throws Resources.NotFoundException Invalid resource ID.
         */
        //******************************************************************
        @CheckResult
        public
        @Nullable
        Drawable getDrawableDirect(@DrawableRes int resId)
                throws Resources.NotFoundException
        //******************************************************************
        {
            return sContext.getResources()
                           .getDrawable(resId);
        }
    }


    /**
     * Methods for API 21.
     */
    //******************************************************************
    @TargetApi(21)
    private static class Api21
            extends Api16
            //******************************************************************
    {
        /**
         * {@inheritDoc}
         */
        //******************************************************************
        @CheckResult
        @Override
        public
        @Nullable
        Drawable getDrawableDirect(@DrawableRes int resId)
                throws Resources.NotFoundException
        //******************************************************************
        {
            return sContext.getDrawable(resId);
        }
    }


    /**
     * Methods for API 23.
     */
    //******************************************************************
    @TargetApi(23)
    private static class Api23
            extends Api21
            //******************************************************************
    {
        /**
         * Check whether the application has a permission.
         *
         * @param permission The permission to check.
         * @return Whether the application has the specified permission.
         */
        //******************************************************************
        @CheckResult
        public boolean checkPermission(@NonNull String permission)
        //******************************************************************
        {
            return sContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        /**
         * {@inheritDoc}
         */
        //******************************************************************
        @CheckResult
        @Override
        public
        @ColorInt
        int getColor(@ColorRes int resId)
                throws Resources.NotFoundException
        //******************************************************************
        {
            return sContext.getColor(resId);
        }
    }


    private static final Api16 API = Build.VERSION.SDK_INT >= 23
                                     ? new Api23()
                                     : Build.VERSION.SDK_INT >= 21
                                       ? new Api21()
                                       : new Api16();


    //******************************************************************
    public static void setContext(Context context)
    //******************************************************************
    {
        sContext = context;
    }

    /**
     * Get the application context.
     * <p>
     * The context should never be <code>null</code> after {@link
     * <p>
     * <p>
     * This context is useful for things like registering intent
     * receivers. Do not use it for dialogs through, they will crash.
     *
     * @return The application context.
     * @see #getContext
     * @see #hasContext
     * @see #setContext
     */
    //******************************************************************
    @CheckResult
    public static @NonNull
    Context getApplicationContext()
    //******************************************************************
    {
        return sContext.getApplicationContext();
    }

    /**
     * Get string from the resources.
     *
     * @param resId The resource ID.
     * @return The string.
     * @throws Resources.NotFoundException Invalid resource ID.
     */
    //******************************************************************
    @CheckResult
    public static
    @NonNull
    String getString(@StringRes int resId)
            throws Resources.NotFoundException
    //******************************************************************
    {
        return sContext.getString(resId);
    }

    /**
     * Get string array from the resources.
     *
     * @param resId The resource ID.
     * @return The string array.
     * @throws Resources.NotFoundException Invalid resource ID.
     */
    //******************************************************************
    @CheckResult
    public static
    @NonNull
    String[] getStringArray(@ArrayRes int resId)
            throws Resources.NotFoundException
    //******************************************************************
    {
        return sContext.getResources()
                       .getStringArray(resId);
    }

    /**
     * Get a bitmap from the resources.
     *
     * @param resId The resource ID.
     * @return The bitmap.
     * @throws Resources.NotFoundException Invalid resource ID.
     */
    //******************************************************************
    @CheckResult
    public static
    @NonNull
    Bitmap getBitmap(@DrawableRes int resId)
            throws Resources.NotFoundException
    //******************************************************************
    {
        Bitmap ret = BitmapFactory.decodeResource(sContext.getResources(), resId);
        if (ret == null)
            throw new Resources.NotFoundException(
                    String.format(Locale.ROOT, "Invalid bitmap resource %x", resId));
        return ret;
    }

    /**
     * Get a boolean from the resources.
     *
     * @param resId The resource ID.
     * @return The boolean.
     * @throws Resources.NotFoundException Invalid resource ID.
     */
    //******************************************************************
    @CheckResult
    public static boolean getBoolean(@BoolRes int resId)
            throws Resources.NotFoundException
    //******************************************************************
    {
        return sContext.getResources()
                       .getBoolean(resId);
    }

    /**
     * Get a color from the resources.
     *
     * @param resId The resource ID.
     * @return The color.
     * @throws Resources.NotFoundException Invalid resource ID.
     */
    //******************************************************************
    @CheckResult
    public static
    @ColorInt
    int getColor(@ColorRes int resId)
            throws Resources.NotFoundException
    //******************************************************************
    {
        return API.getColor(resId);

    }

    /**
     * Get a drawable from the resources.
     *
     * @param resId The resource ID.
     * @return The drawable, or <code>null</code> if the resource could
     * not be resolved..
     * @throws Resources.NotFoundException Invalid resource ID.
     */
    //******************************************************************
    @CheckResult
    public static
    @Nullable
    Drawable getDrawable(@DrawableRes int resId)
            throws Resources.NotFoundException
    //******************************************************************
    {
        return API.getDrawable(resId);
    }

    /**
     * Get the application ID.
     * <p>
     * This is the application package name defined in
     * <code>AndroidManifest.xml</code>.
     *
     * @return The application ID in reverse-DNS format.
     */
    //******************************************************************
    @CheckResult
    public static
    @NonNull
    String getApplicationId()
    //******************************************************************
    {
        return sContext.getPackageName();
    }

    //******************************************************************
    @CheckResult
    public static
    @Nullable
    Drawable getApplicationIcon()
    //******************************************************************
    {
        final int icon = sContext.getApplicationInfo().icon;
        return icon == 0 ? null : getDrawable(icon);
    }

    /**
     * Get integer array from the resources.
     *
     * @param resId The resource ID.
     * @return The integer array.
     * @throws Resources.NotFoundException Invalid resource ID.
     */
    //******************************************************************
    @CheckResult
    public static
    @NonNull
    int[] getIntegerArray(@ArrayRes int resId)
            throws Resources.NotFoundException
    //******************************************************************
    {
        return sContext.getResources()
                       .getIntArray(resId);
    }

    /**
     * Get resources.
     *
     * @return The resources.
     */
    //******************************************************************
    @CheckResult
    public static
    @NonNull
    Resources getResources()
    //******************************************************************
    {
        return sContext.getResources();
    }

    /**
     * Check if this is main (UI) thread.
     *
     * @return Whether this is main (UI) thread.
     */
    //******************************************************************
    @CheckResult
    public static boolean isMainThread()
    //******************************************************************
    {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * Rendezvous with the main (UI) thread.
     * <p>
     * <a href="http://en.wikipedia.org/wiki/Rendezvous_%28Plan_9%29">Rendezvous</a>
     * is a data synchronization primitive originating from Plan 9
     * operating system. Two threads meet at a synchronization point to
     * exchange a single datum.
     * <p>
     * This method uses a similar approach to temporarily “join” two
     * threads so that the executed code appears to be running on
     * either of them without any need for further synchronization.
     * <p>
     * Internally, it runs on the main thread so you can call any UI
     * method that checks the thread it is executed on.
     * <p>
     * It is similar to
     * <a href="http://developer.android.com/reference/android/app/Activity.html#runOnMainThread(java.lang.Runnable)"><code>Activity.runOnMainThread</code></a>
     * but unlike it, this method is synchronous and waits for the
     * runnable to finish before returning. It also passes any
     * exception thrown by it to the calling thread instead of just
     * ignoring it.
     *
     * @param runnable The runnable to run on the main thread.
     */
    //******************************************************************
    public static void rendezvous(final @NonNull Runnable runnable)
    //******************************************************************
    {
        if (isMainThread())
        {
            runnable.run();
            return;
        }

        final RendezvousRunnable rendezvous = new RendezvousRunnable(runnable);
        synchronized (rendezvous)
        {
            handler.post(rendezvous);
            try
            {
                rendezvous.wait();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread()
                      .interrupt();
            }
            if (rendezvous.mException != null)
                throw rendezvous.mException;
        }
    }

    /**
     * Runnable for {@linkplain #rendezvous}.
     */
    //******************************************************************
    protected static final class RendezvousRunnable
            implements Runnable
            //******************************************************************
    {
        /**
         * The runnable to run in the main thread.
         */
        public final
        @NonNull
        Runnable mRunnable;

        /**
         * Exception that was thrown while running {@linkplain #mRunnable the runnable}.
         */
        public
        @Nullable
        RuntimeException mException = null;

        /**
         * Constructor.
         *
         * @param runnable The runnable to run in the main thread.
         */
        //******************************************************************
        protected RendezvousRunnable(@NonNull Runnable runnable)
        //******************************************************************
        {
            mRunnable = runnable;
        }

        /**
         * Run the stored runnable.
         */
        //******************************************************************
        @MainThread
        @Override
        public void run()
        //******************************************************************
        {
            synchronized (this)
            {
                try
                {
                    mRunnable.run();
                }
                catch (RuntimeException e)
                {
                    mException = e;
                }
                notifyAll();
            }
        }
    }

    /**
     * Toast a message.
     *
     * @param longToast Should the toast be a long one?
     * @param message   Message to toast.
     */
    //******************************************************************
    @UiThread
    public static void toast(boolean longToast,
                             @NonNull String message)
    //******************************************************************
    {

        Toast
                .makeText(sContext, message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Toast a formatted message.
     *
     * @param longToast Should the toast be a long one?
     * @param message   Formatting for the message.
     * @param values    Values used for formatting.
     */
    //******************************************************************
    @UiThread
    public static void toast(boolean longToast,
                             @NonNull String message,
                             Object... values)
    //******************************************************************
    {
        toast(longToast, String.format(message, values));
    }

    /**
     * Toast a message.
     *
     * @param longToast Should the toast be a long one?
     * @param message   ID of the resource to toast.
     */
    //******************************************************************
    @UiThread
    public static void toast(boolean longToast,
                             @StringRes int message)
    //******************************************************************
    {
        toast(longToast, getString(message));
    }

    /**
     * Toast a formatted message.
     *
     * @param longToast Should the toast be a long one?
     * @param message   ID of the resource to format.
     * @param values    Values used for formatting.
     */
    //******************************************************************
    @UiThread
    public static void toast(boolean longToast,
                             @PluralsRes @StringRes int message,
                             Object... values)
    //******************************************************************
    {
        toast(longToast, getString(message, values));
    }

    /**
     * Get a string or a plural loaded from the resources, formatting it.
     * <p>
     * First, this method checks if the provided resource ID is of
     * a string. If not and the first value is an integer, it checks
     * plurals.
     * <p>
     * The quantity to select the appropriate plural version is always
     * the first parameter. Use positional parameters (<tt>%1$s</tt>)
     * in the values if you need a different order in the string.
     *
     * @param resId  The resource ID.
     * @param values Values used for formatting.
     * @return The formatted string.
     * @throws Resources.NotFoundException Invalid resource ID.
     */
    //TODO find out why the second resId type annotation is ignored
    @SuppressLint("ResourceType")
    //******************************************************************
    @CheckResult
    public static
    @NonNull
    String getString(@PluralsRes @StringRes int resId,
                     Object... values)
            throws Resources.NotFoundException
    //******************************************************************
    {
        final Resources resources = sContext.getResources();
        CharSequence seq = resources.getText(resId, null);
        if (seq == null && values.length > 0 && values[0] instanceof Number)
            try
            {
                seq = resources.getQuantityText(resId, ((Number)values[0]).intValue());
            }
            catch (Resources.NotFoundException e)
            {
            }
        if (seq == null)
            throw new Resources.NotFoundException(
                    String.format(Locale.ROOT, "Invalid string or plural resource %x", resId));
        return String.format(seq.toString(), values);
    }


    /**
     * Get display metrics.
     *
     * @return Current display metrics.
     */
    // ******************************************************************
    @CheckResult
    public static @NonNull
    DisplayMetrics getDisplayMetrics()
    // ******************************************************************
    {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics;
    }
    // ******************************************************************
    @CheckResult
    public static @NonNull
    WindowManager getWindowManager()
    // ******************************************************************
    {
        WindowManager manager = (WindowManager)sContext.getSystemService(Context.WINDOW_SERVICE);
        assert manager != null;
        return manager;
    }
}
