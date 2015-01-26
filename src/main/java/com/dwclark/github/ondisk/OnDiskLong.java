package com.github.dwclark.ondisk;

import java.util.Arrays;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.channels.*;
import static java.nio.file.StandardOpenOption.*;

public class OnDiskLong implements AutoCloseable {

    final FileChannel channel;
    final MappedByteBuffer buffer;
    final int num;

    public OnDiskLong(FileChannel channel, MappedByteBuffer buffer, int num) {
        this.channel = channel;
        this.buffer = buffer;
        this.num = num;
    }

    public void close() {
        try {
            channel.close();
        }
        catch(IOException ioe) {
            //just swallow it, there isn't anything we can do
        }
    }
    
    public static OnDiskLong existing(File file) throws IOException{
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        int num = ((int) channel.size()) / 8;
        return new OnDiskLong(channel, buffer, num);
    }

    public static OnDiskLong store(long[] ary, File file) throws IOException {
        Arrays.sort(ary);
        StandardOpenOption[] options = { CREATE, WRITE, READ, TRUNCATE_EXISTING };
        FileChannel channel = FileChannel.open(file.toPath(), options);
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, ary.length * 8);
        buffer.asLongBuffer().put(ary);
        channel.close();
        return existing(file);
    }

    private boolean has(LongBuffer searchBuffer, final long val) {
        int lo = 0;
        int hi = num - 1;
        while (lo <= hi) {
            // Key is in a[lo..hi] or not present.
            int mid = lo + (hi - lo) / 2;
            long testVal = searchBuffer.get(mid);
            if (val < testVal) {
                hi = mid - 1;
            }
            else if(val > testVal) {
                lo = mid + 1;
            }
            else {
                return true;
            }
        }

        return false;
    }

    public boolean hasAll(final long... vals) {
        LongBuffer searchBuffer = buffer.asLongBuffer();
        for(long val : vals) {
            if(!has(searchBuffer, val)) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasAll(File file, final long... vals) {
        try(OnDiskLong odl = existing(file)) {
            return odl.hasAll(vals);
        }
        catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public boolean hasAny(final long... vals) {
        LongBuffer searchBuffer = buffer.asLongBuffer();
        for(long val : vals) {
            if(has(searchBuffer, val)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasAny(File file, final long... vals) {
        try(OnDiskLong odl = existing(file)) {
            return odl.hasAny(vals);
        }
        catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
