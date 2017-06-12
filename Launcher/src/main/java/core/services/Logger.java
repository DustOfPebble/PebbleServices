package core.services;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class Logger {
    private static final String LogTag = Logger.class.getSimpleName();

    private final long WriteDelay = 5000*60;
    private Hub Service = null;
    private boolean Live = true;
    private long StoredStamps = 0;
    private File WorkingDirectory;

    private class LogEvent {
        public String Tag;
        public String Message;
        public Long Stamps;

        public LogEvent(String Tag, String Message, long Stamps) {
            this.Tag = Tag;
            this.Message = Message;
            this.Stamps = Stamps;
        }
    }
    private ArrayList<LogEvent> Logs;

    public Logger(Hub Owner) {
        Service =  Owner;
        SelectDirectory(Owner.getPackageName());
    }

    public void Live(boolean enabled) {
        Live = enabled;
        StoredStamps = Calendar.getInstance().getTimeInMillis();
        Logs.clear();
    }

    public void debug(String Tag, String Message) {
        if (Live) Log.d(Tag, Message);
        else {
            long UpdatedStamps = Calendar.getInstance().getTimeInMillis();
            Logs.add(new LogEvent(Tag,Message,UpdatedStamps));
            if (UpdatedStamps - StoredStamps < WriteDelay ) return;
            StoredStamps = UpdatedStamps;
            flush();
            Logs.clear();
        }
    }

    private void flush() {
        File LogFile = new File(WorkingDirectory.getPath(),"log.txt");
        BufferedWriter LogWriter = WriterOf(LogFile);
        if (LogWriter == null) return;

        try {
            for (LogEvent Log : Logs) {
                String Information = "";
                Information += "Time:" + String.valueOf(Log.Stamps);
                Information += " >> "+ Log.Tag;
                Information += " >> "+ Log.Message;
                LogWriter.write(Information);
                LogWriter.newLine();
            }
            LogWriter.flush();
            LogWriter.close();

        } catch (Exception WriteError) { Log.d(LogTag, "Error in Log writing ..."); }
    }

    private void SelectDirectory(String LogDirectory) {
        boolean ExternalAccess = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

        if (ExternalAccess) {
            WorkingDirectory = Environment.getExternalStoragePublicDirectory(LogDirectory);
            WorkingDirectory.mkdir();
        }
        else WorkingDirectory = Service.getFilesDir();
        Service.Log(LogTag, "Selecting workspace {"+ WorkingDirectory.getAbsolutePath()+"}" );
    }


    static private BufferedWriter WriterOf(File Selected) {
        try { return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Selected), "UTF-8")); }
        catch (Exception FileError) {
            Log.d(LogTag, "Failed to create stream from " + Selected.getName());
            return null;
        }
    }
}
