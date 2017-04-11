package lib.smartwatch;

import com.getpebble.android.kit.util.PebbleDictionary;

public class SmartwatchBundle extends PebbleDictionary {

    public void update(int key, int Value ){
        if (this.contains(key)) this.remove(key);
        this.addInt32(key,Value);
    }

    public void update(int key, short Value ){
        if (this.contains(key)) this.remove(key);
        this.addInt16(key,Value);
    }

    public void update(int key, byte Value ){
        if (this.contains(key)) this.remove(key);
        this.addInt8(key,Value);
    }

    public void update(int key, String Value ){
        if (this.contains(key)) this.remove(key);
        this.addString(key,Value);
    }
}
