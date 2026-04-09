package com.xiw.kuwei.helper.fetcher;

import com.diboot.core.util.ContextHolder;
import com.xiw.kuwei.annotation.Fetcher;

import java.util.List;
import java.util.Map;

public class FetcherManager {

    private Map<Integer, abstractFetcher> fetcherMap;

    {
        // todo init fetcher map
        List<Object> beansByAnnotation = ContextHolder.getBeansByAnnotation(Fetcher.class);
        for (Object fetcher : beansByAnnotation) {
        }
    }

    public static abstractFetcher getFetcher() {
        return new SinaFetcher();
    }

}
