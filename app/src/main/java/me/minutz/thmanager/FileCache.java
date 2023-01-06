package me.minutz.thmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;

public class FileCache
{
    private File cacheDir;

    public FileCache(final Context context)
    {
        this.cacheDir = context.getCacheDir();
        if (!this.cacheDir.exists())
        {
            this.cacheDir.mkdirs();
        }
    }

    public File getFile(final Context context, final String url) throws FileNotFoundException
    {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String filename = settings.getString(url, null);

        if (url == null)
        {
            throw new FileNotFoundException();
        }

        final File f = new File(this.cacheDir, filename);
        return f;
    }

    public void downloadAndCache(final Context context, final String url)
    {
        String filename = "";

        saveFileToMap(context, url, filename);
    }

    private void saveFileToMap(final Context context, final String url, final String filename)
    {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        final SharedPreferences.Editor editor = settings.edit();
        editor.putString(url, filename);
        editor.commit();
    }
}