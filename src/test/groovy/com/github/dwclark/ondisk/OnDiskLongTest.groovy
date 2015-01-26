package com.github.dwclark.ondisk;

import spock.lang.*;

public class OnDiskLongTest extends Specification {

    long[] vals = [ 4L, 1L, 3L, 12L, 13L, 5L, 7L, 20L, 19L, 25L ] as long[];

    def "Test Write"() {
        setup:
        File file = File.createTempFile("longs", ".bin");
        OnDiskLong.store(vals, file).close();
        
        expect:
        file.length() == vals.length * 8;

        cleanup:
        file.delete();
    }

    def "Test Has All"() {
        setup:
        File file = File.createTempFile("longs", ".bin");
        OnDiskLong odl = OnDiskLong.store(vals, file);
        
        expect:
        odl.hasAll(1L, 20L, 13L);
        odl.hasAll(25L);
        !odl.hasAll(1L, 2L, 3L);
        
        cleanup:
        odl.close();
        file.delete();
    }

    def "Test Has Any"() {
        setup:
        File file = File.createTempFile("longs", ".bin");
        OnDiskLong odl = OnDiskLong.store(vals, file);
        
        expect:
        odl.hasAny(1L, 2L, 3L, 4L, 5L, 6L);
        odl.hasAny(7L);
        !odl.hasAny(30L, 31L, 32L);
        
        cleanup:
        odl.close();
        file.delete();
    }

    def "Test Large"() {
        setup:
        int size = 1_000_000;
        long[] ary = new long[size];
        for(int i = 1; i <= size; ++i) {
            ary[i-1] = i;
        }

        File file = File.createTempFile("longs", ".bin");
        OnDiskLong odl = OnDiskLong.store(ary, file);

        expect:
        odl.hasAll(1L, 100L, 10_000L, 999_999L);
        odl.hasAny(1L, 100L, 10_000L, 999_999L);

        cleanup:
        odl.close();
        file.delete();
        
    }
}