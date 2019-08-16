package org.javolution.io;


import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class TestStructMemoryLayout {

    private static final int NUM_RECORDS = 50 * 1000 * 1000;

    private static final StructMemoryTrade structMemoryTrade = new StructMemoryTrade();

    private static final int capacity = NUM_RECORDS * structMemoryTrade.size();

    private static final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);


    public static void main(final String[] args) {

        System.out.println(" --- TestStructMemoryLayout --- ");
        System.out.println("over all size: " + capacity);
        System.out.println("struct size:   " + structMemoryTrade.size());

        structMemoryTrade.setByteBuffer(byteBuffer, 0);

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
            final StructMemoryTrade trade = get(i);

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

    private static StructMemoryTrade get(final int index) {
        return (StructMemoryTrade) structMemoryTrade.setByteBufferPosition(index * structMemoryTrade.size());
    }

    public static void init() {

        final byte[] londonStockExchange = {'X', 'L', 'O', 'N'};
        final int venueCode = pack(londonStockExchange);

        final byte[] billiton = {'B', 'H', 'P'};
        final int instrumentCode = pack(billiton);

        for (int i = 0; i < NUM_RECORDS; i++) {
            StructMemoryTrade trade = get(i);

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
        private Unsigned16 side = new Unsigned16();

        @Override
        public boolean isPacked() {
            return true;
        }

        public long getTradeId() {
            return tradeId.get();
        }

        public void setTradeId(final long tradeId) {
            this.tradeId.set(tradeId);
        }

        public long getClientId() {
            return clientId.get();
        }

        public void setClientId(final long clientId) {
            this.clientId.set(clientId);
        }

        public int getVenueCode() {
            return venueCode.get();
        }

        public void setVenueCode(final int venueCode) {
            this.venueCode.set(venueCode);
        }

        public int getInstrumentCode() {
            return instrumentCode.get();
        }

        public void setInstrumentCode(final int instrumentCode) {
            this.instrumentCode.set(instrumentCode);
        }

        public long getPrice() {
            return price.get();
        }

        public void setPrice(final long price) {
            this.price.set(price);
        }

        public long getQuantity() {
            return quantity.get();
        }

        public void setQuantity(final long quantity) {
            this.quantity.set(quantity);
        }

        public char getSide() {
            return (char) side.get();
        }

        public void setSide(final char side) {
            this.side.set(side);
        }
    }
}