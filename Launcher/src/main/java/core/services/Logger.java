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

    private final long WriteDelay = 5*1000*60;
    private Hub Service = null;
    private boolean Live = true;
    private long StoredStamps = 0;
    private long WriteStamps = 0;
    private File WorkingDirectory;
    private ArrayList<LogEvent> Logs = new ArrayList<>();

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

    public Logger(Hub Owner) {
        Service =  Owner;
        SelectDirectory(Owner.getPackageName());
    }

    public void Live(boolean enabled) {
        Live = enabled;
        StoredStamps = Calendar.getInstance().getTimeInMillis();
        WriteStamps = StoredStamps;
        Logs.clear();
    }

    public void debug(String Tag, String Message) {
        if (Live) Log.d(Tag, Message);
        else {
            long UpdatedStamps = Calendar.getInstance().getTimeInMillis();
            long Delay = UpdatedStamps - StoredStamps;
            StoredStamps = UpdatedStamps;
            Logs.add(new LogEvent(Tag,Message,Delay));
            if (UpdatedStamps - WriteStamps < WriteDelay ) return;
            WriteStamps = UpdatedStamps;
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
                Information += "Time: +" + String.valueOf(Log.Stamps)+"ms";
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
        Log.d(LogTag, "Selecting workspace {"+ WorkingDirectory.getAbsolutePath()+"}" );
    }


    static private BufferedWriter WriterOf(File Selected) {
        try { return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Selected,true), "UTF-8")); }
        catch (Exception FileError) {
            Log.d(LogTag, "Failed to create stream from " + Selected.getName());
            return null;
        }
    }
}
