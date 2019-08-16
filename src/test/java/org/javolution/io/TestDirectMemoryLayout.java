package org.javolution.io;

//import sun.misc.Unsafe;
//import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class TestDirectMemoryLayout {

    private static final int NUM_RECORDS = 50 * 1000 * 1000;

    private static final int capacity = NUM_RECORDS * DirectMemoryTrade.objectSize;

    private static final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);

    private static int address;
    private static final DirectMemoryTrade flyweight = new DirectMemoryTrade();

    public static void main(final String[] args) {

        System.out.println(" --- TestDirectMemoryLayout --- ");
        System.out.println("over all size: " + capacity);
        System.out.println("struct size:   " + DirectMemoryTrade.objectSize);

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
            final DirectMemoryTrade trade = get(i);

            value1 += trade.getTradeId();
            value1 += trade.getClientId();
            value1 += trade.getVenueCode();
            value1 += trade.getInstrumentCode();
            value1 += trade.getPrice();
            value1 += trade.getQuantity();
        }
        long duration2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start2);

        System.out.println(runNum + " - write duration " + duration1 + "ms; read duration " + duration2 + "ms (" + value1 + ")");

        destroy();
    }

    private static DirectMemoryTrade get(final int index) {
        final int offset = address + (index * DirectMemoryTrade.getObjectSize());
        flyweight.setObjectOffset(offset);
        return flyweight;
    }

    public static void init() {
        final int requiredHeap = NUM_RECORDS * DirectMemoryTrade.getObjectSize();
        address = 0; //byteBuffer.allocateMemory(requiredHeap);

        final byte[] londonStockExchange = {'X', 'L', 'O', 'N'};
        final int venueCode = pack(londonStockExchange);

        final byte[] billiton = {'B', 'H', 'P'};
        final int instrumentCode = pack(billiton);

        for (int i = 0; i < NUM_RECORDS; i++) {
            DirectMemoryTrade trade = get(i);

            trade.setTradeId(i);
            trade.setClientId(1);
            trade.setVenueCode(venueCode);
            trade.setInstrumentCode(instrumentCode);

            trade.setPrice(i);
            trade.setQuantity(i);

            trade.setSide((i & 1) == 0 ? 'B' : 'S');
        }
    }

    private static void destroy() {
        //byteBuffer.freeMemory(address);
    }

    private static int pack(final byte[] value) {
        int result = 0;
        switch (value.length) {
            case 4:
                result |= (value[3]);
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

    private static class DirectMemoryTrade {
        private static int offset = 0;

        private static final int tradeIdOffset = offset += 0;
        private static final int clientIdOffset = offset += 8;
        private static final int venueCodeOffset = offset += 8;
        private static final int instrumentCodeOffset = offset += 4;
        private static final int priceOffset = offset += 4;
        private static final int quantityOffset = offset += 8;
        private static final int sideOffset = offset += 8;

        static final int objectSize = offset += 2;

        private int objectOffset;

        public static int getObjectSize() {
            return objectSize;
        }

        void setObjectOffset(final int objectOffset) {
            this.objectOffset = objectOffset;
        }

        public long getTradeId() {
            return byteBuffer.getLong(objectOffset + tradeIdOffset);
        }

        public void setTradeId(final long tradeId) {
            byteBuffer.putLong(objectOffset + tradeIdOffset, tradeId);
        }

        public long getClientId() {
            return byteBuffer.getLong(objectOffset + clientIdOffset);
        }

        public void setClientId(final long clientId) {
            byteBuffer.putLong(objectOffset + clientIdOffset, clientId);
        }

        public int getVenueCode() {
            return byteBuffer.getInt(objectOffset + venueCodeOffset);
        }

        public void setVenueCode(final int venueCode) {
            byteBuffer.putInt(objectOffset + venueCodeOffset, venueCode);
        }

        public int getInstrumentCode() {
            return byteBuffer.getInt(objectOffset + instrumentCodeOffset);
        }

        public void setInstrumentCode(final int instrumentCode) {
            byteBuffer.putInt(objectOffset + instrumentCodeOffset, instrumentCode);
        }

        public long getPrice() {
            return byteBuffer.getLong(objectOffset + priceOffset);
        }

        public void setPrice(final long price) {
            byteBuffer.putLong(objectOffset + priceOffset, price);
        }

        public long getQuantity() {
            return byteBuffer.getLong(objectOffset + quantityOffset);
        }

        public void setQuantity(final long quantity) {
            byteBuffer.putLong(objectOffset + quantityOffset, quantity);
        }

        public char getSide() {
            return byteBuffer.getChar(objectOffset + sideOffset);
        }

        public void setSide(final char side) {
            byteBuffer.putChar(objectOffset + sideOffset, side);
        }
    }
}