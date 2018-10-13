package com.bookmap.sergey.custommodules;

import java.awt.Color;

import com.bookmap.sergey.custommodules.utils.OrderBookBase;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.layers.utils.OrderBook;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.BarDataListener;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.DepthDataListener;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.Intervals;
import velox.api.layer1.simplified.SnapshotEndListener;

@Layer1SimpleAttachable
@Layer1StrategyName("Quotes Delta")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class QuotesDelta implements CustomModule, BarDataListener, DepthDataListener, SnapshotEndListener {

    protected final OrderBookBase book = new OrderBookBase();
    private int bidDelta = 0;
    private int askDelta = 0;
    private boolean snapshotCompleted = false;

    private Indicator line1;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api) {
        line1 = api.registerIndicator("Quotes Delta", GraphType.BOTTOM, Color.ORANGE);
    }

    @Override
    public void onBar(OrderBook orderBook, Bar bar) {
        line1.addPoint(bidDelta - askDelta);
    }

    @Override
    public long getBarInterval() {
        return Intervals.INTERVAL_100_MILLISECONDS;
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        if (!snapshotCompleted) {
            return;
        }
        Integer prevSize = book.getSizeOrZero(isBid, price);
        int delta = size - prevSize;
        if (isBid) {
            bidDelta += delta;
        } else {
            askDelta += delta;
        }
        book.onDepth(isBid, price, size);
    }

    @Override
    public void onSnapshotEnd() {
        snapshotCompleted = true;
    }

    @Override
    public void stop() {
    }
}