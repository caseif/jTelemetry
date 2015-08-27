package net.caseif.phtoolkit;

import java.io.IOException;
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
         * @throws IllegalStateException If an exception occurs while sending a
         *     request to the remote server
         * @since 1.0
         */
        public void submit() throws IOException {
            //TODO
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
         * Adds a piece of data to this {@link Payload} in the form of a "short"
         * (16-bit) integer.
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
         * integer.
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
         * Adds a piece of data to this {@link Payload} in the form of a "long"
         * (64-bit) integer.
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
         * Adds a piece of data to this {@link Payload} in the form of a
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
         * Adds a piece of data to this {@link Payload} in the form of a
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
            addData(DataType.STRING_ARRAY, key, data);
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
            addData(DataType.INT_ARRAY, key, data);
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

            private DataType getType() {
                return type;
            }

            private String getKey() {
                return key;
            }

            private Object getContent() {
                return content;
            }

        }

    }

    private enum DataType {
        STRING,
        BOOLEAN,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,

        STRING_ARRAY,
        INT_ARRAY;
    }

}
