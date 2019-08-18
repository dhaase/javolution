package org.javolution.io;

import java.util.concurrent.TimeUnit;

public class TestJavaMemoryLayout {
    private static final int NUM_RECORDS = 50 * 1000 * 1000;

    private static JavaMemoryTrade[] trades = new JavaMemoryTrade[NUM_RECORDS];

    static {
        for (int i = 0; i < NUM_RECORDS; i++) {
            trades[i] = new JavaMemoryTrade();
        }
    }

    public static void main(final String[] args) {
        for (int i = 0; i < 5; i++) {
            System.gc();
            perfRun(i);
        }
    }

    private static void perfRun(final int runNum) {

        long start1 = System.nanoTime();
        init();
        long duration1 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start1);

        System.out.format("Memory %,d total, %,d free\n",
                Runtime.getRuntime().totalMemory(),
                Runtime.getRuntime().freeMemory());

        long value1 = 0;

        long start2 = System.nanoTime();
        for (int i = 0; i < NUM_RECORDS; i++) {
            final JavaMemoryTrade trade = get(i);

            value1 += trade.getTradeId();
            value1 += trade.getClientId();
            value1 += trade.getVenueCode();
            value1 += trade.getInstrumentCode();
            value1 += trade.getPrice();
            value1 += trade.getQuantity();
        }
        long duration2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start2);

        System.out.println(runNum + " - write duration " + duration1 + "ms; read duration " + duration2 + "ms (" + value1 + ")");
    }

    private static JavaMemoryTrade get(final int index) {
        return trades[index];
    }

    public static void init() {

        final byte[] londonStockExchange = {'X', 'L', 'O', 'N'};
        final int venueCode = pack(londonStockExchange);

        final byte[] billiton = {'B', 'H', 'P'};
        final int instrumentCode = pack(billiton);

        for (int i = 0; i < NUM_RECORDS; i++) {
            final JavaMemoryTrade trade = trades[i];

            trade.setTradeId(i);
            trade.setClientId(1);
            trade.setVenueCode(venueCode);
            trade.setInstrumentCode(instrumentCode);

            trade.setPrice(i);
            trade.setQuantity(i);

            trade.setSide((i & 1) == 0 ? 'B' : 'S');
        }
    }

    private static int pack(final byte[] value) {
        int result = 0;
        switch (value.length) {
            case 4:
                result = (value[3]);
            case 3:
                result |= ((int) value[2] << 8);
            case 2:
                result |= ((int) value[1] << 16);
            case 1:
                result |= ((int) value[0] << 24);
                break;

            default:
                throw new IllegalArgumentException("Invalid array size");
        }

        return result;
    }

    private static class JavaMemoryTrade {
        private long tradeId;
        private long clientId;
        private int venueCode;
        private int instrumentCode;
        private long price;
        private long quantity;
        private char side;

        public long getTradeId() {
            return tradeId;
        }

        public void setTradeId(final long tradeId) {
            this.tradeId = tradeId;
        }

        public long getClientId() {
            return clientId;
        }

        public void setClientId(final long clientId) {
            this.clientId = clientId;
        }

        public int getVenueCode() {
            return venueCode;
        }

        public void setVenueCode(final int venueCode) {
            this.venueCode = venueCode;
        }

        public int getInstrumentCode() {
            return instrumentCode;
        }

        public void setInstrumentCode(final int instrumentCode) {
            this.instrumentCode = instrumentCode;
        }

        public long getPrice() {
            return price;
        }

        public void setPrice(final long price) {
            this.price = price;
        }

        public long getQuantity() {
            return quantity;
        }

        public void setQuantity(final long quantity) {
            this.quantity = quantity;
        }

        public char getSide() {
            return side;
        }

        public void setSide(final char side) {
            this.side = side;
        }
    }
}