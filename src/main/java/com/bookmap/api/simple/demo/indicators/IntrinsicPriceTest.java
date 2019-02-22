package com.bookmap.api.simple.demo.indicators;

import com.bookmap.api.simple.demo.utils.data.OrderBookBase;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.*;

import java.awt.*;

@Layer1SimpleAttachable
@Layer1StrategyName("Intrinsic Price Test")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class IntrinsicPriceTest extends IntrinsicPriceTestSettings
        implements CustomModule, TradeDataListener, DepthDataListener, SnapshotEndListener, IntervalListener {

    private OrderBookBase orderBook = new OrderBookBase();
    protected InitialState initialState;

    private double lastTradePrice;
    private int lastTradeSize;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        super.initialize(alias, info, api, initialState);
        this.initialState = initialState;
    }


    @Override
    public void onDepth(boolean isBid, int price, int size) {
        orderBook.onDepth(isBid, price, size);
    }

    @Override
    public long getInterval() {
        return Intervals.INTERVAL_50_MILLISECONDS;
    }

    @Override
    public void onInterval() {
        if (Double.isNaN(lastTradePrice)) {
            return;
        }
        double bp = (double) orderBook.bids.firstKey();
        int bs = orderBook.bids.firstEntry().getValue();
        double ap = (double) orderBook.asks.firstKey();
        int as = orderBook.asks.firstEntry().getValue();

        double tp = lastTradePrice;
        int ts = lastTradeSize;

        double intrinsic = (bp * as + ap * bs + tp * ts) / (bs + as + ts);
        indicator.addPoint(intrinsic);
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        lastTradePrice = price;
        lastTradeSize = size;
    }

    @Override
    public void onSnapshotEnd() {
        onTrade(initialState.getLastTradePrice(), initialState.getLastTradeSize(), initialState.getTradeInfo());
    }

    @Override
    public void stop() {
    }

    protected String getLineTitle() {
        return "Intrinsic Price Test";
    }
}
