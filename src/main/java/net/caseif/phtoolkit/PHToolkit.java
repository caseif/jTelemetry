package net.caseif.phtoolkit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight Java toolkit for submitting arbitrary information to a web server
 * (i.e. "phoning home").
 *
 * @author Max Roncace
 * @version 1.0.0
 */
public class PHToolkit {

    private final String recipient;

    /**
     * Constructs a new {@link PHToolkit} object with the given recipient
     * address. This address must have an HTTP or HTTPS protocol.
     *
     * @param recipient The recipient address of this {@link PHToolkit}
     * @throws IllegalArgumentException If the address's protocol is not valid
     * @since 1.0
     */
    public PHToolkit(String recipient) throws IllegalArgumentException {
        this.recipient = recipient;
    }

    /**
     * Returns the recipient address of this {@link PHToolkit}.
     *
     * @return The recipient address of this {@link PHToolkit}
     * @since 1.0
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Creates a new {@link Payload} parented by this {@link PHToolkit}.
     *
     * @return The new {@link Payload}
     * @since 1.0
     */
    public Payload createPayload() {
        return new Payload(this);
    }

    /**
     * Represents a payload containing data entries to be submitted to a server
     * in a single request.
     *
     * @since 1.0
     */
    public class Payload {

        private PHToolkit parent;

        private Map<String, DataEntry> dataMap = new HashMap<>();

        private Payload(PHToolkit parent) {
            this.parent = parent;
        }

        /**
         * Returns the parent {@link PHToolkit} of this {@link Payload}.
         *
         * @return The parent {@link PHToolkit} of this {@link Payload}
         * @since 1.0
         */
        public PHToolkit getParent() {
            return parent;
        }

        /**
         * Attempts to submit this payload to the address defined by the parent
         * {@link PHToolkit}.
         *
         * @return The HTTP response code returned by the server
         * @throws IllegalStateException If an exception occurs while sending a
         *     request to the remote server
         * @since 1.0
         */
        public int submit() throws IOException {
            URL url = new URL(getParent().getRecipient());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(serializeData());
            }
            return conn.getResponseCode();
        }

        private byte[] serializeData() {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            // magic number for verification purposes
            byte[] magic = new byte[]{(byte) 0xB0, (byte) 0x00, (byte) 0xB1, (byte) 0xE5};
            buffer.put(magic);
            for (DataEntry entry : dataMap.values()) {
                byte[] serial = entry.serialize();
                buffer.limit(buffer.limit() + serial.length);
                buffer.put(serial);
            }
            return buffer.array();
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a string.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, String data) throws IllegalArgumentException {
            addData(DataType.STRING, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a boolean
         * (1-bit integer).
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, boolean data) throws IllegalArgumentException {
            addData(DataType.BOOLEAN, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a short
         * (16-bit) signed integer.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, short data) throws IllegalArgumentException {
            addData(DataType.SHORT, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a 32-bit
         * signed integer.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, int data) throws IllegalArgumentException {
            addData(DataType.INT, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a long
         * (64-bit) signed integer.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, long data) throws IllegalArgumentException {
            addData(DataType.LONG, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of an IEEE
         * floating-point number.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, float data) throws IllegalArgumentException {
            addData(DataType.FLOAT, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of an IEEE
         * double-precision floating point number.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, double data) throws IllegalArgumentException {
            addData(DataType.DOUBLE, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a string
         * array.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, String[] data) throws IllegalArgumentException {
            addData(DataType.ARRAY, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a 32-bit
         * integer array.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key
         * @since 1.0
         */
        public void addData(String key, int[] data) throws IllegalArgumentException {
            addData(DataType.ARRAY, key, data);
        }

        private void addData(DataType type, String key, Object data) throws IllegalArgumentException {
            if (dataMap.containsKey(key)) {
                throw new IllegalArgumentException("Cannot redefine key \"" + key + "\" within the payload");
            }
            dataMap.put(key, new DataEntry(type, key, data));
        }

        private class DataEntry {

            private DataType type;
            private String key;
            private Object content;

            private DataEntry(DataType type, String key, Object content) {
                this.type = type;
                this.key = key;
                this.content = content;
            }

            private byte[] serialize() {
                ByteBuffer buffer = ByteBuffer.allocate(16 + key.length());

                buffer.putInt(type.ordinal()); // enter the type of content

                // enter the key
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                buffer.putInt(keyBytes.length); // enter key length
                buffer.put(keyBytes); // enter key content

                if (type == DataType.ARRAY) { // special handling for arrays
                    DataEntry[] entries = (DataEntry[]) content;
                    // serialize each entry and put it in the main buffer
                    for (DataEntry entry : entries) {
                        byte[] bytes = entry.serialize();
                        buffer.limit(buffer.limit() + bytes.length);
                        buffer.put(bytes);
                    }
                } else if (type == DataType.STRING) { // special handling for strings
                    // convert the string to a byte array
                    byte[] strBytes = ((String) content).getBytes(StandardCharsets.UTF_8);
                    buffer.limit(buffer.limit() + 8 + strBytes.length);
                    buffer.putInt(strBytes.length); // enter the string length
                    buffer.put(strBytes); // enter the string data
                } else { // default handling
                    assert type.getLength() != -1; // program shouldn't progress to here if content length isn't fixed
                    buffer.limit(buffer.limit() + type.getLength());
                    // check the type and insert the content
                    switch (type) {
                        case BOOLEAN: {
                            buffer.put((boolean) content ? (byte) 1 : (byte) 0);
                        }
                        case SHORT: {
                            buffer.putShort((short) content);
                        }
                        case INT: {
                            buffer.putInt((int) content);
                        }
                        case LONG: {
                            buffer.putLong((long) content);
                        }
                        case FLOAT: {
                            buffer.putFloat((float) content);
                        }
                        case DOUBLE: {
                            buffer.putDouble((double) content);
                        }
                        default: {
                            throw new AssertionError();
                        }
                    }
                }
                return buffer.array();
            }

        }

    }

    /**
     * Represents a type of data.
     *
     * @since 1.0
     */
    private enum DataType {
        /**
         * A UTF-8 string. String data entries are to be prefixed with a 32-bit integer representing their respective
         * length.
         *
         * @since 1.0
         */
        STRING(-1),
        /**
         * A boolean value (1-bit integer).
         *
         * @since 1.0
         */
        BOOLEAN(1),
        /**
         * A short (16-bit) integer value.
         *
         * @since 1.0
         */
        SHORT(2),
        /**
         * A short (32-bit) integer value.
         *
         * @since 1.0
         */
        INT(4),
        /**
         * A long (64-bit) integer value.
         *
         * @since 1.0
         */
        LONG(8),
        /**
         * A IEEE single-precision (32-bit) floating point number
         *
         * @since 1.0
         */
        FLOAT(4),
        /**
         * A IEEE double-precision (64-bit) floating point number
         *
         * @since 1.0
         */
        DOUBLE(8),

        /**
         * An array of objects. Array-type data entries are to be prefixed with a 32-bit signed integer representing
         * their respective length (i.e. the number of entries contained).
         *
         * @since 1.0
         */
        ARRAY(-1);

        private final int length;

        DataType(int length) {
            this.length = length;
        }

        private int getLength() {
            return length;
        }

    }

}
