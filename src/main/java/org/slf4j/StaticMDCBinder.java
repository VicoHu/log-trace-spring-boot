package org.slf4j;

import org.slf4j.spi.MDCAdapter;

/**
 * StaticMDCBinder
 *
 * @author: VicoHu
 * @date: 2024/7/31
 */
public class StaticMDCBinder {
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private final MDCAdapter mdcAdapter = new MultithreadingSupportMDCAdapter();

    private StaticMDCBinder() {}

    public static StaticMDCBinder getSingleton() {
        return SINGLETON;
    }

    public MDCAdapter getMDCA() {
        return mdcAdapter;
    }
}
