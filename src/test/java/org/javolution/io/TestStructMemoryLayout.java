package org.javolution.io;


import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class TestStructMemoryLayout {

    private static final int NUM_RECORDS = 50 * 1000 * 1000;

    private static final StructMemoryTrade flyweight = new StructMemoryTrade();

    private static final int capacity = NUM_RECORDS * flyweight.size();

    private static final ByteBuffer globalByteBuffer = ByteBuffer.allocateDirect(capacity);


    public static void main(final String[] args) {

        System.out.println(" --- TestStructMemoryLayout --- ");
        System.out.println("over all size: " + capacity);
        System.out.println("struct size:   " + flyweight.size());

        flyweight.setByteBuffer(globalByteBuffer, 0);

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
            final StructMemoryTrade trade = flyweight.get(i);

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

    public static void init() {

        final byte[] londonStockExchange = {'X', 'L', 'O', 'N'};
        final int venueCode = pack(londonStockExchange);

        final byte[] billiton = {'B', 'H', 'P'};
        final int instrumentCode = pack(billiton);

        for (int i = 0; i < NUM_RECORDS; i++) {
            final StructMemoryTrade trade = flyweight.get(i);

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

    private static class StructMemoryTrade extends Struct {

        private Signed64 tradeId = new Signed64();
        private Signed64 clientId = new Signed64();
        private Signed32 venueCode = new Signed32();
        private Signed32 instrumentCode = new Signed32();
        private Signed64 price = new Signed64();
        private Signed64 quantity = new Signed64();
        private UTFChar16 side = new UTFChar16();

        private ByteBuffer byteBuffer;

        private int tradeIdIdx;
        private int clientIdIdx;
        private int venueCodeIdx;
        private int instrumentCodeIdx;
        private int priceIdx;
        private int quantityIdx;
        private int sideIdx;

        public StructMemoryTrade get(final int index) {
            final StructMemoryTrade smt = (StructMemoryTrade) setByteBufferPosition(index * size());
            final int byteBufferPosition = getByteBufferPosition();

            this.tradeIdIdx = byteBufferPosition + tradeId.offset();
            this.clientIdIdx = byteBufferPosition + clientId.offset();
            this.venueCodeIdx = byteBufferPosition + venueCode.offset();
            this.instrumentCodeIdx = byteBufferPosition + instrumentCode.offset();
            this.priceIdx = byteBufferPosition + price.offset();
            this.quantityIdx = byteBufferPosition + quantity.offset();
            this.sideIdx = byteBufferPosition + side.offset();

            this.byteBuffer = getByteBuffer();

            return smt;
        }

        @Override
        public boolean isPacked() {
            return true;
        }

/*
        public long getTradeId() {
            return byteBuffer.getLong(tradeIdIdx);
        }

        public void setTradeId(final long tradeId) {
            byteBuffer.putLong(tradeIdIdx, tradeId);
        }

        public long getClientId() {
            return byteBuffer.getLong(clientIdIdx);
        }

        public void setClientId(final long clientId) {
            byteBuffer.putLong(clientIdIdx, clientId);
        }

        public int getVenueCode() {
            return byteBuffer.getInt(venueCodeIdx);
        }

        public void setVenueCode(final int venueCode) {
            byteBuffer.putInt(venueCodeIdx, venueCode);
        }

        public int getInstrumentCode() {
            return byteBuffer.getInt(instrumentCodeIdx);
        }

        public void setInstrumentCode(final int instrumentCode) {
            byteBuffer.putInt(instrumentCodeIdx, instrumentCode);
        }

        public long getPrice() {
            return byteBuffer.getLong(priceIdx);
        }

        public void setPrice(final long price) {
            byteBuffer.putLong(priceIdx, price);
        }

        public long getQuantity() {
            return byteBuffer.getLong(quantityIdx);
        }

        public void setQuantity(final long quantity) {
            byteBuffer.putLong(quantityIdx, quantity);
        }

        public char getSide() {
            return byteBuffer.getChar(sideIdx);
        }

        public void setSide(final char side) {
            byteBuffer.putChar(sideIdx, side);
        }
*/

        public long getTradeId() {
            if (byteBuffer != null) {
                return byteBuffer.getLong(tradeIdIdx);
            } else {
                return tradeId.get();
            }
        }

        public void setTradeId(final long tradeId) {
            if (byteBuffer != null) {
                byteBuffer.putLong(tradeIdIdx, tradeId);
            } else {
                this.tradeId.set(tradeId);
            }
        }

        public long getClientId() {
            if (byteBuffer != null) {
                return byteBuffer.getLong(clientIdIdx);
            } else {
                return clientId.get();
            }
        }

        public void setClientId(final long clientId) {
            if (byteBuffer != null) {
                byteBuffer.putLong(clientIdIdx, clientId);
            } else {
                this.clientId.set(clientId);
            }
        }

        public int getVenueCode() {
            if (byteBuffer != null) {
                return byteBuffer.getInt(venueCodeIdx);
            } else {
                return venueCode.get();
            }
        }

        public void setVenueCode(final int venueCode) {
            if (byteBuffer != null) {
                byteBuffer.putInt(venueCodeIdx, venueCode);
            } else {
                this.venueCode.set(venueCode);
            }
        }

        public int getInstrumentCode() {
            if (byteBuffer != null) {
                return byteBuffer.getInt(instrumentCodeIdx);
            } else {
                return instrumentCode.get();
            }
        }

        public void setInstrumentCode(final int instrumentCode) {
            if (byteBuffer != null) {
                byteBuffer.putInt(instrumentCodeIdx, instrumentCode);
            } else {
                this.instrumentCode.set(instrumentCode);
            }
        }

        public long getPrice() {
            if (byteBuffer != null) {
                return byteBuffer.getLong(priceIdx);
            } else {
                return price.get();
            }
        }

        public void setPrice(final long price) {
            if (byteBuffer != null) {
                byteBuffer.putLong(priceIdx, price);
            } else {
                this.price.set(price);
            }
        }

        public long getQuantity() {
            if (byteBuffer != null) {
                return byteBuffer.getLong(quantityIdx);
            } else {
                return quantity.get();
            }
        }

        public void setQuantity(final long quantity) {
            if (byteBuffer != null) {
                byteBuffer.putLong(quantityIdx, quantity);
            } else {
                this.quantity.set(quantity);
            }
        }

        public char getSide() {
            if (byteBuffer != null) {
                return byteBuffer.getChar(sideIdx);
            } else {
                return (char) side.get();
            }
        }

        public void setSide(final char side) {
            if (byteBuffer != null) {
                byteBuffer.putChar(sideIdx, side);
            } else {
                this.side.set(side);
            }
        }
    }
}