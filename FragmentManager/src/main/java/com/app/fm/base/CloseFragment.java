package com.app.fm.base;

public interface CloseFragment {
    void close(RootFragment fragment);

    void show(RootFragment fragment);

    void beginTransaction();

    void commit();
}
